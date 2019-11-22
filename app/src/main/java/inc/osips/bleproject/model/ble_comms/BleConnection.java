package inc.osips.bleproject.model.ble_comms;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

import inc.osips.bleproject.interfaces.ControllerViewInterface;
import inc.osips.bleproject.interfaces.WirelessDeviceConnector;
import inc.osips.bleproject.model.ble_comms.services.BleGattService;
import inc.osips.bleproject.model.utilities.GeneralUtil;
import inc.osips.bleproject.view.activities.DeviceScannerActivity;

public class BleConnection implements WirelessDeviceConnector {

    private volatile BleGattService gattService;
    private BluetoothDevice bleDevice;
    private Activity activity;

    private boolean mBound = false;
    private ServiceConnection mConnection;

    public BleConnection(ControllerViewInterface viewInterface, Parcelable device) {
        this.bleDevice = (BluetoothDevice) device;
        activity = viewInterface.getControlContext();

        /*
         * Defines callbacks for service binding, passed to bindService()
         */
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                //Log.i(TAG, "binding services");
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
                //Log.i(TAG, "Service Disconnected");
                GeneralUtil.transitionActivity(activity, DeviceScannerActivity.class);
            }
        };
    }

    @Override
    public ServiceConnection getConnection() {
        return mConnection;
    }

    @Override
    public boolean isConnected() {
        return mBound;
    }

    //API 21 and Above
    private void ConnectToBleDevice() {
        if (makeConnectionBLE()) {
            return;
        } else {
            GeneralUtil.message("Cannot Connect to Device");
            GeneralUtil.transitionActivity(activity,
                    DeviceScannerActivity.class);
        }
    }


    private boolean makeConnectionBLE() {
        if (!gattService.initialize()) {
            //Log.e(TAG, "Unable to initialize Bluetooth");
            GeneralUtil.transitionActivity(activity, DeviceScannerActivity.class);
        }
        if (gattService != null) {
            final boolean result = gattService.connect(bleDevice);
            return result;
        }
        else return false;
    }

    @Override
    public void sendInstructionsToDevice(String instuctions) {
        gattService.writeLEDInstructions(instuctions);
    }
}
