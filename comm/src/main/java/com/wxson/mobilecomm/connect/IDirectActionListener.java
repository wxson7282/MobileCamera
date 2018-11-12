package com.wxson.mobilecomm.connect;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import java.util.Collection;

/**
 * Created by wxson on 2018/3/3.
 * Package com.wxson.remote_camera.wifi_factory.
 */

public interface IDirectActionListener extends WifiP2pManager.ChannelListener {

    void wifiP2pEnabled(boolean enabled);

    void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo);

    void onDisconnection();

    void onSelfDeviceAvailable(WifiP2pDevice wifiP2pDevice);

    void onPeersAvailable(Collection<WifiP2pDevice> wifiP2pDeviceList);

    void onP2pDiscoveryStopped();

}
