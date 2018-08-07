package com.myscrap.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by Ms2 on 1/28/2016.
 */
public class MyItem implements ClusterItem {
    private LatLng mPosition;
    private double latitude;
    private double longitude;
    private String mCompanyName, mCompanyType, mCompanyAddress,mCompanyCountry,mCompanyImage,mMarkerId;
    private String isNew;
    private int id ;
    private boolean isFavourite ;

    public MyItem(double lat, double lng, String companyName, String mCompanyType, String isNew, String companyAddress, String companyCountry, String companyImage, String markerId) {
        mPosition = new LatLng(lat, lng);
        setLatitude(lat);
        setLongitude(lng);
        setIsNew(isNew);
        setCompanyAddress(companyAddress);
        setCompanyCountry(companyCountry);
        setCompanyName(companyName);
        setCompanyImage(companyImage);
        setMarkerId(markerId);
        setCompanyType(mCompanyType);
    }

    public MyItem(LatLng position, String companyName, String companyType, String isNew, String companyAddress, String companyCountry, String markerId, String companyImage) {
        mPosition = position;
        setLatitude(position.latitude);
        setLongitude(position.longitude);
        setIsNew(isNew);
        setCompanyAddress(companyAddress);
        setCompanyCountry(companyCountry);
        setCompanyName(companyName);
        setCompanyImage(companyImage);
        setMarkerId(markerId);
        setCompanyType(mCompanyType);
    }

    public MyItem() {
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public String getCompanyName() {
        return mCompanyName;
    }

    public void setCompanyName(String mCompanyName) {
        this.mCompanyName = mCompanyName;
    }

    public String getCompanyAddress() {
        return mCompanyAddress;
    }

    public void setCompanyAddress(String mCompanyAddress) {
        this.mCompanyAddress = mCompanyAddress;
    }

    public String getCompanyImage() {
        return mCompanyImage;
    }

    public void setCompanyImage(String mCompanyImage) {
        this.mCompanyImage = mCompanyImage;
    }

    public String getMarkerId() {
        return mMarkerId;
    }

    public void setMarkerId(String mMarkerId) {
        this.mMarkerId = mMarkerId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCompanyCountry() {
        return mCompanyCountry;
    }

    public void setCompanyCountry(String mCompanyCountry) {
        this.mCompanyCountry = mCompanyCountry;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    public String getCompanyType() {
        return mCompanyType;
    }

    public void setCompanyType(String mCompanyType) {
        this.mCompanyType = mCompanyType;
    }


    public String getIsNew() {
        return isNew;
    }

    public void setIsNew(String isNew) {
        this.isNew = isNew;
    }
}