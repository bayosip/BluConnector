package inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.p2p_comms;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import inc.osips.iot_wireless_communication.R;
import inc.osips.iot_wireless_communication.wireless_comms_module.interfaces.WirelessDeviceConnectionScanner;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.utility.Constants;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.utility.Util;

public class P2p_Scanner implements WirelessDeviceConnectionScanner, WifiP2pManager.ActionListener, WifiP2pManager
        .PeerListListener {

    private WifiManager wifiManager;
    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel p2pChannel;
    private Context context;
    private boolean scanState = false;
    private WifiP2pManager.PeerListListener mPeerListListener = this;//default peerListener

    private long SCAN_TIME = 9000; //default scan time

    private static final String TAG = "P2p_Scanner";


    public P2p_Scanner(@NonNull Context activity, @Nullable WifiP2pManager
            .PeerListListener mPeerListListener, long scanTime) {
        if (scanTime >= 1000) SCAN_TIME = scanTime;
        this.context = activity;

        if (mPeerListListener != null)
            this.mPeerListListener = mPeerListListener;

        initialisePrequisites();

        if (!wifiManager.isWifiEnabled()) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                askUserToTurnWifiOn29();
            else askUserToTurnWifiOn();
        }
    }

    private void initialisePrequisites() {
        if (wifiManager == null)
            wifiManager = (WifiManager) context.getApplicationContext().
                    getSystemService(Context.WIFI_SERVICE);

        p2pManager = (WifiP2pManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_P2P_SERVICE);


        p2pChannel = p2pManager.initialize(context, Looper.getMainLooper(), null);
    }


    private void askUserToTurnWifiOn() {
        final AlertDialog wifiAsk = new AlertDialog.Builder(context)
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
    private void askUserToTurnWifiOn29() {

        final AlertDialog wifiAsk = new AlertDialog.Builder(context)
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
        if (wifiManager.isWifiEnabled())
            scanForWifiP2pDevices();
    }

    private void scanForWifiP2pDevices() {
        if (!scanState) {

            Util.getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanStop();
                }
            }, SCAN_TIME);

            if (p2pManager != null && wifiManager.isWifiEnabled()) {
                if (ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Util.message(context, "Please allow permission to all scanning for devices");
                    return;
                }
                p2pManager.discoverPeers(p2pChannel, this);
                scanState = true;
            }
        } else {
            scanStop();
        }
    }

    private void scanStop() {
        Util.message(context, "Scanning Stopped!");

        if (scanState) {
            scanState = false;
            Log.w(TAG, "scanning stopped");
            if (p2pManager != null)
                p2pManager.stopPeerDiscovery(p2pChannel, this);
            context.sendBroadcast(new Intent(WirelessDeviceConnectionScanner.SCANNING_STOPPED));
        }
    }

    @Override
    public void showDiscoveredDevices() {
        scanState = false;
        if (p2pManager != null) {
            if (ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Util.message(context, "Requires FINE LOCATION PERMISSION");
                return;
            }
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
        Util.message(context, context.getString(R.string.p2p_discovery_started));
    }

    @Override
    public void onFailure(int i) {
        Log.e(TAG, "Wifi Peer Discovery Failed To Start!");
        Util.message(context, context.getString(R.string.p2p_discovery_failed));
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        for (WifiP2pDevice p2pDevice: wifiP2pDeviceList.getDeviceList()){
            context.sendBroadcast(new Intent(WirelessDeviceConnectionScanner.DEVICE_DISCOVERED)
                    .putExtra(Constants.DEVICE_DATA, p2pDevice));
        }
    }
}
