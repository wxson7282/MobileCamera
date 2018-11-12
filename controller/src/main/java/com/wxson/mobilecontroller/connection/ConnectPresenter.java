package com.wxson.mobilecontroller.connection;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.wxson.mobilecomm.connect.DirectBroadcastReceiver;
import com.wxson.mobilecomm.connect.FileTransfer;
import com.wxson.mobilecontroller.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.os.Looper.getMainLooper;
import static android.support.v4.util.Preconditions.checkNotNull;

/**
 * Created by wxson on 2018/4/6.
 * Package com.wxson.mobilecontroller.connection.
 */
public class ConnectPresenter implements IConnectionContract.IPresenterController {

    static final String TAG = "ConnectPresenter";
    private BroadcastReceiver mBroadcastReceiver;
    private WifiP2pInfo mWifiP2pInfo;
    private Context mContext;
    private IConnectionContract.IView mView;
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;
    private boolean mWifiP2pEnabled = false;
    private LoadingDialog mLoadingDialog;
    private List<WifiP2pDevice> mWifiP2pDeviceList;
    private DeviceAdapter mDeviceAdapter;
    private WifiP2pDevice mWifiP2pDevice;
//    private int mWifiP2pDeviceListSize;

    @SuppressLint("RestrictedApi")
    public ConnectPresenter(@NonNull IConnectionContract.IView view, Context context) {
        mView = checkNotNull(view);
        mView.setPresenter(this);
        mContext = context;
    }

    @Override
    public void start() {
        mWifiP2pManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mWifiP2pManager.initialize(mContext, getMainLooper(), this);
//        registerBroadcastReceiver();
        mLoadingDialog = new LoadingDialog(mContext);
        mWifiP2pDeviceList = new ArrayList<>();
        mDeviceAdapter = new DeviceAdapter(mWifiP2pDeviceList);
        mDeviceAdapter.setClickListener(new DeviceAdapter.OnClickListener() {
            @Override
            public void onItemClick(int position) {
                mWifiP2pDevice = mWifiP2pDeviceList.get(position);
                mView.showToast(mWifiP2pDeviceList.get(position).deviceName);
                connect();
            }
        });
//        RecyclerView rv_deviceList = (RecyclerView) mContext.findViewById(R.id.rvDeviceList);
        RecyclerView rv_deviceList = mView.getRvDeviceList();
        rv_deviceList.setAdapter(mDeviceAdapter);
        rv_deviceList.setLayoutManager(new LinearLayoutManager(mContext));

    }

