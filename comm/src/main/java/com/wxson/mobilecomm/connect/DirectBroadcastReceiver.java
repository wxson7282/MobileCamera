package com.wxson.mobilecomm.connect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wxson on 2018/3/3.
 * Package com.wxson.remote_camera.wifi_factory.
 */

public class DirectBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "DirectBroadcastReceiver";
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;
    private IDirectActionListener mDirectActionListener;

    public DirectBroadcastReceiver(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, IDirectActionListener directActionListener) {
        this.mWifiP2pManager = wifiP2pManager;
        this.mChannel = channel;
        this.mDirectActionListener = directActionListener;
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION );
        return intentFilter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action=intent.getAction();
        Log.e(TAG, "接收到广播： " + action);

        if (action != null) {
            switch (action) {
                // 空
                case "":
                    break;
                // 用于指示 Wifi P2P 是否可用
                case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION: {
                    int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                    if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                        mDirectActionListener.wifiP2pEnabled(true);
//                        mWifiP2pManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
//                            @Override
//                            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
//                                mDirectActionListener.onPeersAvailable(wifiP2pDeviceList.getDeviceList());
//                            }
//                        });
                    } else {
                        mDirectActionListener.wifiP2pEnabled(false);
                        List<WifiP2pDevice> wifiP2pDeviceList = new ArrayList<>();
                        mDirectActionListener.onPeersAvailable(wifiP2pDeviceList);
                    }
                    break;
                }
                // 对等节点列表发生了变化
                case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION: {
                    mWifiP2pManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
                        @Override
                        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                            mDirectActionListener.onPeersAvailable(wifiP2pDeviceList.getDeviceList());
                        }
                    });
                    break;
                }
                //peer discovery has either started or stopped.
                case WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION:{
                    int state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED);
                    //如果peer discovery已经结束
                    if (state == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED){
                        mDirectActionListener.onP2pDiscoveryStopped();
                    }
                    break;
                }
                // Wifi P2P 的连接状态发生了改变
                case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION: {
                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                    if (networkInfo.isConnected()) {
                        mWifiP2pManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                            @Override
                            public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
                                mDirectActionListener.onConnectionInfoAvailable(wifiP2pInfo);
                            }
                        });
                        Log.e(TAG, "已连接p2p设备");
                    } else {
                        mDirectActionListener.onDisconnection();
                        Log.e(TAG, "与p2p设备已断开连接");
                    }
                    break;
                }
                //本设备的设备信息发生了变化
                case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION: {
                    mDirectActionListener.onSelfDeviceAvailable((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
                    break;
                }
            }
        }
    }
}
