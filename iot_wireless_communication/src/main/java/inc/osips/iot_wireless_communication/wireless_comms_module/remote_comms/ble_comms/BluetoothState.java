package inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.ble_comms;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import inc.osips.iot_wireless_communication.R;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.utilities.Util;

/**
 * Created by Adebayo Osipitan on 9/13/2016.
 */
public class BluetoothState extends BroadcastReceiver {

    Context context;

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
                    Util.message( context,context.getString(R.string.bluetooth_off));
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Util.message( context,context.getString(R.string.bt_turning_off));
                    break;
                case BluetoothAdapter.STATE_ON:
                    Util.message(context,context.getString(R.string.bt_on));
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Util.message(context,context.getString(R.string.bt_turning_on));
                    break;
            }
        }
    }
}
