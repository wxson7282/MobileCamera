package com.wxson.mobilecontroller.camera_manager;

import android.annotation.SuppressLint;
import android.media.MediaCodec;

import static android.support.v4.util.Preconditions.checkNotNull;

/**
 * Created by wxson on 2018/5/30.
 * Package com.wxson.mobilecontroller.camera_manager.
 */
public class CameraManagerPresenter implements ICameraManagerContract.IPresenter {

    private static final String TAG = "CameraManagerPresenter";
    private ICameraManagerContract.IView mView;
    private MediaCodec mMediaCodec;

    @SuppressLint("RestrictedApi")
    public CameraManagerPresenter(ICameraManagerContract.IView view) {
        mView = checkNotNull(view);
        mView.setPresenter(this);
    }

    @Override
    public void start() {
    }

}

