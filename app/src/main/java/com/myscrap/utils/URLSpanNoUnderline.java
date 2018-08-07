package com.myscrap.utils;

import android.annotation.SuppressLint;
import android.text.TextPaint;
import android.text.style.URLSpan;

/**
 * Created by ms3 on 5/15/2017.
 */

@SuppressLint("ParcelCreator")
public class URLSpanNoUnderline extends URLSpan{

    public URLSpanNoUnderline(String url) {
        super(url);
    }
    @Override public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
    }

}
