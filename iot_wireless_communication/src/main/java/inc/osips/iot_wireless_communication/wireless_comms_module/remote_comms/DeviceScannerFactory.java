package inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms;

import android.app.Activity;
import android.bluetooth.le.ScanCallback;
import android.net.wifi.p2p.WifiP2pManager;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import inc.osips.iot_wireless_communication.wireless_comms_module.interfaces.WirelessConnectionScanner;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.ble_comms.BLE_Scanner;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.utilities.Constants;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.wifi_comms.Wifi_Scanner;

public class DeviceScannerFactory {


    private Activity activity;
    public static final String SCANNING_STOPPED = "device_scanning stopped";

    public DeviceScannerFactory(Activity activity) {
        this.activity = activity;
    }

    public Builder getRemoteDeviceBuilderScannerOfType(String connectionType){

        if(TextUtils.isEmpty(connectionType))return null;

        else if (connectionType.equals(Constants.BLE)){
            return new BLEScanBuilder(activity);
        }

        else if (connectionType.equals(Constants.WIFI)){
            return new WifiScanBuilder(activity);
        }

        else return null;
    }

    abstract public static class Builder{
        protected Activity activity;
        protected long milliSecs = 0;

        private Builder(Activity activity) {
            this.activity = activity;
        }

        abstract public WirelessConnectionScanner build();
        abstract public Builder setmScanCallback(@Nullable ScanCallback mScanCallback);
        abstract public Builder setmWifiPeerListListener(@Nullable WifiP2pManager.PeerListListener mPeerListListener);
        abstract public Builder setDeviceUniqueID(@Nullable String UUID_IP);
        abstract public Builder setScanTime(long milliSecs);
    }


    private class BLEScanBuilder extends Builder {
        private ScanCallback mScanCallback;
        private String baseUUID = null;


        private BLEScanBuilder(Activity activity) {
            super(activity);
        }


        public Builder setmScanCallback(ScanCallback mScanCallback) {
            this.mScanCallback = mScanCallback;
            return this;
        }

        @Override
        public Builder setmWifiPeerListListener(@Nullable WifiP2pManager.PeerListListener mPeerListListener) {
            return this;
        }

        @Override
        public Builder setDeviceUniqueID(@Nullable String UUID_IP) {
            this.baseUUID = UUID_IP;
            return this;
        }

        @Override
        public Builder setScanTime(long milliSecs) {
            this.milliSecs = milliSecs;
            return this;
        }

        public WirelessConnectionScanner build(){
            return new BLE_Scanner(activity, mScanCallback, baseUUID, milliSecs);
        }
    }

    private class WifiScanBuilder extends Builder{

        private WifiP2pManager.PeerListListener mPeerListListener;
        private String address = null;

        private WifiScanBuilder(Activity activity) {
            super(activity);
        }

        @Override
        public Builder setmWifiPeerListListener(WifiP2pManager.PeerListListener mPeerListListener) {
            this.mPeerListListener = mPeerListListener;
            return this;
        }

        @Override
        public Builder setDeviceUniqueID(@Nullable String UUID_IP) {
            this.address = UUID_IP;
            return this;
        }

        @Override
        public Builder setScanTime(long milliSecs) {
            this.milliSecs = milliSecs;
            return this;
        }

        @Override
        public WirelessConnectionScanner build(){
            return new Wifi_Scanner(activity, mPeerListListener, milliSecs);
        }

        @Override
        public Builder setmScanCallback(@Nullable ScanCallback mScanCallback) {
            return this;
        }
    }
}
