package com.myscrap.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.myscrap.model.User;

/**
 * Created by ms3 on 5/11/2017.
 */

public class MyPreferenceManager {

    private String TAG = MyPreferenceManager.class.getSimpleName();
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private static final String PREF_NAME = "myscrap_gcm";
    public static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PASSWORD = "user_password";
    private static final String KEY_NOTIFICATIONS = "notifications";

    @SuppressLint("CommitPrefEdits")
    public MyPreferenceManager(Context context) {
        int PRIVATE_MODE = 0;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void storeUser(User user)
    {
        editor.putString(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_PASSWORD, user.getPassword());
        editor.apply();

        Log.d(TAG, "User is stored in shared preferences. " + user.getName() + ", " + user.getEmail());
    }

    public User getUser() {
        if (pref.getString(KEY_USER_ID, null) != null) {
            String id, name, email, password;
            id = pref.getString(KEY_USER_ID, null);
            name = pref.getString(KEY_USER_NAME, null);
            email = pref.getString(KEY_USER_EMAIL, null);
            password = pref.getString(KEY_USER_PASSWORD, null);
            return new User(id, name, email, password);
        }
        return null;
    }
    public User getUserCredentials() {
        String mail = pref.getString(KEY_USER_EMAIL, null);
        if ( mail!= null && !mail.equalsIgnoreCase("")) {
            String id, name, email, password;
            name = pref.getString(KEY_USER_NAME, null);
            email = pref.getString(KEY_USER_EMAIL, null);
            password = pref.getString(KEY_USER_PASSWORD, null);
            return new User("", name, email, password);
        }
        return null;
    }

    public String getNotifications() {
        return pref.getString(KEY_NOTIFICATIONS, null);
    }

    public void clear() {
        editor.remove(KEY_USER_ID);
        editor.apply();
        //editor.commit();
    }

}
