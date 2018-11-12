package com.wxson.mobilecamera.activity;

import android.view.TextureView;

import com.wxson.mobilecomm.camera.AutoFitTextureView;
import com.wxson.mobilecomm.connect.IDirectActionListener;
import com.wxson.mobilecomm.connect.IWifiP2pConnectStatusListener;
import com.wxson.mobilecomm.mvp.IBasePresenter;
import com.wxson.mobilecomm.mvp.IBaseView;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by wxson on 2018/2/1.
 *
 */

public interface IMainContract {
    interface View extends IBaseView<Presenter>, EasyPermissions.PermissionCallbacks{
        //取得Activity
        MainActivity getActivity();
        //ProgressDialog setMessage
        void ProgressDialog_setMessage(String msg);
        //ProgressDialog setProgress
        void ProgressDialog_setProgress(int progress);
        //ProgressDialog show
        void ProgressDialog_show();
        //ProgressDialog cancel
        void ProgressDialog_cancel();
        //显示连接状态
        void showConnectStatus(boolean connected);
        //显示Toast
        void showToast(String msg);
        //显示LoadingDialog
        void showLoadingDialog(String msg);
        //解除LoadingDialog
        void dismissLoadingDialog();
        //请求相机权限
        void requestCameraPermission();
        //取得TextureView
        AutoFitTextureView getTextureView();
    }

    interface Presenter extends IBasePresenter, IDirectActionListener {
        //注入连接状态监听器
        void setWifiP2pConnectStatusListener(IWifiP2pConnectStatusListener wifiP2pConnectStatusListener);
        //unbindServiceConnection
        void unbindServiceConnection();
        //移除组
        void removeGroup();
        //建立组
        void createGroup();
        //打开相机
        void openCamera();
        //关闭相机
        void closeCamera();
        //注销广播Receiver
        void unregisterBroadcastReceiver();
        //SurfaceTextureListener
        TextureView.SurfaceTextureListener getSurfaceTextureListener();
        //拍照
        void captureStillPicture();

    }
}
