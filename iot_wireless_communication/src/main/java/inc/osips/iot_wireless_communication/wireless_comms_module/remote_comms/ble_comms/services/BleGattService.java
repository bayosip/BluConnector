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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import inc.osips.iot_wireless_communication.R;
import inc.osips.iot_wireless_communication.wireless_comms_module.BleWriteService;
import inc.osips.iot_wireless_communication.wireless_comms_module.interfaces.WirelessDeviceConnector;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.ble_comms.SampleGattAttributes;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.utility.Constants;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.utility.Util;

/**
 * Created by Adebayo Osipitan on 10/11/2016.
 */

public class BleGattService extends Service {
    private final static String TAG = BleGattService.class.getSimpleName();
    private final HashMap<String, BluetoothGatt> multiBleGatt = new HashMap<>();
    private final HashMap<BluetoothGatt, BleWriteService> gattServicesMap = new HashMap<>();
    private boolean hasService;
    private BluetoothManager bManager;
    private BluetoothAdapter bAdapter;
    private boolean isHasService = false;

    //  Queue for BLE events
    //  This is needed so that rapid BLE events don't get dropped
    private static final Queue<Object> BleQueue = new LinkedList<>();
    private int GATT_MAX_MTU_SIZE = 517;// default ATT MTU size

    public  String EXTRA_UUID;

    //public String serviceUUID;// = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    int mydata;
    String MyLogData;

//    private BluetoothGattCharacteristic myWriteCharx;
//    private BluetoothGattCharacteristic myReadCharx;
//    private BluetoothGattCharacteristic myNotifycharx;

    public class BTLeServiceBinder extends Binder {
        public BleGattService getService(){
            return BleGattService.this;
        }
    }

