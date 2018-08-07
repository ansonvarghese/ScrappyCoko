package com.myscrap.webservice;

/**
 * Created by Ms2 on 2/22/2016.
 */
public interface URLRequestListener {
    void onRequestComplete(String result);
    void onRequestFailure(String exception);
}
