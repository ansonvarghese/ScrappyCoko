package com.myscrap.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.TransformationMethod;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Ms2 on 8/7/2016.
 */
public class LinkTransformationMethod implements TransformationMethod {
    private Activity homeActivity ;
    public LinkTransformationMethod(Activity mHomeActivity) {
        this.homeActivity = mHomeActivity;
    }

    @Override
    public CharSequence getTransformation(CharSequence source, View view) {
        if (!(view instanceof TextView)) {
            return source;
        }
        TextView textView = (TextView) view;
        if (textView.getText() == null || !(textView.getText() instanceof Spannable)) {
            return source;
        }
        Spannable text = (Spannable) textView.getText();
        URLSpan[] spans = text.getSpans(0, textView.length(), URLSpan.class);
        for (URLSpan span : spans) {
            int start = text.getSpanStart(span);
            int end = text.getSpanEnd(span);
            String url = span.getURL();

            text.removeSpan(span);
            text.setSpan(new CustomTabsURLSpan(url, homeActivity), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return text;
    }

    @Override
    public void onFocusChanged(View view, CharSequence sourceText, boolean focused, int direction, Rect previouslyFocusedRect) {

    }
}
