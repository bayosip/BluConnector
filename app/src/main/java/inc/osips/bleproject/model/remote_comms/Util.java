package inc.osips.bleproject.model.remote_comms;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import inc.osips.bleproject.App;

public class Util {
    private static Handler uiHandler;

    public static void message(Context context,String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    static {
        uiHandler = new Handler(Looper.getMainLooper());
    }


    public static Handler getHandler() {
        return uiHandler;
    }
}
