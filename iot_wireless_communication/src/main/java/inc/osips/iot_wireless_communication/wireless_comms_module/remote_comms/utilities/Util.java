package inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.utilities;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

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
