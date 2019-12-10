package inc.osips.bleproject.interfaces;

import android.app.Activity;
import android.content.Context;

public interface ControllerViewInterface {

    Activity getControlContext();
    void processInstructions (final String commands);
    void getUUIDFromPopUp();
    void removeUUIDPopUp();

}
