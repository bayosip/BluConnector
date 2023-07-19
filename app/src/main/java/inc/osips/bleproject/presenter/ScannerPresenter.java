package inc.osips.bleproject.presenter;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.LinkedList;
import java.util.List;

import inc.osips.bleproject.interfaces.PresenterInterface;
import inc.osips.bleproject.utilities.Constants;
import inc.osips.iot_wireless_communication.wireless_comms_module.interfaces.WirelessDeviceConnectionScanner;
import inc.osips.bleproject.interfaces.ScannerViewInterface;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.Devices;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.DeviceScannerFactory;
import inc.osips.bleproject.utilities.GeneralUtil;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.ble_comms.HW_Compatibility_Checker;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.utility.IoTCommException;

public class ScannerPresenter implements PresenterInterface {

    private static final String TAG = "ScannerPresenter";
    private WirelessDeviceConnectionScanner scanner;
    private ScannerViewInterface viewInterface;
    private List<Devices> devices;


    public ScannerPresenter(@NonNull ScannerViewInterface viewInterface, @NonNull String commsType) {
        this.viewInterface = viewInterface;
        DeviceScannerFactory factory = DeviceScannerFactory.withContext(viewInterface.getCurrentActivity());
        DeviceScannerFactory.Builder builder = null;
        try {
            builder = factory.getRemoteDeviceBuilderScannerOfType(commsType);
        } catch (IoTCommException e) {
            Log.e(TAG, "ScannerPresenter: ", e);
            e.printStackTrace();
        }

        if (builder != null)
            scanner = builder.build();
        devices = new LinkedList<>();
    }

    public boolean shouldStartScan() {
        return scanner.isScanning();
    }

    public Activity getScanningActivity() {
        return viewInterface.getCurrentActivity();
    }

    public void startScanningForRemoteDevices() {
        scanner.onStart(null);
    }

    public void stopScanner() {
        scanner.onStop();
    }


    @Override
    public void registerBroadCastReceiver(Activity activity) {
        activity.registerReceiver(commsReceiver, commsUpdateIntentFilter());
    }

    @Override
    public void unregisterBroadCastReceiver(Activity activity) {
        activity.unregisterReceiver(commsReceiver);
    }

    private final BroadcastReceiver commsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            if (action == null) return;
            switch (action) {
                case Constants.BLE_ACTION_TURN_ON_BLUETOOTH:
                    HW_Compatibility_Checker.requestUserBluetooth(viewInterface.getCurrentActivity());
                    break;
                case WirelessDeviceConnectionScanner.SCANNING_STOPPED:
                    Log.w("Ble", "scan stopped broadcast");
                    viewInterface.progressFromScan(devices);
                    break;
                case WirelessDeviceConnectionScanner.DEVICE_DISCOVERED:
                    Parcelable device = intent.getParcelableExtra(Constants.DEVICE_DATA);
                    if (device instanceof BluetoothDevice) {
                        BluetoothDevice ble = (BluetoothDevice) device;
                        Devices aDevice = new Devices(ble.getName(),
                                ble.getAddress(), ble.getBondState(), ble);
                        devices.add(aDevice);
                    } else if (device instanceof WifiP2pDevice) {
                        WifiP2pDevice p2pDevice = (WifiP2pDevice) device;
                        Devices aDevice = new Devices(p2pDevice.deviceName,
                                p2pDevice.deviceAddress, p2pDevice.status, p2pDevice);
                        devices.add(aDevice);

                    } else if (device instanceof NsdServiceInfo) {
                        NsdServiceInfo service = (NsdServiceInfo) device;
                        Devices aDevice = new Devices(service.getServiceName(),
                                service.getHost().toString(), service.getPort(), service);
                        devices.add(aDevice);
                    }
                    break;
                case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                    int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                    switch (state) {
                        case WifiP2pManager.WIFI_P2P_STATE_ENABLED:
                            GeneralUtil.message("Wifi is Ok");
                            break;
                        case WifiP2pManager.WIFI_P2P_STATE_DISABLED:
                            GeneralUtil.message("Wifi has been turned off, " +
                                    "Please turn on to use this feature");
                            break;
                        default:
                            break;
                    }
                    break;
                case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                    if (scanner != null) {
                        scanner.showDiscoveredDevices();
                        scanner.onStop();
                    }
                    break;
            }
        }
    };

    /**
     * This sets up the filter for broadcasts that we want to be notified of.
     * This needs to match the broadcast receiver cases.
     *
     * @return intentFilter
     */
    private IntentFilter commsUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BLE_ACTION_TURN_ON_BLUETOOTH);
        intentFilter.addAction(WirelessDeviceConnectionScanner.SCANNING_STOPPED);
        intentFilter.addAction(WirelessDeviceConnectionScanner.DEVICE_DISCOVERED);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        return intentFilter;
    }
}
