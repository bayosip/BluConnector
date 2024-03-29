package inc.osips.bleproject.view.fragments.home_fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import at.markushi.ui.CircleButton;
import inc.osips.bleproject.databinding.ContentSearchBinding;
import inc.osips.bleproject.interfaces.OnDiscoveredDevicesClickListener;
import inc.osips.bleproject.R;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.Devices;
import inc.osips.bleproject.utilities.Constants;
import inc.osips.bleproject.utilities.GeneralUtil;
import inc.osips.bleproject.view.activities.ControllerActivity;
import inc.osips.bleproject.view.listviews.DevicesViewHolderAdapter;

public class DeviceScannerFragment extends BaseFragment implements OnDiscoveredDevicesClickListener  {

    private static final String DEVICE_DATA = "Device Data";
    private CircleButton searchButton;

    private ProgressDialog ringProgressDialog;
    private LinearLayout layoutSearch;
    private RecyclerView discoveredDevices;
    private DevicesViewHolderAdapter adapter;
    private EditText msgBox;
    private String type;
    private boolean isFirstScan = true;
    private List<Devices> remoteDevices;
    private ContentSearchBinding binding;
    private final List<Devices> selectedDev = new ArrayList<>();


    public static DeviceScannerFragment getInstance(@NonNull String type){
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
        binding =  ContentSearchBinding.bind(view);
        type = getArguments().getString(Constants.COMM_TYPE, "");
        initialiseWidgets(view);
    }

    public void initialiseWidgets(View view){
        remoteDevices = new LinkedList<>();
        adapter =  new DevicesViewHolderAdapter(remoteDevices, this);

        searchButton = view.findViewById(R.id.buttonConnect);
        if (type.equals(Constants.BLE)){
            searchButton.setImageDrawable(GeneralUtil.setADrawable(requireActivity(),
                    R.drawable.ic_bluetooth_searching_24dp));
        }else {
            searchButton.setImageDrawable(GeneralUtil.setADrawable(requireActivity(),
                    R.drawable.ic_wifi_24dp));
        }
        layoutSearch = view.findViewById(R.id.layoutSearch);
        discoveredDevices = view.findViewById(R.id.peerListView);

        discoveredDevices = view.findViewById(R.id.peerListView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity(),
                RecyclerView.VERTICAL, false);

        discoveredDevices.setLayoutManager(layoutManager);
        discoveredDevices.setAdapter(adapter);

        searchButton.setOnClickListener(v -> {
            if(!isFirstScan)
                resizeForSearch();
            if(!activity.isAlreadyScanning()){
                accessRemoteScanning();
            }
        });

        binding.layoutDiscoveredDevices.buttonConnect.setOnClickListener(v->{
            Bundle data = new Bundle();
            data.putString(Constants.COMM_TYPE, type);
            ArrayList<Parcelable> devices = new ArrayList<>();
            for (Devices d: selectedDev){
                devices.add(d.getDeviceData());
            }
            data.putParcelableArrayList(Constants.DEVICE_DATA, devices);
            goToDeviceControlView(data);
        });
    }

    private void accessRemoteScanning() {
        Dexter.withContext(requireContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        launchRingDialog();
                        if (remoteDevices.size()>0){
                            remoteDevices.clear();
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission,
                                                                   PermissionToken token) {
                        token.cancelPermissionRequest();
                    }
                }).check();
    }

    private void resizeForIncomingListOfDevice(){
        GeneralUtil.expand(binding.layoutDiscoveredDevices.getRoot());
        GeneralUtil.ResizeAnimation animation =  new GeneralUtil.ResizeAnimation(layoutSearch,
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 300);
        layoutSearch.startAnimation(animation);
    }

    private void resizeForSearch(){
        GeneralUtil.collapse(binding.layoutDiscoveredDevices.getRoot());
        GeneralUtil.ResizeAnimation animation =  new GeneralUtil.ResizeAnimation(layoutSearch,
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutSearch.startAnimation(animation);
    }

    private void launchRingDialog() {
        ringProgressDialog = ProgressDialog.show(activity,
                "Please wait ...", "Connecting ...", true);
        ringProgressDialog.setCancelable(true);
        ringProgressDialog.setOnCancelListener(dialogInterface -> activity.stopDeviceScanning());

        //Thread thread = new Thread();
        new Thread(() -> {
            try {
                activity.startDeviceScanning();
            } catch (Exception e) {
                activity.runOnUiThread(() -> GeneralUtil.message(
                        "Cannot Scan for bluetooth Le device"));
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
            activity.runOnUiThread(() -> {
                if (ringProgressDialog!=null && ringProgressDialog.isShowing())
                    ringProgressDialog.dismiss();
                activity.stopDeviceScanning();
                if (ringProgressDialog!= null && !ringProgressDialog.isShowing()) {
                    if (devices.size() > 0) {
                        isFirstScan = false;
                        resizeForIncomingListOfDevice();
                        remoteDevices.clear();
                        remoteDevices.addAll(devices);
                        adapter.notifyDataSetChanged();
                        //discoveredDevices.
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
    public void selectDevicesToConnectTo(Devices device) {
        activity.stopDeviceScanning();
        selectedDev.add(device);
        binding.layoutDiscoveredDevices.buttonConnect
                .setText(getString(R.string.connect_device_s, selectedDev.size()));
    }

    @Override
    public Context getListenerContext() {
        return activity;
    }

}
