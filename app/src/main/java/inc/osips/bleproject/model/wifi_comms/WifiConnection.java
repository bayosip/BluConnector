package inc.osips.bleproject.model.wifi_comms;

import android.app.Activity;
import android.content.Context;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;
import android.os.Parcelable;

import inc.osips.bleproject.interfaces.ControllerViewInterface;
import inc.osips.bleproject.interfaces.WirelessDeviceConnector;
import inc.osips.bleproject.model.utilities.GeneralUtil;

public class WifiConnection implements WirelessDeviceConnector {


    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel p2pChannel;
    private WifiP2pConfig config;
    private WifiP2pDevice p2pDevice;
    private Activity activity;
    private boolean isConnected = false;


    public WifiConnection(ControllerViewInterface viewInterface, final Parcelable device) {
        this.p2pDevice = (WifiP2pDevice) device;
        this.activity =viewInterface.getControlContext();
        initialisePrequisites();

        p2pManager.connect(p2pChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                GeneralUtil.message("Connected to " + p2pDevice.deviceName);
            }

            @Override
            public void onFailure(int i) {
                GeneralUtil.message("Failed To Connect...");
            }
        });
    }

    private void initialisePrequisites(){
        p2pManager = (WifiP2pManager) activity.getApplicationContext()
                .getSystemService(Context.WIFI_P2P_SERVICE);

        p2pChannel = p2pManager.initialize(activity, Looper.getMainLooper(), null);

        config = new WifiP2pConfig();
        config.deviceAddress = p2pDevice.deviceAddress;
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
}
