package inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.ble_comms.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import inc.osips.iot_wireless_communication.R;
import inc.osips.iot_wireless_communication.wireless_comms_module.interfaces.WirelessDeviceConnector;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.utility.Constants;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.utility.Util;

/**
 * Created by Adebayo Osipitan on 10/11/2016.
 */

public class BleGattService extends Service {
    private final static String TAG = BleGattService.class.getSimpleName();
    private BluetoothGatt bleGatt;
    private boolean hasService;
    private volatile List<BluetoothGattService> services = new LinkedList<>();
    private BluetoothManager bManager;
    private BluetoothAdapter bAdapter;
    private boolean isHasService = false;

    //  Queue for BLE events
    //  This is needed so that rapid BLE events don't get dropped
    private static final Queue<Object> BleQueue = new LinkedList<>();
    private int mConnectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private int GATT_MAX_MTU_SIZE = 517;// default ATT MTU size
    private static boolean mLedSwitchState = false;

    public  String EXTRA_UUID;

    public String serviceUUID;// = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    int mydata;
    String MyLogData;

    private BluetoothGattCharacteristic myWriteCharx;
    private BluetoothGattCharacteristic myReadCharx;
    private BluetoothGattCharacteristic myNotifycharx;

    public class BTLeServiceBinder extends Binder {
        public BleGattService getService(){
            return BleGattService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        //throw new UnsupportedOperationException("");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        disconnect();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new BTLeServiceBinder();
    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */

    /**
     * Implements callback methods for GATT events.
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        /**
         * This is called on a connection state change (either connection or disconnection)
         * @param gatt The GATT database object
         * @param status Status of the event
         * @param newState New state (connected or disconnected)
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            switch (newState){
                case BluetoothProfile.STATE_CONNECTED:
                    broadcastUpdate(Constants.ACTION_CONNECTED);
                    MyLogData += "Connected to GATT server.\n";
                    Log.i(TAG, "Connected to GATT server.");
                    // Attempts to discover services after successful connection.
                    MyLogData +="Attempting to start service discovery:" +
                            bleGatt.discoverServices() +"\n";
                    Log.i(TAG, "Attempting to start service discovery:" +
                            bleGatt.discoverServices());
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i(TAG, "Disconnected from GATT server.");
                    MyLogData += "Disconnected from GATT server.\n";
                    broadcastUpdate(Constants.ACTION_DISCONNECTED);
                    break;
                case BluetoothProfile.STATE_DISCONNECTING:
                    Util.message(BleGattService.this,getString(R.string.disconnecting));
                    break;
            }
        }

        /**
         * This is called when service discovery has completed.
         * It broadcasts an update to the main activity.
         * @param gatt The GATT database object
         * @param status Status of whether the discovery was successful.
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (!services.isEmpty() || services.size() >0)
                  services.clear();

                services.addAll(gatt.getServices());
                //getGattServices(services);

                /*BluetoothGattService myGattService = gatt.getService(UUID.fromString(serviceUUID));
                myWriteCharx = myGattService.getCharacteristic(UUID.fromString(writeUUID));
                myReadCharx = myGattService.getCharacteristic(UUID.fromString(readUUID));*/
                Log.w(TAG, "Services Discovered");
                broadcastUpdate(Constants.ACTION_BLE_SERVICES_DISCOVERED, gatt.getServices());
                //readBleCharacteristic();
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
                MyLogData += "onServicesDiscovered received: " + status+ "\n";
            }
        }

