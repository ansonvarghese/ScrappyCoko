package com.myscrap.webservice;

/**
 * Created by Ms2 on 4/7/2016.
 */
public class EndPoints {

    private static final String BASE_WEB_URL = "https://myscrap.com";


    // previously used api for forget password

  /*  public static final String URL_FORGOT_PASSWORD  = BASE_WEB_URL+"/index.php/webservice/forgot_password";
    public static final String URL_CHANGE_PASSWORD  = BASE_WEB_URL+"/index.php/webservice/change_password";
    public static final String URL_SEND_OTP  = BASE_WEB_URL+"/index.php/webservice/verify_code";*/


    public static final String URL_FORGOT_PASSWORD  = BASE_WEB_URL+"/android/forgot_password";
    public static final String URL_CHANGE_PASSWORD  = BASE_WEB_URL+"/android/change_password";
    public static final String URL_SEND_OTP  = BASE_WEB_URL+"/android/verify_code";
}
