package com.wxson.mobilecontroller.connection;

import android.net.wifi.p2p.WifiP2pDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wxson.mobilecontroller.R;

import java.util.List;

import static com.wxson.mobilecontroller.connection.ConnectPresenter.getDeviceStatus;

/**
 * Created by wxson on 2018/3/4.
 * Package com.wxson.remote_camera.connection.
 */

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    private List<WifiP2pDevice> wifiP2pDeviceList;
    private OnClickListener clickListener;

    public interface OnClickListener {
        void onItemClick(int position);
    }

    public DeviceAdapter(List<WifiP2pDevice> wifiP2pDeviceList) {
        this.wifiP2pDeviceList = wifiP2pDeviceList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.onItemClick((Integer) v.getTag());
                }
            }
        });
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.tvDeviceName.setText(wifiP2pDeviceList.get(position).deviceName);
        holder.tvDeviceAddress.setText(wifiP2pDeviceList.get(position).deviceAddress);
        holder.tvDeviceDetails.setText(getDeviceStatus(wifiP2pDeviceList.get(position).status));
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return wifiP2pDeviceList.size();
    }

    public void setClickListener(OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvDeviceName;
        private TextView tvDeviceAddress;
        private TextView tvDeviceDetails;

        ViewHolder(View itemView) {
            super(itemView);
            tvDeviceName = (TextView) itemView.findViewById(R.id.tvDeviceName);
            tvDeviceAddress = (TextView) itemView.findViewById(R.id.tvDeviceAddress);
            tvDeviceDetails = (TextView) itemView.findViewById(R.id.tvDeviceDetails);
        }
    }

}
