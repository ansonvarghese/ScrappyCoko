package com.myscrap.utils;

import android.graphics.Typeface;
import android.os.Build;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ms3 on 5/11/2017.
 */

public class TypefaceUtil {

    public static void overrideFont(Typeface customFontFileNameInAssets) {
        if (Build.VERSION.SDK_INT >= 21) {
            Map<String, Typeface> newMap = new HashMap<>();
            newMap.put("SERIF", customFontFileNameInAssets);
            try {
                final Field staticField = Typeface.class
                        .getDeclaredField("sSystemFontMap");
                staticField.setAccessible(true);
                staticField.set(null, newMap);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            try {
                final Field staticField = Typeface.class
                        .getDeclaredField("SERIF");
                staticField.setAccessible(true);
                staticField.set(null, customFontFileNameInAssets);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
