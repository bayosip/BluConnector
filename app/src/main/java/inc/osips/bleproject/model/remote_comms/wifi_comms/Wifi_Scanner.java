package inc.osips.bleproject.model.remote_comms.wifi_comms;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;

import inc.osips.bleproject.R;
import inc.osips.bleproject.interfaces.WirelessConnectionScanner;
import inc.osips.bleproject.utilities.GeneralUtil;

public class Wifi_Scanner implements WirelessConnectionScanner, WifiP2pManager.ActionListener {

    private WifiManager wifiManager;
    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel p2pChannel;
    private Activity activity;
    private boolean scanState = false;
    private WifiP2pManager.PeerListListener mPeerListListener;


    public Wifi_Scanner (Activity activity, WifiP2pManager
            .PeerListListener mPeerListListener){
        this.activity = activity;
        this.mPeerListListener =mPeerListListener;

        initialisePrequisites();

        if(!wifiManager.isWifiEnabled())
            askUserToTurnWifiOn();
    }

    private void initialisePrequisites(){
        if (wifiManager == null)
            wifiManager = (WifiManager) activity.getApplicationContext().
                    getSystemService(Context.WIFI_SERVICE);

        p2pManager = (WifiP2pManager) activity.getApplicationContext()
                .getSystemService(Context.WIFI_P2P_SERVICE);


        p2pChannel = p2pManager.initialize(activity, Looper.getMainLooper(), null);
    }

    private void askUserToTurnWifiOn(){
        final AlertDialog wifiAsk = new AlertDialog.Builder(activity)
                .setCancelable(true)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.enable_wifi_title)
                .setMessage(R.string.enable_wifi_msg)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        wifiManager.setWifiEnabled(true);
                    }
                }).create();

        wifiAsk.show();
    }

    @Override
    public boolean isScanning() {
        return scanState;
    }

    @Override
    public void onStart() {
        if(p2pManager!=null && wifiManager.isWifiEnabled() && !scanState) {
            p2pManager.discoverPeers(p2pChannel, this);
            scanState = true;
        }

    }

    @Override
    public void showDiscoveredDevices() {
        scanState = false;
        if (p2pManager!=null){
            p2pManager.requestPeers(p2pChannel, mPeerListListener);
        }
    }

    @Override
    public void onStop() {
        p2pChannel.close();
    }

    @Override
    public void onSuccess() {
        GeneralUtil.message("Wifi Peer Discovery Started!");
    }

    @Override
    public void onFailure(int i) {
        GeneralUtil.message("Wifi Peer Discovery Failed To Start!");
    }

    public static class Builder{

        private Activity activity;
        private boolean scanState = false;
        private WifiP2pManager.PeerListListener mPeerListListener;
        public Builder() {
        }

    }
}
