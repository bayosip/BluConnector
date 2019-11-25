package inc.osips.bleproject.model.wifi_comms;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcelable;

import inc.osips.bleproject.interfaces.ControllerViewInterface;
import inc.osips.bleproject.interfaces.WirelessDeviceConnector;
import inc.osips.bleproject.model.utilities.GeneralUtil;

public class WifiConnection implements WirelessDeviceConnector {


    private WifiP2pDevice p2pDevice;
    private Activity activity;
    private boolean isConnected = false;


    public WifiConnection(ControllerViewInterface viewInterface, final Parcelable p2pDevice) {
        this.p2pDevice = (WifiP2pDevice) p2pDevice;
        this.activity =viewInterface.getControlContext();
    }



    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public ServiceConnection getConnection() {
        return null;
    }

    @Override
    public void sendInstructionsToDevice(String instuctions) {

    }


    private ServiceConnection mConnection =
            /*
             * Defines callbacks for service binding, passed to bindService()
             */
            new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {

                }
            };
}
