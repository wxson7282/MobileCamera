package com.wxson.mobilecomm.connect;

/**
 * Created by wxson on 2018/8/18.
 * Package com.wxson.mobilecomm.connect.
 * 监听连接状态变更
 */
public interface IWifiP2pConnectStatusListener {
    void onWifiP2pConnectStatusChanged(boolean wifiP2pConnected);
}