    @Override
    public void startFileTransferTask(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                if (uri != null) {
                    String path = getPath(mContext, uri);
                    if (path != null) {
                        File file = new File(path);
                        if (file.exists() && mWifiP2pInfo != null) {
                            mView.showFileList(file.getName());
                            FileTransfer fileTransfer = new FileTransfer(file.getPath(), file.length());
                            Log.e(TAG, "待发送的文件：" + fileTransfer);
                            new FileTransferTask_controller(mContext, fileTransfer).execute(mWifiP2pInfo.groupOwnerAddress.getHostAddress());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void startWifiSetting() {
        if (mWifiP2pManager != null && mChannel != null) {
            mContext.startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
        } else {
            mView.showToast("当前设备不支持Wifi Direct");
        }
    }

    @Override
    public void startDiscoverPeers() {
        if (!mWifiP2pEnabled) {
            mView.showToast("需要先打开Wifi");
            return;
        }
        mLoadingDialog.show("正在搜索附近设备", true, false);
        mWifiP2pDeviceList.clear();
        mDeviceAdapter.notifyDataSetChanged();
        //搜寻附近带有 Wi-Fi P2P 的设备
        mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                mView.showToast("Success");
            }

            @Override
            public void onFailure(int reasonCode) {
                mView.showToast("Failure");
                mLoadingDialog.cancel();
            }
        });

    }

    @Override
    public void wifiP2pEnabled(boolean enabled) {
        mWifiP2pEnabled = enabled;
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        mView.dismissLoadingDialog();
        mWifiP2pDeviceList.clear();
        mDeviceAdapter.notifyDataSetChanged();
        mView.setBtnDisconnect(true);
        mView.setBtnChooseFile(true);
        mView.setBtnSend(true);
        Log.e(TAG, "onConnectionInfoAvailable");
        Log.e(TAG, "onConnectionInfoAvailable groupFormed: " + wifiP2pInfo.groupFormed);
        Log.e(TAG, "onConnectionInfoAvailable isGroupOwner: " + wifiP2pInfo.isGroupOwner);
        Log.e(TAG, "onConnectionInfoAvailable getHostAddress: " + wifiP2pInfo.groupOwnerAddress.getHostAddress());
        StringBuilder stringBuilder = new StringBuilder();
        if (mWifiP2pDevice != null) {
            stringBuilder.append("连接的设备名：");
            stringBuilder.append(mWifiP2pDevice.deviceName);
            stringBuilder.append("\n");
            stringBuilder.append("连接的设备的地址：");
            stringBuilder.append(mWifiP2pDevice.deviceAddress);
        }
        stringBuilder.append("\n");
        stringBuilder.append("本设备是否群主：");
        stringBuilder.append(wifiP2pInfo.isGroupOwner ? "是群主" : "非群主");
        stringBuilder.append("\n");
        stringBuilder.append("群主IP地址：");
        stringBuilder.append(wifiP2pInfo.groupOwnerAddress.getHostAddress());
        mView.showStatus(stringBuilder.toString());
        if (wifiP2pInfo.groupFormed && !wifiP2pInfo.isGroupOwner) {
            mWifiP2pInfo = wifiP2pInfo;
            //向对方发送连接成功消息
            final int PORT = mContext.getResources().getInteger(R.integer.portNumberA);
            new StringTransferTask(mWifiP2pInfo.groupOwnerAddress.getHostAddress(), PORT, "connected").start();
        }
        //启动WifiServerService
        mContext.startService(new Intent(mContext, ControllerWifiServerService.class));
        Log.i(TAG, "onConnectionInfoAvailable startService ControllerWifiServerService");
    }

    @Override
    public void onDisconnection() {
        Log.e(TAG, "onDisconnection");
        mView.setBtnDisconnect(false);
        mView.setBtnChooseFile(false);
        mView.setBtnSend(false);
        mView.showToast("已断开连接");
        mWifiP2pDeviceList.clear();
        mDeviceAdapter.notifyDataSetChanged();
        mView.showStatus(null);
        mWifiP2pInfo = null;
//        ivConnectStatus.setImageResource(R.drawable.ic_disconnected);
        mView.showConnectStatus(false);

    }

    @Override
    public void onSelfDeviceAvailable(WifiP2pDevice wifiP2pDevice) {
        Log.i(TAG, "onSelfDeviceAvailable");
        Log.i(TAG, "DeviceName: " + wifiP2pDevice.deviceName);
        Log.i(TAG, "DeviceAddress: " + wifiP2pDevice.deviceAddress);
        Log.i(TAG, "Status: " + wifiP2pDevice.status);
//        tvMyDeviceName.setText(wifiP2pDevice.deviceName);
        mView.showMyDeviceName(wifiP2pDevice.deviceName);
//        tvMyDeviceAddress.setText(wifiP2pDevice.deviceAddress);
        mView.showMyDeviceAddress(wifiP2pDevice.deviceAddress);
//        tvMyDeviceStatus.setText(getDeviceStatus(wifiP2pDevice.status));
        mView.showMyDeviceStatus(getDeviceStatus(wifiP2pDevice.status));

    }

    @Override
    public void onPeersAvailable(Collection<WifiP2pDevice> wifiP2pDeviceList) {
        Log.i(TAG, "onPeersAvailable :" + wifiP2pDeviceList.size());
        mWifiP2pDeviceList.clear();
        mWifiP2pDeviceList.addAll(wifiP2pDeviceList);
        mDeviceAdapter.notifyDataSetChanged();
        mLoadingDialog.cancel();
        if (mWifiP2pDeviceList.size() == 0){
            Log.e(TAG, "No devices found");
        }
    }

    @Override
    public void onP2pDiscoveryStopped() {
        Log.i(TAG, "onP2pDiscoveryStopped");
        if (mWifiP2pDeviceList.size() == 0){
            ///再度搜寻附近带有 Wi-Fi P2P 的设备
            startDiscoverPeers();
        }
    }

    @Override
    public void onChannelDisconnected() {
        Log.e(TAG, "onChannelDisconnected");
    }

    @Override
    public void connect() {
        final WifiP2pConfig config = new WifiP2pConfig();
        if (config.deviceAddress != null && mWifiP2pDevice != null) {
            config.deviceAddress = mWifiP2pDevice.deviceAddress;
            config.wps.setup = WpsInfo.PBC;
            mView.showLoadingDialog("正在连接 " + mWifiP2pDevice.deviceName);
            mWifiP2pManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
//                    ivConnectStatus.setImageResource(R.drawable.ic_connected);
                    mView.showConnectStatus(true);
                    Log.e(TAG, "connect onSuccess");
                }

                @Override
                public void onFailure(int reason) {
//                    ivConnectStatus.setImageResource(R.drawable.ic_disconnected);
                    mView.showConnectStatus(false);
                    mView.showToast("连接失败 " + reason);
                    mView.dismissLoadingDialog();
                }
            });
        }
    }

    @Override
    public void disconnect() {
        //向对方发送连接断开消息
        if (mWifiP2pInfo != null && !mWifiP2pInfo.isGroupOwner){
            final int PORT = mContext.getResources().getInteger(R.integer.portNumberA);
            //必须阻塞UI线程，UNTIL消息发送成功，否则连接切断后，消息就发不出了
            StringTransferTask stringTransferTask = new StringTransferTask(mWifiP2pInfo.groupOwnerAddress.getHostAddress(), PORT, "disconnected");
            stringTransferTask.start();
            try{
                stringTransferTask.join();
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        mWifiP2pManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onFailure(int reasonCode) {
                Log.e(TAG, "disconnect onFailure:" + reasonCode);
            }

            @Override
            public void onSuccess() {
                Log.e(TAG, "disconnect onSuccess");
                mView.showStatus(null);
                mView.setBtnDisconnect(false);
                mView.setBtnChooseFile(false);
                mView.setBtnSend(false);
            }
        });
    }

    @Override
    public void registerBroadcastReceiver() {
        mBroadcastReceiver = new DirectBroadcastReceiver(mWifiP2pManager, mChannel, this);
        mContext.registerReceiver(mBroadcastReceiver, DirectBroadcastReceiver.getIntentFilter());
    }

    @Override
    public void unregisterBroadcastReceiver() {
        mContext.unregisterReceiver(mBroadcastReceiver);
        Log.e(TAG, "unregisterBroadcastReceiver");
    }


    @Override
    public void startStringTransferTask(String sendString){
        if (mWifiP2pInfo != null && !mWifiP2pInfo.isGroupOwner){
            final int PORT = mContext.getResources().getInteger(R.integer.portNumberA);
            StringTransferTask stringTransferTask = new StringTransferTask(mWifiP2pInfo.groupOwnerAddress.getHostAddress(), PORT, sendString);
            stringTransferTask.start();
        }
    }
