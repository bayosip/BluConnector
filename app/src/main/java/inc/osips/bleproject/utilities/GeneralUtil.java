package inc.osips.bleproject.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
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
    private static SharedPreferences appPref;

    private static final String APP_PREFS_NAME = "inc.osips.bleproject.app_pref";
    private static Handler uiHandler;
    private static SharedPreferences.Editor editor;

    static {
        uiHandler = new Handler(Looper.getMainLooper());
        appPref = App.context.getSharedPreferences(APP_PREFS_NAME,
                Context.MODE_PRIVATE);
        editor = appPref.edit();
    }


    public static void message(String message) {
        Toast.makeText(App.context, message, Toast.LENGTH_SHORT).show();
    }

    public static Handler getHandler() {
        return uiHandler;
    }

    public static SharedPreferences getAppPref() {
        if (appPref == null) appPref = App.context.getSharedPreferences(APP_PREFS_NAME,
                Context.MODE_PRIVATE);

        return appPref;
    }

    public static boolean checkIfPrefExists(String name){
        if (appPref!=null && appPref.contains(name)) return true;

        return false;
    }

    public static String getAppPrefStoredStringWithName(String name){
        if (checkIfPrefExists(name))
            return appPref.getString(name, "");
        return "";
    }

    public static SharedPreferences.Editor getEditor() {
        return editor;
    }


    /*This function takes a String array @parameter and checks
     *if it exist and if its not the same as previous saved,
     * removes previous ave on satisfied condition then saves new instruction
     * */
    public static void saveButtonConfig(String... data){
        if(appPref.contains(data[0]) && !appPref.getString(data[0],"").equalsIgnoreCase(data[1])){
            editor.remove(data[0]).commit();
        }
        if (!TextUtils.isEmpty(data[1]))
            editor.putString(data[0], data[1]).commit();
    }

    public static void exitApp(Activity activity) {
        if (Build.VERSION.SDK_INT >= 21) activity.finishAndRemoveTask();
        else activity.finish();
        Intent intent = new Intent(activity, AppFinish.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        activity.startActivity(intent);
    }

    public static Drawable setADrawable(Activity activity, int drawableID) {
        Drawable drawable;
        if (Build.VERSION.SDK_INT >= 21) {
            drawable = activity.getResources().getDrawable(drawableID,
                    activity.getApplicationContext().getTheme());
        } else {
            drawable = activity.getResources().getDrawable(drawableID);
        }
        return drawable;
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

    public static void expand(final View v) {
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? 550
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Expansion speed of 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Collapse speed of 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static class ResizeAnimation extends Animation {
        private View mView;
        private float mToHeight;
        private float mFromHeight;

        private float mToWidth;
        private float mFromWidth;

        public ResizeAnimation(View v, float fromWidth, float fromHeight, float toWidth, float toHeight) {
            mToHeight = toHeight;
            mToWidth = toWidth;
            mFromHeight = fromHeight;
            mFromWidth = fromWidth;
            mView = v;
            float targetH = Math.max(mFromHeight, mToHeight);
            setDuration((int)(targetH / v.getContext().getResources().getDisplayMetrics().density));
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            float height =
                    (mToHeight - mFromHeight) * interpolatedTime + mFromHeight;
            float width = (mToWidth - mFromWidth) * interpolatedTime + mFromWidth;
            ViewGroup.LayoutParams p = mView.getLayoutParams();
            p.height = (int) height;
            p.width = (int) width;
            mView.requestLayout();
        }
    }
}
