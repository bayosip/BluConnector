package inc.osips.bleproject.view.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcelable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import at.markushi.ui.CircleButton;
import inc.osips.bleproject.App;
import inc.osips.bleproject.interfaces.ScannerViewInterface;
import inc.osips.bleproject.R;
import inc.osips.bleproject.model.utilities.GeneralUtil;
import inc.osips.bleproject.presenter.ScannerPresenter;

public class BLE_ScannerActivity extends AppCompatActivity implements ScannerViewInterface {

    private CircleButton connectButton;
    private static final int REQUEST_ENABLE_BLE = 1;
    private ScannerPresenter presenter;
    private ProgressDialog ringProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialisePrequistes();
        initiateWidgets();
    }

    private void initialisePrequistes(){
        App.setCurrentActivity(BLE_ScannerActivity.this);
        presenter = new ScannerPresenter(this);
    }

    public void initiateWidgets(){
        connectButton = findViewById(R.id.buttonConnect);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.shouldStartScan();
            }
        });
    }

    public void launchRingDialog() {
        ringProgressDialog = ProgressDialog.show(BLE_ScannerActivity.this,
                "Please wait ...", "Connecting ...", true);
        ringProgressDialog.setCancelable(true);
        ringProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                presenter.stopScanner();
            }
        });

        //Thread thread = new Thread();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    presenter.startScanningForBleDevices();
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            GeneralUtil.message(
                                    "Cannot Scan for bluetooth le device");
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public void progressFromScan(final Parcelable result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ringProgressDialog.dismiss();
                presenter.stopScanner();
                if (!ringProgressDialog.isShowing()){
                    GeneralUtil.transitionActivity(BLE_ScannerActivity.this,
                            new Intent(BLE_ScannerActivity.this, ControllerActivity.class)
                            .putExtra("Device Data", result));
                }
            }
        });
    }

    @Override
    protected void onStop() {
        presenter.stopScanner();
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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

    @Override
    public Activity getCurrentActivity() {
        return this;
    }
}
