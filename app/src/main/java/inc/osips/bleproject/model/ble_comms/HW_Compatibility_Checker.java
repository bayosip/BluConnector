package inc.osips.bleproject.model.ble_comms;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import inc.osips.bleproject.interfaces.PresenterInterface;

import static android.content.Context.SENSOR_SERVICE;


public class HW_Compatibility_Checker {

    private Context context;
    public static final int REQUEST_ENABLE_BT =1;

    /*public BluetoothCheck(Context context) {
        this.context = context;
    }*/
    public static boolean checkBluetooth(BluetoothAdapter bleAdapter) {

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (bleAdapter == null || !bleAdapter.isEnabled()) {
            return false;
        }
        else {
            return true;
        }
    }

    public static void requestUserBluetooth(PresenterInterface presenter) {

        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        presenter.getScanningAcativity().startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT );
    }


    public static boolean checkForAccelerometer (Context activity){
        SensorManager sm = (SensorManager)activity.getSystemService(SENSOR_SERVICE);
        if (sm.getSensorList(Sensor.TYPE_ACCELEROMETER).size()!=0){
            return true;
        }
        return false;
    }
}
