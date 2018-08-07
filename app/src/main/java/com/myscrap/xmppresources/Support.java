package com.myscrap.xmppresources;

/**
 * Created by ms3 Android MyScrap.
 */

public class Support
{
    public static String getJidFromName(String userId, String username)
    {
        String[] split = username.split("\\s+");

        return split[0].toLowerCase()+userId;
    }


}
