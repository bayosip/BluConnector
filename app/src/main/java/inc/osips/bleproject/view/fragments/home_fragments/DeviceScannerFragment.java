package inc.osips.bleproject.view.fragments.home_fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.LinkedList;
import java.util.List;

import at.markushi.ui.CircleButton;
import inc.osips.bleproject.interfaces.OnDiscoveredDevicesClickListener;
import inc.osips.bleproject.interfaces.ScannerViewInterface;
import inc.osips.bleproject.R;
import inc.osips.bleproject.model.Devices;
import inc.osips.bleproject.model.utilities.Constants;
import inc.osips.bleproject.model.utilities.GeneralUtil;
import inc.osips.bleproject.presenter.ScannerPresenter;
import inc.osips.bleproject.view.activities.ControllerActivity;
import inc.osips.bleproject.view.listviews.DevicesViewHolderAdapter;

public class DeviceScannerFragment extends BaseFragment implements OnDiscoveredDevicesClickListener  {

    private static final String DEVICE_DATA = "Device Data";
    private CircleButton searchButton;

    private ProgressDialog ringProgressDialog;
    private LinearLayout layoutDevices, layoutSearch;
    private RecyclerView discoveredDevices;
    private DevicesViewHolderAdapter adapter;
    private Button sendMessage;
    private EditText msgBox;
    private String type;
    private boolean isFirstScan = true;
    private List<Devices> remoteDevices;


    public static DeviceScannerFragment getInstance(String type){
        DeviceScannerFragment fragment = new DeviceScannerFragment();
        Bundle extra = new Bundle();
        extra.putString(Constants.COMM_TYPE, type);
        fragment.setArguments(extra);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_search, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        type = getArguments().getString(Constants.COMM_TYPE);
        initialiseWidgets(view);
    }

    public void initialiseWidgets(View view){
        remoteDevices = new LinkedList<>();
        adapter =  new DevicesViewHolderAdapter(remoteDevices, this);

        searchButton = view.findViewById(R.id.buttonConnect);
        if (type.equals(Constants.BLE)){
            searchButton.setImageDrawable(GeneralUtil.setADrawable(activity, R.drawable.ic_bluetooth_searching_24dp));
        }else {
            searchButton.setImageDrawable(GeneralUtil.setADrawable(activity, R.drawable.ic_wifi_24dp));
        }
        layoutSearch = view.findViewById(R.id.layoutSearch);
        layoutDevices = view.findViewById(R.id.layoutDiscoveredDevices);
        discoveredDevices = view.findViewById(R.id.peerListView);
        sendMessage = view.findViewById(R.id.buttonSend);
        msgBox = view.findViewById(R.id.editWriteMsg);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isFirstScan)
                    resizeForSearch();
                if(!activity.isAlreadyScanning())launchRingDialog();
            }
        });
        discoveredDevices = view.findViewById(R.id.peerListView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity,
                RecyclerView.VERTICAL, false);

        discoveredDevices.setLayoutManager(layoutManager);
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

    private void launchRingDialog() {
        ringProgressDialog = ProgressDialog.show(activity,
                "Please wait ...", "Connecting ...", true);
        ringProgressDialog.setCancelable(true);
        ringProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                activity.stopDeviceScanning();
            }
        });

        //Thread thread = new Thread();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    activity.startDeviceScanning();
                } catch (Exception e) {
                    activity.runOnUiThread(new Runnable() {
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

    public void goToDeviceControlView(final Bundle data) {
        if (!ringProgressDialog.isShowing()){
            GeneralUtil.transitionActivity(activity,
                    new Intent(activity, ControllerActivity.class)
                            .putExtra(DEVICE_DATA, data));
        }

    }

    public void progressFromScan(final List<Devices> devices) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ringProgressDialog.dismiss();
                activity.stopDeviceScanning();
                if (!ringProgressDialog.isShowing()) {
                    if(devices.size()> 0){
                        isFirstScan =false;
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
    public void onStart() {
        super.onStart();
        activity.registerReceiver();
    }

    @Override
    public void onStop() {
        activity.stopDeviceScanning();
        super.onStop();
    }

    @Override
    public void selectDeviceToConnectTo(Devices device) {
        activity.stopDeviceScanning();
        Bundle data = new Bundle();
        data.putString(Constants.COMM_TYPE, type);
        data.putParcelable(Constants.DEVICE_DATA, device.getDeviceData());
        goToDeviceControlView(data);
    }

    @Override
    public Context getListenerContext() {
        return activity;
    }

}
