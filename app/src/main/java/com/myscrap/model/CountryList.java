package com.myscrap.model;

/**
 * Created by ms3 on 7/20/2017.
 */

public class CountryList {

    private String countryName;
    private String countryNameCount;

    public CountryList(String countryName, String countryNameCount) {
        this.setCountryName(countryName);
        this.setCountryNameCount(countryNameCount);
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCountryNameCount() {
        return countryNameCount;
    }

    public void setCountryNameCount(String countryNameCount) {
        this.countryNameCount = countryNameCount;
    }
}
