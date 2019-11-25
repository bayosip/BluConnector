package inc.osips.bleproject.presenter;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

import inc.osips.bleproject.interfaces.ControllerViewInterface;
import inc.osips.bleproject.interfaces.WirelessDeviceConnector;
import inc.osips.bleproject.model.DeviceConnectionFactory;
import inc.osips.bleproject.model.ble_comms.services.BleGattService;
import inc.osips.bleproject.model.utilities.Constants;
import inc.osips.bleproject.model.utilities.GeneralUtil;
import inc.osips.bleproject.view.activities.DeviceScannerActivity;

public class RemoteControllerPresenter extends VoiceControlPresenter {

    private WirelessDeviceConnector deviceConnector;
    private Activity activity;
    private String deviceName;


    public RemoteControllerPresenter(final ControllerViewInterface viewInterface, String type,
                                     Parcelable device) {
        super(viewInterface);
        activity = viewInterface.getControlContext();
        DeviceConnectionFactory factory = new DeviceConnectionFactory(viewInterface);

        deviceConnector = factory.establishConnectionWithDeviceOf(type, device);

    }

    public void bindBleService(){
        Log.i(TAG, "starting service");
        Intent intent = new Intent(activity, BleGattService.class);
        if (deviceConnector !=null){
            activity.bindService(intent, deviceConnector.getConnection(), Context.BIND_AUTO_CREATE);
        }
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void unbindBleService(){
        if (deviceConnector!=null && deviceConnector.isConnected()) {
            activity.unbindService(deviceConnector.getConnection());
        }
    }


    public void registerBleMsgReceiver(){
        activity.registerReceiver(ctrlUpdateReceiver, commsUpdateIntentFilter());
        /*if (gattService != null) {
            final boolean result = gattService.connect(bleDevice);
            Log.i(TAG, "Connect request result=" + result);
        }*/
    }

    public void unregisterBleMsgReceiver(){
        activity.unregisterReceiver(ctrlUpdateReceiver);
        stopListening();
        GeneralUtil.transitionActivity(viewInterface.getControlContext(), DeviceScannerActivity.class);
    }

    private final BroadcastReceiver ctrlUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case BleGattService.ACTION_CONNECTED:
                    // No need to do anything here. Service discovery is started by the service.
                    break;
                case BleGattService.ACTION_DISCONNECTED:
                    Log.i(TAG, "Service Disconnected");
                    GeneralUtil.message("Device Disconnected");
                    //if (App.getCurrentActivity() instanceof ControllerActivity)
                    GeneralUtil.transitionActivity(activity, DeviceScannerActivity.class);
                    break;
                case BleGattService.ACTION_DATA_AVAILABLE:
                    // This is called after a Notify completes
                    break;
                case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                    break;
                case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                    break;
                case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                    break;
            }
        }

        @Override
        public IBinder peekService(Context myContext, Intent service) {
            return super.peekService(myContext, service);
        }
    };

    /**
     * This sets up the filter for broadcasts that we want to be notified of.
     * This needs to match the broadcast receiver cases.
     * @return intentFilter
     */
    private IntentFilter commsUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleGattService.ACTION_CONNECTED);
        intentFilter.addAction(BleGattService.ACTION_DISCONNECTED);
        intentFilter.addAction(BleGattService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        return intentFilter;
    }

    public void sendInstructionsToDevice(String instruct) {

    }
}
