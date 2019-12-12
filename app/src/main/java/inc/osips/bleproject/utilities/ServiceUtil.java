package inc.osips.bleproject.utilities;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;

import android.net.wifi.p2p.WifiP2pManager;
import android.os.IBinder;
import android.util.Log;

import java.util.Iterator;
import java.util.List;

import inc.osips.bleproject.model.remote_comms.ble_comms.services.BleGattService;
import inc.osips.bleproject.model.remote_comms.wifi_comms.service.P2pDataTransferService;
import inc.osips.bleproject.view.fragments.home_fragments.DeviceScannerFragment;

import static android.content.Context.ACTIVITY_SERVICE;

public class ServiceUtil {


    private static Intent serviceIntent;
    private static BroadcastReceiver commsUpdateReceiver;

    public static Intent getServiceIntent() {
        return serviceIntent;
    }

    public static void startBLEService(final Context activity){
        serviceIntent = new Intent(activity, BleGattService.class);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            if(checkForIfDeviceIsFullyWake(activity))
                ContextCompat.startForegroundService(activity, serviceIntent);
            else {
                GeneralUtil.getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ContextCompat.startForegroundService(activity, serviceIntent);
                        }catch (Exception e){
                            Log.e(activity.getClass().getSimpleName(), "Service Error", e);
                            e.printStackTrace();
                            GeneralUtil.message( "Service Error, Please Restart App");
                        }
                    }
                }, 1500);
            }
        }else{
           activity.startService(serviceIntent);
        }
    }

    private static boolean checkForIfDeviceIsFullyWake(Context activity){
        ActivityManager activityManager = (ActivityManager) activity.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
        if (runningAppProcesses != null) {
            int importance = runningAppProcesses.get(0).importance;
            // higher importance has lower number (?)
            if (importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
               return true;
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    public static Boolean isServiceBLEAlreadyRunningAPI16(Context activity) {
        ActivityManager activityManager = (ActivityManager) activity.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : activityManager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (BleGattService.class.getName().equalsIgnoreCase(serviceInfo.service.getClassName()))
                return true;
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    public static Boolean isServiceWiFiAlreadyRunningAPI16(Context activity) {
        ActivityManager activityManager = (ActivityManager) activity.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : activityManager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (P2pDataTransferService.class.getName().equalsIgnoreCase(serviceInfo.service.getClassName()))
                return true;
        }
        return false;
    }

    public static void stopService(Context context){
        if (serviceIntent!=null) {
            context.stopService(serviceIntent);
            serviceIntent = null;
        }
    }

    public static boolean isAppRunning(Context context) {
        ActivityManager m = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfoList = m.getRunningTasks(10);
        Iterator<ActivityManager.RunningTaskInfo> itr = runningTaskInfoList.iterator();
        int n = 0;
        while (itr.hasNext()) {
            n++;
            itr.next();
        }
        if (n == 1) { // App is killed

            return false;
        }

        return true; // App is in background or foreground
    }

    public static BroadcastReceiver getCommsUpdateReceiver(final Activity activity){

        if(commsUpdateReceiver==null) {
            commsUpdateReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    final String action = intent.getAction();
                    switch (action) {
                        case BleGattService.ACTION_CONNECTED:
                            // No need to do anything here. Service discovery is started by the service.
                            break;
                        case BleGattService.ACTION_DISCONNECTED:
                            Log.i(activity.getClass().getSimpleName(), "Service Disconnected");
                            GeneralUtil.message("Device Disconnected");
                            //if (App.getCurrentActivity() instanceof ControllerActivity)
                            GeneralUtil.transitionActivity(activity, DeviceScannerFragment.class);
                            break;
                        case BleGattService.ACTION_DATA_AVAILABLE:
                            // This is called after a Notify completes
                            break;
                        case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                            break;
                        case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                            break;
                        case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                            break;
                        case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                            break;
                    }
                }

                @Override
                public IBinder peekService(Context myContext, Intent service) {
                    return super.peekService(myContext, service);
                }
            };
        }

        return commsUpdateReceiver;
    }
}
