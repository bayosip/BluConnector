package inc.osips.bleproject.view.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.List;

import inc.osips.bleproject.R;
import inc.osips.bleproject.interfaces.ScannerViewInterface;
import inc.osips.bleproject.model.remote_comms.Devices;
import inc.osips.bleproject.utilities.GeneralUtil;
import inc.osips.bleproject.presenter.ScannerPresenter;
import inc.osips.bleproject.view.fragments.home_fragments.CommChooserFragment;
import inc.osips.bleproject.view.fragments.home_fragments.DeviceScannerFragment;

public class Home extends AppCompatActivity implements ScannerViewInterface {

    private static final int REQUEST_ENABLE_BLE = 1;
    private ScannerPresenter presenter;
    private DeviceScannerFragment scannerFragment;
    private CommChooserFragment chooserFragment;
    private static final String frag1 = CommChooserFragment.class.getSimpleName();
    private static final String frag2 = DeviceScannerFragment.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initialisePrequistes();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, chooserFragment, frag1).commit();

        ;
    }

    private void initialisePrequistes(){

        chooserFragment = CommChooserFragment.getInstance();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BLE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                GeneralUtil.message("Bluetooth On");
            }
            else if (resultCode == RESULT_CANCELED) {
                GeneralUtil.message( "Please turn on Bluetooth");
            }
        }
    }

    public void changeFragmentToScannerFrag(String scanType){
        scannerFragment = DeviceScannerFragment.getInstance(scanType);
        presenter = new ScannerPresenter(this, scanType);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, scannerFragment, frag2);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commitAllowingStateLoss();
    }

    public boolean isAlreadyScanning(){
        return presenter.shouldStartScan();
    }

    public void stopDeviceScanning(){
        presenter.stopScanner();
    }

    public void startDeviceScanning(){
        presenter.startScanningForRemoteDevices();
    }

    public void registerReceiver(){
        presenter.registerBroadCastReceiver(this);
    }

    public void unregisterReceiver(){
        presenter.unregisterBroadCastReceiver(this);
    }

    @Override
    public Activity getCurrentActivity() {
        return this;
    }


    @Override
    public void goToDeviceControlView(Bundle data) {
        scannerFragment.goToDeviceControlView(data);
    }

    @Override
    public void progressFromScan(List<Devices> devices) {
        scannerFragment.progressFromScan(devices);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (presenter!=null)
            presenter.registerBroadCastReceiver(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (presenter!=null)
            presenter.unregisterBroadCastReceiver(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter!=null)
            presenter.stopScanner();

    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof DeviceScannerFragment){
            super.onBackPressed();
            presenter = null;
        }else {
            GeneralUtil.exitApp(Home.this);
        }
    }
}
