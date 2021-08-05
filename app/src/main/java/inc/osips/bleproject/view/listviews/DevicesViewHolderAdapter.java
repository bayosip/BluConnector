package inc.osips.bleproject.view.listviews;

import android.content.Context;
import android.util.Log;
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

public class DevicesViewHolderAdapter extends RecyclerView.Adapter<DevicesViewHolder> implements DevicesViewHolder.RefreshItem {

    private static final String TAG = "DevicesVHAdapter";
    List<Devices> discoveredDevices;
    List<String> discoveredServices;
    Context context;
    OnDiscoveredDevicesClickListener itemListener;
    ServiceSelectorListener listener;
    boolean isListOfServices = false;
    private int selectedPos = RecyclerView.NO_POSITION;

    @Override
    public void setSelectedPosition(int position) {
        selectedPos = position;
    }

    public DevicesViewHolderAdapter(List<Devices> discoveredDevices, OnDiscoveredDevicesClickListener listener) {
        this.discoveredDevices = discoveredDevices;
        this.context = listener.getListenerContext();
        this.itemListener = listener;
    }

    public DevicesViewHolderAdapter(List<String> discoveredServices, ServiceSelectorListener listener){
        isListOfServices = true;
        this.discoveredServices = discoveredServices;
        this.listener = listener;
        this.context = listener.getListenerContext();
    }

    @NonNull
    @Override
    public DevicesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.device_list_item,parent, false);
        DevicesViewHolder holder;
        if(isListOfServices){
            holder =  new DevicesViewHolder(view, this);
            holder.setServiceSelection(listener);
        }else {
            holder =  new DevicesViewHolder(view);
            holder.setDeviceListener(itemListener);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull DevicesViewHolder holder, int position) {
        if(isListOfServices) {
            holder.setServiceItems(discoveredServices);
            Log.w(TAG, "onBindViewHolder: pos - " + position + " vs selectedPos - "+ selectedPos  );
            holder.changeItemBackground(position == selectedPos);
        }else {
            holder.setItems(discoveredDevices);
        }
    }

    @Override
    public int getItemCount() {
        return isListOfServices? discoveredServices.size():discoveredDevices.size();
    }
}
