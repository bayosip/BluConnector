package inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.p2p_comms;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.ServerSocket;

import inc.osips.iot_wireless_communication.wireless_comms_module.interfaces.WirelessDeviceConnector;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.DeviceConnectionFactory;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.utility.Util;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.p2p_comms.service.P2pDataTransferService;

public class P2pConnection implements WirelessDeviceConnector {


    private WifiP2pDevice p2pDevice;
    private Activity activity;
    private P2pDataTransferService p2pService;
    private boolean isConnected = false, mBound = false;
    private static final String TAG = "WiFi Connectionm";
    private int PORT, TIME_OUT = 3000;


    public P2pConnection(@NonNull Activity activity, @NonNull final Parcelable p2pDevice, int time_out) {
        this.p2pDevice = (WifiP2pDevice) p2pDevice;
        this.activity = activity;

        if (time_out>1000)
            this.TIME_OUT = time_out;
        initializeServerSocket();
    }

    public void initializeServerSocket() {
        // Initialize a server socket on the next available port.
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "initializeServerSocket: ", e );
        }

        // Store the chosen port.
        if(serverSocket!=null)
            PORT = serverSocket.getLocalPort();
        else PORT = 8888;

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
    public void selectServiceUsingUUID(@NonNull String UUID) {
    }

    @Override
    public void sendInstructionsToRemoteDevice(String instuctions) {
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
                    activity.sendBroadcast(new Intent(DeviceConnectionFactory.DEVICE_CONNECTION_SERVICE_STOPPED));
                }
            };

    private void connectToWifiDevice() {
        if (tryWiFiConnection()) {
            return;
        } else {
            Util.message(activity,"Cannot Connect to Device");

            activity.sendBroadcast(new Intent(DeviceConnectionFactory.FAILED_DEVICE_CONNECTION));
        }
    }


    private boolean tryWiFiConnection() {
        if (p2pService != null && !p2pService.init())  {
            final boolean result = p2pService.connect(p2pDevice);
            return result;
        }
        else return false;
    }

    @Override
    public void connectToDeviceWithDeviceInfoFrom(@NonNull Intent intent) {
        if (p2pService!= null)
            p2pService.establishConnection(intent, PORT, TIME_OUT);
    }
}
