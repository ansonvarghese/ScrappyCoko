package com.myscrap.model;

import android.location.Location;

public class CurrentLocationDetails {

    private static Location currentLocation = null;
    public static Location getCurrentLocation() {
        return currentLocation;
    }

    public static void setCurrentLocation(Location currentLocation) {
        CurrentLocationDetails.currentLocation = currentLocation;
    }
}
