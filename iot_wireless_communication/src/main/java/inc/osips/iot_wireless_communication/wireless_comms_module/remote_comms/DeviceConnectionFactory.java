package inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import inc.osips.iot_wireless_communication.wireless_comms_module.interfaces.WirelessDeviceConnector;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.ble_comms.BleConnection;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.utility.Constants;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.p2p_comms.P2pConnection;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.utility.IoTCommException;

public class DeviceConnectionFactory {

    private Context context;
    public final static String FAILED_DEVICE_CONNECTION = "device_connection_failed";
    public final static String DEVICE_CONNECTION_SERVICE_STOPPED = "connection_service_stopped";
    private volatile static DeviceConnectionFactory factory_instance = null;
    private DeviceConnectionFactory(@NonNull Context activity) {
        this.context = activity;
    }

    public synchronized static DeviceConnectionFactory withContext(@NonNull Context context){
        if (factory_instance ==null){
            factory_instance = new DeviceConnectionFactory(context);
        }
        return factory_instance;
    }
    public Builder getDeviceConnectionBuilder(@NonNull String connectionType, @NonNull Parcelable device) throws IoTCommException {

        if(TextUtils.isEmpty(connectionType))return null;

        else if (connectionType.equals(Constants.BLE)){
            return new BleDeviceConnectionBuilder(context, device);
        }

        else if (connectionType.equals(Constants.P2P)){
            return new P2PDeviceConnectionBuilder(context, device);
        }

        else throw new IoTCommException("Invalid, or Unsupported Remote Communication Type", connectionType);
    }

    abstract public static class Builder{
        Context context1;
        Parcelable device;

        private Builder(Context activity, Parcelable device) {

            this.context1 = activity;
            this.device = device;
        }

        abstract public WirelessDeviceConnector build();
        abstract public DeviceConnectionFactory.Builder setDeviceUniqueID(String UUID_IP);
        public String getDeviceType() throws IoTCommException{
            if(device instanceof BluetoothDevice){
                return Constants.BLE;
            }else if (device instanceof WifiP2pDevice) return Constants.P2P;

            else throw  new IoTCommException("This Parcelable object is not recognised", "Error!");
        }
        abstract public Builder setConnectionTimeOut(int timeOut);
        abstract public Builder setMaxTransmissionUnit(int size);
    }

    private class P2PDeviceConnectionBuilder extends Builder{

        String UUID_ServiceType;
        int PORT, timeOut;


        private P2PDeviceConnectionBuilder(Context activity, Parcelable device) {
            super(activity, device);
        }

        @Override
        public WirelessDeviceConnector build() {
            return new P2pConnection(context1, device,  timeOut);
        }

        @Override
        public Builder setDeviceUniqueID(@Nullable String ip) {
            this.UUID_ServiceType = ip;
            return this;
        }

        @Override
        public Builder setConnectionTimeOut(int timeOut) {
            this.timeOut = timeOut;
            return this;
        }

        @Override
        public Builder setMaxTransmissionUnit(int size) {
            return this;
        }
    }

    private class BleDeviceConnectionBuilder extends Builder{

        String UUID_IP;
        int ATT_MTU =0;
        private BleDeviceConnectionBuilder(Context activity, Parcelable device) {
            super(activity, device);
        }

        @Override
        public WirelessDeviceConnector build(){
                return new BleConnection(context1, device, UUID_IP, ATT_MTU);
        }

        @Override
        public Builder setDeviceUniqueID(@Nullable String UUID) {
            this.UUID_IP = UUID;
            return this;
        }

        @Override
        public Builder setConnectionTimeOut(int timeOut) {
            return this;
        }

        @Override
        public Builder setMaxTransmissionUnit(int size) {
            ATT_MTU = size;
            return this;
        }
    }

    private class LocalWiFiServiceConnectionBuilder extends Builder{

        private LocalWiFiServiceConnectionBuilder(Activity activity, Parcelable device) {
            super(activity, device);
        }

        @Override
        public WirelessDeviceConnector build() {
            return null;
        }

        @Override
        public Builder setDeviceUniqueID(String UUID_IP) {
            return null;
        }

        @Override
        public String getDeviceType() {
            return null;
        }

        @Override
        public Builder setConnectionTimeOut(int timeOut) {
            return null;
        }

        @Override
        public Builder setMaxTransmissionUnit(int size) {
            return null;
        }
    }
}
