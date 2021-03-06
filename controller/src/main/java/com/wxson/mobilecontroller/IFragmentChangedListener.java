package com.wxson.mobilecontroller;

/**
 * Created by wxson on 2018/6/6.
 * Package com.wxson.mobilecontroller.
 */
public interface IFragmentChangedListener {
    //监听Fragment变更
    void onFragmentChanged(String fragmentName);
    //注册广播监听器
    void registerFragmentBroadcastReceiver();
    //撤销广播监听器
    void unregisterFragmentBroadcastReceiver();
}
