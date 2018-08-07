package com.myscrap.database;


import com.myscrap.model.MyItem;

import java.util.ArrayList;

/**
 * Created by Ms2 on 7/25/2016.
 */
public interface MarkerListener {

    void addMarker(MyItem item);
    void deleteMarkerList();
    void updateMarker(MyItem item);
    ArrayList<MyItem> getMarkerList();
    int getMarkerCount();

}
