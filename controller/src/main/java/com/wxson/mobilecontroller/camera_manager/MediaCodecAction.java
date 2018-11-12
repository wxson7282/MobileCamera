package com.wxson.mobilecontroller.camera_manager;

import android.media.MediaCodec;
import android.util.Log;
import android.view.Surface;

import com.wxson.mobilecomm.codec.H264VgaFormat;

import java.io.IOException;

/**
 * Created by wxson on 2018/7/2.
 * Package com.wxson.mobilecontroller.camera_manager.
 */
public class MediaCodecAction {
    private static final String TAG = "MediaCodecAction";

    public static MediaCodec PrepareDecoder(Surface surface, byte[] csd){
        //编码类型 H264
        String mime = "video/avc";
//        //编码类型 H265
//        String mime = "video/hevc";
        try {
            MediaCodec mediaCodec = MediaCodec.createDecoderByType(mime);
            //注册解码器回调
            DecoderCallback decoderCallback = new DecoderCallback();
            mediaCodec.setCallback(decoderCallback.getCallback());
            //设定格式
//            H265VgaFormat h265VgaFormat = new H265VgaFormat();
            H264VgaFormat h264VgaFormat = new H264VgaFormat();
//            H264QVgaFormat qVgaFormat = new H264QVgaFormat();
//            mediaCodec.configure(h265VgaFormat.getDecodeFormat(csd), surface, null, 0);
            mediaCodec.configure(h264VgaFormat.getDecodeFormat(csd), surface, null, 0);
//            mediaCodec.configure(qVgaFormat.getDecodeFormat(csd), surface, null, 0);
            Log.e(TAG, "DecoderCallback mMediaCodec.configure");
            return mediaCodec;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void StartDecoder(MediaCodec mediaCodec){
        mediaCodec.start();
    }

    public static void ReleaseDecoder(MediaCodec mediaCodec){
        mediaCodec.stop();
        mediaCodec.release();
    }
}
