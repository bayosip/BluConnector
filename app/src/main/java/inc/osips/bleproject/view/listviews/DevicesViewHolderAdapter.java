package inc.osips.bleproject.view.listviews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import inc.osips.bleproject.R;
import inc.osips.bleproject.interfaces.OnDiscoveredDevicesClickListener;
import inc.osips.bleproject.interfaces.ServiceSelectorListener;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.Devices;

public class DevicesViewHolderAdapter extends RecyclerView.Adapter<DevicesViewHolder> {

    List<Devices> discoveredDevices;
    List<String> discoveredServices;
    Context context;
    OnDiscoveredDevicesClickListener itemListener;
    ServiceSelectorListener listener;
    boolean isListOfServices = false;


    public DevicesViewHolderAdapter(List<Devices> discoveredDevices, OnDiscoveredDevicesClickListener listener) {
        this.discoveredDevices = discoveredDevices;
        this.context = listener.getListenerContext();
        this.itemListener = listener;
    }

    public DevicesViewHolderAdapter(List<String> discoveredServices, ServiceSelectorListener listener){
        isListOfServices = true;
        this.discoveredServices = discoveredServices;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DevicesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.device_list_item,parent, false);
        DevicesViewHolder holder =  new DevicesViewHolder(view);
        if(isListOfServices){
            holder.setServiceSelection(listener);
        }else {
            holder.setDeviceListener(itemListener);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull DevicesViewHolder holder, int position) {
        if(isListOfServices) {
            holder.setServiceItems(discoveredServices);
        }else {
            holder.setItems(discoveredDevices);
        }
    }

    @Override
    public int getItemCount() {
        return discoveredDevices.size();
    }
}
