package inc.osips.bleproject.presenter;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import inc.osips.bleproject.App;
import inc.osips.bleproject.interfaces.ControllerViewInterface;
import inc.osips.bleproject.model.ble_comms.services.BleGattService;
import inc.osips.bleproject.model.utilities.GeneralUtil;
import inc.osips.bleproject.view.activities.BLE_ScannerActivity;
import inc.osips.bleproject.view.activities.ControllerActivity;

public class RemoteControllerPresenter extends VoiceControlPresenter {

    private volatile BleGattService gattService;
    private BluetoothDevice device;
    private Activity activity;
    private boolean mBound = false;

    private ServiceConnection mConnection;


    public RemoteControllerPresenter(final ControllerViewInterface viewInterface, BluetoothDevice device) {
        super(viewInterface);
        this.device =  device;
        activity = viewInterface.getControlContext();

        /*
         * Defines callbacks for service binding, passed to bindService()
         */
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                Log.i(TAG, "binding services");
                BleGattService.BTLeServiceBinder binder = (BleGattService.BTLeServiceBinder) service;
                gattService = binder.getService();
                mBound = true;
                if (Build.VERSION.SDK_INT >= 21) {
                    ConnectToBleDevice();
                } else {
                    GeneralUtil.message("API too low for App!");
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                mBound = false;
                Log.i(TAG, "Service Disconnected");
                unregisterBleMsgReceiver();
                GeneralUtil.transitionActivity(viewInterface.getControlContext(), BLE_ScannerActivity.class);
            }
        };
    }

    public void bindBleService(){
        Log.i(TAG, "starting service");
        Intent intent = new Intent(activity, BleGattService.class);
        activity.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }


    public void unbindBleService(){
        if (mBound) {
            mBound = false;
            activity.unbindService(mConnection);
        }
    }

    public void sendInstructionsToDevice(String instuctions){
        gattService.writeLEDInstructions(instuctions);
    }

    public void registerBleMsgReceiver(){
        activity.registerReceiver(ctrlUpdateReceiver, commsUpdateIntentFilter());
        if (gattService != null) {
            final boolean result = gattService.connect(device);
            Log.i(TAG, "Connect request result=" + result);
        }
    }

    public void unregisterBleMsgReceiver(){
        activity.unregisterReceiver(ctrlUpdateReceiver);
        stopListening();
        GeneralUtil.transitionActivity(viewInterface.getControlContext(), BLE_ScannerActivity.class);
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
                    GeneralUtil.transitionActivity(activity, BLE_ScannerActivity.class);
                    break;
                case BleGattService.ACTION_DATA_AVAILABLE:
                    // This is called after a Notify completes
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
        return intentFilter;
    }

    //API 21 and Above
    private void ConnectToBleDevice() {
        if (makeConnectionBLE()) {
            return;
        } else {
            GeneralUtil.message("Cannot Connect to Device");
            GeneralUtil.transitionActivity(viewInterface.getControlContext(),
                    BLE_ScannerActivity.class);
        }
    }


    public boolean makeConnectionBLE() {
        if (!gattService.initialize()) {
            Log.e(TAG, "Unable to initialize Bluetooth");
            GeneralUtil.transitionActivity(viewInterface.getControlContext(), BLE_ScannerActivity.class);
        }
        return gattService.connect(device);
    }
}
