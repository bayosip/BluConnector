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
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import inc.osips.bleproject.interfaces.ControllerViewInterface;
import inc.osips.iot_wireless_communication.wireless_comms_module.interfaces.WirelessDeviceConnector;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.DeviceConnectionFactory;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.Devices;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.ble_comms.services.BleGattService;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.p2p_comms.service.P2pDataTransferService;
import inc.osips.bleproject.utilities.Constants;
import inc.osips.bleproject.utilities.GeneralUtil;
import inc.osips.bleproject.utilities.ServiceUtil;
import inc.osips.bleproject.view.activities.Home;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.utility.IoTCommException;

public class RemoteControllerPresenter extends VoiceControlPresenter {

    private WirelessDeviceConnector deviceConnector;
    private Activity activity;
    private String deviceName;
    private DeviceConnectionFactory.Builder builder;
    private Intent intent;
    private String deviceAddr =null;
    private final List<String> listOfRemoteServices = new ArrayList<>();
    private Map<Integer, Boolean> serviceMap = new HashMap<>();
    private String type ="";
    private int count = 1;
    private final List<Parcelable> listDevice= new ArrayList<>();

    public List<String> getListOfRemoteServices() {
        return listOfRemoteServices;
    }

    public RemoteControllerPresenter(final ControllerViewInterface viewInterface, String type,
                                     List<Parcelable> devices) {
        super(viewInterface);
        this.type = type;
        activity = viewInterface.getControlContext();
        this.listDevice.clear();
        listDevice.addAll(devices);
        DeviceConnectionFactory factory = DeviceConnectionFactory.withContext(viewInterface.getControlContext());
        Parcelable device = devices.get(0);
        try {
            builder = factory.getDeviceConnectionBuilder(type, device);
            if (device instanceof BluetoothDevice && type.equalsIgnoreCase(Constants.BLE)){
                Log.i("Connection type", builder.getDeviceType());
                //this.deviceAddr = ((BluetoothDevice) device).getAddress();
                this.deviceName = ((BluetoothDevice) device).getName();
                serviceMap.put(count, false);
                //viewInterface.getUUIDFromPopUp();
                intent = new Intent(activity, BleGattService.class);
                deviceConnector = builder.build();
                if (!ServiceUtil.isAnyRemoteConnectionServiceRunningAPI16(viewInterface.getControlContext()))
                    bindBleService();
            }else if (device instanceof WifiP2pDevice && type.equalsIgnoreCase(Constants.P2P)) {
                Log.i("Connection", builder.getDeviceType());
                if (builder !=null){
                    deviceConnector = builder.build();

                    intent = new Intent(activity, P2pDataTransferService.class);
                    if(!ServiceUtil.isAnyRemoteConnectionServiceRunningAPI16(viewInterface.getControlContext()))
                        bindBleService();
                }
                this.deviceName = ((WifiP2pDevice) device).deviceName;
            }
        } catch (IoTCommException e) {
            Log.e(TAG, "RemoteControllerPresenter: ",e );
            e.printStackTrace();
        }
    }

