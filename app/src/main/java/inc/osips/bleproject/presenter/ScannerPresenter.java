package inc.osips.bleproject.presenter;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import inc.osips.bleproject.App;
import inc.osips.bleproject.interfaces.PresenterInterface;
import inc.osips.bleproject.interfaces.WirelessConnectionScanner;
import inc.osips.bleproject.interfaces.ScannerViewInterface;
import inc.osips.bleproject.model.Devices;
import inc.osips.bleproject.model.DeviceScannerFactory;
import inc.osips.bleproject.model.utilities.Constants;
import inc.osips.bleproject.model.utilities.GeneralUtil;
import inc.osips.bleproject.view.activities.ControllerActivity;

public class ScannerPresenter extends ScanCallback implements PresenterInterface, WifiP2pManager
        .PeerListListener{

    private WirelessConnectionScanner scanner;
    private ScannerViewInterface viewInterface;
    private List<Devices> devices;


    public ScannerPresenter(ScannerViewInterface viewInterface, String commsType) {
        this.viewInterface = viewInterface;
        DeviceScannerFactory factory = new DeviceScannerFactory(this);
        scanner = factory.getRemoteConnectionScannerOfType(commsType);
        devices = new LinkedList<>();
    }

    public void shouldStartScan(){
        if(!scanner.isScanning())
            viewInterface.launchRingDialog();
    }

    public Activity getScanningActivity(){
        return viewInterface.getCurrentActivity();
    }

    public void startScanningForRemoteDevices(){
        scanner.onStart();
    }

    public void stopScanner(){
        scanner.onStop();
    }


    @Override
    public ScanCallback getScanCallBack() {
        return this;
    }

    @Override
    public WifiP2pManager.PeerListListener getPeerListListener() {
        return this;
    }


    @Override
    public void registerBroadCastReceiver(Activity activity) {
        activity.registerReceiver(wifiReceiver, commsUpdateIntentFilter());
    }

    @Override
    public void unregisterBroadCastReceiver(Activity activity) {
        activity.unregisterReceiver(wifiReceiver);
    }

    @Override
    public void onScanResult(int callbackType, final ScanResult result) {
        Log.i("callbackType", String.valueOf(callbackType));
        Log.i("result", result.toString());
        final int RSSI = result.getRssi();
        if (RSSI>=-105) {
            scanner.onStop();
            Bundle data = new Bundle();
            data.putString(Constants.COMM_TYPE, Constants.BLE);
            data.putParcelable(Constants.DEVICE_DATA, result.getDevice());
            if(!(App.getCurrentActivity() instanceof ControllerActivity))
                viewInterface.goToDeviceControlView(data);
        }
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        for (ScanResult sr : results) {
            Log.i("ScanResult - Results", sr.toString());
            //ToastMakers.message(scannerActivity.getApplicationContext(), sr.toString());
        }
    }

    @Override
    public void onScanFailed(int errorCode) {
        Log.e("Scan Failed", "Error Code: " + errorCode);
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] bytes) {
        final int RSSI = rssi;
        if (RSSI >= -105){
            scanner.onStop();
            Bundle data = new Bundle();
            data.putString(Constants.COMM_TYPE, Constants.BLE);
            data.putParcelable(Constants.DEVICE_DATA, device);
            if(!(App.getCurrentActivity() instanceof ControllerActivity)) {
                viewInterface.goToDeviceControlView(data);
            }
        }
    }

    private final BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:

                case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                    scanner.showDiscoveredDevices();
                    scanner.onStop();
                    break;
                case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                    int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                    switch (state){
                        case WifiP2pManager.WIFI_P2P_STATE_ENABLED:
                            GeneralUtil.message("Wifi is Ok");
                            break;
                        case WifiP2pManager.WIFI_P2P_STATE_DISABLED:
                            GeneralUtil.message("Wifi has been turned off, " +
                                    "Please turn on to use this feature");
                            break;
                    }
                    break;
                case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                    break;
            }
        }
    };

    /**
     * This sets up the filter for broadcasts that we want to be notified of.
     * This needs to match the broadcast receiver cases.
     * @return intentFilter
     */
    private IntentFilter commsUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        return intentFilter;
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        for (WifiP2pDevice p2pDevice: wifiP2pDeviceList.getDeviceList()){
            Devices device = new Devices(p2pDevice.deviceName,
                    p2pDevice.deviceAddress, p2pDevice.status, p2pDevice);
            devices.add(device);
        }

        if (devices.size() ==0){
            viewInterface.progressFromScan(devices);
        }
    }
}
