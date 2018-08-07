package com.myscrap.customphoto.customcropper.gestures;

import android.content.Context;
import android.os.Build;

public final class VersionedGestureDetector {

    public static GestureDetectorGestureDetector newInstance(Context context,
                                                             OnGestureListener listener) {
        final int sdkVersion = Build.VERSION.SDK_INT;
        GestureDetectorGestureDetector detector;

        if (sdkVersion < Build.VERSION_CODES.ECLAIR) {
            detector = new CupcakeGestureDetectorGestureDetector(context);
        } else if (sdkVersion < Build.VERSION_CODES.FROYO) {
            detector = new EclairGestureDetectorGestureDetector(context);
        } else {
            detector = new FroyoGestureDetectorGestureDetector(context);
        }

        detector.setOnGestureListener(listener);

        return detector;
    }

}