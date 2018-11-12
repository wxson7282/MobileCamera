package com.wxson.mobilecontroller.camera_manager;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Surface;

import com.wxson.mobilecontroller.connection.ControllerWifiServerService;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by wxson on 2018/5/25.
 * Package com.wxson.mobilecontroller.camera_manager.
 */
public class VideoDecodeTask extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = "VideoDecodeTask";
    private MediaCodec mMediaCodec;
    private Surface mOutputSurface;
    private byte[] mInputData;
    private MediaCodec.BufferInfo mBufferInfo;
    private int mInputNotReadyCount;
    private static final int MAX_INPUT_NOT_READY_COUNT = 500000;
    private boolean mInputDataReady = false;

    public VideoDecodeTask(Surface surface) {
        mOutputSurface = surface;
        //注册输入数据准备好监听器
        ControllerWifiServerService.IInputDataReadyListener inputDataReadyListener = new ControllerWifiServerService.IInputDataReadyListener(){
            @Override
            public void onInputDataReady(byte[] inputData, MediaCodec.BufferInfo bufferInfo) {
                //如果输入数据没有处理完，则丢弃新来的数据，没有缓冲区
                if (!mInputDataReady){
                    Log.e(TAG, "onInputDataReady");
                    mInputDataReady = true;
                    mInputData = inputData;
                    mBufferInfo = bufferInfo;
                }
            }
        };
        ControllerWifiServerService.setInputDataReadyListener(inputDataReadyListener);
    }

    @Override
    protected void onPreExecute() {
        Log.e(TAG, "onPreExecute");
        super.onPreExecute();
        mInputNotReadyCount = 0;
        //编码类型
        String mime = "video/avc";      //H264
        try {
            mMediaCodec = MediaCodec.createDecoderByType(mime);
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(mime, 640, 480);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 1300 * 1000);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 25);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);
            mMediaCodec.configure(mediaFormat, mOutputSurface, null, 0);
            Log.e(TAG, "onPreExecute mMediaCodec.configure");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        Log.e(TAG, "onPostExecute");
        mMediaCodec.stop();
        mMediaCodec.release();
        super.onPostExecute(aBoolean);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        Log.e(TAG, "doInBackground");
        try{
            long BUFFER_TIMEOUT = 0;
//            long BUFFER_TIMEOUT = -1;   //无限期等待
            mMediaCodec.start();
            ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
            //循环等待编码帧数据到达 且缓冲区准备好
            for (;;){
                if (mInputDataReady){
                    //处理输入数据
                    Log.e(TAG, "doInBackground InputDataReady");
                    int inputBufferId = mMediaCodec.dequeueInputBuffer(BUFFER_TIMEOUT);
                    Log.e(TAG, "inputBufferId=" + inputBufferId);
                    if (inputBufferId >= 0){
                        Log.e(TAG, "doInBackground inputBuffer ready");
                        ByteBuffer inputBuffer = inputBuffers[inputBufferId];
                        inputBuffer.clear();
                        inputBuffer.put(mInputData, 0, mBufferInfo.size);
                        Log.e(TAG, "mBufferInfo.size=" + mBufferInfo.size + " mBufferInfo.presentationTimeUs=" + mBufferInfo.presentationTimeUs);
                        mMediaCodec.queueInputBuffer(inputBufferId,0, mBufferInfo.size, mBufferInfo.presentationTimeUs,0);
                        mInputNotReadyCount = 0;
                        mInputDataReady = false;
                        Log.e(TAG, "doInBackground MediaCodec over");
                    }
                }
                else {
//                    Log.e(TAG, "doInBackground inputData not ready");
                    //如果经过n次循环没有得到输入数据，则退出循环
                    mInputNotReadyCount++;
                    if (mInputNotReadyCount >= MAX_INPUT_NOT_READY_COUNT){
                        Log.e(TAG, "doInBackground 没有得到输入数据，退出循环");
                        break;
                    }
                }
                //输出数据自动送到mOutputSurface
            }
        }
        catch (Exception e){
            Log.e(TAG, "doInBackground 解码异常");
            e.printStackTrace();
        }
        finally {
            return null;
        }
    }

}
