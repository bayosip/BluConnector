package inc.osips.bleproject.view.listviews;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import inc.osips.bleproject.R;
import inc.osips.bleproject.interfaces.OnDiscoveredDevicesClickListener;
import inc.osips.bleproject.interfaces.ServiceSelectorListener;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.Devices;
import inc.osips.bleproject.utilities.GeneralUtil;

public class DevicesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


    private View layout;
    private TextView deviceName, deviceAddress;
    private OnDiscoveredDevicesClickListener listener;
    ServiceSelectorListener listener1;
    private List<Devices> devices;
    private boolean isRemoteServices = false;
    private RefreshItem refresh;
    private Context context;

    interface RefreshItem {
        void setSelectedPosition(int position);
    }

    public DevicesViewHolder(@NonNull View itemView) {
        super(itemView);
        initialiseWidgets(itemView);
    }

    public DevicesViewHolder(@NonNull View itemView, RefreshItem refreshItem) {
        super(itemView);
        isRemoteServices = true;
        initialiseWidgets(itemView);
        this.refresh = refreshItem;
    }

    public void setItems(List<Devices> discoveredDevices){
        this.devices = discoveredDevices;
        deviceName.setText(discoveredDevices.get(getAdapterPosition()).getDeviceName());
        deviceAddress.setText(discoveredDevices.get(getAdapterPosition()).getDeviceAddress());
    }
    public void setServiceItems(List<String> discoveredServices){
        deviceName.setText(discoveredServices.get(getAdapterPosition()));
    }

    public void setDeviceListener(OnDiscoveredDevicesClickListener listener) {
        this.listener = listener;
        context = listener.getListenerContext();
    }

    public void setServiceSelection(ServiceSelectorListener listener){
        this.listener1 = listener;
        context = listener1.getListenerContext();
    }

    private void initialiseWidgets(View v){
        layout =  v.findViewById(R.id.layoutItem);
        layout.setOnClickListener(this);
        deviceName = v.findViewById(R.id.textDeviceName);
        deviceAddress = v.findViewById(R.id.textDeviceAddress);
    }

    public void changeItemBackground(boolean shouldChange) {
        if (shouldChange){
            layout.setBackgroundResource(R.color.ripple);
            deviceName.setTextColor(Color.WHITE);
        }
        else {
            layout.setBackgroundResource(R.color.app_background_white);
            deviceName.setTextColor(ContextCompat.getColor(listener1.getListenerContext(),
                    R.color.title_color));
        }
    }

    @Override
    public void onClick(View view) {
        if(isRemoteServices){
            refresh.setSelectedPosition(getAbsoluteAdapterPosition());
            listener1.selectAServiceWith(getAbsoluteAdapterPosition());
        }else {
            if (getAbsoluteAdapterPosition() < devices.size())
                listener.selectDevicesToConnectTo(devices.get(getAbsoluteAdapterPosition()));

            GeneralUtil.getHandler().postDelayed(() -> deviceName.setTextColor(ContextCompat.getColor(context, R.color.title_color)), 200);
        }
    }
}
