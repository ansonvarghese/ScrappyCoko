package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by ms3 on 4/5/2017.
 */

public class AddMessageResponse {

    @SerializedName("results")
    private List<AddMessage> results;

    public List<AddMessage> getResults() {
        return results;
    }

    public void setResults(List<AddMessage> results) {
        this.results = results;
    }
}
