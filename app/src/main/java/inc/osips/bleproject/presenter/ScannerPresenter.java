package inc.osips.bleproject.presenter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.util.Log;

import java.util.List;

import inc.osips.bleproject.App;
import inc.osips.bleproject.interfaces.PresenterInterface;
import inc.osips.bleproject.interfaces.Scanner;
import inc.osips.bleproject.interfaces.ScannerViewInterface;
import inc.osips.bleproject.model.Scan_n_Connection;
import inc.osips.bleproject.model.utilities.GeneralUtil;
import inc.osips.bleproject.view.activities.ControllerActivity;

public class ScannerPresenter extends ScanCallback implements PresenterInterface {

    private Scanner scanner;
    private ScannerViewInterface viewInterface;


    public ScannerPresenter(ScannerViewInterface viewInterface) {
        this.viewInterface = viewInterface;
        scanner = new Scan_n_Connection(this);
    }

    public void shouldStartScan(){
        if(!scanner.isScanning())
            viewInterface.launchRingDialog();
    }

    public Activity getScanningAcativity(){
        return viewInterface.getCurrentActivity();
    }

    public void startScanningForBleDevices(){
        scanner.onStart();
    }

    public void stopScanner(){
        if(scanner.isScanning())
            scanner.onStop();
    }

    @Override
    public ScanCallback getScanCallBack() {
        return this;
    }

    @Override
    public void onScanResult(int callbackType, final ScanResult result) {
        Log.i("callbackType", String.valueOf(callbackType));
        Log.i("result", result.toString());
        final int RSSI = result.getRssi();
        if (RSSI>=-105) {
            scanner.onStop();
            if(!(App.getCurrentActivity() instanceof ControllerActivity))
                viewInterface.progressFromScan(result);
        }
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        for (ScanResult sr : results) {
            Log.i("ScanResult - Results", sr.toString());
            //ToastMakers.message(scannerActivity.getApplicationContext(), sr.toString());
        }
    }

    @Override
    public void onScanFailed(int errorCode) {
        Log.e("Scan Failed", "Error Code: " + errorCode);
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] bytes) {
        final int RSSI = rssi;
        if (RSSI >= -105){
            scanner.onStop();
            if(!(App.getCurrentActivity() instanceof ControllerActivity))
                viewInterface.progressFromScan(device);
        }
    }


}
