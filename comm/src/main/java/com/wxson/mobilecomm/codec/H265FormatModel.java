package com.wxson.mobilecomm.codec;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

/**
 * Created by wxson on 2018/7/11.
 * Package com.wxson.common_lib.
 */
public abstract class H265FormatModel {

    private static final String TAG = "H265FormatModel";
    private String mMime = "video/hevc";

    public abstract int getWidth();
    public abstract int getHeight();

    public MediaFormat getEncodeFormat(){
        int width = getWidth();
        int height = getHeight();
        int frameRate = 15;
        int frameInterval = 1;
        int bitRateFactor = 8;

        MediaFormat encodeFormat = MediaFormat.createVideoFormat(mMime, width, height);
        encodeFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * bitRateFactor);
        encodeFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
        encodeFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        encodeFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, frameInterval);

        return encodeFormat;
    }

    public MediaFormat getDecodeFormat(byte[] csd0){
        int width = getWidth();
        int height = getHeight();

        MediaFormat decodeFormat = MediaFormat.createVideoFormat(mMime, width, height);
        decodeFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, width * height);
        decodeFormat.setByteBuffer("csd-0", ByteBuffer.wrap(csd0));

        return decodeFormat;
    }
}
