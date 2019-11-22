package inc.osips.bleproject.model.wifi_comms;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class WifiService extends Service {

    private final IBinder wifiServiceBinder = new WifiServiceBiner();
    private Thread serviceThread;
    private Socket socket;
    private String hostAddress;
    private static final int TIME_OUT = 3000;
    private static final int PORT = 8888;


    public class WifiServiceBiner extends Binder{

        public WifiService getService(){
            return  WifiService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return wifiServiceBinder;
    }

    public boolean initialize(InetAddress hostAddress){
        socket = new Socket();
        this.hostAddress = hostAddress.getHostAddress();

        try {
            socket.connect(new InetSocketAddress(hostAddress, PORT), TIME_OUT);

        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
        return false;
    }

}
