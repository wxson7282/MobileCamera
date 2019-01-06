package com.wxson.mobilecamera.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.wxson.mobilecamera.R;
import com.wxson.mobilecamera.connect.CameraWifiServerService;
import com.wxson.mobilecamera.connect.MessageDialog;
import com.wxson.mobilecomm.camera.AutoFitTextureView;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

import static android.support.v4.util.Preconditions.checkNotNull;

public class MainActivity extends BaseActivity
        implements View.OnClickListener, IMainContract.View{

    protected static final String TAG = "MainActivity";
    private IMainContract.Presenter mPresenter;
    private ImageView ivConnectStatus;
    private ProgressDialog mProgressDialog;

    //region private member for camera
    //定义内容窗口
    private AutoFitTextureView mTextureView;
    //requestCode
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    //endregion


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //建立mPresenter
        mPresenter = new MainPresenter(this, this);
        ivConnectStatus = (ImageView) findViewById(R.id.ivConnectStatus_GroupServer);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setTitle("正在接收文件");
        mProgressDialog.setMax(100);
        //取得TextureView组件
        mTextureView = (AutoFitTextureView) findViewById(R.id.texture);
        // 为该组件设置监听器
        mTextureView.setSurfaceTextureListener(mPresenter.getSurfaceTextureListener());
        //注册按钮监听器
        ((Button)findViewById(R.id.btnCreateGroup)).setOnClickListener(this);
        ((Button)findViewById(R.id.btnDeleteGroup)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.btnCapture)).setOnClickListener(this);
        //首次运行时设置默认值
        PreferenceManager.setDefaultValues(this, R.xml.pref_codec, false);
        //启动Presenter
        mPresenter.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()){
            case R.id.menuSystemSet:{
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            }
            default:{
                break;
            }
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mPresenter.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.closeCamera();
        mPresenter.unbindServiceConnection();
        mPresenter.unregisterBroadcastReceiver();
        mPresenter.removeGroup();
        stopService(new Intent(this, CameraWifiServerService.class));
        Log.i(TAG, "onDestroy");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnCreateGroup:
                mPresenter.createGroup();
                break;
            case R.id.btnDeleteGroup:
                mPresenter.removeGroup();
                break;
            case R.id.btnCapture:
                mPresenter.captureStillPicture();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        final MessageDialog messageDialog = new MessageDialog();
        messageDialog.show(null, "退出当前界面将取消文件传输，是否确认退出？", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                messageDialog.dismiss();
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    MainActivity.super.onBackPressed();
                }
            }
        }, getSupportFragmentManager());
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setPresenter(IMainContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public MainActivity getActivity() {
        return this;
    }

    @Override
    public void showToast(String msg) {
        super.showToast(msg);
    }

    @Override
    public void showLoadingDialog(String msg) {
        super.showLoadingDialog(msg);
    }

    @Override
    public void dismissLoadingDialog() {
        super.dismissLoadingDialog();
    }

    @Override
    public void showConnectStatus(boolean connected) {
        if (connected){
            ivConnectStatus.setImageResource(R.drawable.ic_connected);
        }
        else{
            ivConnectStatus.setImageResource(R.drawable.ic_disconnected);
        }
    }

    @Override
    public void ProgressDialog_setMessage(String msg) {
        mProgressDialog.setMessage(msg);
    }

    @Override
    public void ProgressDialog_setProgress(int progress) {
        mProgressDialog.setProgress(progress);
    }

    @Override
    public void ProgressDialog_show() {
        mProgressDialog.show();
    }

    @Override
    public void ProgressDialog_cancel() {
        mProgressDialog.cancel();
    }

    @Override
    public void requestCameraPermission() {
        Log.i(TAG, "requestCameraPermission");
        String[] perms = {Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            Log.i(TAG, "已获取相机权限");
            //打开相机
            mPresenter.openCamera();
        } else {
            // Do not have permissions, request them now
            Log.i(TAG, "申请相机权限");
            EasyPermissions.requestPermissions(this, getString(R.string.camera_rationale),
                    REQUEST_CAMERA_PERMISSION, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    //下面两个方法是实现EasyPermissions的EasyPermissions.PermissionCallbacks接口
    //分别返回授权成功和失败的权限
    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.i(TAG, "onPermissionsGranted");
        Log.i(TAG, "获取权限成功" + perms);
        showToast("获取权限成功");
        //打开相机
        mPresenter.openCamera();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.i(TAG, "onPermissionsDenied");
        Log.i(TAG, "获取权限失败，退出当前页面" + perms);
        showToast("获取权限失败");
        this.finish();  //退出当前页面
    }

    @Override
    public AutoFitTextureView getTextureView() {
        return mTextureView;
    }
}
