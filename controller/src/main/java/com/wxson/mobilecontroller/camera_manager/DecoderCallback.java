package com.wxson.mobilecontroller.camera_manager;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.support.annotation.NonNull;
import android.util.Log;

import com.wxson.mobilecontroller.connection.ControllerWifiServerService;

import java.nio.ByteBuffer;

/**
 * Created by wxson on 2018/5/22.
 * Package com.wxson.mobilecontroller.camera_manager.
 */
public class DecoderCallback {
    private static final String TAG = "DecoderCallback";
    private byte[] mInputData;
    private MediaCodec.BufferInfo mBufferInfo;
    private boolean mInputDataReady = false;

    public DecoderCallback() {
        //注册输入数据准备好监听器
        ControllerWifiServerService.IInputDataReadyListener inputDataReadyListener = new ControllerWifiServerService.IInputDataReadyListener(){
            @Override
            public void onInputDataReady(byte[] inputData, MediaCodec.BufferInfo bufferInfo) {
                //如果输入数据没有处理完(mInputDataReady==true)，则丢弃新来的数据，没有缓冲区
                if (!mInputDataReady){
                    Log.e(TAG, "onInputDataReady get one frame");
                    mInputDataReady = true;
                    mInputData = inputData;
                    mBufferInfo = bufferInfo;
                }
                else{
                    Log.e(TAG, "onInputDataReady lost one frame");
                }
            }
        };
        ControllerWifiServerService.setInputDataReadyListener(inputDataReadyListener);
    }

    private MediaCodec.Callback mCallback = new MediaCodec.Callback(){
        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec mediaCodec, int inputBufferId) {
//            Log.e(TAG, "onInputBufferAvailable");
            try {
                ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferId);
                if (inputBuffer != null){
                    int length = 0;
                    long timeStamp = 0;
                    inputBuffer.clear();
                    if (mInputDataReady){
//                        inputBuffer.clear();
                        //如果输入数据准备好，注入解码器
                        length = mBufferInfo.size;
                        timeStamp = mBufferInfo.presentationTimeUs;
                        inputBuffer.put(mInputData, 0, length);
                        mInputDataReady = false;
                        Log.e(TAG, "输入数据注入解码器");
                    }
                    //把inputBuffer放回队列
                    mediaCodec.queueInputBuffer(inputBufferId,0, length, timeStamp,0);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec mediaCodec, int outputBufferId, @NonNull MediaCodec.BufferInfo bufferInfo) {
            Log.e(TAG, "onOutputBufferAvailable");
//            ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferId);
            mediaCodec.releaseOutputBuffer(outputBufferId, true);
        }

        @Override
        public void onError(@NonNull MediaCodec mediaCodec, @NonNull MediaCodec.CodecException e) {
            Log.e(TAG, "onError");
        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec mediaCodec, @NonNull MediaFormat mediaFormat) {
            Log.e(TAG, "onOutputFormatChanged");
        }
    };

    public MediaCodec.Callback getCallback(){
        return mCallback;
    }

}
