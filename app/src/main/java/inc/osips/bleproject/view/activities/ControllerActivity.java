package inc.osips.bleproject.view.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import inc.osips.bleproject.App;
import inc.osips.bleproject.interfaces.ControllerViewInterface;
import inc.osips.bleproject.interfaces.ControlFragmentListener;
import inc.osips.bleproject.R;
import inc.osips.bleproject.utilities.Constants;
import inc.osips.bleproject.utilities.GeneralUtil;
import inc.osips.bleproject.utilities.ServiceUtil;
import inc.osips.bleproject.presenter.RemoteControllerPresenter;
import inc.osips.bleproject.view.fragments.control_fragments.ButtonControlFragment;
import inc.osips.bleproject.view.fragments.control_fragments.ControlAdapter;
import inc.osips.bleproject.view.fragments.control_fragments.ServiceSelectorDialog;
import inc.osips.bleproject.view.fragments.control_fragments.VoiceControlFragment;

public class ControllerActivity extends AppCompatActivity implements ControlFragmentListener, ControllerViewInterface {

    private VoiceControlFragment voiceFrag;
    private ButtonControlFragment manualFrag;
    private ServiceSelectorDialog dialog;
    private ViewPager pager;
    private PagerAdapter mPagerAdapter;
    private TextView myDeviceName;
    private Toolbar ctrlToolBar;
    private Button disconnectButton;
    private RemoteControllerPresenter presenter;
    private List<Fragment> fragList;
    private String commType;
    private List<Parcelable> listDevice= new ArrayList<>();
    private String instructions ="";

    public static final String BUTTON_CONFIG = "button_config";
    public static final String ON_OFF = "on_off";
    public static final String LEFT = "left";
    public static final String RIGHT = "right";
    public static final String UP = "up";
    public static final String DOWN = "down";

    public static final int UUID = 1;
    public static final int ADDR = 0;

    private static final String TAG = ControllerActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        initialisePrequisite();
        initialiseWidgets();

