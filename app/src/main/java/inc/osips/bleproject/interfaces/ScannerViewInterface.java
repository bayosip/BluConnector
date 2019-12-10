package inc.osips.bleproject.interfaces;

import android.os.Bundle;
import android.os.Parcelable;

import java.util.List;

import inc.osips.bleproject.model.Devices;

public interface ScannerViewInterface extends AppActivity{

    void goToDeviceControlView(final Bundle data);
    void progressFromScan(final List<Devices> devices);
}
