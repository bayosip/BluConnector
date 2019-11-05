package inc.osips.bleproject.interfaces;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;

public interface PresenterInterface extends BluetoothAdapter.LeScanCallback {

    Activity getScanningAcativity();
    ScanCallback getScanCallBack();

}
