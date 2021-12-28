package inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.p2p_comms;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.UUID;

import inc.osips.iot_wireless_communication.wireless_comms_module.interfaces.WirelessDeviceConnector;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.DeviceConnectionFactory;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.utility.Util;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.p2p_comms.service.P2pDataTransferService;

public class P2pConnection implements WirelessDeviceConnector {


    private final WifiP2pDevice p2pDevice;
    private final Context context;
    private P2pDataTransferService p2pService;
    private volatile boolean mBound = false;
    private static final String TAG = "WiFi Connectionm";
    private int PORT, TIME_OUT = 3000;


    public P2pConnection(@NonNull Context context, @NonNull final Parcelable p2pDevice, int time_out) {
        this.p2pDevice = (WifiP2pDevice) p2pDevice;
        this.context = context;
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
    public void increaseMessagingByteLimit(@NonNull String address, int size) {
    }

    @Override
    public void maxOutMessagingByteLimit(@NonNull String address) {
    }

    @Override
    public boolean isConnected() {
        return mBound;
    }

    @Override
    public ServiceConnection getServiceConnection() {
        return mConnection;
    }

    @Override
    public void enableNotificationsFor(String serviceUuid, String attrId, String descriptor, String deviceAddress) {
    }

    @Override
    public void disableNotificationsFor(String serviceUuid, String attrId, String descriptor, String deviceAddress) {
    }

    @Override
    public void disconnectDevice(@NonNull String address) {
    }

    @Override
    public void connectAnotherDeviceSimultaneously(@NonNull Parcelable device,
                                                   @Nullable String serviceUUID) throws Exception {
        throw new IllegalAccessException("P2p Does not allow multi-device connection yet!!!");
    }

    @Override
    public void selectServiceUsingUUID(@Nullable String deviceAddress, @NonNull String UUID) {
    }

    @Override
    public void sendInstructionsToRemoteDevice(@Nullable String deviceAddr, @NonNull String instructions) {
        p2pService.writeInstructions(instructions);
    }

    @Override
    public void sendInstructionsToRemoteDevice(@Nullable String deviceAddress,
                                               @Nullable UUID charxUuid, @NonNull String instructions) {
        p2pService.writeInstructions(instructions);
    }

    @Override
    public void sendInstructionsToConnectedDevice(String deviceAddr, @Nullable UUID charxUuid, byte[] data) {
        p2pService.writeInstructions(data);
    }

    @Override
    public void sendInstructionsToConnectedDevice(String deviceAddr, byte[] data) {
        p2pService.writeInstructions(data);
    }

    private final ServiceConnection mConnection =
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
                    context.sendBroadcast(new Intent(DeviceConnectionFactory.DEVICE_CONNECTION_SERVICE_STOPPED));
                }
            };

    private void connectToWifiDevice() {
        if (tryWiFiConnection()) {
            return;
        } else {
            Util.message(context,"Cannot Connect to Device");

            context.sendBroadcast(new Intent(DeviceConnectionFactory.FAILED_DEVICE_CONNECTION));
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
