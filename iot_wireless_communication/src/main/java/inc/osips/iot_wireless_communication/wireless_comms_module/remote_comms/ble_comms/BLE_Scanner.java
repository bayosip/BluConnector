package inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.ble_comms;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import inc.osips.iot_wireless_communication.wireless_comms_module.interfaces.WlanConnectionScanner;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.utilities.Constants;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.utilities.Util;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BLE_Scanner extends ScanCallback implements WlanConnectionScanner {

    private long SCAN_TIME = 6000; //default scan time
    private BluetoothAdapter bleAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private Activity activity;

    private List<ScanFilter> filters;
    private ScanCallback mScanCallback = this;
    private boolean scanState = false;
    private String deviceName = null;//"Osi_p BLE-LED Controller";
    private static final String TAG = "BLE_Scanner";

    private List<String> deviceAddresses;
    //private

    private ScanSettings settings;
    //"6e400001-b5a3-f393-e0a9-e50e24dcca9e";

    private ParcelUuid uuidParcel = null;
    //UUID uuid;

    public BLE_Scanner(@NonNull Activity activity, @Nullable ScanCallback mScanCallback, @NonNull String baseUUID, long scantime){
        deviceAddresses = new ArrayList<>();
        if (scantime >=1000)SCAN_TIME = scantime;

        this.activity = activity;
        if (mScanCallback != null)
            this.mScanCallback = mScanCallback;
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
        }
    }

    private void scanForSpecificBLEDevices() {
        if (!scanState) {
            ScanFilter myDevice = new ScanFilter.Builder()
                    .setServiceUuid(uuidParcel).build();
            Log.d(TAG, uuidParcel.toString());
            filters = new ArrayList<>();

            if (myDevice !=null){
                filters.add(myDevice);
            }
            else {
                myDevice = new ScanFilter.Builder()
                        .setDeviceName(deviceName).build();
                filters.add(myDevice);
            }
            //start scan for scan_time, then stop
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanStop();
                }
            }, SCAN_TIME);

            scanState = true;

            bluetoothLeScanner.startScan(filters, settings, mScanCallback);
        }
        else{
            scanStop();
        }
    }

    private void scanForAllBLEDevices(){
        if (!scanState){
            //start scan for scan_time, then stop
            Util.getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanStop();
                }
            }, SCAN_TIME);

            scanState = true;
            bluetoothLeScanner.startScan(null, settings, mScanCallback);
        }
        else{
            scanStop();
        }
    }

    private void scanStop() {
        Util.message(activity,"Scanning Stopped!");

        if (scanState) {
            scanState = false;
            Log.w(TAG, "scanning stopped");
            if(bluetoothLeScanner != null)
                bluetoothLeScanner.stopScan(mScanCallback);

            activity.sendBroadcast(new Intent(WlanConnectionScanner.SCANNING_STOPPED));
        }
    }

    @Override
    public void showDiscoveredDevices() {}

    @TargetApi(21)
    @Override
    public void onScanResult(int callbackType, final ScanResult result) {
        Log.i("callbackType", String.valueOf(callbackType));
        Log.i("result", result.toString());
        final int RSSI = result.getRssi();
        BluetoothDevice ble = result.getDevice();
        if (RSSI>=-105 && !deviceAddresses.contains(ble.getAddress())) {
            activity.sendBroadcast(new Intent(WlanConnectionScanner.DEVICE_DISCOVERED)
                    .putExtra(Constants.DEVICE_DATA, ble));
            deviceAddresses.add(ble.getAddress());
        }
    }

    @TargetApi(21)
    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        for (ScanResult sr : results) {
            Log.w("ScanResult - Results", sr.toString());
        }
    }

    @TargetApi(21)
    @Override
    public void onScanFailed(int errorCode) {
        Log.e("Scan Failed", "Error Code: " + errorCode);
    }

}
