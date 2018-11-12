package com.wxson.mobilecamera.mediacodec;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.support.annotation.NonNull;
import android.util.Range;

/**
 * Created by wxson on 2018/4/25.
 * Package com.wxson.remote_camera.mediacodec.
 */
public class MediaCodecUtil {

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

    public static Range<Double> getAchievableFrameRates(@NonNull MediaCodecInfo.VideoCapabilities videoCapabilities, int width, int height){
        return videoCapabilities.getAchievableFrameRatesFor(width,height);
    }
}
