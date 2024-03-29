package inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.ble_comms;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.UUID;

import inc.osips.iot_wireless_communication.wireless_comms_module.interfaces.WirelessDeviceConnector;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.DeviceConnectionFactory;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.utility.Util;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.ble_comms.services.BleGattService;

public class BleConnection implements WirelessDeviceConnector {

    private volatile BleGattService gattService;
    private BluetoothDevice bleDevice;
    private final Context context;
    private String baseUUID;

    private boolean mBound = false;
    private static final String TAG = "BLE Connection";

    public BleConnection(@NonNull Context context, @NonNull Parcelable bleDevice,
                         @Nullable String baseUUID) {
        if (!TextUtils.isEmpty(baseUUID))
            this.baseUUID = baseUUID;
        this.bleDevice = (BluetoothDevice) bleDevice;
        this.context = context;
    }

    @Override
    public ServiceConnection getServiceConnection() {
        return mConnection;
    }

    @Override
    public void enableNotificationsFor(String serviceUuid, String attrId,
                                       String descriptor, String deviceAddress) {
        gattService.writeToDescriptorToEnableNotifications(serviceUuid,
                attrId, descriptor, deviceAddress);
    }

    @Override
    public void disableNotificationsFor(String serviceUuid, String attrId,
                                        String descriptor, String deviceAddress) {
        gattService.writeToDescriptorToDisableNotifications(serviceUuid,
                attrId, descriptor, deviceAddress);
    }

    @Override
    public void connectToDeviceWithDeviceInfoFrom(@NonNull Intent intent) {
    }

    @Override
    public boolean isConnected() {
        return mBound;
    }

    //API 21 and Above
    private void ConnectToFirstBleDevice(){
        if (!tryBLEConnection(bleDevice, baseUUID)) {
            Util.message(context,"Cannot Connect to Device");
            context.sendBroadcast(new Intent(DeviceConnectionFactory.FAILED_DEVICE_CONNECTION));
        }
    }

    @Override
    public void connectAnotherDeviceSimultaneously(@NonNull Parcelable device,
                                                   @Nullable String serviceUUID) {
        if (!tryBLEConnection(device, serviceUUID)) {
            Util.message(context,"Cannot Connect to Device -> " +((BluetoothDevice)device).getName());
            context.sendBroadcast(new Intent(DeviceConnectionFactory.FAILED_DEVICE_CONNECTION));
        }
    }

    private boolean tryBLEConnection(@NonNull Parcelable device,
                                     @Nullable String serviceUUID) {
        if (gattService != null){
            if(gattService.init()){
                return gattService.connect((BluetoothDevice) device, serviceUUID);
            }
            return false;
        }
        else{
            Log.w(TAG, "No connection established");
            return false;
        }
    }

    @Override
    public void increaseMessagingByteLimit(@NonNull String address, int size) {
        gattService.increaseGattMaxMtuSizeOfDeviceAddr(address, size);
    }

    @Override
    public void maxOutMessagingByteLimit(@NonNull String address) {
        gattService.maxOutGattMtuSize(address);
    }

    @Override
    public void disconnectDevice(@NonNull String address) {
        gattService.disconnect(address);
    }

    @Override
    public void selectServiceUsingUUID(@NonNull String deviceAddress, @NonNull String UUID) {
        gattService.selectServiceFromUUID(deviceAddress,UUID);
    }

    @Override
    public void sendStringInstructionsToRemoteDevice(@Nullable String deviceAddress, @NonNull String instructions) {
        sendStringInstructionsToRemoteDevice(deviceAddress, null, instructions);
    }

    @Override
    public void sendStringInstructionsToRemoteDevice(@Nullable String deviceAddress,
                                                     @Nullable UUID charxUuid, @NonNull String instructions) {
        gattService.sendStringInstructionsToConnectedDevice(deviceAddress, charxUuid, instructions);
    }

    @Override
    public void sendBytesInstructionsToRemoteDevice(String deviceAddr, @Nullable UUID charxUuid,
                                                    byte[] data) {
        gattService.sendBytesInstructionsToConnectedDevice(deviceAddr, charxUuid, data);
    }

    @Override
    public void sendBytesInstructionsToRemoteDevice(String deviceAddr, byte[] data) {
        sendBytesInstructionsToRemoteDevice(deviceAddr, null, data);
    }

    private final ServiceConnection mConnection =
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
                        ConnectToFirstBleDevice();
                    } else {
                        Util.message(context,"API too low for App!");
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName arg0) {
                    mBound = false;
                    Log.i(TAG, "Service Disconnected");
                    context.sendBroadcast(new Intent(DeviceConnectionFactory.DEVICE_CONNECTION_SERVICE_STOPPED));
                }
            };

}
