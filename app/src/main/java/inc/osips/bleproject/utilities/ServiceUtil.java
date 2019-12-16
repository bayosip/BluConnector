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
    private static Boolean isServiceBLEAlreadyRunningAPI16(Context activity) {
        ActivityManager activityManager = (ActivityManager) activity.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : activityManager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (BleGattService.class.getName().equalsIgnoreCase(serviceInfo.service.getClassName()))
                return true;
        }
        return false;
    }

    public static Boolean isAnyRemoteConnectionServiceRunningAPI16(Context context){
        if (isServiceWiFiAlreadyRunningAPI16(context) || isServiceBLEAlreadyRunningAPI16(context))
            return true;
        else return false;
    }

    @SuppressWarnings("deprecation")
    private static Boolean isServiceWiFiAlreadyRunningAPI16(Context activity) {
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
}
