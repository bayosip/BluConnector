package inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms;

import android.app.Activity;
import android.bluetooth.le.ScanCallback;
import android.net.nsd.NsdManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import inc.osips.iot_wireless_communication.wireless_comms_module.interfaces.WirelessDeviceConnectionScanner;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.ble_comms.BLE_Scanner;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.utility.IoTCommException;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.wlan_comms.WLANServiceScanner;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.utility.Constants;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.p2p_comms.P2p_Scanner;

public class DeviceScannerFactory {


    private Activity activity;
    private volatile static DeviceScannerFactory factory_instance = null;

    private DeviceScannerFactory(@NonNull Activity activity) {
        this.activity = activity;
    }

    public synchronized static DeviceScannerFactory withActivity(@NonNull Activity activity){
        if(factory_instance ==null)
            factory_instance = new DeviceScannerFactory(activity);
        return factory_instance;
    }

    public Builder getRemoteDeviceBuilderScannerOfType(@NonNull String connectionType) throws IoTCommException {

        if(TextUtils.isEmpty(connectionType))return null;

        else if (connectionType.equals(Constants.BLE)){
            return new BLEScanBuilder(activity);
        }

        else if (connectionType.equals(Constants.P2P)){
            return new WifiP2pScanBuilder(activity);
        }

        else if(connectionType.equals(Constants.WLAN)){
            return new WlanServiceScanBuilder(activity);
        }

        else throw new IoTCommException("Invalid, or Unsupported Remote Communication Type", connectionType);
    }

    abstract public static class Builder{
        protected Activity activity;
        protected long milliSecs = 0;

        private Builder(Activity activity) {
            this.activity = activity;
        }

        abstract public WirelessDeviceConnectionScanner build();
        abstract public Builder setmScanCallback(@Nullable ScanCallback mScanCallback);
        abstract public Builder setmP2PPeerListListener(@Nullable WifiP2pManager.PeerListListener mPeerListListener);
        abstract public Builder setmNsdDiscoveryListener(@Nullable NsdManager.DiscoveryListener mNsdDiscoveryListener);
        abstract public Builder setDeviceUniqueID(@Nullable String UUID_IP);
        abstract public Builder setScanTime(long milliSecs);
    }


    private class BLEScanBuilder extends Builder {
        private ScanCallback mScanCallback;
        private String baseUUID = null;


        private BLEScanBuilder(Activity activity) {
            super(activity);
        }


        public Builder setmScanCallback(@Nullable ScanCallback mScanCallback) {
            this.mScanCallback = mScanCallback;
            return this;
        }

        @Override
        public Builder setmP2PPeerListListener(@Nullable WifiP2pManager.PeerListListener mPeerListListener) {
            return this;
        }

        @Override
        public Builder setmNsdDiscoveryListener(@Nullable NsdManager.DiscoveryListener mNsdDiscoveryListener) {
            return this;
        }

        @Override
        public Builder setDeviceUniqueID(@Nullable String UUID) {
            this.baseUUID = UUID;
            return this;
        }

        @Override
        public Builder setScanTime(long milliSecs) {
            this.milliSecs = milliSecs;
            return this;
        }

        public WirelessDeviceConnectionScanner build() throws NullPointerException{
                return new BLE_Scanner(activity, mScanCallback, baseUUID, milliSecs);
        }
    }

    private class WifiP2pScanBuilder extends Builder{

        private WifiP2pManager.PeerListListener mPeerListListener;
        private String address = null;

        private WifiP2pScanBuilder(Activity activity) {
            super(activity);
        }

        @Override
        public Builder setmP2PPeerListListener(@Nullable WifiP2pManager.PeerListListener mPeerListListener) {
            this.mPeerListListener = mPeerListListener;
            return this;
        }

        @Override
        public Builder setmNsdDiscoveryListener(@Nullable NsdManager.DiscoveryListener mNsdDiscoveryListener) {
            return this;
        }

        @Override
        public Builder setDeviceUniqueID(@Nullable String ipAddress) {

            this.address = ipAddress;
            return this;
        }

        @Override
        public Builder setScanTime(long milliSecs) {
            this.milliSecs = milliSecs;
            return this;
        }

        @Override
        public WirelessDeviceConnectionScanner build(){
                return new P2p_Scanner(activity, mPeerListListener, milliSecs);
        }

        @Override
        public Builder setmScanCallback(@Nullable ScanCallback mScanCallback) {
            return this;
        }
    }

    private class WlanServiceScanBuilder extends Builder{

        NsdManager.DiscoveryListener mDiscoveryListener;
        String serviceType;

        private WlanServiceScanBuilder(Activity activity) {
            super(activity);
        }

        @Override
        public WirelessDeviceConnectionScanner build() throws NullPointerException{
            if (TextUtils.isEmpty(serviceType)) {
                throw new NullPointerException("Specify The Service Type to Scan ");
            }

           return new WLANServiceScanner(activity, milliSecs, mDiscoveryListener, serviceType);
        }

        @Override
        public Builder setmScanCallback(@Nullable ScanCallback mScanCallback) {
            return this;
        }

        @Override
        public Builder setmP2PPeerListListener(@Nullable WifiP2pManager.PeerListListener mPeerListListener) {
            return this;
        }

        @Override
        public Builder setmNsdDiscoveryListener(@Nullable NsdManager.DiscoveryListener mNsdDiscoveryListener) {
            if (mNsdDiscoveryListener!=null)
                this.mDiscoveryListener = mNsdDiscoveryListener;
            return this;
        }

        @Override
        public Builder setDeviceUniqueID(@Nullable String serviceType) {
            this.serviceType = serviceType;
            return this;
        }

        @Override
        public Builder setScanTime(long milliSecs) {
            this.milliSecs = milliSecs;
            return this;
        }
    }
}
