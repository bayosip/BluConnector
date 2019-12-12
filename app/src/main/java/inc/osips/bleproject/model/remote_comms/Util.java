package inc.osips.bleproject.model.remote_comms;

import android.content.Context;
import android.widget.Toast;

import inc.osips.bleproject.App;

public class Util {

    public static void message(Context context,String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
