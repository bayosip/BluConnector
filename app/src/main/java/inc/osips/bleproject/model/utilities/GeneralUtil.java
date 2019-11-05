package inc.osips.bleproject.model.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.net.InetAddress;

import inc.osips.bleproject.App;
import inc.osips.bleproject.interfaces.SpeechInitCallBack;
import inc.osips.bleproject.view.activities.AppFinish;
import inc.osips.bleproject.view.activities.ControllerActivity;

/**
 * Created by BABY v2.0 on 10/11/2016.
 */

public class GeneralUtil {
    private Context context;
    private Toast toast;

    private static Handler uiHandler;

    static {
        uiHandler = new Handler(Looper.getMainLooper());
    }


    public static void message(String message) {
        Toast.makeText(App.context, message, Toast.LENGTH_SHORT).show();
    }

    public static Handler getHandler() {
        return uiHandler;
    }


    public static void exitApp(Activity activity) {
        if (Build.VERSION.SDK_INT >= 21) activity.finishAndRemoveTask();
        else activity.finish();
        Intent intent = new Intent(activity, AppFinish.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        activity.startActivity(intent);
    }

    public static void transitionActivity(Activity oldActivity, Class newActivity) {
        Activity activity = oldActivity;
        if (Build.VERSION.SDK_INT >= 21) activity.finishAndRemoveTask();
        else activity.finish();

        //oldActivity.overridePendingTransition(R.anim.from_middle, R.anim.to_middle);
        oldActivity.startActivity(new Intent(oldActivity, newActivity));
    }

    public static void transitionActivity(Activity oldActivity, Intent intent) {
        if (Build.VERSION.SDK_INT >= 21) oldActivity.finishAndRemoveTask();
        else oldActivity.finish();
        oldActivity.startActivity(intent);
    }

    public static boolean isNetworkConnected(Context activity) {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork !=null && (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI||
                activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)) {
            return activeNetwork.isConnected();
        }
        return false;
    }

    public static void isInternetAvailable(final Context activity, final SpeechInitCallBack callBack) {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (isNetworkConnected(activity))
                    try {
                        InetAddress ipAddr;
                        ipAddr = InetAddress.getByName("google.com");
                        Log.i(ControllerActivity.class.getSimpleName(), ipAddr.toString());
                        callBack.initSpeechControlFeature(!ipAddr.equals(""));

                    } catch (Exception e) {
                        e.printStackTrace();
                        callBack.initSpeechControlFeature(false);
                    }
            }
        });

    }
}
