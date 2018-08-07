package com.myscrap.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.support.customtabs.CustomTabsIntent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.myscrap.R;
import com.myscrap.application.AppController;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Ms2 on 2/4/2016.
 */
public class UserUtils {
    private static String REGION_USER_PREFERENCES = "com.myscrap";
    private static String PREF_API_KEY_STATUS = "API_KEY_STATUS";
    private static String PREF_SAVE_API_KEY = "API_KEY";
    private static String PREF_LOGIN_STATUS = "LOGIN_STATUS";
    private static String PREF_UPDATE_CANCEL_VERSION = "UPDATE_CANCEL_VERSION";
    private static String PREF_USER_ID = "USER_ID";
    private static String PREF_ENABLE_USER_NOTIFICATION = "USER_NOTIFICATION";
    private static String PREF_ENABLE_USER_TIPS = "USER_TIPS";
    private static String PREF_USER_FIRE_BASE_ID = "USER_FIREBASE_ID";
    private static String PREF_USER_OWNED_COMPANY_ID = "USER_OWNED_COMPANY_ID";
    private static String PREF_USER_OWNED_COMPANY_DETAILS = "USER_OWNED_COMPANY_DETAILS";
    private static String PREF_USER_EMAIL = "USER_EMAIL";
    private static String PREF_USER_CHAT_LIST = "USER_CHAT_LIST";
    private static String PREF_USER_CHAT_MESSAGE = "USER_CHAT_MESSAGE";
    private static String PREF_USER_CHAT_ROOM_UNREAD_MESSAGE_COUNT = "USER_CHAT_ROOM_UNREAD_MESSAGE_COUNT";
    private static String PREF_USER_CHAT_UNREAD_MESSAGE_COUNT = "USER_CHAT_UNREAD_MESSAGE_COUNT";
    private static String PREF_USER_PHONE = "USER_PHONE";
    private static String PREF_USER_DOB = "USER_DOB";
    private static String PREF_USER_WEB = "USER_WEB";
    private static String PREF_USER_PASSWORD = "USER_PASSWORD";
    private static String PREF_USER_JID = "USER_JID";
    private static String PREF_USER_SUBSCRIPTION ="PREF_USER_SUBSCRIPTION";
    private static String PREF_FIRST_NAME = "USER_FIRST_NAME ";
    private static String PREF_LAST_NAME = "USER_LAST_NAME ";
    private static String PREF_COLOR_CODE = "USER_COLOR_CODE";
    private static String PREF_USER_COLOR = "PREF_USER_COLOR";
    private static String PREF_USER_COUNTRY_NAME = "USER_COUNTRY_NAME ";
    private static String PREF_USER_CITY_NAME = "USER_CITY_NAME ";
    private static String PREF_USER_GENDER = "USER_GENDER";
    private static String PREF_USER_DESIGNATION = "USER_DESIGNATION ";
    private static String PREF_USER_PROFILE_PICTURE = "USER_PROFILE_PICTURE";
    private static String PREF_USER_FRIEND_LIST = "USER_FRIEND_LIST";
    private static String PREF_USER_PROFILE_BITMAP = "USER_PROFILE_BITMAP";
    private static String PREF_USER_COMPANY_PROFILE_PICTURE = "USER_COMPANY_PROFILE_PICTURE";
    private static String PREF_GUEST_LOGIN_STATUS = "USER_GUEST_LOGIN";
    private static String PREF_USER_PROFILE_BACKGROUND_PICTURE = "USER_PROFILE_BACKGROUND_PICTURE";
    private static String PREF_COMPANY_PROFILE_BACKGROUND_PICTURE = "USER_COMPANY_PROFILE_BACKGROUND_PICTURE";
    private static String PREF_USER_OLD_FEEDS = "USER_OLD_FEEDS";
    private static String PREF_USER_OLD_EXCHANGE = "USER_OLD_EXCHANGE";
    private static String PREF_USER_OLD_FRIEND_DETAILS_FEEDS = "USER_OLD_FRIEND_DETAILS_FEEDS";
    private static String PREF_USER_POSTS = "USER_POSTS";
    private static String PREF_COMPANY_POSTS = "COMPANY_POSTS";
    private static String PREF_USER_FRIENDS_SUGGESTION = "USER_FRIENDS_SUGGESTION";
    private static String PREF_USER_FRIENDS_LIST = "USER_FRIENDS_LIST";
    private static String PREF_USER_NEAREST_FRIENDS_LIST = "USER_NEAREST_FRIENDS_LIST";
    private static String PREF_USER_FOLLOWERS_LIST = "USER_FOLLOWERS_LIST";
    private static String PREF_USER_VIEWERS_LIST = "USER_VIEWERS_LIST";
    private static String PREF_COMPANY_FOLLOWERS_LIST = "COMPANY_FOLLOWERS_LIST";
    private static String PREF_USER_NOTIFICATION_LIST = "USER_NOTIFICATION_LIST";
    private static String PREF_USER_NOTIFICATION_COUNT = "USER_NOTIFICATION_COUNT";
    private static String PREF_USER_MODERATOR_NOTIFICATION_COUNT = "USER_NOTIFICATION_COUNT";
    private static String PREF_USER_FR_NOTIFICATION_COUNT = "USER_FR_NOTIFICATION_COUNT";
    private static String PREF_USER_MSG_NOTIFICATION_COUNT = "USER_MSG_NOTIFICATION_COUNT";
    private static String PREF_USER_PROFILE_COMPLETE = "USER_PROFILE_COMPLETE";
    private static String PREF_FRIENDS_JID = "FRIENDS_JID";
    private static String PREF_FRIENDS_ID = "FRIENDS_ID";
    private static String PREF_FRIENDS_NAME = "FRIENDS_NAME";
    private static String PREF_FRIENDS_URL = "PREF_FRIENDS_URL";
    private static String PREF_FRIENDS_COMPANY = "PREF_FRIENDS_COMPANY";
    private static String PREF_FRIENDS_POSITION = "PREF_FRIENDS_POSITION";
    private static String PREF_FRIENDS_LOCATION = "PREF_FRIENDS_LOCATION";
    private static String PREF_FRIENDS_COLOR = "PREF_FRIENDS_COLOR";
    private static String PREF_USER_VIEWERS_COUNT = "USER_VIEWERS_COUNT";
    private static String PREF_USER_BUMPED_COUNT = "USER_BUMPED_COUNT";
    private static String PREF_USER_LAST_LOCATION_LAT = "USER_LAST_LOCATION_LAT";
    private static String PREF_USER_LAST_LOCATION_LNG = "USER_LAST_LOCATION_LNG";
    private static String PREF_USER_PRIVACY = "USER_PRIVACY";
    private static String PREF_USER_ALBUM_LIST = "USER_ALBUM_LIST";
    private static String PREF_USER_ALBUM = "USER_ALBUM";
    private static String PREF_XMPP_LOGIN_STATUS = "USER_XMPP_LOGIN_STATUS";
    private static String PREF_USER_LOCATION_ENABLE = "USER_LOCATION_ENABLE";
    private static String PREF_CREATE_LISTING_STATUS = "CREATE_LISTING_STATUS";
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static String today;
    private static int IO_BUFFER_SIZE = 4 * 1024;


