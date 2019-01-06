package com.wxson.mobilecomm.codec;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Range;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by wxson on 2018/12/7.
 * Package com.wxson.mobilecomm.codec.
 */
public class MediaCodecUtil {

    private static final String TAG = "MediaCodecUtil";

    public static CharSequence[] getMimeList(CharSequence[] mimeListValues){
        Log.i(TAG, "getMimeList");
        ArrayList<CharSequence> availableMimes = new ArrayList<>();
        for (CharSequence mimeValue : mimeListValues){
            MediaCodecInfo mediaCodecInfo = selectCodec(mimeValue.toString());
            if (mediaCodecInfo!=null){
//                MediaCodecInfo.CodecCapabilities codecCapabilities = getCodecCapabilities(mimeValue.toString(),mediaCodecInfo);
//                MediaCodecInfo.VideoCapabilities videoCapabilities = getVideoCapabilities(codecCapabilities);
//
//                Log.i(TAG, "IsSizeSupported(320, 240)=" + videoCapabilities.isSizeSupported(320,240));
//                Log.i(TAG, "IsSizeSupported(640, 480)=" + videoCapabilities.isSizeSupported(640,480));
//                Log.i(TAG, "IsSizeSupported(800, 600)=" + videoCapabilities.isSizeSupported(800,600));
//                Log.i(TAG, "IsSizeSupported(1024, 768)=" + videoCapabilities.isSizeSupported(1024,768));
//                Log.i(TAG, "IsSizeSupported(1280, 1024)=" + videoCapabilities.isSizeSupported(1280,1024));

                availableMimes.add(mimeValue);
            };
        }
        return availableMimes.toArray(new CharSequence[0]);
    }

    public static MediaCodecInfo selectCodec(@NonNull String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    public static MediaCodecInfo.CodecCapabilities getCodecCapabilities(@NonNull String mimeType, @NonNull MediaCodecInfo mediaCodecInfo) {
        return mediaCodecInfo.getCapabilitiesForType(mimeType);
    }

    public static MediaCodecInfo.VideoCapabilities getVideoCapabilities(@NonNull MediaCodecInfo.CodecCapabilities codecCapabilities) {
        return codecCapabilities.getVideoCapabilities();
    }

    public static Range<Double> getSupportedFrameRates(@NonNull MediaCodecInfo.VideoCapabilities videoCapabilities, int width, int height){
        return videoCapabilities.getSupportedFrameRatesFor(width, height);
    }

//    public static Range<Double> getAchievableFrameRates(@NonNull MediaCodecInfo.VideoCapabilities videoCapabilities, int width, int height){
//        return videoCapabilities.getAchievableFrameRatesFor(width,height);
//    }

}
