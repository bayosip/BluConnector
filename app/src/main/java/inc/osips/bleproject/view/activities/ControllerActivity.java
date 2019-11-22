package inc.osips.bleproject.view.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.DialogInterface;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Build;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import inc.osips.bleproject.App;
import inc.osips.bleproject.interfaces.ControllerViewInterface;
import inc.osips.bleproject.interfaces.ControlFragmentListener;
import inc.osips.bleproject.R;
import inc.osips.bleproject.model.utilities.Constants;
import inc.osips.bleproject.model.utilities.GeneralUtil;
import inc.osips.bleproject.presenter.RemoteControllerPresenter;
import inc.osips.bleproject.view.fragments.ButtonControlFragment;
import inc.osips.bleproject.view.fragments.ControlAdapter;
import inc.osips.bleproject.view.fragments.VoiceControlFragment;

public class ControllerActivity extends AppCompatActivity implements ControlFragmentListener, ControllerViewInterface {

    private BluetoothDevice bleDevice;
    private WifiP2pDevice p2pDevice;
    private ScanResult result;
    private VoiceControlFragment voiceFrag;
    private ButtonControlFragment manualFrag;
    private ViewPager pager;
    private PagerAdapter mPagerAdapter;
    private TextView myDeviceName;
    private Toolbar ctrlToolBar;
    private Button disconnectButton;
    private RemoteControllerPresenter presenter;
    private List<Fragment> fragList;
    private String commType;

    private static final String TAG = ControllerActivity.class.getSimpleName();
    private String deviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        initialisePrequisite();

        initiateWidgets();

        myDeviceName.setText(deviceName);
    }

    private void initialisePrequisite() {
        App.setCurrentActivity(ControllerActivity.this);
        voiceFrag = new VoiceControlFragment();
        manualFrag = new ButtonControlFragment();
        fragList = new LinkedList<>();
        fragList.add(manualFrag);
        fragList.add(voiceFrag);
        mPagerAdapter = new ControlAdapter(getSupportFragmentManager(), getApplicationContext(), fragList);
        Bundle data = getIntent().getBundleExtra(Constants.DEVICE_DATA);
        commType = data.getString(Constants.COMM_TYPE, Constants.ERROR);

        if (!commType.equals(Constants.ERROR)){
            Parcelable p = data.getParcelable(Constants.DEVICE_DATA);
            if (p instanceof ScanResult || p instanceof BluetoothDevice)
                commType = Constants.BLE;
            else if (p instanceof WifiP2pDevice)
                commType = Constants.WIFI;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            result = data.getParcelable(Constants.DEVICE_DATA);

            bleDevice = result.getDevice();
            deviceName = bleDevice.getName();
            GeneralUtil.message(deviceName);
        } else {
            bleDevice = data.getParcelable(Constants.DEVICE_DATA);
            deviceName = bleDevice.getName();
        }
        Log.i(TAG + "result", bleDevice.toString());
        presenter = new RemoteControllerPresenter(this, commType, bleDevice);
    }

    @Override
    public void initiateWidgets() {
        ctrlToolBar = findViewById(R.id.appbar_controller);
        setSupportActionBar(ctrlToolBar);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayShowTitleEnabled(false);
        bar.setDisplayHomeAsUpEnabled(false);

        disconnectButton = findViewById(R.id.buttonDisconnect);
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tryToDisconnectFromDevice();
                    }
                });
            }
        });
        myDeviceName = findViewById(R.id.textViewDeviceName);
        pager = findViewById(R.id.contentFragment);
        pager.setAdapter(mPagerAdapter);
    }


    private void tryToDisconnectFromDevice() {
        final AlertDialog.Builder alertDialog;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            alertDialog = new AlertDialog.Builder(this,
                    android.R.style.Theme_DeviceDefault_Dialog);
        else
            alertDialog = new AlertDialog.Builder(ControllerActivity.this);

        alertDialog.setCancelable(false)
                .setIcon(R.mipmap.ic_launcher).setMessage(getApplicationContext()
                .getString(R.string.disconnect_warning))
                .setTitle("Notice").setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                presenter.unbindBleService();
                dialog.dismiss();
            }
        });
        alertDialog.show();


    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService

        presenter.bindBleService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        if (bleDevice != null) {
            presenter.unbindBleService();
            bleDevice = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.unregisterBleMsgReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.registerBleMsgReceiver();
    }

    /*
     * This gets called when any of the buttons on any fragment is pressed
     */
    @Override
    public void sendInstructions(String instruct) {
        presenter.sendInstructionsToDevice(instruct);
    }

    @Override
    public void speechInputCall() {
        presenter.speechInputCall();
    }

    @Override
    public void stopListening() {
        presenter.stopListening();
    }

    @Override
    public void startListening() {
        presenter.initSpeech();
    }


    @Override
    public Activity getFragmentactivity() {
        return getControlContext();
    }

    @Override
    public Activity getCurrentActivity() {
        return this;
    }

    @Override
    public Activity getControlContext() {
        return ControllerActivity.this;
    }

    @Override
    public void processInstructions(final String commands) {
        String command = commands.toLowerCase();
        if (command.contains("on") || command.contains("turn on"))
            sendInstructions("on");
        else if (commands.contains("off")|| command.contains("turn off"))
            sendInstructions("off");
        else sendInstructions(commands);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                voiceFrag.showVoiceCommandAsText(commands);
            }
        });

        stopListening();
        voiceFrag.closePopUp();
    }
}