    public HashMap<String, BluetoothGatt> getMultiBleGatt() {
        return multiBleGatt;
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
        //disconnect();
        for (String addr: multiBleGatt.keySet()){
            disconnect(multiBleGatt.get(addr));
        }
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

    private BluetoothGattCallback createGattCallBack(final String address) {
        return new BluetoothGattCallback() {
            /**
             * This is called on a connection state change (either connection or disconnection)
             *
             * @param gatt     The GATT database object
             * @param status   Status of the event
             * @param newState New state (connected or disconnected)
             */
            private BluetoothGatt bleGatt;
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if(status == BluetoothGatt.GATT_SUCCESS){
                    switch (newState) {
                        case BluetoothProfile.STATE_CONNECTED:
                            if (gatt!=null) {
                                bleGatt = gatt;

                                multiBleGatt.put(address, gatt);
                                broadcastUpdate(Constants.ACTION_CONNECTED, address);
                                MyLogData += "Connected to GATT server.\n";
                                Log.i(TAG, "Connected to GATT server.");
                                // Attempts to discover services after successful connection.
                                MyLogData += "Attempting to start service discovery:" +
                                        gatt.discoverServices() + "\n";
                                Log.i(TAG, MyLogData);
                            }
                            break;
                        case BluetoothProfile.STATE_DISCONNECTED:
                            Log.i(TAG, "Disconnected from GATT server.");
                            MyLogData += "Disconnected from GATT server.\n";
                            broadcastUpdate(Constants.ACTION_DISCONNECTED, address);
                            gatt.close();
                            break;
                        case BluetoothProfile.STATE_DISCONNECTING:
                            Util.message(BleGattService.this, getString(R.string.disconnecting));
                            break;
                    }
                }else {
                    Util.getHandler().post(()->Util.message(BleGattService.this,
                            "Connection rejected or device too far"));

                    gatt.close();
                }
            }

            /**
             * This is called when service discovery has completed.
             * It broadcasts an update to the main activity.
             *
             * @param gatt   The GATT database object
             * @param status Status of whether the discovery was successful.
             */
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    //getGattServices(services);

                /*BluetoothGattService myGattService = gatt.getService(UUID.fromString(serviceUUID));
                myWriteCharx = myGattService.getCharacteristic(UUID.fromString(writeUUID));
                myReadCharx = myGattService.getCharacteristic(UUID.fromString(readUUID));*/
                    Log.w(TAG, "Services Discovered");
                    broadcastUpdate(Constants.ACTION_BLE_SERVICES_DISCOVERED, address,
                            gatt.getServices());
                    //readBleCharacteristic();
                } else {
                    Log.w(TAG, "onServicesDiscovered received: " + status);
                    MyLogData += "onServicesDiscovered received: " + status + "\n";
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
                    if (characteristic.getUuid() == UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")) {

                    }
                    broadcastUpdate(Constants.ACTION_DATA_AVAILABLE, address, characteristic);
                    broadcastUpdateRaw(Constants.ACTION_RAW_DATA_AVAILABLE, address, characteristic);
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
                }else {
                    boolean flag =status==BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH;
                    Log.e(TAG, "onCharacteristicWrite: Failed with GATT_INVALID_ATTRIBUTE_LENGTH ="
                            + flag);
                    Util.message(BleGattService.this, "Write failed! Send smaller bytes");
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
                Log.d(TAG, "onCharacteristicChanged: "+ mydata +
                        ", hex: "+ characteristic.getStringValue(0));
                // Tell the activity that new car data is available
                broadcastUpdate(Constants.ACTION_BLE_CHARX_DATA_CHANGE, address,characteristic);
                broadcastUpdateRaw(Constants.ACTION_BLE_CHARX_DATA_CHANGE_RAW, address,characteristic);
            }

            @Override
            public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                Log.i(TAG, "onMtuChanged: ATT MTU changed to " + mtu
                        +", success: "+ status);
                if (status == BluetoothGatt.GATT_SUCCESS){
                    sendBroadcast(new Intent(WirelessDeviceConnector.MTU_CHANGE_SUCCESS));
                }else {
                    sendBroadcast(new Intent(WirelessDeviceConnector.MTU_CHANGE_FAILURE));
                }
            }
        };
    }

    /**
     * Sends a broadcast to the listener in the main activity.
     * @param action The type of action that occurred.
     */
    private void broadcastUpdate(final String action, final String deviceAddress) {
        final Intent intent = new Intent(action);
        intent.putExtra(Constants.DEVICE_ADDRESS, deviceAddress);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final String deviceAddress,
                                 final List<BluetoothGattService> availableServices){
        ArrayList<String> uuid_strings = new ArrayList<>();
        for(BluetoothGattService service: availableServices){
            uuid_strings.add(service.getUuid().toString());
        }

        final Intent intent = new Intent(action);
        intent.putExtra(Constants.DEVICE_ADDRESS, deviceAddress);
        intent.putStringArrayListExtra(Constants.SERVICE_UUID, uuid_strings);
        sendBroadcast(intent);
    }

    /**
     * Sends a broadcast to the listener in the main activity.
     * @param action The type of action that occurred, and
     * @param characteristic the ble gatt charx active on broadcast call.
     */
    private void broadcastUpdate(final String action, final String deviceAddress,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        intent.putExtra(Constants.DEVICE_ADDRESS, deviceAddress);
        intent.putExtra(Constants.EXTRA_UUID, characteristic.getUuid().toString());
        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        if (characteristic.getProperties() == BluetoothGattCharacteristic.PROPERTY_NOTIFY||
            characteristic.getProperties() == BluetoothGattCharacteristic.PROPERTY_INDICATE) {
            final byte[] data = characteristic.getValue();
            String s = String.format("%s ", data[0]);
            Log.d(TAG, "broadcastUpdate: ->" + s);
            intent.putExtra(Constants.EXTRA_DATA, (int)Integer.parseInt(s, 16));
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);

                for(byte byteChar : data) stringBuilder.append(String.format("%s ", byteChar));

                Log.e(TAG, stringBuilder.toString());
                String output = new String(data, StandardCharsets.UTF_8) + "\n" +
                        stringBuilder.toString();
                intent.putExtra(Constants.EXTRA_DATA, output);
            }
        }
        sendBroadcast(intent);
    }

    private void broadcastUpdateRaw(final String action, final String deviceAddress,
                                      final BluetoothGattCharacteristic characteristic){
        final Intent intent = new Intent(action);
        intent.putExtra(Constants.DEVICE_ADDRESS, deviceAddress);
        intent.putExtra(Constants.EXTRA_UUID, characteristic.getUuid().toString());

        final byte[] data = characteristic.getValue();
        intent.putExtra(Constants.EXTRA_DATA_RAW, data);
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
        if(bAdapter==null)
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
    public boolean connect(@NonNull final BluetoothDevice device, @Nullable String serviceUUID) {


//        if(!TextUtils.isEmpty(serviceUUID))
//            this.serviceUUID = serviceUUID;

        if (bAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        if(multiBleGatt.containsKey(device.getAddress())) {
            //try to reconnect to previously connected device
            Log.i(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            MyLogData += "Trying to use an existing mBluetoothGatt for connection.\n";
            BluetoothGatt gatt = multiBleGatt.get(device.getAddress());
            assert gatt != null;
            return gatt.connect();
        }

        BluetoothGatt gatt = device.connectGatt(this,
                false,
                createGattCallBack(device.getAddress()));

        if(!TextUtils.isEmpty(serviceUUID)){
            assert serviceUUID != null;
            selectServiceFromUUID(device.getAddress(), serviceUUID);
        }
        Log.i(TAG, "Trying to create a new connection.");
        MyLogData +="Trying to create a new connection.\n";
        return true;
    }

    public void increaseGattMaxMtuSizeOfDeviceAddr(String addr, int size){
        BluetoothGatt gatt = multiBleGatt.get(addr);
        try {
            assert gatt != null;
            gatt.requestMtu(size);
        }catch (NullPointerException | AssertionError ex){
            ex.printStackTrace();
        }
    }

    public void maxOutGattMtuSize(String addr){
        BluetoothGatt gatt = multiBleGatt.get(addr);
        try {
            assert gatt != null;
            gatt.requestMtu(this.GATT_MAX_MTU_SIZE);
        }catch (NullPointerException | AssertionError ex){
            ex.printStackTrace();
        }
    }

    public void selectServiceFromUUID (@NonNull String deviceAddr, @NonNull String UUID){
        //this.serviceUUID = UUID;
        BluetoothGatt gatt = multiBleGatt.get(deviceAddr);
        assert gatt != null;
        BluetoothGattService service = getGattServices(gatt, UUID);

        if (service==null){
            Util.message(BleGattService.this, "No service with selected UUID!");
        }else {
            getGattServicesCharx(gatt, service);
            Log.d(TAG, "selectServiceFromUUID: service uuid: " + UUID + " connected");
        }
    }

    /*
     * Loop through the discovered ble services finds the service matching
     * */

    private BluetoothGattService getGattServices(@NonNull BluetoothGatt gatt, @NonNull String UUID){
        if ( gatt.getServices() == null ||gatt.getServices().isEmpty()){
            stopSelf();
            return null;
        }
        List<BluetoothGattService> services = gatt.getServices();
        String uuid ;

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : services) {

            uuid = gattService.getUuid().toString();
            Log.d(TAG, "getGattServices: " + uuid);
            if (uuid.equalsIgnoreCase(UUID)) {
                return gattService;
            }
        }
        return null;
    }

    private void getGattServicesCharx(@NonNull BluetoothGatt gatt,
                                      @NonNull BluetoothGattService gattService) {

        List<BluetoothGattCharacteristic> gattCharacteristics =
                gattService.getCharacteristics();
        BleWriteService mServiceObj = new BleWriteService(gattService);

        // Loops through available Characteristics.
        for (BluetoothGattCharacteristic gattCharacteristic :
                gattCharacteristics) {
            Log.d(TAG, "getGattServicesCharx: ->" + gattCharacteristic.getUuid().toString() +
                    ", property: " + gattCharacteristic.getProperties());
            int property = gattCharacteristic.getProperties();
            if (property> BluetoothGattCharacteristic.PROPERTY_BROADCAST &&
                    property != BluetoothGattCharacteristic.PROPERTY_READ &&
                property != BluetoothGattCharacteristic.PROPERTY_NOTIFY &&
                property != BluetoothGattCharacteristic.PROPERTY_INDICATE) {
                //myWriteCharx = gattCharacteristic;
                mServiceObj.getCharacteristics().add(gattCharacteristic);
                gattServicesMap.put(gatt, mServiceObj);
            }else if(property >=BluetoothGattCharacteristic.PROPERTY_READ &&
                    property<BluetoothGattCharacteristic.PROPERTY_NOTIFY){
                readBleCharacteristic(gatt, gattCharacteristic);
            }else if (property>= BluetoothGattCharacteristic.PROPERTY_NOTIFY
            && property< BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE){
                //myNotifycharx = gattCharacteristic;
                setCharacteristicNotification(gatt, gattCharacteristic, true);
                Log.i(TAG, "getGattServices: Notify charX discovered");
            }
        }
    }

    public BluetoothGattService getServiceFromStringUuid(@NonNull BluetoothGatt gatt,
                                                          String uuid){
        UUID mUid = UUID.fromString(uuid);
        return gatt.getService(mUid);
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect(BluetoothGatt bleGatt) {
        if (bleGatt== null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            Util.message( this,"No Device Connected!");
        }
        else {
            bleGatt.disconnect();
            close(bleGatt);
            broadcastUpdate(Constants.ACTION_DISCONNECTED, bleGatt.getDevice().getAddress());
        }
    }
    /**
     * After using a given BLE device, the App must call this method to ensure resources are
     * released properly.
     */
    private void close(BluetoothGatt bleGatt) {
        if (bleGatt == null) {
            return;
        }
        bleGatt.close();
        bleGatt = null;
    }

    public void sendInstructionsToConnectedDevice(String deviceAddr, @Nullable UUID charxDescriptor, byte[] data){
        BluetoothGatt bleGatt = multiBleGatt.get(deviceAddr);
        try {
            if (bleGatt != null) {
                //byte[] bytes = instructions.getBytes();
                BluetoothGattCharacteristic gattCharacteristic = null;
                BleWriteService wService = gattServicesMap.get(bleGatt);
                assert wService != null;
                if (charxDescriptor != null) {
                    for (BluetoothGattCharacteristic charx : wService.getCharacteristics()) {
                        if (charx.getDescriptor(charxDescriptor) != null) {
                            gattCharacteristic = charx;
                            break;
                        }
                    }
                }
                if (gattCharacteristic == null)
                    gattCharacteristic = wService.getCharacteristics().get(0);

                gattCharacteristic.setValue(data);
                writeBleCharacteristic(bleGatt, gattCharacteristic);
            }
        }catch (NullPointerException e)
            {
                Log.w(TAG, "Characteristic is null: " + e.toString());
                Util.getHandler().post(() -> Util.message( BleGattService.this, getString(R.string.ble_err_connection)));
            }
    }

    public void sendInstructionsToConnectedDevice(String deviceAddr, @Nullable UUID charxDescriptor,
                                                  String instructions) {
        BluetoothGatt bleGatt = multiBleGatt.get(deviceAddr);
        Log.d(TAG, "sendInstructionsToConnectedDevice: -> device address: " + deviceAddr );
        try {
            if (bleGatt != null) {
                //byte[] bytes = instructions.getBytes();
                BluetoothGattCharacteristic gattCharacteristic =null;
                BleWriteService wService = gattServicesMap.get(bleGatt);
                assert wService != null;
                //Checks for characteristic with particular descriptor
                if(charxDescriptor!=null){
                    for (BluetoothGattCharacteristic charx: wService.getCharacteristics()){
                        if(charx.getDescriptor(charxDescriptor)!=null){
                            gattCharacteristic = charx;
                            break;
                        }
                    }
                }
                if (gattCharacteristic==null) gattCharacteristic = wService.getCharacteristics().get(0);

                gattCharacteristic.setValue(instructions);
                writeBleCharacteristic(bleGatt, gattCharacteristic);
            }
        }catch (NullPointerException e) {
            Log.w(TAG, "Characteristic is null: " + e.toString());
            Util.getHandler().post(() -> Util.message( BleGattService.this, getString(R.string.ble_err_connection)));
        }catch (Exception ex){
            Log.w(TAG, "Service is null: " + ex.toString());
            Util.getHandler().post(() -> Util.message( BleGattService.this, getString(R.string.ble_err_connection)));
        }
    }

    /**
     * Request a write on a given {@code BluetoothGattCharacteristic}.
     */
    private void writeBleCharacteristic(BluetoothGatt bleGatt, BluetoothGattCharacteristic myWriteCharx) {
        try{
            BleQueue.add(myWriteCharx);
            if (BleQueue.size() == 1) {
                bleGatt.writeCharacteristic(myWriteCharx);
                Log.i(TAG, "Writing Characteristic");
            }
        }catch (NullPointerException e){
            Log.w(TAG, "BluetoothAdapter not initialized: "+e.toString());
            Util.getHandler().post(() -> Util.message( BleGattService.this,"Bluetooth error! Check Connection"));
        }
    }

    /**
     * This method is used to read the state of the LED from the device
     */
    public void readBleCharacteristic(BluetoothGatt bleGatt,
                                      BluetoothGattCharacteristic myReadCharx) {
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
    public boolean setCharacteristicNotification(BluetoothGatt bleGatt,
                                               BluetoothGattCharacteristic myNotifycharx,
                                               boolean enabled) {
        if (myNotifycharx!= null)
           return bleGatt.setCharacteristicNotification(myNotifycharx, enabled);

        // This is specific to Heart Rate Measurement.
        //writeToDescriptor(myNotifycharx, SampleGattAttributes.HEART_RATE_MEASUREMENT, bleGatt);
        return false;
    }

    public void writeToDescriptorToEnableNotifications(BluetoothGattCharacteristic charx, String uuidStr,
                                                       String CCC_DESCRIPTOR_UUID, BluetoothGatt gatt){
        if (setCharacteristicNotification(gatt, charx, true)) {
            UUID mUUID =
                    UUID.fromString(uuidStr);

            if (mUUID.equals(charx.getUuid())) {
                BluetoothGattDescriptor descriptor = charx.getDescriptor(
                        UUID.fromString(CCC_DESCRIPTOR_UUID));
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
        }
    }

    public void writeToDescriptorToDisableNotifications(BluetoothGattCharacteristic charx, String uuidStr,
                                                       String CCC_DESCRIPTOR_UUID, BluetoothGatt gatt){

        if (setCharacteristicNotification(gatt, charx, false)) {
            UUID mUUID =
                    UUID.fromString(uuidStr);

            if (mUUID.equals(charx.getUuid())) {
                BluetoothGattDescriptor descriptor = charx.getDescriptor(
                        UUID.fromString(CCC_DESCRIPTOR_UUID));
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
        }
    }

    public String getMyLogData (){
        return MyLogData;
    }
}
