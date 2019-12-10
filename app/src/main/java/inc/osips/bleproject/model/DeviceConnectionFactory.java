package inc.osips.bleproject.model;

import android.app.Activity;
import android.bluetooth.le.ScanCallback;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.UUID;

import inc.osips.bleproject.interfaces.ControllerViewInterface;
import inc.osips.bleproject.interfaces.WirelessConnectionScanner;
import inc.osips.bleproject.interfaces.WirelessDeviceConnector;
import inc.osips.bleproject.model.ble_comms.BLE_Scanner;
import inc.osips.bleproject.model.ble_comms.BleConnection;
import inc.osips.bleproject.model.utilities.Constants;
import inc.osips.bleproject.model.wifi_comms.WifiConnection;
import inc.osips.bleproject.model.wifi_comms.Wifi_Scanner;

public class DeviceConnectionFactory {

    private Activity activity;

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
            return "WiFi";
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
            return "BLE";
        }
    }
}
