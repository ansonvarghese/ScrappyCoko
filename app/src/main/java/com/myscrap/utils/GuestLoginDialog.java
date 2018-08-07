package com.myscrap.utils;

import android.content.Context;
import android.content.Intent;

import com.afollestad.materialdialogs.MaterialDialog;
import com.myscrap.LoginActivity;
import com.myscrap.xmppdata.ChatMessagesTable;

/**
 * Created by Ms2 on 4/21/2016.
 */
public  class GuestLoginDialog {

    public static void show(final Context context) {
        if(context != null){
            MaterialDialog dialog;
            MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
                    .title("Guest Login")
                    .content("Login into MyScrap or create an account.")
                    .positiveText("LOGIN")
                    .negativeText("NOT NOW")
                    .onPositive((dialog1, which) ->
                    {
                        dialog1.dismiss();

                        goToLogin(context);

                    })
                    .onNegative((dialog12, which) -> dialog12.dismiss());
            dialog = builder.build();
            if(dialog != null && !dialog.isShowing())
                dialog.show();
        }

    }

    private static void goToLogin(Context context)
    {



        Intent mIntent = new Intent(context, LoginActivity.class);
        context.startActivity(mIntent);
    }

}
