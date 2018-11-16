package com.wxson.mobilecontroller;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.wxson.mobilecomm.connect.ByteBufferTransferTask;
import com.wxson.mobilecontroller.camera_manager.CameraManagerFragment;
import com.wxson.mobilecontroller.camera_manager.CameraManagerPresenter;
import com.wxson.mobilecontroller.camera_manager.ICameraManagerContract;
import com.wxson.mobilecontroller.connection.ConnectFragment;
import com.wxson.mobilecontroller.connection.ConnectPresenter;
import com.wxson.mobilecontroller.connection.ControllerWifiServerService;
import com.wxson.mobilecontroller.connection.IConnectionContract;
import com.wxson.mobilecontroller.home.HomeFragment;

import java.net.InetAddress;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity
        implements ConnectFragment.OnFragmentInteractionListener,
        CameraManagerFragment.OnFragmentInteractionListener,
        HomeFragment.OnFragmentInteractionListener,
        EasyPermissions.PermissionCallbacks{

    protected static final String TAG = "MainActivity";
    //当前显示的fragment的key
    private static final String STATE_FRAGMENT_SHOW = "STATE_FRAGMENT_SHOW";
    private FragmentManager mFragmentManager = getSupportFragmentManager();
    private Fragment mCurrentFragment;
    private ControllerWifiServerService mControllerWifiServerService;
//    private TextView mTextMessage;
    HomeFragment homeFragment;
    CameraManagerFragment cameraManagerFragment;
    ConnectFragment connectFragment;
    private IConnectionContract.IPresenterController mConnectPresenter;
    private ICameraManagerContract.IPresenter mCameraManagerPresenter;

    private ServiceConnection mServiceConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.e(TAG, "onServiceConnected");
            ControllerWifiServerService.MyBinder binder = (ControllerWifiServerService.MyBinder) iBinder;
            mControllerWifiServerService = binder.getService();
            mControllerWifiServerService.setStringTransferListener(mStringTransferListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e(TAG, "onServiceDisconnected");
            mControllerWifiServerService = null;
            bindService();
        }
    };

    private ControllerWifiServerService.StringTransferListener mStringTransferListener = new ControllerWifiServerService.StringTransferListener() {
        @Override
        public void onStringArrived(final String arrivedString, final InetAddress clientInetAddress) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //把客户端地址传给ByteBufferTransferTask
                    ByteBufferTransferTask.setInetAddress(clientInetAddress);
                    Log.e(TAG, "onStringArrived. clientInetAddress=" + clientInetAddress.getHostAddress());
                    switch (arrivedString){
                        case "connected":
//                            ivConnectStatus.setImageResource(R.drawable.ic_connected);
                            break;
                        case "disconnected":
//                            ivConnectStatus.setImageResource(R.drawable.ic_disconnected);
                            break;
                        case "openCamera":
                            //启动相机
                            //**********************************
                            //**********************************
                            break;
                        case "closeCamera":
//                            closeCamera();
                            break;
                    }
                }
            });
        }
    };

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    manageFragments(homeFragment.getClass().getName());
                    return true;
                case R.id.navigation_camera:
                    manageFragments(cameraManagerFragment.getClass().getName());
                    return true;
                case R.id.navigation_connect:
                    manageFragments(connectFragment.getClass().getName());
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //申请定位权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            requestLocationPermission();
        }

        //绑定WifiServerService
        bindService();
        //正常启动时调用
        homeFragment = HomeFragment.newInstance("","");
        cameraManagerFragment = CameraManagerFragment.newInstance("","");
        connectFragment = ConnectFragment.newInstance("","");

        //把全体Fragment加入到FragmentManager
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.add(R.id.controllerFLayout,homeFragment,homeFragment.getClass().getName()).show(homeFragment);
        transaction.add(R.id.controllerFLayout,cameraManagerFragment,cameraManagerFragment.getClass().getName()).hide(cameraManagerFragment);
        transaction.add(R.id.controllerFLayout,connectFragment,connectFragment.getClass().getName()).hide(connectFragment);
        transaction.commit();
        mCurrentFragment = homeFragment;
        sendFragmentIntent();

        // Create the presenter
        if (connectFragment!=null){
            mConnectPresenter = new ConnectPresenter(connectFragment, this);
        }
        if (cameraManagerFragment!=null)
            mCameraManagerPresenter = new CameraManagerPresenter(cameraManagerFragment);

//        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.connect, menu);
//        return true;
//    }

    @Override
    public void onFragmentInteraction(String text) {
        switch (text){
            case "capture":
                mConnectPresenter.startStringTransferTask("capture");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mControllerWifiServerService != null) {
            mControllerWifiServerService.setProgressChangListener(null);
            unbindService(mServiceConnection);
        }
        stopService(new Intent(this, ControllerWifiServerService.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequestPermissionsResult");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    //下面两个方法是实现EasyPermissions的EasyPermissions.PermissionCallbacks接口
    //分别返回授权成功和失败的权限
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.i(TAG, "onPermissionsGranted");
        Log.i(TAG, "获取权限成功" + perms);
        showToast("获取权限成功");
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.i(TAG, "onPermissionsDenied");
        Log.i(TAG, "获取权限失败，退出当前页面" + perms);
        showToast("获取权限失败");
        this.finish();  //退出当前页面
    }

    //requestCode
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private void requestLocationPermission(){
        Log.i(TAG, "requestLocationPermission");
        String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)){
            // Already have permission, do the thing
            Log.i(TAG, "已获取定位权限");
        }
        else{
            // Do not have permissions, request them now
            Log.i(TAG, "申请定位权限");
            EasyPermissions.requestPermissions(this, getString(R.string.location_rationale),
                    REQUEST_LOCATION_PERMISSION, perms);
        }
    }

    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    private void bindService() {
        Intent intent = new Intent(MainActivity.this, ControllerWifiServerService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        Log.e(TAG, "bindService mServiceConnection");
    }

    private void manageFragments(String savedFragmentName){
        List<Fragment> fragments = mFragmentManager.getFragments();
        int count = fragments.size();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        //遍历fragments
        if (TextUtils.isEmpty(savedFragmentName)){
            //遍历fragments
            for (int i=0; i<count; i++){
                if (i==0) {
                    transaction.show(fragments.get(i));
                    mCurrentFragment = fragments.get(i);
                }
                else {
                    transaction.hide(fragments.get(i));
                }
            }
        }
        else{
            //遍历fragments
            for (int i=0; i<count; i++){
                if (savedFragmentName.equals(fragments.get(i).getClass().getName())){
                    transaction.show(fragments.get(i));
                    mCurrentFragment = fragments.get(i);
                }
                else {
                    transaction.hide(fragments.get(i));
                }
            }
        }
        transaction.commit();
        sendFragmentIntent();
    }

    private void sendFragmentIntent(){
        Intent intent = new Intent();
        intent.setAction("com.wxson.mobilecontroller.FragmentChanged");
        String currentFragmentName;
        if (mCurrentFragment != null) currentFragmentName = mCurrentFragment.getClass().getSimpleName();
        else currentFragmentName = "";
        intent.putExtra("CurrentFragmentName", currentFragmentName);
        sendBroadcast(intent);
    }
}
