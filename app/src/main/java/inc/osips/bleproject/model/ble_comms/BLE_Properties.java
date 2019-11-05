package inc.osips.bleproject.model.ble_comms;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by BABY v2.0 on 10/24/2016.
 */

public class BLE_Properties {

    public static String hexToString(byte[] data) {
        final StringBuilder sb = new StringBuilder(data.length);

        for(byte byteChar : data) {
            sb.append(String.format("%02X ", byteChar));
        }
        return sb.toString();
    }

    public static int hasWriteProperty(int property) {
        return property & BluetoothGattCharacteristic.PROPERTY_WRITE;
    }

    public static int hasReadProperty(int property) {
        return property & BluetoothGattCharacteristic.PROPERTY_READ;
    }

    public static int hasNotifyProperty(int property) {
        return property & BluetoothGattCharacteristic.PROPERTY_NOTIFY;
    }
}
