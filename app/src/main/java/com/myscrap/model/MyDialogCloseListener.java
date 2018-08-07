package com.myscrap.model;

import android.content.DialogInterface;

import java.util.ArrayList;

/**
 * Created by ms3 on 6/4/2017.
 */

public interface MyDialogCloseListener {
    void handleTwentyFourHours();
    void handleDialogClose(DialogInterface dialog);
    void handleOpenTimeDialogClose(DialogInterface dialog, int position, String openTime);
    void handleCloseTimeDialogClose(DialogInterface dialog, int position, String closeTime);
   // void handleDeleteRow(ArrayList<CompanyWorkingHoursItem> companyWorkingHoursItemList);
}
