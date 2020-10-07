package inc.osips.bleproject.interfaces;

import android.app.Activity;

import java.util.List;

public interface ControllerViewInterface extends ServiceSelectorListener {

    Activity getControlContext();
    void processInstructions (final String commands);
    void getUUIDFromPopUp(List<String> listUUID);
    void removeUUIDPopUp();

}
