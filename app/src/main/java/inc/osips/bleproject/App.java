package inc.osips.bleproject;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import inc.osips.bleproject.model.utilities.ServiceUtil;
import inc.osips.bleproject.view.activities.ErrorActivity;

public class App extends Application {

    public static Context context;
    private static final String TAG = "BLE App";

    @Override
    public void onCreate()
    {
        super.onCreate();
        context = getApplicationContext();

        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
                .trackActivities(true) //default: false
                .minTimeBetweenCrashesMs(2000) //default: 3000
                .logErrorOnRestart(true)
                .errorActivity(ErrorActivity.class) //default: null (default error activity)
                .apply();
    }

    private static Activity mCurrentActivity = null;
    public static Activity getCurrentActivity(){
        return mCurrentActivity;
    }

    public static void setCurrentActivity(Activity currentActivity){
        mCurrentActivity = currentActivity;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }





    @Override
    public void onTerminate() {
        if(ServiceUtil.isServiceBLEAlreadyRunningAPI16(context)){
            ServiceUtil.stopService(this);
        }
        super.onTerminate();
    }

}
