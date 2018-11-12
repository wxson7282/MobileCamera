package com.wxson.mobilecontroller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * Created by wxson on 2018/6/5.
 * Package com.wxson.mobilecontroller.
 */
public class FragmentBroadcastReceiver extends BroadcastReceiver {

    private String TAG = "FragmentBroadcastReceiver";
    private final static String FRAGMENT_CHANGED_ACTION = "com.wxson.mobilecontroller.FragmentChanged";
    private IFragmentChangedListener mFragmentChangedListener;

    public FragmentBroadcastReceiver(IFragmentChangedListener fragmentChangedListener) {
        mFragmentChangedListener = fragmentChangedListener;
    }

    public static IntentFilter getIntentFilter(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FRAGMENT_CHANGED_ACTION);
        return intentFilter;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action=intent.getAction();
        Log.e(TAG, "接收到广播： " + action);
        if (action != null){
            switch (action){
                // 空
                case "":
                    break;
                case FRAGMENT_CHANGED_ACTION:
                    //获取当前fragment名称
                    String currentFragmentName = intent.getStringExtra("CurrentFragmentName");
                    mFragmentChangedListener.onFragmentChanged(currentFragmentName);
                    break;
            }
        }
    }
}
