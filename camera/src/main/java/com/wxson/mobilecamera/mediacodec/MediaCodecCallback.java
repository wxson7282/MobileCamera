package com.wxson.mobilecamera.mediacodec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.support.annotation.NonNull;
import android.util.Log;

import com.wxson.mobilecamera.activity.IMainContract;
import com.wxson.mobilecomm.connect.ByteBufferTransfer;
import com.wxson.mobilecomm.connect.ByteBufferTransferTask;
import com.wxson.mobilecomm.connect.IWifiP2pConnectStatusListener;

import java.net.InetAddress;
import java.nio.ByteBuffer;

import static com.wxson.mobilecomm.codec.AvcUtils.GetCsd;
//import static com.wxson.mobilecomm.tool.CommonTools.bytesToHex;

/**
 * Created by wxson on 2018/4/26.
 * Package com.wxson.remote_camera.mediacodec.
 */
public class MediaCodecCallback {

    private static final String TAG = "MediaCodecCallback";
    private ByteBufferTransfer mByteBufferTransfer;
    private ByteBufferTransferTask mByteBufferTransferTask;
//    private static final long mTimeout = 10000;
    private boolean mPreTaskCompleted;
    private int mPort;
    private boolean mWifiP2pConnected = false;

    private MediaCodec.Callback mCallback = new MediaCodec.Callback() {
        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec mediaCodec, int i) {
            Log.i(TAG, "onInputBufferAvailable");
        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec mediaCodec, int index, @NonNull MediaCodec.BufferInfo bufferInfo) {

            Log.i(TAG, "onOutputBufferAvailable");

            //region for debug only
            //取得ByteBuffer
            ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(index);
//            // for h264
//            if ((outputBuffer.get(4) & 0x1f) == 7) {    //h264: sps + pps
//                byte[] csd = new byte[outputBuffer.remaining()];
//                outputBuffer.get(csd);
//                mByteBufferTransfer.setCsd(csd);
//                Log.i(TAG, "h264 Sps+Pps=" + bytesToHex(csd));
//            }
//            else{
//                // for h265
//                int nalType = (outputBuffer.get(4) >> 1) & 0x3f;
//                if (nalType == 32){                         //h265
//                    byte[] csd = new byte[outputBuffer.remaining()];
//                    outputBuffer.get(csd);
//                    mByteBufferTransfer.setCsd(csd);
//                    Log.i(TAG, "h265 csd0=" + bytesToHex(csd));
//                }
//            }

            byte[] csd;
            if (outputBuffer != null) {
                csd = GetCsd(outputBuffer);
                if (csd != null){
                    mByteBufferTransfer.setCsd(csd);
                }
            }

            //endregion

            //如果前一帧传输任务完成
            if (mPreTaskCompleted){
                //获取客户端地址
                InetAddress mInetAddress = ByteBufferTransferTask.getInetAddress();
                // 如果已经获得客户端地址
                if (mInetAddress != null){
                    //取得ByteBuffer
                    if (outputBuffer != null && mWifiP2pConnected){
                        //启动帧数据传输ByteBufferTransferTask
                        Log.i(TAG, "onOutputBufferAvailable 客户端地址：" + mInetAddress.getHostAddress());
                        //从outputBuffer中取出byte[]
                        byte[] bytes = new byte[outputBuffer.remaining()];
                        outputBuffer.get(bytes);
                        mByteBufferTransfer.setByteArray(bytes);
                        mByteBufferTransfer.setBufferInfoFlags(bufferInfo.flags);
                        mByteBufferTransfer.setBufferInfoOffset(bufferInfo.offset);
                        mByteBufferTransfer.setBufferInfoPresentationTimeUs(bufferInfo.presentationTimeUs);
                        mByteBufferTransfer.setBufferInfoSize(bufferInfo.size);
                        //AsyncTask实例只能运行一次
                        mByteBufferTransferTask = new ByteBufferTransferTask(mPort);
                        //定义AsyncTask完成监听器
                        ByteBufferTransferTask.TaskCompletedListener taskCompletedListener =
                                new ByteBufferTransferTask.TaskCompletedListener(){
                                    @Override
                                    public void onPreTaskCompleted() {
                                        mPreTaskCompleted = true;
                                    }
                                };
                        mByteBufferTransferTask.setTaskCompletedListener(taskCompletedListener);
                        mByteBufferTransferTask.setByteBufferTransfer(mByteBufferTransfer);
                        mByteBufferTransferTask.execute(mInetAddress.getHostAddress());
                        mPreTaskCompleted = false;  //任务完成标志
                        Log.i(TAG, "onOutputBufferAvailable mByteBufferTransferTask.execute");
                    }
                    else{
                        Log.e(TAG, "onOutputBufferAvailable outputBuffer：null");
                    }
                }
                else{
                    Log.e(TAG, "onOutputBufferAvailable 客户端地址：null");
                }
            }
            else {
                Log.i(TAG, "onOutputBufferAvailable PreTaskNotCompleted");
            }
            mediaCodec.releaseOutputBuffer(index, false);
        }

        @Override
        public void onError(@NonNull MediaCodec mediaCodec, @NonNull MediaCodec.CodecException e) {
            Log.e(TAG, "onError");
        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec mediaCodec, @NonNull MediaFormat mediaFormat) {
            Log.i(TAG, "onOutputFormatChanged");
        }
    };

    /**
     *  构造函数
     * @param byteBufferTransfer byteBufferTransfer
     * @param PORT PORT
     */
    public MediaCodecCallback(ByteBufferTransfer byteBufferTransfer, int PORT, IMainContract.Presenter presenter) {
        mByteBufferTransfer = byteBufferTransfer;
        mPort = PORT;
        mByteBufferTransferTask = new ByteBufferTransferTask(PORT);
        mPreTaskCompleted = true;
        //注册WiFi连接状态监听器
        IWifiP2pConnectStatusListener WifiP2PConnectStatusListener
                = new IWifiP2pConnectStatusListener() {
            @Override
            public void onWifiP2pConnectStatusChanged(boolean wifiP2pConnected) {
                mWifiP2pConnected = wifiP2pConnected;
            }
        };
        presenter.setWifiP2pConnectStatusListener(WifiP2PConnectStatusListener);
    }

    public MediaCodec.Callback getCallback(){
        return mCallback;
    }
}
