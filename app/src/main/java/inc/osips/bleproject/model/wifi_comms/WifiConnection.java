package inc.osips.bleproject.model.wifi_comms;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.IBinder;
import android.os.Parcelable;

import inc.osips.bleproject.interfaces.ControllerViewInterface;
import inc.osips.bleproject.interfaces.WirelessDeviceConnector;
import inc.osips.bleproject.model.utilities.GeneralUtil;
import inc.osips.bleproject.model.wifi_comms.service.P2pDataTransferService;
import inc.osips.bleproject.view.activities.DeviceScannerActivity;

public class WifiConnection implements WirelessDeviceConnector {


    private WifiP2pDevice p2pDevice;
    private Activity activity;
    private P2pDataTransferService p2pService;
    private boolean isConnected = false, mBound = false;


    public WifiConnection(ControllerViewInterface viewInterface, final Parcelable p2pDevice) {
        this.p2pDevice = (WifiP2pDevice) p2pDevice;
        this.activity =viewInterface.getControlContext();
    }



    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public ServiceConnection getServiceConnection() {
        return mConnection;
    }

    @Override
    public void sendInstructionsToDevice(String instuctions) {
        p2pService.writeLEDInstructions(instuctions);
    }


    private ServiceConnection mConnection =
            /*
             * Defines callbacks for p2pService binding, passed to bindService()
             */
            new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    P2pDataTransferService.P2pServiceBinder binder = (P2pDataTransferService.P2pServiceBinder)iBinder;
                    p2pService = binder.getService();
                    mBound = true;
                    connectToWifiDevice();
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    mBound = false;
                    GeneralUtil.transitionActivity(activity, DeviceScannerActivity.class);
                }
            };

    private void connectToWifiDevice() {
        if (makeConnectionWifi()) {
            return;
        } else {
            GeneralUtil.message("Cannot Connect to Device");
            GeneralUtil.transitionActivity(activity,
                    DeviceScannerActivity.class);
        }
    }


    private boolean makeConnectionWifi() {
        if (p2pService != null && !p2pService.init())  {
            final boolean result = p2pService.connect(p2pDevice);
            return result;
        }
        else return false;
    }

    @Override
    public void connectToDeviceWithDeviceInfoFrom(Intent intent) {
        p2pService.establishConnection(intent);
    }
}