    private void connectToAnotherDevice(Parcelable device){
        if (device instanceof BluetoothDevice && type.equalsIgnoreCase(Constants.BLE)){
            try {
                deviceConnector.connectAnotherDeviceSimultaneously(device, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setEditTextIfStringAvailable(EditText txt, String str){
        if(!TextUtils.isEmpty(str)){
            txt.setText(str);
        }
    }

    public void setASharedPrefFromButtonConfig(String name, EditText txt){
        GeneralUtil.saveButtonConfig(name, txt.getText().toString());
    }

    public void setBaseUuidOfBLEDeviceAndConnect(String uuid){
        if (deviceConnector !=null){//"6e400001-b5a3-f393-e0a9-e50e24dcca9e"
            String str = uuid.toLowerCase();
            if (!TextUtils.isEmpty(str)) {
                serviceMap.put(count, true);
                int index = count-1;
                BluetoothDevice device = (BluetoothDevice)listDevice.get(index);
                deviceConnector.selectServiceUsingUUID(device.getAddress(), str);
                if (type.equalsIgnoreCase(Constants.BLE) && count< listDevice.size()){
                    connectToAnotherDevice(listDevice.get(count));
                    count++;
                    serviceMap.put(count, false);
                }
            }else {
                GeneralUtil.message("Please Select A Valid BaseUUID");
            }
        }
    }

    private void bindBleService(){
        Log.i(TAG, "starting service");
        if (deviceConnector !=null && !ServiceUtil.isAnyRemoteConnectionServiceRunningAPI16(activity)){
            activity.bindService(intent, deviceConnector.getServiceConnection(), Context.BIND_AUTO_CREATE);
            registerRemoteMsgReceiver();
        }
    }

    public String getDeviceName() {
        StringBuilder names = new StringBuilder();
        if (type.equalsIgnoreCase(Constants.BLE)){
            for (Parcelable p: listDevice){
                BluetoothDevice device = (BluetoothDevice)p;
                names.append(device.getName());
                names.append(", ");
            }
        }else return deviceName;
        return names.toString();
    }

    public void unbindBleService(){
        if (deviceConnector!=null && deviceConnector.isConnected() &&
                ServiceUtil.isAnyRemoteConnectionServiceRunningAPI16(viewInterface.getControlContext())) {
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
                case Constants.ACTION_CONNECTED:
                    // No need to do anything here. Service discovery is started by the service.
                    Log.i(TAG, "Connected to GATT server.");
                    break;
                case Constants.ACTION_DISCONNECTED:
                    Log.i(TAG, "Service Disconnected");
                    GeneralUtil.message("Device Disconnected");
                    //if (App.getCurrentActivity() instanceof ControllerActivity)
                    GeneralUtil.transitionActivity(activity, Home.class);
                    break;
                case Constants.ACTION_BLE_SERVICES_DISCOVERED:
                    Log.w("BLE", "services discovered");
                    if (!serviceMap.get(count)) {
                        listOfRemoteServices.clear();
                        listOfRemoteServices.addAll(intent.getStringArrayListExtra(Constants.SERVICE_UUID));
                        viewInterface.getUUIDFromPopUp(listOfRemoteServices);
                        Log.w("BLE", "All services found : " + listOfRemoteServices.get(0));
                    }
                    break;
                case Constants.ACTION_DATA_AVAILABLE:
                    Log.w("BLE DATA", "" + intent.getStringExtra(Constants.EXTRA_DATA));
                    break;
                case P2pDataTransferService.ACTION_DATA_AVAILABLE:
                    Log.w("DATA", intent.getStringExtra(P2pDataTransferService.EXTRA_DATA));
                    // This is called after a Notify completes
                    GeneralUtil.message(intent.getStringExtra(P2pDataTransferService.EXTRA_DATA));
                    break;
                case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                    if(type.equalsIgnoreCase(Constants.P2P)) {
                        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                        switch (state) {
                            case WifiP2pManager.WIFI_P2P_STATE_ENABLED:
                                GeneralUtil.message("Wifi is Ok");
                                break;
                            case WifiP2pManager.WIFI_P2P_STATE_DISABLED:
                                GeneralUtil.message("Wifi has been turned off, " +
                                        "Please turn on to use this feature");
                                GeneralUtil.transitionActivity(activity, Home.class);
                                break;
                        }
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
        intentFilter.addAction(Constants.ACTION_CONNECTED);
        intentFilter.addAction(Constants.ACTION_DISCONNECTED);
        intentFilter.addAction(Constants.ACTION_BLE_SERVICES_DISCOVERED);
        intentFilter.addAction(Constants.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        return intentFilter;
    }

    private void sendInstructionsToDevice(String instruct) {
        deviceConnector.sendInstructionsToRemoteDevice(deviceAddr,null, instruct);
    }

    public void setDeviceAddressAndSendInstructions(String uuidAddr, String instructions) {
        if (!TextUtils.isEmpty(uuidAddr))
            this.deviceAddr = uuidAddr;
        sendInstructionsToDevice(instructions);
    }
}