//************************ private method *********************************************
    /**
     *
     * @param context Current context.
     * @param uri The Uri locating resource.
     * @return The file path.
     */
    private String getPath(Context context, Uri uri) {
        if (isMediaProvider(uri)) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];
            Uri contentUri = null;
            if ("image".equals(type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if ("video".equals(type)) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if ("audio".equals(type)) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }
            final String selection = "_id=?";
            final String[] selectionArgs = new String[]{ split[1] };

            return getDataColumn(context, contentUri, selection, selectionArgs);
        }
        else if (isContentScheme(uri)){
            return getDataColumn(context, uri, null, null);
        }
        else if (isFileScheme(uri)){
            return uri.getPath();
        }
        else{
            return null;
        }
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaProvider(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     *
     * @param uri The Uri to check.
     * @return Whether the Uri scheme is Content.
     */
    private static boolean isContentScheme(Uri uri){
        return "content".equalsIgnoreCase(uri.getScheme());
    }

    /**
     *
     * @param uri The Uri to check.
     * @return Whether the Uri scheme is File.
     */
    private static boolean isFileScheme(Uri uri){
        return "file".equalsIgnoreCase(uri.getScheme());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {
        String returnValue = null;
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                returnValue = cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return returnValue;
    }

    static String getDeviceStatus(int deviceStatus) {
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "可用的";
            case WifiP2pDevice.INVITED:
                return "邀请中";
            case WifiP2pDevice.CONNECTED:
                return "已连接";
            case WifiP2pDevice.FAILED:
                return "失败的";
            case WifiP2pDevice.UNAVAILABLE:
                return "不可用的";
            default:
                return "未知";
        }
    }
//************************ private method *********************************************
}
