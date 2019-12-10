package inc.osips.bleproject.model.ble_comms;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import inc.osips.bleproject.interfaces.WirelessConnectionScanner;
import inc.osips.bleproject.model.DeviceScannerFactory;
import inc.osips.bleproject.model.utilities.GeneralUtil;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BLE_Scanner implements WirelessConnectionScanner {

    private BluetoothAdapter bleAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private Activity activity;

    private List<ScanFilter> filters;
    private ScanCallback mScanCallback;
    private boolean scanState = false;
    private String deviceName = null;//"Osi_p BLE-LED Controller";
    private String baseUUID;//"6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    private ScanSettings settings;


    private ParcelUuid uuidParcel = null;
    //UUID uuid;

    public BLE_Scanner(Activity activity, ScanCallback mScanCallback, String baseUUID){

        this.activity = activity;
        this.mScanCallback = mScanCallback;
        this.baseUUID = baseUUID;
        final BluetoothManager manager = (BluetoothManager) activity
                .getSystemService(Context.BLUETOOTH_SERVICE);

        bleAdapter = manager.getAdapter();
        if (!TextUtils.isEmpty(baseUUID))
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

        if (uuidParcel== null){
            scanForAllBLEDevices();
        }
        else scanForSpecificBLEDevices();
    }

    public void onStop() {
        if (scanState) {
            scanStop();
            scanState = false;
        }
    }

    private void scanForSpecificBLEDevices() {
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
            }, 6000);

            scanState = true;

            bluetoothLeScanner.startScan(filters, settings, mScanCallback);
        }
        else{
            scanStop();
        }
    }

    private void scanForAllBLEDevices(){
        if (!scanState){
            //start scan for 15s, the stop
            GeneralUtil.getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanStop();
                }
            }, 6000);

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
            Log.w("BLE", "scanning stopped");
            if(bluetoothLeScanner != null)
                bluetoothLeScanner.stopScan(mScanCallback);

            activity.sendBroadcast(new Intent(DeviceScannerFactory.SCANNING_STOPPED));
        }
    }



    @Override
    public void showDiscoveredDevices() {

    }
}
