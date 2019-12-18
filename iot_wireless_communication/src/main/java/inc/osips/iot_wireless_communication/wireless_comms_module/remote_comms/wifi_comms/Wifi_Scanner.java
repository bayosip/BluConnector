package inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.wifi_comms;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import inc.osips.iot_wireless_communication.R;
import inc.osips.iot_wireless_communication.wireless_comms_module.interfaces.WirelessConnectionScanner;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.DeviceScannerFactory;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.utilities.Util;

public class Wifi_Scanner implements WirelessConnectionScanner, WifiP2pManager.ActionListener {

    private WifiManager wifiManager;
    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel p2pChannel;
    private Activity activity;
    private boolean scanState = false;
    private WifiP2pManager.PeerListListener mPeerListListener;

    private long SCAN_TIME = 6000; //default scan time

    private static final String TAG = "Wifi_Scanner";


    public Wifi_Scanner (Activity activity, WifiP2pManager
            .PeerListListener mPeerListListener, long scanTime){
        if (scanTime >=1000)SCAN_TIME = scanTime;
        this.activity = activity;
        this.mPeerListListener =mPeerListListener;

        initialisePrequisites();

        if(!wifiManager.isWifiEnabled()){
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                askUserToTurnWifiOn29();
            else askUserToTurnWifiOn();
        }
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

    @TargetApi(29)
    private void askUserToTurnWifiOn29(){

        final AlertDialog wifiAsk = new AlertDialog.Builder(activity)
                .setCancelable(true)
                .setTitle(R.string.enable_wifi_title)
                .setMessage(R.string.enable_wifi_msg)
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
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
        if(wifiManager.isWifiEnabled())
            scanForWifiDevices();
    }

    private void scanForWifiDevices(){
        if (!scanState){

            Util.getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanStop();
                }
            }, SCAN_TIME);

            if(p2pManager!=null && wifiManager.isWifiEnabled()) {
                p2pManager.discoverPeers(p2pChannel, this);
                scanState = true;
            }
        }
        else{
            scanStop();
        }
    }

    private void scanStop() {
        Util.message(activity,"Scanning Stopped!");

        if (scanState) {
            scanState = false;
            Log.w(TAG, "scanning stopped");
            if (p2pManager!=null)
                p2pManager.stopPeerDiscovery(p2pChannel, this);

            activity.sendBroadcast(new Intent(DeviceScannerFactory.SCANNING_STOPPED));
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
        scanStop();
    }

    @Override
    public void onSuccess() {
        Log.i(TAG, "Wifi Peer Discovery Started!");
        Util.message(activity,activity.getString(R.string.p2p_discovery_started));
    }

    @Override
    public void onFailure(int i) {
        Log.e(TAG, "Wifi Peer Discovery Failed To Start!");
        Util.message(activity, activity.getString(R.string.p2p_discovery_failed));
    }
}
