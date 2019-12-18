package inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms;

import android.app.Activity;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import inc.osips.iot_wireless_communication.wireless_comms_module.interfaces.WirelessDeviceConnector;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.ble_comms.BleConnection;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.utilities.Constants;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.wifi_comms.WifiConnection;

public class DeviceConnectionFactory {

    private Activity activity;
    public final static String FAILED_DEVICE_CONNECTION = "device_connection_failed";
    public final static String DEVICE_CONNECTION_SERVICE_STOPPED = "connection_service_stopped";

    public DeviceConnectionFactory(Activity activity) {
        this.activity = activity;
    }

    public Builder establishConnectionWithDeviceOf(String connectionType, Parcelable device){

        if(TextUtils.isEmpty(connectionType))return null;

        else if (connectionType.equals(Constants.BLE)){
            return new BleDeviceConnectionBuilder(activity, device);
        }

        else if (connectionType.equals(Constants.WIFI)){
            return new WifiDeviceConnectionBuilder(activity, device);
        }

        else return null;
    }

    abstract public static class Builder{
        Activity activity;
        Parcelable device;

        private Builder(Activity activity, Parcelable device) {

            this.activity = activity;
            this.device = device;
        }

        abstract public WirelessDeviceConnector build();
        abstract public DeviceConnectionFactory.Builder setDeviceUniqueID(String UUID_IP);
        abstract public String getDeviceType();
    }

    private class WifiDeviceConnectionBuilder extends Builder{

        String UUID_IP;

        private WifiDeviceConnectionBuilder(Activity activity, Parcelable device) {
            super(activity, device);
        }

        @Override
        public WirelessDeviceConnector build() {
            return new WifiConnection(activity, device);
        }

        @Override
        public Builder setDeviceUniqueID(@Nullable String UUID_IP) {
            this.UUID_IP = UUID_IP;
            return this;
        }

        @Override
        public String getDeviceType() {
            return Constants.WIFI;
        }
    }

    private class BleDeviceConnectionBuilder extends Builder{

        String UUID_IP;
        private BleDeviceConnectionBuilder(Activity activity, Parcelable device) {
            super(activity, device);
        }

        @Override
        public WirelessDeviceConnector build() {
            Log.w("Connection++", UUID_IP);
            if (!TextUtils.isEmpty(UUID_IP))
                return new BleConnection(activity, device, UUID_IP);
            else return null;
        }

        @Override
        public Builder setDeviceUniqueID(@NonNull String UUID_IP) {
            this.UUID_IP = UUID_IP;
            return this;
        }

        @Override
        public String getDeviceType() {
            return Constants.BLE;
        }
    }
}
