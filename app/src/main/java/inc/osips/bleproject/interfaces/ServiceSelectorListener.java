package inc.osips.bleproject.interfaces;

import android.content.Context;

public interface ServiceSelectorListener {
    void selectAServiceWith(int pos);

    Context getListenerContext();
}
