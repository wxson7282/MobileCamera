package com.wxson.mobilecomm.codec;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import java.nio.ByteBuffer;

import static com.wxson.mobilecomm.codec.AvcUtils.getPps;
import static com.wxson.mobilecomm.codec.AvcUtils.getSps;
import static com.wxson.mobilecomm.codec.AvcUtils.goToPrefix;

/**
 * Created by wxson on 2018/7/11.
 * Package com.wxson.common_lib.
 */
public abstract class H264FormatModel {

    private static final String TAG = "H264FormatModel";
    private String mMime = "video/avc";

    public abstract int getWidth();
    public abstract int getHeight();

    public MediaFormat getEncodeFormat(){
        int width = getWidth();
        int height = getHeight();
        int frameRate = 30;
        int frameInterval = 0;  //每一帧都是关键帧
        int bitRateFactor = 14;

        MediaFormat encodeFormat = MediaFormat.createVideoFormat(mMime, width, height);
        encodeFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * bitRateFactor);
        encodeFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
        encodeFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        encodeFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, frameInterval);

        return encodeFormat;
    }

    public MediaFormat getDecodeFormat(byte[] csd){
        int width = getWidth();
        int height = getHeight();
        //分割spsPps
        ByteBuffer csdByteBuffer = ByteBuffer.wrap(csd);
        if (csdByteBuffer == null){
            Log.e(TAG, "getDecodeFormat csd is null");
            return null;
        }
        csdByteBuffer.clear();
        if (!goToPrefix(csdByteBuffer)) {
            Log.e(TAG, "getDecodeFormat Prefix error");
            return null;
        }
        byte[] header_sps = getSps(csdByteBuffer);
        byte[] header_pps = getPps(csdByteBuffer);

        MediaFormat decodeFormat = MediaFormat.createVideoFormat(mMime, width, height);
        decodeFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, width * height);
        decodeFormat.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
        decodeFormat.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));

        return decodeFormat;
    }
}
