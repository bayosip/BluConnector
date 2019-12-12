package inc.osips.bleproject.model.remote_comms.wifi_comms.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import inc.osips.bleproject.utilities.GeneralUtil;

public class P2pDataTransferService extends Service {


    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel p2pChannel;
    private WifiP2pConfig config;
    private final Binder serviceBinder = new P2pServiceBinder();
    private Intent serviceIntent;
    private boolean isP2pConnected = false;
    private Thread serviceThread;
    private Socket socket = new Socket();
    private String hostAddress;
    private int TIME_OUT = 3000; //default time out for connection
    private int PORT = 8888; // default port number
    private static final int MESSAGE_READ=1;
    private SendReceive sendReceive;

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


    public boolean init(){
        p2pManager = (WifiP2pManager) getApplicationContext()
                .getSystemService(Context.WIFI_P2P_SERVICE);

        p2pChannel = p2pManager.initialize(getApplicationContext(), Looper.getMainLooper(), null);

        if (p2pManager!=null && p2pChannel!= null)return true;

        return false;
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

    public void writeLEDInstructions(String instruct) {
        sendReceive.write(instruct.getBytes());
    }

    private void disconnect() {

    }

    public void establishConnection(Intent intent){
        if (p2pManager !=null){
            NetworkInfo info = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if(info!=null && info.isConnected()){
                p2pManager.requestConnectionInfo(p2pChannel, new WifiP2pManager.ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
                        final InetAddress hostAddress = wifiP2pInfo.groupOwnerAddress;
                        if (wifiP2pInfo.groupFormed) {
                            serviceThread = new Thread(){
                                @Override
                                public void run() {
                                    try {
                                        socket.connect(new InetSocketAddress(hostAddress, PORT), TIME_OUT);
                                        sendReceive = new SendReceive(socket);
                                        sendReceive.start();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            serviceThread.start();
                        }
                    }
                });
            }else GeneralUtil.message("Wifi Device is Not Connected!");
        }
    }

    Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what)
            {
                case MESSAGE_READ:
                    byte[] readBuff= (byte[]) msg.obj;
                    String tempMsg=new String(readBuff,0,msg.arg1);
                    break;
            }
            return true;
        }
    });

    private class SendReceive extends Thread{
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public SendReceive(Socket skt)
        {
            socket=skt;
            try {
                inputStream=socket.getInputStream();
                outputStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer=new byte[1024];
            int bytes;

            while (socket!=null)
            {
                try {
                    bytes=inputStream.read(buffer);
                    if(bytes>0)
                    {
                        handler.obtainMessage(MESSAGE_READ,bytes,-1,buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes)
        {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public class P2pServiceBinder extends Binder{

        public P2pDataTransferService getService(){
            return P2pDataTransferService.this;
        }
    }


}
