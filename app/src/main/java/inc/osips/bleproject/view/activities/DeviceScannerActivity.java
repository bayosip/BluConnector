package inc.osips.bleproject.view.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.LinkedList;
import java.util.List;

import at.markushi.ui.CircleButton;
import inc.osips.bleproject.App;
import inc.osips.bleproject.interfaces.OnDiscoveredDevicesClickListener;
import inc.osips.bleproject.interfaces.ScannerViewInterface;
import inc.osips.bleproject.R;
import inc.osips.bleproject.model.Devices;
import inc.osips.bleproject.model.utilities.Constants;
import inc.osips.bleproject.model.utilities.GeneralUtil;
import inc.osips.bleproject.presenter.ScannerPresenter;
import inc.osips.bleproject.view.listviews.DevicesViewHolderAdapter;

public class DeviceScannerActivity extends AppCompatActivity implements ScannerViewInterface, OnDiscoveredDevicesClickListener {

    private CircleButton searchButton;
    private static final int REQUEST_ENABLE_BLE = 1;
    private ScannerPresenter presenter;
    private ProgressDialog ringProgressDialog;
    private LinearLayout layoutDevices, layoutSearch;
    private RecyclerView discoveredDevices;
    private DevicesViewHolderAdapter adapter;
    private Button sendMessage;
    private EditText msgBox;
    private String type;
    private int flag =0;
    private List<Devices> remoteDevices;

    private static final String DEVICE_DATA = "Device Data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        initialisePrequistes();
        initiateWidgets();
    }

    private void initialisePrequistes(){
        App.setCurrentActivity(DeviceScannerActivity.this);
        type = getIntent().getExtras().getString(Constants.COMM_TYPE);
        presenter = new ScannerPresenter(this, type);
        remoteDevices = new LinkedList<>();
        adapter =  new DevicesViewHolderAdapter(remoteDevices, this);
    }

    public void initiateWidgets(){
        searchButton = findViewById(R.id.buttonConnect);
        if (type.equals(Constants.BLE)){
            searchButton.setImageDrawable(GeneralUtil.setADrawable(this, R.drawable.ic_bluetooth_searching_24dp));
        }else {
            searchButton.setImageDrawable(GeneralUtil.setADrawable(this, R.drawable.ic_wifi_24dp));
        }
        layoutSearch = findViewById(R.id.layoutSearch);
        layoutDevices = findViewById(R.id.layoutDiscoveredDevices);
        discoveredDevices = findViewById(R.id.peerListView);
        sendMessage = findViewById(R.id.buttonSend);
        msgBox = findViewById(R.id.editWriteMsg);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.shouldStartScan();
            }
        });
        discoveredDevices = findViewById(R.id.peerListView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(),
                RecyclerView.VERTICAL, false);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(discoveredDevices.getContext(),
                layoutManager.getOrientation());
        discoveredDevices.addItemDecoration(itemDecoration);
        discoveredDevices.setAdapter(adapter);
    }

    private void resizeForIncomingListOfDevice(){
        GeneralUtil.expand(layoutDevices);
        GeneralUtil.ResizeAnimation animation =  new GeneralUtil.ResizeAnimation(layoutSearch,
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutSearch.startAnimation(animation);
    }

    private void resizeForSearch(){
        GeneralUtil.collapse(layoutDevices);
        GeneralUtil.ResizeAnimation animation =  new GeneralUtil.ResizeAnimation(layoutSearch,
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutSearch.startAnimation(animation);
    }

    public void launchRingDialog() {
        ringProgressDialog = ProgressDialog.show(DeviceScannerActivity.this,
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
                    //if()
                    presenter.startScanningForRemoteDevices();
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            GeneralUtil.message(
                                    "Cannot Scan for bluetooth Le device");
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public void goToDeviceControlView(final Bundle data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (ringProgressDialog.isShowing())
                    ringProgressDialog.dismiss();

                presenter.stopScanner();

                if (!ringProgressDialog.isShowing()){
                    GeneralUtil.transitionActivity(DeviceScannerActivity.this,
                            new Intent(DeviceScannerActivity.this, ControllerActivity.class)
                            .putExtra(DEVICE_DATA, data));
                }
            }
        });
    }

    @Override
    public void progressFromScan(final List<Devices> devices) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ringProgressDialog.dismiss();
                presenter.stopScanner();
                if (!ringProgressDialog.isShowing()) {
                    if(devices.size()> 0){
                        resizeForIncomingListOfDevice();
                        remoteDevices.clear();
                        remoteDevices.addAll(devices);
                        adapter.notifyDataSetChanged();
                        //discoveredDevices.
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.registerBroadCastReceiver(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.unregisterBroadCastReceiver(this);
    }

    @Override
    protected void onStop() {
        presenter.stopScanner();
        super.onStop();
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

    @Override
    public Activity getCurrentActivity() {
        return this;
    }

    @Override
    public void selectDeviceToConnectTo(Devices device) {
        presenter.stopScanner();
        Bundle data = new Bundle();
        data.putString(Constants.COMM_TYPE, type);
        data.putParcelable(Constants.DEVICE_DATA, device.getDeviceData());
        //goToDeviceControlView(data);
    }

    @Override
    public Context getListenerContext() {
        return this;
    }
}
