package inc.osips.bleproject.view.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import inc.osips.bleproject.App;
import inc.osips.bleproject.interfaces.ControllerViewInterface;
import inc.osips.bleproject.interfaces.ControlFragmentListener;
import inc.osips.bleproject.R;
import inc.osips.bleproject.model.UUID_IP_TextWatcher;
import inc.osips.bleproject.utilities.Constants;
import inc.osips.bleproject.utilities.GeneralUtil;
import inc.osips.bleproject.utilities.ServiceUtil;
import inc.osips.bleproject.presenter.RemoteControllerPresenter;
import inc.osips.bleproject.view.fragments.control_fragments.ButtonControlFragment;
import inc.osips.bleproject.view.fragments.control_fragments.ControlAdapter;
import inc.osips.bleproject.view.fragments.control_fragments.VoiceControlFragment;

public class ControllerActivity extends AppCompatActivity implements ControlFragmentListener, ControllerViewInterface {

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
    private Dialog uuidPopUp;

    public static final String BUTTON_CONFIG = "button_config";
    public static final String ON_OFF = "on_off";
    public static final String LEFT = "left";
    public static final String RIGHT = "right";
    public static final String UP = "up";
    public static final String DOWN = "down";
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
        fragList = new LinkedList<>();
        fragList.add(manualFrag);
        fragList.add(voiceFrag);
        mPagerAdapter = new ControlAdapter(getSupportFragmentManager(), getApplicationContext(), fragList);
        Bundle data = getIntent().getBundleExtra(Constants.DEVICE_DATA);
        commType = data.getString(Constants.COMM_TYPE, Constants.ERROR);

        try {
            if (!commType.equals(Constants.ERROR)) {
                Parcelable parcelDevice = data.getParcelable(Constants.DEVICE_DATA);
                if (parcelDevice != null) {
                    presenter = new RemoteControllerPresenter(this, commType, parcelDevice);
                }
                else throw new Exception("No device to connect with!");
            }else throw new Exception("No device to connect with!");
        }catch (Exception e){
            GeneralUtil.message(e.getMessage());
        }
    }

    public void getUUIDFromPopUp(){

        uuidPopUp = new Dialog(this);
        uuidPopUp.requestWindowFeature(Window.FEATURE_NO_TITLE);
        uuidPopUp.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        uuidPopUp.setContentView(R.layout.enter_uuid_ip_dialog);
        uuidPopUp.setOwnerActivity(this);
        uuidPopUp.setCancelable(false);

        Button enterUUID = uuidPopUp.findViewById(R.id.buttonUUID);
        final EditText baseUUID =  uuidPopUp.findViewById(R.id.editTextBaseUUID);
        baseUUID.addTextChangedListener(new UUID_IP_TextWatcher(baseUUID, commType));

        enterUUID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str = baseUUID.getText().toString();
                presenter.setBaseUuidOfBLEDeviceAndConnect(str);
                //uuidPopUp.dismiss();
            }
        });

        uuidPopUp.show();
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

        saveConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.setASharedPrefFromButtonConfig(ON_OFF, txtOnOff);
                presenter.setASharedPrefFromButtonConfig(LEFT, txtLeft);
                presenter.setASharedPrefFromButtonConfig(RIGHT, txtRight);
                presenter.setASharedPrefFromButtonConfig(UP, txtUp);
                presenter.setASharedPrefFromButtonConfig(DOWN, txtDown);

                if(!GeneralUtil.getAppPref().contains(BUTTON_CONFIG))
                    GeneralUtil.getEditor().putBoolean(BUTTON_CONFIG, true).commit();

                manualFrag.shouldEnableButtons();
                buttonConfig.dismiss();
            }
        });

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                buttonConfig.show();
            }
        });

    }

    @Override
    public void removeUUIDPopUp(){
        if (uuidPopUp!=null){
            uuidPopUp.dismiss();
        }
    }

    public void initialiseWidgets() {
        ctrlToolBar = findViewById(R.id.appbar_controller);
        setSupportActionBar(ctrlToolBar);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayShowTitleEnabled(false);
        bar.setDisplayHomeAsUpEnabled(false);

        ImageButton changeConfig = findViewById(R.id.buttonConfig);
        changeConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getButtonConfigPopUp();
            }
        });
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
                    getUUIDFromPopUp();
                }
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
        if(presenter!=null && ServiceUtil.isAnyRemoteConnectionServiceRunningAPI16(this))
            presenter.unregisterBleMsgReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(presenter!=null && ServiceUtil.isAnyRemoteConnectionServiceRunningAPI16(this))
            presenter.registerRemoteMsgReceiver();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        removeUUIDPopUp();
        tryToDisconnectFromDevice();
    }
}
