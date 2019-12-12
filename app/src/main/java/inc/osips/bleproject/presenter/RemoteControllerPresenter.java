package inc.osips.bleproject.presenter;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

import inc.osips.bleproject.interfaces.ControllerViewInterface;
import inc.osips.bleproject.interfaces.WirelessDeviceConnector;
import inc.osips.bleproject.model.remote_comms.DeviceConnectionFactory;
import inc.osips.bleproject.model.remote_comms.ble_comms.services.BleGattService;
import inc.osips.bleproject.utilities.Constants;
import inc.osips.bleproject.utilities.GeneralUtil;
import inc.osips.bleproject.utilities.ServiceUtil;
import inc.osips.bleproject.view.activities.Home;

public class RemoteControllerPresenter extends VoiceControlPresenter {

    private WirelessDeviceConnector deviceConnector;
    private Activity activity;
    private String deviceName;
    private DeviceConnectionFactory.Builder builder;

    public RemoteControllerPresenter(final ControllerViewInterface viewInterface, String type,
                                     Parcelable device) {
        super(viewInterface);
        activity = viewInterface.getControlContext();
        DeviceConnectionFactory factory = new DeviceConnectionFactory(viewInterface.getControlContext());

         builder = factory.establishConnectionWithDeviceOf(type, device);

        if (device instanceof BluetoothDevice && type.equalsIgnoreCase(Constants.BLE)){
            Log.i("Connection type", builder.getDeviceType());
            this.deviceName = ((BluetoothDevice) device).getName();
            viewInterface.getUUIDFromPopUp();
        }else if (device instanceof WifiP2pDevice && type.equalsIgnoreCase(Constants.WIFI)) {
            Log.i("Connection", builder.getDeviceType());
            if (builder !=null){
                deviceConnector = builder.build();
                if(!ServiceUtil.isServiceBLEAlreadyRunningAPI16(viewInterface.getControlContext()))
                    bindBleService();
            }
            this.deviceName = ((WifiP2pDevice) device).deviceName;
        }
    }

    public void setBaseUuidOfBLEDeviceAndConnect(String uuid){
        if (builder !=null){//"6e400001-b5a3-f393-e0a9-e50e24dcca9e"
            String str = uuid.toLowerCase();
            deviceConnector = builder.setDeviceUniqueID(str).build();
            if(!ServiceUtil.isServiceBLEAlreadyRunningAPI16(viewInterface.getControlContext()))
                bindBleService();
        }
    }

    private void bindBleService(){
        Log.i(TAG, "starting service");
        Intent intent = new Intent(activity, BleGattService.class);
        if (deviceConnector !=null && !ServiceUtil.isServiceBLEAlreadyRunningAPI16(activity)){
            activity.bindService(intent, deviceConnector.getServiceConnection(), Context.BIND_AUTO_CREATE);
            registerRemoteMsgReceiver();
        }
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void unbindBleService(){
        if (deviceConnector!=null && deviceConnector.isConnected() &&
                ServiceUtil.isServiceBLEAlreadyRunningAPI16(viewInterface.getControlContext())) {
            activity.unbindService(deviceConnector.getServiceConnection());
            deviceConnector = null;
        }
    }


    public void registerRemoteMsgReceiver(){
        activity.registerReceiver(ctrlUpdateReceiver, commsUpdateIntentFilter());
    }

    public void unregisterBleMsgReceiver(){
        activity.unregisterReceiver(ctrlUpdateReceiver);
        stopListening();
    }

    private final BroadcastReceiver ctrlUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Action Received");
            final String action = intent.getAction();
            Log.w(TAG, action);
            switch (action) {
                case BleGattService.ACTION_CONNECTED:
                    // No need to do anything here. Service discovery is started by the service.
                    Log.i(TAG, "Connected to GATT server.");
                    break;
                case BleGattService.ACTION_DISCONNECTED:
                    Log.i(TAG, "Service Disconnected");
                    GeneralUtil.message("Device Disconnected");
                    //if (App.getCurrentActivity() instanceof ControllerActivity)
                    GeneralUtil.transitionActivity(activity, Home.class);
                    break;
                case BleGattService.ACTION_BLE_SERVICES_DISCOVERED:
                    Log.w("BLE", "services discovered");
                    viewInterface.removeUUIDPopUp();
                    break;
                case BleGattService.ACTION_DATA_AVAILABLE:
                    // This is called after a Notify completes
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
                            GeneralUtil.transitionActivity(activity, Home.class);
                            break;
                    }
                    break;
                case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                    if (deviceConnector!= null)
                        deviceConnector.connectToDeviceWithDeviceInfoFrom(intent);
                    break;

                case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                    break;
                case DeviceConnectionFactory.DEVICE_CONNECTION_SERVICE_STOPPED:
                case DeviceConnectionFactory.FAILED_DEVICE_CONNECTION:
                    GeneralUtil.transitionActivity(activity, Home.class);
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
        intentFilter.addAction(BleGattService.ACTION_BLE_SERVICES_DISCOVERED);
        intentFilter.addAction(BleGattService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        return intentFilter;
    }

    public void sendInstructionsToDevice(String instruct) {
        deviceConnector.sendInstructionsToDevice(instruct);
    }
}
