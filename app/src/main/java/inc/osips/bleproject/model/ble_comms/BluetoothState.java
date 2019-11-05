package inc.osips.bleproject.model.ble_comms;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import inc.osips.bleproject.model.utilities.GeneralUtil;

/**
 * Created by BABY v2.0 on 9/13/2016.
 */
public class BluetoothState extends BroadcastReceiver {

    Context context;
    GeneralUtil toast;

    public BluetoothState(Context contxt){
        this.context = contxt;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();

        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    toast.message( "Bluetooth is off");
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    toast.message("Bluetooth is turning off...");
                    break;
                case BluetoothAdapter.STATE_ON:
                    toast.message("Bluetooth is on");
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    toast.message("Bluetooth is turning on...");
                    break;
            }
        }
    }
}
