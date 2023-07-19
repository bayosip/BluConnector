package inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.ble_comms;

import static android.content.Context.SENSOR_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import androidx.core.app.ActivityCompat;


public class HW_Compatibility_Checker {
    public static final int REQUEST_ENABLE_BT = 1;

    public static boolean checkBluetooth(BluetoothAdapter bleAdapter) {

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        return bleAdapter != null && bleAdapter.isEnabled();
    }

    public static void requestUserBluetooth(Activity activity) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public static boolean checkForAccelerometer (Context activity){
        SensorManager sm = (SensorManager)activity.getSystemService(SENSOR_SERVICE);
        return sm.getSensorList(Sensor.TYPE_ACCELEROMETER).size() != 0;
    }
}
