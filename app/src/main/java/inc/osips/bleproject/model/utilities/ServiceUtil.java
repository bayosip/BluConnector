package inc.osips.bleproject.model.utilities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;
import android.util.Log;

import java.util.Iterator;
import java.util.List;

import inc.osips.bleproject.model.ble_comms.services.BleGattService;

import static android.content.Context.ACTIVITY_SERVICE;

public class ServiceUtil {


    private static Intent serviceIntent;

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
    public static Boolean isServiceAlreadyRunningAPI16(Context activity) {
        ActivityManager activityManager = (ActivityManager) activity.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : activityManager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (BleGattService.class.getName().equalsIgnoreCase(serviceInfo.service.getClassName()))
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
