package inc.osips.bleproject.model.ble_comms.services;

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
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import inc.osips.bleproject.model.utilities.GeneralUtil;

/**
 * Created by BABY v2.0 on 10/11/2016.
 */

public class BleGattService extends Service {
    private final static String TAG = BleGattService.class.getSimpleName();
    private BluetoothGatt bleGatt;
    private boolean hasService;
    private BluetoothManager bManager;
    private BluetoothAdapter bAdapter;

    //  Queue for BLE events
    //  This is needed so that rapid BLE events don't get dropped
    private static final Queue<Object> BleQueue = new LinkedList<>();
    private int mConnectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private static boolean mLedSwitchState = false;

    public final static String ACTION_CONNECTED =
            "inc.osips.bleproject.Model.Services.BleGattService.ACTION_CONNECTED";
    public final static String ACTION_DISCONNECTED =
            "inc.osips.bleproject.Model.Services.BleGattService.ACTION_DISCONNECTED";
    public final static String ACTION_BLE_SERVICES_DISCOVERED =
            "inc.osips.bleproject.Model.Services.BleGattService.ACTION_BLE_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "inc.osips.bleproject.Model.Services.BleGattService.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "inc.osips.bleproject.Model.Services.BleGattService.EXTRA_DATA";
    public  String EXTRA_UUID;

    public final String serviceUUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public final String writeUUID = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    public final String readUUID  = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    int mydata;
    String MyLogData;

    private BluetoothGattCharacteristic myWriteCharx;
    private BluetoothGattCharacteristic myReadCharx;

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
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                broadcastUpdate(ACTION_CONNECTED);
                MyLogData += "Connected to GATT server.\n";
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                MyLogData +="Attempting to start service discovery:" +
                        bleGatt.discoverServices() +"\n";
                Log.i(TAG, "Attempting to start service discovery:" +
                        bleGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
                MyLogData += "Disconnected from GATT server.\n";
                broadcastUpdate(ACTION_DISCONNECTED);
            }else if (newState == BluetoothProfile.STATE_DISCONNECTING){
                GeneralUtil.message("Disconnecting...");
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

                BluetoothGattService myGattService = gatt.getService(UUID.fromString(serviceUUID));
                myWriteCharx = myGattService.getCharacteristic(UUID.fromString(writeUUID));
                myReadCharx = myGattService.getCharacteristic(UUID.fromString(readUUID));
                broadcastUpdate(ACTION_BLE_SERVICES_DISCOVERED);
                readLedCharacteristic();
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
                broadcastUpdate(ACTION_DATA_AVAILABLE);
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
            broadcastUpdate(ACTION_DATA_AVAILABLE);
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

    /**
     * Initialize a reference to the local Bluetooth adapter.
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        Log.i(TAG, "Initialising BLE");
        if (bManager == null) {
            bManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
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
    public boolean connect(final BluetoothDevice device) {
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
        Log.i(TAG, "Trying to create a new connection.");
        MyLogData +="Trying to create a new connection.\n";
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (bleGatt== null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            GeneralUtil.message( "No Device Connect!");
        }
        else {
            bleGatt.disconnect();
            close();
            bleGatt = null;
            broadcastUpdate(ACTION_DISCONNECTED);
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

    public void writeLEDInstructions(String instruct) {
        try {
            if (bleGatt != null) {
                byte[] bytes = instruct.getBytes();
                myWriteCharx.setValue(instruct);
                writeCharacteristic(myWriteCharx);
            }
        }catch (NullPointerException e)
        {
            Log.w(TAG, "Characteristic is null: " + e.toString());
            GeneralUtil.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    GeneralUtil.message( "Bluetooth error! Check Connection");
                }
            });
        }
    }

    /**
     * Request a write on a given {@code BluetoothGattCharacteristic}.
     *
     * @param characteristic The characteristic to write.
     */
    private void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        try{
            BleQueue.add(characteristic);
            if (BleQueue.size() == 1) {
                bleGatt.writeCharacteristic(characteristic);
                Log.i(TAG, "Writing Characteristic");
            }
        }catch (NullPointerException e){
            Log.w(TAG, "BluetoothAdapter not initialized: "+e.toString());
            GeneralUtil.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    GeneralUtil.message( "Bluetooth error! Check Connection");
                }
            });
        }
    }

    /**
     * This method is used to read the state of the LED from the device
     */
    public void readLedCharacteristic() {
        if (bAdapter == null || bleGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        bleGatt.readCharacteristic(myReadCharx);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    private void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                               boolean enabled) {
    }

    public String getMyLogData (){
        return MyLogData;
    }

}
