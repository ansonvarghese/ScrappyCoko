package com.myscrap.adapters;

import android.view.View;

import com.myscrap.model.MyItem;


/**
 * Created by Ms2 on 7/18/2016.
 */
public interface OnItemTouchListener {
    void onViewTouch(View view, int position, MyItem markerList);
}
