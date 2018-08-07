package com.myscrap.view;

import android.annotation.SuppressLint;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.text.style.URLSpan;

import com.myscrap.R;
import com.myscrap.application.AppController;

/**
 * Created by ms3 on 10/23/2017.
 */

@SuppressLint("ParcelCreator")
public class URLSpanNoUnderline extends URLSpan {
    public URLSpanNoUnderline(String url) {
        super(url);
    }
    @Override public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
    }
}
