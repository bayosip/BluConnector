package inc.osips.bleproject.model.ble_comms;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import inc.osips.bleproject.interfaces.PresenterInterface;
import inc.osips.bleproject.interfaces.WirelessConnectionScanner;
import inc.osips.bleproject.model.utilities.GeneralUtil;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BLE_Scanner implements WirelessConnectionScanner {

    private BluetoothAdapter bleAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private Activity activity;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private ScanCallback mScanCallback;
    private boolean scanState = false;
    private String deviceName = "Osi_p BLE-LED Controller";
    private final String baseUUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";

    private ParcelUuid uuidParcel;
    //UUID uuid;

    public BLE_Scanner(Activity activity, ScanCallback mScanCallback){

        this.activity = activity;
        this.mScanCallback = mScanCallback;

        final BluetoothManager manager = (BluetoothManager) activity
                .getSystemService(Context.BLUETOOTH_SERVICE);

        bleAdapter = manager.getAdapter();
        uuidParcel = new ParcelUuid(UUID.fromString(baseUUID));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bluetoothLeScanner = bleAdapter.getBluetoothLeScanner();
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
        }
    }

    public boolean isScanning() {
        return scanState;
    }

    public void onStart() {
        if (!HW_Compatibility_Checker.checkBluetooth(bleAdapter)) {
            HW_Compatibility_Checker.requestUserBluetooth(activity);
        }
        scanForBLEDevices();
    }

    public void onStop() {
        if (isScanning())
            scanStop();
    }

    private void scanForBLEDevices() {
        if (!scanState) {
            ScanFilter myDevice = new ScanFilter.Builder()
                    .setServiceUuid(uuidParcel).build();
            Log.d("Device UUID ", uuidParcel.toString());
            filters = new ArrayList<>();
            if (myDevice !=null){
                filters.add(myDevice);
            }
            else {
                myDevice = new ScanFilter.Builder()
                        .setDeviceName(deviceName).build();
                filters.add(myDevice);
            }
            //start scan for 15s, the stop
            GeneralUtil.getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanStop();
                }
            }, 15000);

            scanState = true;

            bluetoothLeScanner.startScan(null, settings, mScanCallback);
        }
        else{
            scanStop();
        }
    }

    private void scanStop() {
        GeneralUtil.message("Scanning Stopped!");
        if (scanState) {
            scanState = false;

            if(bluetoothLeScanner != null)
                bluetoothLeScanner.stopScan(mScanCallback);
        }
    }

    @Override
    public void showDiscoveredDevices() {

    }
}
