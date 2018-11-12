package com.wxson.mobilecontroller.camera_manager;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.wxson.mobilecontroller.connection.ControllerWifiServerService;

/**
 * Created by wxson on 2018/7/4.
 * Package com.wxson.mobilecontroller.camera_manager.
 */
public class RemoteTextureView extends TextureView implements TextureView.SurfaceTextureListener {
    private static final String TAG = "RemoteTextureView";
    private Context mContext;
    private MediaCodec mMediaCodec;

    public RemoteTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        this.setSurfaceTextureListener(this);
        this.setRotation(90);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        Log.i(TAG, "onSurfaceTextureAvailable...");
//        //设置视频解码器
//        setVideoDecoder(surfaceTexture);
        //注册首帧数据监听器
        registerFirstByteBufferListener();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        Log.i(TAG, "onSurfaceTextureSizeChanged...");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        Log.i(TAG, "onSurfaceTextureDestroyed...");
        //释放视频解码器
        MediaCodecAction.ReleaseDecoder(mMediaCodec);
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        Log.i(TAG, "onSurfaceTextureUpdated...");
    }

//    //设置视频解码器
//    private void setVideoDecoder(SurfaceTexture surfaceTexture) {
//        //准备解码器
//        Surface surface = new Surface(surfaceTexture);
//        mMediaCodec = MediaCodecAction.PrepareDecoder(surface);
//    }

    //注册首帧数据监听器
    public void registerFirstByteBufferListener() {
        ControllerWifiServerService.IFirstByteBufferListener firstByteBufferListener = new ControllerWifiServerService.IFirstByteBufferListener() {
            @Override
            public void onFirstByteBufferArrived(byte[] csd) {
                //准备解码器
                Surface surface = new Surface(RemoteTextureView.super.getSurfaceTexture());
                mMediaCodec = MediaCodecAction.PrepareDecoder(surface, csd);
                //启动解码器
                MediaCodecAction.StartDecoder(mMediaCodec);
                Log.i(TAG, "onFirstByteBufferArrived StartDecoder");
            }
        };
        ControllerWifiServerService.setFirstByteBufferListener(firstByteBufferListener);
    }

}
