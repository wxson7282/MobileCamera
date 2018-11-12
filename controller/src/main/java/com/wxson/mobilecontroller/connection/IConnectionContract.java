package com.wxson.mobilecontroller.connection;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;

import com.wxson.mobilecomm.connect.IDirectActionListener;
import com.wxson.mobilecomm.mvp.IBasePresenter;
import com.wxson.mobilecomm.mvp.IBaseView;

/**
 * Created by wxson on 2018/4/5.
 * Package com.wxson.mobilecontroller.connection.
 */
public interface IConnectionContract {
    interface IView extends IBaseView<IPresenterController> {
        //显示进度条
        void showLoadingDialog(String message);
        //关闭进度条
        void dismissLoadingDialog();
        //显示Toast
        void showToast(String message);
        //在Presenter中数据回调的方法中, 先检查View.isActive()是否为true, 来保证对Fragment的操作安全
        boolean isActive();
        //显示FileList
        void showFileList(String fileName);
        //设定btnDisconnect
        void setBtnDisconnect(boolean enabled);
        //设定btnChooseFile
        void setBtnChooseFile(boolean enabled);
        //设定btnSend
        void setBtnSend(boolean enabled);
        //显示本地设备连接状态tvStatus
        void showStatus(String status);
        //显示连接状态
        void showConnectStatus(boolean connected);
        //显示本地设备名
        void showMyDeviceName(String deviceName);
        //显示本地设备地址
        void showMyDeviceAddress(String deviceAddress);
        //显示本地设备状态
        void showMyDeviceStatus(String deviceStatus);
        //获得设备清单控件
        RecyclerView getRvDeviceList();
    }
    interface IPresenterController extends IBasePresenter, IDirectActionListener {
        //根据系统文件选择器的回调，启动FileTransferTask
        void startFileTransferTask(int requestCode, int resultCode, Intent data);
        //启动系统wifi设置
        void startWifiSetting();
        //启动搜索附近设备
        void startDiscoverPeers();
        //连接
        void connect();
        //断开连接
        void disconnect();
        //注册广播监听器
        void registerBroadcastReceiver();
        //撤销广播监听器
        void unregisterBroadcastReceiver();
        //启动发送字符串任务
        void startStringTransferTask(String sendString);
        }
}
