package inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.wlan_comms;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;

import inc.osips.iot_wireless_communication.R;
import inc.osips.iot_wireless_communication.wireless_comms_module.interfaces.WirelessDeviceConnectionScanner;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.utility.Constants;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.utility.Util;

public class WLANServiceScanner implements WirelessDeviceConnectionScanner, NsdManager.DiscoveryListener {


    private WifiManager wifiManager;
    private NsdManager nsdManager;
    private boolean scanState = false;
    private Context context;
    private long SCAN_TIME = 6000; //default scan time
    private NsdManager.DiscoveryListener listener = this;
    private List<NsdServiceInfo> discoveredServices = new LinkedList<>();

    private static final String TAG = "WLANServiceScanner";

    public WLANServiceScanner(@NonNull Context context, long scanTime,
                              @Nullable NsdManager.DiscoveryListener listener, @NonNull String serviceType) {
        this.context = context;
        if (scanTime >=1000)SCAN_TIME = scanTime;
        this.context = context;

        if (listener != null)
            this.listener = listener;

        initialisePrequisites();

        if(!wifiManager.isWifiEnabled()){
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                askUserToTurnWifiOn29();
            else askUserToTurnWifiOn();
        }
    }

    private void initialisePrequisites() {
        if (wifiManager == null)
            wifiManager = (WifiManager) context.getApplicationContext().
                    getSystemService(Context.WIFI_SERVICE);

        nsdManager = (NsdManager) context.getApplicationContext().
                getSystemService(Context.NSD_SERVICE);

    }

    private void askUserToTurnWifiOn(){
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
    private void askUserToTurnWifiOn29(){

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

    private void scanStop() {
        Util.message(context, "Scanning Stopped!");

        if (scanState) {
            nsdManager.stopServiceDiscovery(this);
            scanState = false;
            context.sendBroadcast(new Intent(WirelessDeviceConnectionScanner.SCANNING_STOPPED));
        }
    }

    private void scanForWLANServices(){
    }

    @Override
    public boolean isScanning() {
        return scanState;
    }


    @Override
    public void onStart(@Nullable String deviceName) {
//        if(wifiManager.isWifiEnabled()){
//
//        }
    }



    @Override
    public void onStop() {
       scanStop();
    }

    @Override
    public void showDiscoveredDevices() {
        scanState = false;
        for(NsdServiceInfo service: discoveredServices){
            context.sendBroadcast(new Intent(WirelessDeviceConnectionScanner.DEVICE_DISCOVERED)
                    .putExtra(Constants.DEVICE_DATA, service));
        }
    }

    @Override
    public void onDiscoveryStarted(String regType) {
        Log.d(TAG, "Service discovery started");
    }

    @Override
    public void onServiceFound(NsdServiceInfo service) {
        // A service was found! Do something with it.
        Log.d(TAG, "Service discovery success" + service);
        discoveredServices.add(service);
        context.sendBroadcast(new Intent(WirelessDeviceConnectionScanner.DEVICE_DISCOVERED)
                .putExtra(Constants.DEVICE_DATA, service));
        //nsdManager.resolveService(service, resolveListener);
    }

    @Override
    public void onServiceLost(NsdServiceInfo service) {
        // When the network service is no longer available.
        // Internal bookkeeping code goes here.
        Log.e(TAG, "service lost: " + service);
    }

    @Override
    public void onDiscoveryStopped(String serviceType) {
        Log.i(TAG, "Discovery stopped: " + serviceType);
    }

    @Override
    public void onStartDiscoveryFailed(String serviceType, int errorCode) {
        Log.e(TAG, "Discovery failed: Error code:" + errorCode);
        nsdManager.stopServiceDiscovery(this);
    }

    @Override
    public void onStopDiscoveryFailed(String serviceType, int errorCode) {
        Log.e(TAG, "Discovery failed: Error code:" + errorCode);
        nsdManager.stopServiceDiscovery(this);
    }
}
