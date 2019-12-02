package inc.osips.bleproject.model;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.net.wifi.p2p.WifiP2pManager;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import inc.osips.bleproject.interfaces.PresenterInterface;
import inc.osips.bleproject.interfaces.WirelessConnectionScanner;
import inc.osips.bleproject.model.ble_comms.BLE_Scanner;
import inc.osips.bleproject.model.utilities.Constants;
import inc.osips.bleproject.model.wifi_comms.Wifi_Scanner;

public class DeviceScannerFactory {


    private Activity activity;

    public DeviceScannerFactory(Activity activity) {
        this.activity = activity;
    }

    public Builder getRemoteDeviceBuilderScannerOfType(String connectionType){

        if(TextUtils.isEmpty(connectionType))return null;

        else if (connectionType.equals(Constants.BLE)){
            return new BLEBuilder(activity);
        }

        else if (connectionType.equals(Constants.WIFI)){
            return new WifiBuilder(activity);
        }

        else return null;
    }

    abstract public static class Builder{
        Activity activity;

        private Builder(Activity activity) {
            this.activity = activity;
        }

        abstract public WirelessConnectionScanner build();
        abstract public Builder setmScanCallback(@Nullable ScanCallback mScanCallback);
        abstract public Builder setmWifiPeerListListener(@Nullable WifiP2pManager.PeerListListener mPeerListListener);
    }


    private class BLEBuilder extends Builder {
        private ScanCallback mScanCallback;

        private BLEBuilder(Activity activity) {
            super(activity);
        }


        public Builder setmScanCallback(ScanCallback mScanCallback) {
            this.mScanCallback = mScanCallback;
            return this;
        }

        @Override
        public Builder setmWifiPeerListListener(WifiP2pManager.PeerListListener mPeerListListener) {
            return this;
        }

        public WirelessConnectionScanner build(){
            return new BLE_Scanner(activity, mScanCallback);
        }
    }

    private static class WifiBuilder extends Builder{

        private WifiP2pManager.PeerListListener mPeerListListener;

        private WifiBuilder(Activity activity) {
            super(activity);
        }

        public Builder setmWifiPeerListListener(WifiP2pManager.PeerListListener mPeerListListener) {
            this.mPeerListListener = mPeerListListener;
            return this;
        }

        @Override
        public WirelessConnectionScanner build(){
            return new Wifi_Scanner(activity, mPeerListListener);
        }

        @Override
        public Builder setmScanCallback(ScanCallback mScanCallback) {
            return this;
        }
    }
}