        myDeviceName.setText(presenter.getDeviceName());
    }

    private void initialisePrequisite() {
        App.setCurrentActivity(ControllerActivity.this);
        voiceFrag = new VoiceControlFragment();
        manualFrag = new ButtonControlFragment();
        dialog = ServiceSelectorDialog.getInstance();
        fragList = new LinkedList<>();
        fragList.add(manualFrag);
        fragList.add(voiceFrag);
        mPagerAdapter = new ControlAdapter(getSupportFragmentManager(), getApplicationContext(), fragList);
        Bundle data = getIntent().getBundleExtra(Constants.DEVICE_DATA);
        commType = data.getString(Constants.COMM_TYPE, Constants.ERROR);

        try {
            if (!commType.equals(Constants.ERROR)) {
                listDevice.clear();
                listDevice.addAll(data.getParcelableArrayList(Constants.DEVICE_DATA));
                if (!listDevice.isEmpty()) {
                    presenter = new RemoteControllerPresenter(this, commType, listDevice);
//                    if (ServiceUtil.isAnyRemoteConnectionServiceRunningAPI16(getApplicationContext()))
//                    {
//                        if (commType.equals(Constants.BLE)) {
//                            for (int i = 1; i < listDevice.size(); i++) {
//                                presenter.connectToAnotherDevice(listDevice.get(i));
//                            }
//                        }
//                    }
                }
                else {
                    Log.e(TAG, "initialisePrequisite: ", new Exception("No device to connect with!"));
                }
            }else throw new Exception("No device to connect with!");
        }catch (Exception e){
            e.printStackTrace();
            GeneralUtil.message(e.getMessage());
        }
    }

    @Override
    public void getUUIDFromPopUp(List<String> listUUID){
        Log.d(TAG, "getUUIDFromPopUp:  was called");
        if (!listUUID.isEmpty()) {
            ServiceSelectorDialog.setListUUID(listUUID, UUID);
            if (!dialog.isAdded())
                dialog.show(getSupportFragmentManager(), "Service Selector");
        }
    }

    @Override
    public void getDeviceAddressFromPopUp() {
            List<String> addresses = new ArrayList<>();
            for (Parcelable p : listDevice){
                BluetoothDevice bd = (BluetoothDevice)p;
                addresses.add(bd.getAddress());
            }
            ServiceSelectorDialog.setListUUID(addresses, ADDR);
            if (!dialog.isAdded())
                dialog.show(getSupportFragmentManager(), "Device Selector");
    }

    @Override
    public void setSelectedServiceUUID(String uuidAddr) {
        presenter.setBaseUuidOfBLEDeviceAndConnect(uuidAddr);
        dialog.dismiss();
    }

    @Override
    public void setSelectedAddresses(List<String> addresses) {
        dialog.dismiss();
        for (String addr: addresses){
            presenter.setDeviceAddressAndSendInstructions(addr, instructions);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void getButtonConfigPopUp(){
        final Dialog buttonConfig = new Dialog(this);
        buttonConfig.requestWindowFeature(Window.FEATURE_NO_TITLE);
        buttonConfig.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        buttonConfig.setContentView(R.layout.change_button_control_config);
        buttonConfig.setOwnerActivity(this);
        buttonConfig.setCancelable(true);

        Button saveConfig = buttonConfig.findViewById(R.id.button_save_config);
        final EditText txtOnOff =  buttonConfig.findViewById(R.id.editOn);
        presenter.setEditTextIfStringAvailable(txtOnOff, GeneralUtil.getAppPrefStoredStringWithName(ON_OFF));

        final EditText txtLeft =  buttonConfig.findViewById(R.id.editArrowLeft);
        presenter.setEditTextIfStringAvailable(txtLeft, GeneralUtil.getAppPrefStoredStringWithName(LEFT));

        final EditText txtRight =  buttonConfig.findViewById(R.id.editArrowRight);
        presenter.setEditTextIfStringAvailable(txtRight, GeneralUtil.getAppPrefStoredStringWithName(RIGHT));

        final EditText txtUp =  buttonConfig.findViewById(R.id.editArrowUp);
        presenter.setEditTextIfStringAvailable(txtUp, GeneralUtil.getAppPrefStoredStringWithName(UP));

        final EditText txtDown =  buttonConfig.findViewById(R.id.editArrowDown);
        presenter.setEditTextIfStringAvailable(txtDown, GeneralUtil.getAppPrefStoredStringWithName(DOWN));

        saveConfig.setOnClickListener(view -> {
            presenter.setASharedPrefFromButtonConfig(ON_OFF, txtOnOff);
            presenter.setASharedPrefFromButtonConfig(LEFT, txtLeft);
            presenter.setASharedPrefFromButtonConfig(RIGHT, txtRight);
            presenter.setASharedPrefFromButtonConfig(UP, txtUp);
            presenter.setASharedPrefFromButtonConfig(DOWN, txtDown);

            if(!GeneralUtil.getAppPref().contains(BUTTON_CONFIG))
                GeneralUtil.getEditor().putBoolean(BUTTON_CONFIG, true).commit();

            manualFrag.shouldEnableButtons();
            buttonConfig.dismiss();
        });

        runOnUiThread(buttonConfig::show);

    }

    @Override
    public void removeUUIDPopUp(){
        if (dialog!=null && dialog.isVisible()|dialog.isAdded()){
            runOnUiThread(() -> dialog.dismiss());
        }
    }

    public void initialiseWidgets() {
        ctrlToolBar = findViewById(R.id.appbar_controller);
        setSupportActionBar(ctrlToolBar);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayShowTitleEnabled(false);
        bar.setDisplayHomeAsUpEnabled(false);

        ImageButton changeConfig = findViewById(R.id.buttonConfig);
        changeConfig.setOnClickListener(view -> getButtonConfigPopUp());
        disconnectButton = findViewById(R.id.buttonDisconnect);
        disconnectButton.setOnClickListener(v -> runOnUiThread(this::tryToDisconnectFromDevice));
        myDeviceName = findViewById(R.id.textViewDeviceName);
        if (presenter!= null)
            myDeviceName.setText(presenter.getDeviceName());
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
                if (!ServiceUtil.isAnyRemoteConnectionServiceRunningAPI16(ControllerActivity.this)){
                    getUUIDFromPopUp(presenter.getListOfRemoteServices());
                }
                dialog.dismiss();
            }
        }).setPositiveButton("Yes", (dialog, which) -> {
            presenter.unbindBleService();
            dialog.dismiss();
        });
        alertDialog.show();

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        presenter.unbindBleService();

    }

    @Override
    protected void onPause() {
        super.onPause();
        removeUUIDPopUp();
        if(presenter!=null)
            presenter.unregisterBleMsgReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(presenter!=null)
            presenter.registerRemoteMsgReceiver();
    }

    /*
     * This gets called when any of the buttons on any fragment is pressed
     */
    @Override
    public void sendInstructions(String instruct) {
        this.instructions = instruct;
        getDeviceAddressFromPopUp();
        //presenter.sendInstructionsToDevice(instruct);
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

        runOnUiThread(() -> voiceFrag.showVoiceCommandAsText(commands));

        stopListening();
        voiceFrag.closePopUp();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        removeUUIDPopUp();
        tryToDisconnectFromDevice();
    }
}
