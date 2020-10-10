package inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.ble_comms;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import inc.osips.iot_wireless_communication.wireless_comms_module.interfaces.WirelessDeviceConnector;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.DeviceConnectionFactory;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.utility.Util;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.ble_comms.services.BleGattService;

public class BleConnection implements WirelessDeviceConnector {

    private volatile BleGattService gattService;
    private BluetoothDevice bleDevice;
    private Activity activity;
    private String baseUUID;
    private int GATT_MAX_MTU_SIZE = 0;

    private boolean mBound = false;
    private static final String TAG = "BLE Connection";

    public BleConnection(@NonNull Activity activity, @NonNull Parcelable bleDevice,
                         @Nullable String baseUUID, int GATT_MAX_MTU_SIZE) {
        if (!TextUtils.isEmpty(baseUUID))
            this.baseUUID = baseUUID;
        this.bleDevice = (BluetoothDevice) bleDevice;
        this.activity = activity;
        this.GATT_MAX_MTU_SIZE = GATT_MAX_MTU_SIZE;
    }

    @Override
    public ServiceConnection getServiceConnection() {
        return mConnection;
    }

    @Override
    public void connectToDeviceWithDeviceInfoFrom(Intent intent) {

    }

    @Override
    public boolean isConnected() {
        return mBound;
    }

    //API 21 and Above
    private void ConnectToBleDevice(){
        if (tryBLEConnection()) {
            return;
        } else {
            Util.message(activity,"Cannot Connect to Device");
            activity.sendBroadcast(new Intent(DeviceConnectionFactory.FAILED_DEVICE_CONNECTION));
        }
    }


    private boolean tryBLEConnection() {

        if (gattService != null){
            if(gattService.init()){
                final boolean result = gattService.connect(bleDevice, baseUUID, GATT_MAX_MTU_SIZE);
                return result;
            }
            return false;
        }
        else{
            Log.w(TAG, "no uuid");
            return false;
        }
    }

    @Override
    public void selectServiceUsingUUID(@NonNull String UUID) {
        gattService.selectServiceFromUUID(UUID);
    }

    @Override
    public void sendInstructionsToRemoteDevice(String instuctions) {
        gattService.sendInstructionsToConnectedDevice(instuctions);
    }

    private ServiceConnection mConnection =
            /*
             * Defines callbacks for service binding, passed to bindService()
             */
        new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName className,
                                               IBinder service) {
                    //Log.i(TAG, "binding services");
                    BleGattService.BTLeServiceBinder binder = (BleGattService.BTLeServiceBinder) service;
                    gattService = binder.getService();
                    mBound = true;
                    if (Build.VERSION.SDK_INT >= 21) {
                        ConnectToBleDevice();
                    } else {
                        Util.message(activity,"API too low for App!");
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName arg0) {
                    mBound = false;
                    Log.i(TAG, "Service Disconnected");
                    activity.sendBroadcast(new Intent(DeviceConnectionFactory.DEVICE_CONNECTION_SERVICE_STOPPED));
                }
            };

}
