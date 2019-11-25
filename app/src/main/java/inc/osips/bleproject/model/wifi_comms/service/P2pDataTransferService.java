package inc.osips.bleproject.model.wifi_comms.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;

import java.net.InetAddress;

import inc.osips.bleproject.model.utilities.GeneralUtil;

public class P2pDataTransferService extends Service {


    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel p2pChannel;
    private WifiP2pConfig config;
    private final Binder serviceBinder = new P2pServiceBinder();
    private Intent serviceIntent;
    private boolean isP2pConnected = false;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        serviceIntent = intent;
        return serviceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        disconnect();
        return super.onUnbind(intent);
    }


    public void init(){
        p2pManager = (WifiP2pManager) getApplicationContext()
                .getSystemService(Context.WIFI_P2P_SERVICE);

        p2pChannel = p2pManager.initialize(getApplicationContext(), Looper.getMainLooper(), null);
    }

    public boolean connect(final WifiP2pDevice p2pDevice){
        config = new WifiP2pConfig();
        config.deviceAddress = p2pDevice.deviceAddress;


        p2pManager.connect(p2pChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                GeneralUtil.message("Connected to " + p2pDevice.deviceName);
                isP2pConnected = true;
            }

            @Override
            public void onFailure(int i) {
                GeneralUtil.message("Failed To Connect...");
                onUnbind(serviceIntent);
            }
        });
        return  isP2pConnected;
    }

    private void disconnect() {

    }

    private void establishConnection(Intent intent){
        if (p2pManager !=null){
            NetworkInfo info = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if(info.isConnected()){
                p2pManager.requestConnectionInfo(p2pChannel, new WifiP2pManager.ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
                        final InetAddress hostAddress = wifiP2pInfo.groupOwnerAddress;
                    }
                });
            }
        }
    }


    public class P2pServiceBinder extends Binder{

        public P2pDataTransferService getService(){
            return P2pDataTransferService.this;
        }
    }
}