        private void handleBleQueue() {
            if(BleQueue.size() > 0) {
                // Determine which type of event is next and fire it off
                if (BleQueue.element() instanceof BluetoothGattDescriptor) {
                    bleGatt.writeDescriptor((BluetoothGattDescriptor) BleQueue.element());
                } else if (BleQueue.element() instanceof BluetoothGattCharacteristic) {
                    bleGatt.writeCharacteristic((BluetoothGattCharacteristic) BleQueue.element());
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(Constants.ACTION_DATA_AVAILABLE, characteristic);
                Log.i(TAG, "onCharacteristicRead: xchar: " + characteristic.getUuid() + ", read:"
                + characteristic.getStringValue(0));
            }
        }

        /**
         * This is called when a characteristic write has completed. Is uses a queue to determine if
         * additional BLE actions are still pending and launches the next one if there are.
         * @param gatt The GATT database object
         * @param characteristic The characteristic that was written.
         * @param status Status of whether the write was successful.
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Pop the item that was written from the queue
                BleQueue.remove();
                // See if there are more items in the BLE queues
                handleBleQueue();
            }
        }

        /**
         * This is called when a characteristic with notify set changes.
         * It broadcasts an update to the main activity with the changed data.
         * @param gatt The GATT database object
         * @param characteristic The characteristic that was changed
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            // Get the UUID of the characteristic that changed
            String uuid = characteristic.getUuid().toString();
            mydata= characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32,0);
            // Tell the activity that new car data is available
            broadcastUpdate(Constants.ACTION_DATA_AVAILABLE);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            Log.i(TAG, "onMtuChanged: ATT MTU changed to" + mtu
                    +", success: "+ status);
            if (status == BluetoothGatt.GATT_SUCCESS){
                sendBroadcast(new Intent(WirelessDeviceConnector.MTU_CHANGE_SUCCESS));
            }else {
                sendBroadcast(new Intent(WirelessDeviceConnector.MTU_CHANGE_FAILURE));
            }
        }
    };

    /**
     * Sends a broadcast to the listener in the main activity.
     * @param action The type of action that occurred.
     */
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final List<BluetoothGattService> availableServices){
        ArrayList<String> uuid_strings = new ArrayList<>();
        for(BluetoothGattService service: availableServices){
            uuid_strings.add(service.getUuid().toString());
        }

        final Intent intent = new Intent(action);
        intent.putStringArrayListExtra(Constants.SERVICE_UUID, uuid_strings);
        sendBroadcast(intent);
    }

