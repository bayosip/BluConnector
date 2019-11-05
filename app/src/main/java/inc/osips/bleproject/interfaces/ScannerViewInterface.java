package inc.osips.bleproject.interfaces;

import android.os.Parcelable;

public interface ScannerViewInterface extends AppActivity{

    void launchRingDialog();
    void progressFromScan(final Parcelable result);
}