    public static void clearUserUtils(Context context) {
        SharedPreferences settings = context.getSharedPreferences(REGION_USER_PREFERENCES, Context.MODE_PRIVATE);
        settings.edit().clear().apply();
    }

    public static void saveApiKeySent(Context context, String status) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_API_KEY_STATUS, status);
        Log.d("API_KEY_STATUS", status);
    }

    public static String getApiKeyStatus(Context context) {
        if (context != null) {
            String apiKeyStatus = CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_API_KEY_STATUS);
            Log.d("apiKeyStatus", apiKeyStatus);
            return apiKeyStatus;
        }
        return null;
    }

    public static void saveApiKey(Context context, String key) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_SAVE_API_KEY, key);
        Log.d("API_KEY", key);
    }

    public static String getApiKey(Context context) {
        if (context != null) {
            String apiKey = CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_SAVE_API_KEY);
            Log.d("API_KEY", apiKey);
            return apiKey;
        }
        return null;
    }

    public static boolean isApiKeyAlreadySent(Context context) {
        String loginStatus = UserUtils.getApiKeyStatus(context);
        return loginStatus.equalsIgnoreCase("1");
    }

    public static void saveLoginStatus(Context context, String loginVal) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_LOGIN_STATUS, loginVal);
        Log.d("LOGIN_STATUS", loginVal);
    }


    // newly added method
    public static void removeLoginStatus(Context context) {
        if (context != null)
            CommonUtils.removePrefString(context, REGION_USER_PREFERENCES, PREF_LOGIN_STATUS);
    }


    public static String getLoginStatus(Context context) {
        if (context != null) {
            String loginStatus = CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_LOGIN_STATUS);
            Log.d("loginStatus", loginStatus);
            return loginStatus;
        }
        return null;
    }





    //   XMPP Login status methods
    public static void saveXMPPLoginStatus(Context context, String loginStatus)
    {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_XMPP_LOGIN_STATUS, loginStatus);
        Log.d("LOGIN_STATUS", loginStatus);
    }

    // newly added method
    public static void removeXmppLoginStatus(Context context)
    {
        if (context != null)
            CommonUtils.removePrefString(context, REGION_USER_PREFERENCES, PREF_XMPP_LOGIN_STATUS);
    }

    public static String getXMPPLoginStatus(Context context)
    {
        if (context != null)
        {
            String xmppLoginStatus = CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_XMPP_LOGIN_STATUS);
            Log.d("loginStatus", xmppLoginStatus);
            return xmppLoginStatus;
        }
        return null;
    }







    public static void saveUpdateCancelVersion(Context context, String updateVersion)
    {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_UPDATE_CANCEL_VERSION, updateVersion);
        Log.d("UPDATE_CANCEL_VERSION", updateVersion);
    }

    public static String getUpdateCancelVersion(Context context) {
        if (context != null) {
            String updateVersion = CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_UPDATE_CANCEL_VERSION);
            Log.d("UPDATE_CANCEL_VERSION", updateVersion);
            return updateVersion;
        }
        return null;
    }

    public static void setListingStatus(Context context, String listingVal) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_CREATE_LISTING_STATUS, listingVal);
        Log.d("LISTING_STATUS", listingVal);
    }

    public static String getListingStatus(Context context) {
        if (context != null) {
            String listingStatus = CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_CREATE_LISTING_STATUS);
            Log.d("LISTING_STATUS", listingStatus);
            return listingStatus;
        }
        return null;
    }

    public static boolean isListingCreatedNow(Context context) {
        String listingStatus = UserUtils.getListingStatus(context);
        return listingStatus != null && listingStatus.equalsIgnoreCase("1");
    }

    public static void saveLocationSetting(Context context, String enableLocation) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_LOCATION_ENABLE, enableLocation);
        Log.d("LOCATION_ENABLE", enableLocation);
    }

    public static String getLocationSetting(Context context) {
        if (context != null) {
            String enableLocation = CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_LOCATION_ENABLE);
            Log.d("LOCATION_ENABLE", enableLocation);
            return enableLocation;
        }
        return null;
    }

    public static void setNotificationCount(Context context, String notificationCount)
    {
        if (context != null)
        {
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_NOTIFICATION_COUNT, notificationCount);
        }
    }

    public static String getNotificationCount(Context context)
    {

        if (context != null)
        {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_NOTIFICATION_COUNT);
        }
        return null;
    }


    public static void setModeratorNotificationCount(Context context, String moderatorCount) {
        if (context != null) {
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_MODERATOR_NOTIFICATION_COUNT, moderatorCount);
        }
    }

    public static String getModeratorNotificationCount(Context context) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_MODERATOR_NOTIFICATION_COUNT);
        }
        return null;
    }

    public static void setFRNotificationCount(Context context, String frCount) {
        if (context != null) {
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_FR_NOTIFICATION_COUNT, frCount);
        }
    }

    public static String getFRNotificationCount(Context context) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_FR_NOTIFICATION_COUNT);
        }
        return null;
    }

    public static void setMSGNotificationCount(Context context, String msgCount) {
        if (context != null) {
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_MSG_NOTIFICATION_COUNT, msgCount);
        }
    }

    public static String getMSGNotificationCount(Context context) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_MSG_NOTIFICATION_COUNT);
        }
        return null;
    }

    public static void setProfileCompleteness(Context context, String msgCount) {
        if (context != null) {
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_PROFILE_COMPLETE, msgCount);
        }
    }

    public static String getProfileCompleteness(Context context) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_PROFILE_COMPLETE);
        }
        return null;
    }

    public static void setViewersCount(Context context, String viewersCount) {
        if (context != null) {
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_VIEWERS_COUNT, viewersCount);
        }
    }

    public static String getViewersCount(Context context) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_VIEWERS_COUNT);
        }
        return null;
    }

    public static void setBumpedCount(Context context, String viewersCount) {
        if (context != null) {
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_BUMPED_COUNT, viewersCount);
        }
    }

    public static String getBumpedCount(Context context) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_BUMPED_COUNT);
        }
        return null;
    }


    public static void saveGuestLoginStatus(Context context, String guestLoginVal) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_GUEST_LOGIN_STATUS, guestLoginVal);
    }


    // newly added method
    public static void removeGuestLoginStatus(Context context) {
        if (context != null)
            CommonUtils.removePrefString(context, REGION_USER_PREFERENCES, PREF_GUEST_LOGIN_STATUS);
    }


    public static String getGuestLoginStatus(Context context) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_GUEST_LOGIN_STATUS);
        }
        return null;
    }

    public static void saveLoggedUserId(Context context, String userId) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_ID, userId);
    }


    // newly added method
    public static void removeLoggedUserId(Context context) {
        if (context != null)
            CommonUtils.removePrefString(context, REGION_USER_PREFERENCES, PREF_USER_ID);
    }


    public static String getLoggedUserId(Context context) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_ID);
        }
        return null;
    }

    public static void saveNotificationEnable(Context context, String enable) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_ENABLE_USER_NOTIFICATION, enable);
    }


    // newly added method
    public static void removeNotificationEnable(Context context) {
        if (context != null)
            CommonUtils.removePrefString(context, REGION_USER_PREFERENCES, PREF_ENABLE_USER_NOTIFICATION);
    }


    public static String getNotificationEnable(Context context) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_ENABLE_USER_NOTIFICATION);
        }
        return null;
    }

    public static boolean isNotificationEnabled(Context context) {
        String loginStatus = UserUtils.getNotificationEnable(context);
        return loginStatus.equalsIgnoreCase("1");
    }

    public static void saveTipsEnable(Context context, String enable) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_ENABLE_USER_TIPS, enable);
    }

    public static String getTipsEnable(Context context) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_ENABLE_USER_TIPS);
        }
        return null;
    }

    public static boolean isTipsEnabled(Context context) {
        if (context == null)
            return false;
        String loginStatus = UserUtils.getTipsEnable(context);
        return loginStatus.equalsIgnoreCase("1");
    }

    public static void saveOwnedCompanyId(Context context, String companyId, String userId) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_OWNED_COMPANY_ID + userId, companyId);
    }

    public static String getOwnedCompanyId(Context context, String userId) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_OWNED_COMPANY_ID + userId);
        }
        return null;
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static void saveFireBaseInstanceId(Context context, String fireBaseToken) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_FIRE_BASE_ID, fireBaseToken);
    }

    public static String getFireBaseInstanceId(Context context) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_FIRE_BASE_ID);
        }
        return null;
    }

    public static void saveOwnedCompanyDetails(Context context, String companyDetails, String userId) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_OWNED_COMPANY_DETAILS + userId, companyDetails);
    }

    public static String getOwnedCompanyDetails(Context context, String userId) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_OWNED_COMPANY_DETAILS + userId);
        }
        return null;
    }

    public static void saveOwnedCompanyName(Context context, String companyId, String userId) {
        String PREF_USER_OWNED_COMPANY_NAME = "USER_OWNED_COMPANY_NAME";
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_OWNED_COMPANY_NAME + userId, companyId);
    }

    public static void saveUserFeeds(Context context, String userOldFeeds, String userID) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_OLD_FEEDS + userID, userOldFeeds);
    }

    public static String getUserOldFeeds(Context context, String userID) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_OLD_FEEDS + userID);
        }
        return null;
    }

    public static void saveExchange(Context context, String exchange, String userID) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_OLD_EXCHANGE + userID, exchange);
    }

    public static String getExchange(Context context, String userID) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_OLD_EXCHANGE + userID);
        }
        return null;
    }

    public static void saveUserFriendProfileDetails(Context context, String userFriendDetails, String userId) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_OLD_FRIEND_DETAILS_FEEDS + userId, userFriendDetails);
    }

    public static String getUserFriendProfileDetails(Context context, String userId) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_OLD_FRIEND_DETAILS_FEEDS + userId);
        }
        return null;
    }

    public static void saveUserPosts(Context context, String userPosts, String userId) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_POSTS + userId, userPosts);
    }

    public static String getUserPosts(Context context, String userId) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_POSTS + userId);
        }
        return null;
    }

    public static void saveCompanyPosts(Context context, String companyPosts, String markerID) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_COMPANY_POSTS + markerID, companyPosts);
    }

    public static String getCompanyPosts(Context context, String markerID) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_COMPANY_POSTS + markerID);
        }
        return null;
    }

    public static void saveUserFriendSuggestions(Context context, String userFriendsSuggestion) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_FRIENDS_SUGGESTION, userFriendsSuggestion);
    }

    public static String getUserFriendSuggestions(Context context) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_FRIENDS_SUGGESTION);
        }
        return null;
    }


    public static String getUserFriendList(Context context, String friendID) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_FRIENDS_LIST + friendID);
        }
        return null;
    }

    public static void saveCompanyFriendList(Context context, String userFriendsList, String companyID) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_FRIENDS_LIST + companyID, userFriendsList);
    }

    public static String getCompanyFriendList(Context context, String companyID) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_FRIENDS_LIST + companyID);
        }
        return null;
    }

    public static void saveUserNearestFriendList(Context context, String userFriendsList, String friendID) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_NEAREST_FRIENDS_LIST + friendID, userFriendsList);
    }

    public static String getUserNearestFriendList(Context context, String friendID) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_NEAREST_FRIENDS_LIST + friendID);
        }
        return null;
    }

    public static void saveUserFollowerList(Context context, String userFollowersList, String friendId) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_FOLLOWERS_LIST + friendId, userFollowersList);
    }

    public static String getUserFollowerList(Context context, String friendId) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_FOLLOWERS_LIST + friendId);
        }
        return null;
    }

    public static void saveUserNotificationList(Context context, String userID, String userNotificationsList) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_NOTIFICATION_LIST + userID, userNotificationsList);
    }

    public static String getUserNotificationList(Context context, String userID) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_NOTIFICATION_LIST + userID);
        }
        return null;
    }

    public static boolean isGuestLoggedIn(Context context)
    {
        String loginStatus = UserUtils.getGuestLoginStatus(AppController.getInstance());
        return loginStatus.equalsIgnoreCase("1");
    }

    public static boolean isAlreadyLoggedIn(Context context)
    {

        String loginStatus = UserUtils.getLoginStatus(AppController.getInstance());
        return loginStatus.equalsIgnoreCase("1");


      /*  // new logic built
        String loginStatus = UserUtils.getLoginStatus(AppController.getInstance());
        String xmpploginStatus = UserUtils.getXMPPLoginStatus(AppController.getInstance());

        if (loginStatus.equalsIgnoreCase("1") && xmpploginStatus.equalsIgnoreCase("1"))
        {
            return true;
        }
        else
        {
            return false;
        }*/

    }

    public static boolean isAlreadyLocationAllowed(Context context) {
        String loginStatus = UserUtils.getLocationSetting(AppController.getInstance());
        return loginStatus.equalsIgnoreCase("1");
    }

    public static void saveUserEmail(Context context, String emailVal) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_EMAIL, emailVal);
    }


    // newly added method
    public static void removeUserEmail(Context context) {
        if (context != null)
            CommonUtils.removePrefString(context, REGION_USER_PREFERENCES, PREF_USER_EMAIL);
    }


    public static String getUserEmail(Context context) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_EMAIL);
        }
        return null;
    }

    public static void saveUserChatList(Context context, String userID, String saveUserChatList) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_CHAT_LIST + userID, saveUserChatList);
    }

    public static String getUserChatList(Context context, String userID) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_CHAT_LIST + userID);
        }
        return null;
    }

    public static void saveUserChatMessages(Context context, String saveUserChatMessages, String userChatId) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_CHAT_MESSAGE + userChatId, saveUserChatMessages);
    }

    public static String getUserChatMessages(Context context, String userChatId) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_CHAT_MESSAGE + userChatId);
        }
        return null;
    }

    public static void saveUserChatUnreadMessageCount(Context context, String saveUserChatUnreadMessageCount, String chatRoomId) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_CHAT_ROOM_UNREAD_MESSAGE_COUNT + chatRoomId, saveUserChatUnreadMessageCount);
    }

    public static String getUserChatUnreadMessageCount(Context context, String chatRoomId) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_CHAT_ROOM_UNREAD_MESSAGE_COUNT + chatRoomId);
        }
        return null;
    }

    public static void saveMessageCount(Context context, String saveUserChatUnreadMessageCount) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_CHAT_UNREAD_MESSAGE_COUNT, saveUserChatUnreadMessageCount);
    }

    public static String getMessageCount(Context context) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_CHAT_UNREAD_MESSAGE_COUNT);
        }
        return null;
    }

    public static void savePhone(Context context, String mPhone) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_PHONE, mPhone);
    }

    public static String getUserPhone(Context context) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_PHONE);
        }
        return null;
    }

    public static void saveUserPassword(Context context, String password) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_PASSWORD, password);
    }



    public static String getUserPassword(Context context) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_PASSWORD);
        }
        return "";
    }


    public static void saveUserJID(Context context, String userJid)
    {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_JID, userJid);
    }


    public static String getUserJid(Context context) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_JID);
        }
        return "";
    }


    public static void saveFriendsJID(Context context, String friendsJid)
    {
        if (context!= null)
        {
            CommonUtils.setPreferenceString(context,REGION_USER_PREFERENCES, PREF_FRIENDS_JID,friendsJid);
        }
    }


    public static void removeFriendsJID(Context context)
    {
        if (context!= null)
        {
            CommonUtils.removePrefString(context,REGION_USER_PREFERENCES, PREF_FRIENDS_JID);
        }
    }



    public static String getPrefFriendsJid(Context context)
    {
        if (context != null)
        {
            return CommonUtils.getPreferenceString(context,REGION_USER_PREFERENCES,PREF_FRIENDS_JID);
        }
        return "";
    }








    public static void saveFriendsID(Context context, String friendsId)
    {
        if (context!= null)
        {
            CommonUtils.setPreferenceString(context,REGION_USER_PREFERENCES, PREF_FRIENDS_ID,friendsId);
        }
    }
    public static void removeFriendsID(Context context)
    {
        if (context!= null)
        {
            CommonUtils.removePrefString(context,REGION_USER_PREFERENCES, PREF_FRIENDS_ID);
        }
    }



    public static String getPrefFriendsId(Context context)
    {
        if (context != null)
        {
            return CommonUtils.getPreferenceString(context,REGION_USER_PREFERENCES,PREF_FRIENDS_ID);
        }
        return "";
    }












    public static void saveUserFriendsName(Context context, String friendsName)
    {
        if (context!= null)
        {
            CommonUtils.setPreferenceString(context,REGION_USER_PREFERENCES, PREF_FRIENDS_NAME,friendsName);
        }
    }



    public static String getFriendsName(Context context)
    {
        if (context != null)
        {
            return CommonUtils.getPreferenceString(context,REGION_USER_PREFERENCES,PREF_FRIENDS_NAME);
        }
        return "";
    }


    public static void saveFriendsPicture(Context context, String url)
    {
        if (context!= null)
        {
            CommonUtils.setPreferenceString(context,REGION_USER_PREFERENCES, PREF_FRIENDS_URL,url);
        }
    }


    public static String getFriendsURL(Context context)
    {
        if (context != null)
        {
            return CommonUtils.getPreferenceString(context,REGION_USER_PREFERENCES,PREF_FRIENDS_URL);
        }
        return "";
    }

    public static void saveFriendsCompany(Context context, String company)
    {
        if (context!= null)
        {
            CommonUtils.setPreferenceString(context,REGION_USER_PREFERENCES, PREF_FRIENDS_COMPANY,company);
        }
    }


    public static String getPrefFriendsCompany(Context context)
    {
        if (context != null)
        {
            return CommonUtils.getPreferenceString(context,REGION_USER_PREFERENCES,PREF_FRIENDS_COMPANY);
        }
        return "";
    }



    public static void saveFriendsPosition(Context context, String position)
    {
        if (context!= null)
        {
            CommonUtils.setPreferenceString(context,REGION_USER_PREFERENCES, PREF_FRIENDS_POSITION,position);
        }
    }


    public static String getPrefFriendsPosition(Context context)
    {
        if (context != null)
        {
            return CommonUtils.getPreferenceString(context,REGION_USER_PREFERENCES,PREF_FRIENDS_POSITION);
        }
        return "";
    }


    public static void saveFriendsLocation(Context context, String location)
    {
        if (context!= null)
        {
            CommonUtils.setPreferenceString(context,REGION_USER_PREFERENCES, PREF_FRIENDS_LOCATION,location);
        }
    }


    public static String getPrefFriendsLocation(Context context)
    {
        if (context != null)
        {
            return CommonUtils.getPreferenceString(context,REGION_USER_PREFERENCES,PREF_FRIENDS_LOCATION);
        }
        return "";
    }

    public static void saveFriendsColor(Context context, String color)
    {
        if (context!= null)
        {
            CommonUtils.setPreferenceString(context,REGION_USER_PREFERENCES, PREF_FRIENDS_LOCATION,color);
        }
    }


    public static String getPrefFriendsColor(Context context)
    {
        if (context != null)
        {
            return CommonUtils.getPreferenceString(context,REGION_USER_PREFERENCES,PREF_FRIENDS_LOCATION);
        }
        return "";
    }






    public static void saveFirstName(Context context, String firstName)
    {
        if(context != null && firstName.length() > 0)
        {
                String mFirstNameFirstLetterCap = firstName.substring(0, 1).toUpperCase() + firstName.substring(1);
                CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_FIRST_NAME, mFirstNameFirstLetterCap);
        }
    }


    // newly added method
    public static void removeFirstName(Context context)
    {
        if (context != null)
            CommonUtils.removePrefString(context, REGION_USER_PREFERENCES, PREF_FIRST_NAME);
    }




    public static String getFirstName(Context context)
    {
        if(context != null){
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_FIRST_NAME);
        }
        return null;
    }



    // method to valide the subscription for price
    public static void savePriceSubscription(Context context, String subscription)
    {
        if (context != null && subscription.length() > 0)
        {
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES,PREF_USER_SUBSCRIPTION,subscription);
        }
    }

    public static void updatePriceSubscription(Context context, String subscription)
    {
        if (context != null && subscription.length() > 0)
        {
            CommonUtils.updatePreferenceString(context, REGION_USER_PREFERENCES,PREF_USER_SUBSCRIPTION,subscription);
        }
    }

    public static void removePriceSubscription(Context context)
    {
        if (context != null)
        {
            CommonUtils.removePrefString(context,REGION_USER_PREFERENCES, PREF_USER_SUBSCRIPTION);
        }
    }

    public static String getPriceSubscription(Context context)
    {
        if(context != null){
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_SUBSCRIPTION);
        }
        return null;
    }





    public static void saveLastName(Context context, String lastName) {
        if(context != null  && lastName.length() > 0) {
                String mLastNameFirstLetterCap = lastName.substring(0, 1).toUpperCase() + lastName.substring(1);
                CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_LAST_NAME, mLastNameFirstLetterCap);
        }

    }


    // newly added method
    public static void removeLastName(Context context)
    {
        if (context != null)
            CommonUtils.removePrefString(context, REGION_USER_PREFERENCES, PREF_LAST_NAME);
    }

    public static String getLastName(Context context) {
        if(context != null){
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_LAST_NAME);
        }
        return null;
    }




    public static void saveColorCode(Context context, String colorCode)
    {
        if(context != null  && colorCode.length() > 0) {
                CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_COLOR_CODE, colorCode);
        }

    }



    // newly added method
    public static void removeColorCode(Context context)
    {
        if (context != null)
            CommonUtils.removePrefString(context, REGION_USER_PREFERENCES, PREF_COLOR_CODE);
    }




    public static String getColorCode(Context context) {
        if(context != null){
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_COLOR_CODE);
        }
        return null;
    }





    public static void saveUserColor(Context context, String userColor)
    {
        if(context != null  && userColor.length() > 0) {
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_COLOR, userColor);
        }

    }



    // newly added method
    public static void removeUserColor(Context context)
    {
        if (context != null)
            CommonUtils.removePrefString(context, REGION_USER_PREFERENCES, PREF_USER_COLOR);
    }




    public static String getUserColor(Context context) {
        if(context != null){
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_COLOR);
        }
        return null;
    }






    public static void saveCountry(Context context, String country) {
        if(context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_COUNTRY_NAME, country);
    }

    public static String getUserCountry(Context context) {
        if(context != null){
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_COUNTRY_NAME);
        }
        return null;
    }

    public static void saveUserCity(Context context, String city) {
        if(context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_CITY_NAME, city);
    }

    public static String getUserCity(Context context) {
        if(context != null){
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_CITY_NAME);
        }
        return null;
    }

    public static void saveUserGender(Context context, String gender) {
        if(context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_GENDER, gender);
    }

    public static String getUserGender(Context context) {
        if(context != null){
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_GENDER);
        }
        return null;
    }

    public static void saveUserDesignation(Context context, String designation) {
        if(context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_DESIGNATION, designation);
    }

    public static String getUserDesignation(Context context) {
        if(context != null){
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_DESIGNATION);
        }
        return null;
    }

    public static void saveUserDOB(Context context, String dob) {
        if(context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_DOB, dob);
    }

    public static String getUserDOB(Context context) {
        if(context != null){
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_DOB);
        }
        return null;
    }

    public static void saveUserWeb(Context context, String web) {
        if(context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_WEB, web);
    }

    public static String getUserWeb(Context context) {
        if(context != null){
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_WEB);
        }
        return null;
    }

    public static void saveUserProfilePicture(Context context, String userProfilePicture) {
        if(context != null){
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_PROFILE_PICTURE, userProfilePicture);
        }
    }


    // newly added method
    public static void removeUserProfilePicture(Context context)
    {
        if (context != null)
            CommonUtils.removePrefString(context, REGION_USER_PREFERENCES, PREF_USER_PROFILE_PICTURE);
    }



    public static String getUserProfilePicture(Context context) {
        if(context != null){
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_PROFILE_PICTURE);
        }
        return null;
    }


    public static void saveUserFriendLists(Context context, String users)
    {
        if(context != null){
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_FRIEND_LIST, users);
        }
    }

    public static String getUserFriendLists(Context context) {
        if(context != null){
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_FRIEND_LIST);
        }
        return null;
    }

    public static void saveUserProfileBackGroundPicture(Context context, String userProfileBackGroundPicture) {
        if(context != null) {
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_PROFILE_BACKGROUND_PICTURE, userProfileBackGroundPicture);
        }
    }

    public static String getUserProfileBackGroundPicture(Context context) {
        if(context != null){
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_PROFILE_BACKGROUND_PICTURE);
        }
        return null;
    }

    public static void saveCompanyProfilePicture(Context context, String companyID, String userProfilePicture) {
        if(context != null){
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_COMPANY_PROFILE_PICTURE+companyID, userProfilePicture);
        }
    }

    public static String getCompanyProfilePicture(Context context, String companyID) {
        if(context != null){
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_COMPANY_PROFILE_PICTURE+companyID);
        }
        return null;
    }

    public static void saveCompanyProfileBackGroundPicture(Context context, String companyID, String userProfileBackGroundPicture) {
        if(context != null) {
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_COMPANY_PROFILE_BACKGROUND_PICTURE+companyID, userProfileBackGroundPicture);
        }
    }

    public static String getCompanyProfileBackGroundPicture(Context context, String companyID) {
        if(context != null){
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_COMPANY_PROFILE_BACKGROUND_PICTURE+companyID);
        }
        return null;
    }

    public static void  showServerErrorToast(Context context) {
       // Toast.makeText(context,"Sorry, Please try after sometimes!!",Toast.LENGTH_SHORT).show();
    }

    public static void saveLastLocationLat(Context context, String mLocation) {
        if(context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_LAST_LOCATION_LAT, mLocation);
    }

    public static String getLastLocationLat(Context context) {
        if(context != null){
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_LAST_LOCATION_LAT);
        }
        return null;
    }

    public static void saveLastLocationLng(Context context, String mLocation) {
        if(context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_LAST_LOCATION_LNG, mLocation);
    }

    public static String getLastLocationLng(Context context) {
        if(context != null){
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_LAST_LOCATION_LNG);
        }
        return null;
    }

    public static void saveUserPrivacy(Context context, String privacy) {
        if(context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_PRIVACY, privacy);
    }

    public static String getUserPrivacy(Context context) {
        if(context != null){
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_PRIVACY);
        }
        return null;
    }

    public static void saveUserAlbumList(Context context, String albumList, String userID) {
        if(context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_ALBUM_LIST+userID, albumList);
    }

    public static String getUserAlbumList(Context context, String userID) {
        if(context != null){
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_ALBUM_LIST+userID);
        }
        return null;
    }

    public static void saveUserAlbum(Context context, String album) {
        if(context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_ALBUM, album);
    }

    public static String getUserAlbum(Context context) {
        if(context != null){
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_ALBUM);
        }
        return null;
    }

    public static boolean getSimCardStatus(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm != null && !(tm.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE || tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT);
    }

    public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            time *= 1000;
        }
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        mSimpleDateFormat.setTimeZone(tz);
        String dateString = mSimpleDateFormat.format(time);
        String postedDate = getTimeStamp(dateString);
        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }
        final long diff = (now - time);
        if (diff < MINUTE_MILLIS) {
            return "Just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "1 min ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " mins ago";
        } else if (diff < 119 * MINUTE_MILLIS) {
            return "1 hr ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hrs ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return postedDate +" ";
        } else {
            return postedDate+" ";
        }
    }

    public static String getNewsTime(long time) {
        if (time < 1000000000000L) {
            time *= 1000;
        }
        String stringThree = null;
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        mSimpleDateFormat.setTimeZone(tz);
        String dateStr =  mSimpleDateFormat.format(time);

        try {
            Date date = mSimpleDateFormat.parse(dateStr);
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm MMMM dd, yyyy", Locale.getDefault());
            stringThree = sdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return stringThree;
    }

    public static String getFeedsTimeAgo(long time) {

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;
        if (time < 1000000000000L) {
            time *= 1000;
        }
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        mSimpleDateFormat.setTimeZone(tz);
        String dateString = mSimpleDateFormat.format(time);
        String postedDate = getFeedsTimeStamp(dateString);
        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }
        long diff = (now - time);
        if (diff < MINUTE_MILLIS) {
            return "1s ago";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "1m ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + "m ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "1h ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + "h ago";
        } else {
            long elapsedDays = diff / daysInMilli;

            if (elapsedDays < 27){
                long diffMinutes = diff / (60 * 1000) % 60;
                long diffHours = diff / (60 * 60 * 1000) % 24;
                long diffDays = diff / (24 * 60 * 60 * 1000);

                if(diffDays != 0 && diffHours != 0){
                    return diffDays+"d, "+ diffHours+"h ago";
                } else if(diffDays == 0 && diffHours != 0){
                    return diffHours+"h , " + diffMinutes+"m ago";
                } else if(diffDays == 0 ){
                    return diffMinutes+"m ago";
                } else if(diffDays > 0){
                    return diffDays+"d ago";
                } else {
                    return postedDate;
                }
            } else
                return postedDate;

        }
    }

    public static String getViewersTimeAgo(long time) {

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;
        if (time < 1000000000000L) {
            time *= 1000;
        }
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        mSimpleDateFormat.setTimeZone(tz);
        String dateString = mSimpleDateFormat.format(time);
        String postedDate = getFeedsTimeStamp(dateString);
        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }
        long diff = (now - time);
        if (diff < MINUTE_MILLIS) {
            return "1s ago";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "1m ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + "m ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "1h ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + "h ago";
        } else {
            long elapsedDays = diff / daysInMilli;

            if (elapsedDays < 27)
                return elapsedDays +"d ago";
            else
                return postedDate;

        }/*else if (diff < 168 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS +"d";
        } else {
            return postedDate+" ";
        }*/
    }

    private static String getFeedsTimeStamp(String dateStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        format.setTimeZone(tz);
        String timestamp = "";
        String dateString;
        String weekOne;
        String weekTwo;
        Calendar calendar = Calendar.getInstance();
        today = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        today = today.length() < 2 ? "0" + today : today;
        try {
            Date date = format.parse(dateStr);
            SimpleDateFormat sdfOne = new SimpleDateFormat("dd MMM", Locale.getDefault());
            weekOne = sdfOne.format(date);
            timestamp = weekOne;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }

    public static String getTimeStamp(String dateStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        format.setTimeZone(tz);
        String timestamp = "";
        String dateString;
        String weekOne;
        String weekTwo;
        Calendar calendar = Calendar.getInstance();
        today = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        today = today.length() < 2 ? "0" + today : today;
        try {
            Date date = format.parse(dateStr);
            SimpleDateFormat todayFormat = new SimpleDateFormat("dd", Locale.getDefault());
            String dateToday = todayFormat.format(date);
            if (isDateInCurrentWeek(date)) {
                if (dateToday.equals(today)) {
                    format = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    dateString = format.format(date);
                    timestamp = dateString;
                } else {
                    SimpleDateFormat sdfOne = new SimpleDateFormat("dd MMM", Locale.getDefault());
                    SimpleDateFormat sdfTwo = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    weekOne = sdfOne.format(date);
                    weekTwo = sdfTwo.format(date);
                    timestamp = weekOne + " at " + weekTwo ;
                }
            }  else if (isDateInCurrentYear(date)) {
                SimpleDateFormat sdfOne = new SimpleDateFormat("dd MMM", Locale.getDefault());
                SimpleDateFormat sdfTwo = new SimpleDateFormat("HH:mm", Locale.getDefault());
                weekOne = sdfOne.format(date);
                weekTwo = sdfTwo.format(date);
                timestamp = weekOne + " at " + weekTwo;
            } else {
                SimpleDateFormat sdfOne = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                SimpleDateFormat sdfTwo = new SimpleDateFormat("HH:mm", Locale.getDefault());
                weekOne = sdfOne.format(date);
                weekTwo = sdfTwo.format(date);
                timestamp = weekOne + " at " + weekTwo;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }

    public static String getChatRoomTime(long time) {
        if (time < 1000000000000L) {
            time *= 1000;
        }

        SimpleDateFormat  mSimpleDateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        mSimpleDateFormat.setTimeZone(tz);
        String dateString = mSimpleDateFormat.format(time);
        return getChatTimeStamp(dateString);
    }

    private static String getChatTimeStamp(String dateStr) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String timestamp = "";
        String day = "";
        String weekOne = "";
        String weekTwo = "";
        Calendar calendar = Calendar.getInstance();
        today = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        today = today.length() < 2 ? "0" + today : today;
        try {
            Date date = format.parse(dateStr);
            SimpleDateFormat todayFormat = new SimpleDateFormat("dd", Locale.getDefault());
            String dateToday = todayFormat.format(date);
            format = dateToday.equals(today) ? new SimpleDateFormat("HH:mm", Locale.getDefault()) : new SimpleDateFormat("dd MMM", Locale.getDefault());
            timestamp = format.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return timestamp;
    }

    private static boolean isDateInCurrentYear(Date date) {
        Date currentDate = new Date();
        boolean isInYear ;
        long dateDifference = getDiffYears(currentDate,date);
        int diffYears = (int) (dateDifference);
        isInYear = diffYears == 0;
        return  isInYear;
    }


    private static int getDiffYears(Date first, Date last) {
        Calendar a = getCalendar(first);
        Calendar b = getCalendar(last);
        return a.get(Calendar.YEAR) - b.get(Calendar.YEAR);
    }

    private static Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setTime(date);
        return cal;
    }

    public static List<String> extractUrls(String text) {
        List<String> containedUrls = new ArrayList<>();
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find())
        {
            containedUrls.add(text.substring(urlMatcher.start(0),
                    urlMatcher.end(0)));
        }
        return containedUrls;
    }

    private static boolean isDateInCurrentWeek(Date date) {
        long dateDifference = date.getTime() - new Date().getTime();
        int diffDays = (int) (dateDifference / (24 * 60 * 60 * 1000));
        return diffDays >= 0 && diffDays <= 7;
    }

    public static Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
    );

    public static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static String capitalizeFirst(String str) {
        if (str == null || str.length() == 0) {
            return "";
        }
        String[] strArray = str.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String s : strArray) {
            String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
            builder.append(cap).append(" ");
        }
        return builder.toString().trim();
    }

    public static String getURLFromString(String urlContainText) {
        String [] parts = urlContainText.split("\\s+");
        String filteredString = "";
        for( String item : parts )
            try {
                URL url = new URL(item);
                // If possible then replace with anchor...
                filteredString = "<a href=\"" + url + "\">"+ url + "</a> ";
            } catch (MalformedURLException e) {
                // If there was an URL that was not it!...
                filteredString = urlContainText;
            }
        return filteredString;
    }

    public static void saveCompanyFollowerList(Context context, String userFollowersList,String companyID) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_COMPANY_FOLLOWERS_LIST+companyID, userFollowersList);
    }

    public static String getCompanyFollowerList(Context context, String companyID) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_COMPANY_FOLLOWERS_LIST+companyID);
        }
        return null;
    }

    public static void saveProfileViewersList(Context context, String userViewersList,String userId) {
        if (context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_VIEWERS_LIST+userId, userViewersList);
    }

    public static String getProfileViewersList(Context context, String userId) {
        if (context != null) {
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_VIEWERS_LIST+userId);
        }
        return null;
    }

    public static void saveUserProfileBitmap(Context context, String bitmap) {
        if(context != null)
            CommonUtils.setPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_PROFILE_BITMAP, bitmap);
    }

    public static String getUserProfileBitmap(Context context) {
        if(context != null){
            return CommonUtils.getPreferenceString(context, REGION_USER_PREFERENCES, PREF_USER_PROFILE_BITMAP);
        }
        return null;
    }


    public static void hideKeyBoard(Context context, View v) {
        if (context != null && v != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
    }

    /**
     * Closes the specified stream.
     *
     * @param stream The stream to close.
     */
    private static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                Log.e("Bitmap", "Could not close stream", e);
            }
        }
    }

    /**
     * Copy the content of the input stream into the output stream, using a
     * temporary byte array buffer whose size is defined by
     * {@link #IO_BUFFER_SIZE}.
     *
     * @param in The input stream to copy from.
     * @param out The output stream to copy to.
     * @throws IOException If any error occurs during the copy.
     */
    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[IO_BUFFER_SIZE];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }



    public static void showKeyBoard(Context context, View v) {
        if (context != null && v != null) {
            InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    public static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    public static void launchCustomTabURL(Context context, String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setToolbarColor(context.getResources().getColor(R.color.colorPrimary, context.getTheme()));
        } else {
            builder.setToolbarColor(context.getResources().getColor(R.color.colorPrimary));
        }
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.intent.setPackage("com.android.chrome");
        customTabsIntent.launchUrl(context, Uri.parse(url));
    }


    public static String parsingInteger(String convertToInteger) {
        int intValue;
        try {
            intValue = Integer.parseInt(convertToInteger);
            //Log.d("Parsing Integer", "TRUE");
        } catch(NumberFormatException ex){
            intValue = 0;
            //Log.e("Parsing Integer", "FALSE");
        }
        return String.valueOf(intValue);
    }

    public static String parsingLong(String convertToLong) {
        long longValue;
        try {
            longValue = Long.parseLong(convertToLong);
            //Log.d("Parsing Long", "TRUE");
        } catch(NumberFormatException ex){
            longValue = 0;
            //Log.e("Parsing Long", "FALSE");
        }
        return String.valueOf(longValue);
    }

    public static String getDateDifference(String startDate, String endDate){
        String difference = "";
        DateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date currentDate = new Date();
        Date dateOne;
        Date dateTwo;
        try {
            dateOne = simpleDateFormat.parse(startDate);
            dateTwo = simpleDateFormat.parse(endDate);
            //long getDiff = dateTwo.getTime() - dateOne.getTime();
            //int diffDays = (int) (getDiff / (24 * 60 * 60 * 1000));

            long dateExpireOrNot = dateTwo.getTime() - currentDate.getTime();
            int diffExpireOrNotDays = (int) (dateExpireOrNot / (24 * 60 * 60 * 1000));

            if (diffExpireOrNotDays < 0) {
                difference = "Expired";
            } else {
                if (diffExpireOrNotDays == 0) {
                    difference = "Today";
                } else if (diffExpireOrNotDays == 1) {
                    difference = "Tomorrow";
                } else  if (diffExpireOrNotDays > 1 && diffExpireOrNotDays <= 7 ){
                    difference = "This week";
                } else {
                    difference = "Upcoming";
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return difference;
    }

    public static void setEmptyView(View emptyView, int drawable, String text, boolean isNoMarginTopNeeded) {
        if (emptyView != null) {
            ImageView emptyImage = emptyView.findViewById(R.id.emptyImage);
            if (emptyImage != null)
                emptyImage.setImageResource(drawable);
            TextView emptyText = emptyView.findViewById(R.id.emptyText);
            if (emptyText != null){
                LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                if (isNoMarginTopNeeded){
                    llp.setMargins(0, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
                } else {
                    llp.setMargins(0, 10, 0, 0); // llp.setMargins(left, top, right, bottom);
                }
                emptyText.setLayoutParams(llp);
                emptyText.setGravity(Gravity.CENTER);
                emptyText.setText(text);
            }
        }
    }



}