    /**
     * Sends a broadcast to the listener in the main activity.
     * @param action The type of action that occurred, and
     * @param characteristic the ble gatt charx active on broadcast call.
     */
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        if (myNotifycharx.getUuid().equals(characteristic.getUuid())) {
            final String received = characteristic.getStringValue(1);
            Log.i(TAG, String.format("Received from ble device: %s", received));
            intent.putExtra(Constants.EXTRA_DATA, String.valueOf(received));
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%s ", byteChar));
                Log.e(TAG, stringBuilder.toString());
                intent.putExtra(Constants.EXTRA_DATA, new String(data) + "\n" +
                        stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }

    /**
     * Initialize a reference to the local Bluetooth adapter.
     * @return Return true if the initialization is successful.
     */
    public boolean init() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        Log.i(TAG, "Initialising BLE");
        if (bManager == null) {
            bManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bManager == null) {
                Log.e(TAG, "Unable to init BluetoothManager.");
                return false;
            }
        }
        bAdapter = bManager.getAdapter();
        if (bAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }


    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     * @param device The BLEdestination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(@NonNull final BluetoothDevice device, @Nullable String serviceUUID, int GATT_MAX_MTU_SIZE) {

        if(GATT_MAX_MTU_SIZE>1){
            this.GATT_MAX_MTU_SIZE = GATT_MAX_MTU_SIZE;
        }

        if(!TextUtils.isEmpty(serviceUUID))
            this.serviceUUID = serviceUUID;

        if (bAdapter == null || device == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        if(bleGatt !=null) {
            //try to reconnect to previously connected device
            Log.i(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            MyLogData += "Trying to use an existing mBluetoothGatt for connection.\n";
            return bleGatt.connect();
        }
        bleGatt = device.connectGatt(this, false, mGattCallback);
        if (bleGatt!=null)
            bleGatt.requestMtu(this.GATT_MAX_MTU_SIZE);
        Log.i(TAG, "Trying to create a new connection.");
        MyLogData +="Trying to create a new connection.\n";
        return true;
    }

    public void selectServiceFromUUID (@NonNull String UUID){
        this.serviceUUID = UUID;
        if (!getGattServices()){
            Util.message(BleGattService.this, "No service with selected UUID!");
        }else {
            Log.d(TAG, "selectServiceFromUUID: service uuid: " + serviceUUID + " connected");
        }
    }


    /*
    * Loop through the discovered ble services finds the service matching
    * */

    private boolean getGattServices() {
        if (this.services == null || this.services.isEmpty()){
            stopSelf();
            return false;
        }

        String uuid ;

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : this.services) {

            uuid = gattService.getUuid().toString();
            Log.d(TAG, "getGattServices: "+ uuid);
            if (uuid.equalsIgnoreCase(serviceUUID)) {
                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();

                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic :
                        gattCharacteristics) {
                    if (gattCharacteristic.getProperties()==BluetoothGattCharacteristic.PROPERTY_WRITE ) {
                        myWriteCharx = gattCharacteristic;
                        Log.i(TAG, "getGattServices: Write charX discovered");
                    }
                    else if (gattCharacteristic.getProperties()==BluetoothGattCharacteristic.PROPERTY_READ){
                        myReadCharx = gattCharacteristic;
                        readBleCharacteristic();
                        Log.i(TAG, "getGattServices: Read charX discovered");
                    }else if (gattCharacteristic.getProperties() == BluetoothGattCharacteristic.PROPERTY_NOTIFY){
                        myNotifycharx = gattCharacteristic;
                        setCharacteristicNotification(true);
                        Log.i(TAG, "getGattServices: Notify charX discovered");
                    }
                }
                isHasService = true;
                break;
            }
        }

        if(!isHasService)stopSelf();

        return isHasService;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    private void disconnect() {
        if (bleGatt== null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            Util.message( this,"No Device Connected!");
        }
        else {
            bleGatt.disconnect();
            close();
            bleGatt = null;
            broadcastUpdate(Constants.ACTION_DISCONNECTED);
        }
    }
    /**
     * After using a given BLE device, the App must call this method to ensure resources are
     * released properly.
     */
    private void close() {
        if (bleGatt == null) {
            return;
        }
        bleGatt.close();
    }

    public void sendInstructionsToConnectedDevice(String instructions) {
        try {
            if (bleGatt != null) {
                byte[] bytes = instructions.getBytes();
                myWriteCharx.setValue(instructions);
                writeBleCharacteristic();
            }
        }catch (NullPointerException e)
        {
            Log.w(TAG, "Characteristic is null: " + e.toString());
            Util.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    Util.message( BleGattService.this, getString(R.string.ble_err_connection));
                }
            });
        }
    }

    /**
     * Request a write on a given {@code BluetoothGattCharacteristic}.
     */
    private void writeBleCharacteristic() {
        try{
            BleQueue.add(myWriteCharx);
            if (BleQueue.size() == 1) {
                bleGatt.writeCharacteristic(myWriteCharx);
                Log.i(TAG, "Writing Characteristic");
            }
        }catch (NullPointerException e){
            Log.w(TAG, "BluetoothAdapter not initialized: "+e.toString());
            Util.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    Util.message( BleGattService.this,"Bluetooth error! Check Connection");
                }
            });
        }
    }

    /**
     * This method is used to read the state of the LED from the device
     */
    public void readBleCharacteristic() {
        if (bAdapter == null || bleGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        bleGatt.readCharacteristic(myReadCharx);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param enabled        If true, enable notification.  False otherwise.
     */
    private void setCharacteristicNotification(boolean enabled) {
        if (myNotifycharx!= null)
            bleGatt.setCharacteristicNotification(myNotifycharx, enabled);
    }

    public String getMyLogData (){
        return MyLogData;
    }

}
