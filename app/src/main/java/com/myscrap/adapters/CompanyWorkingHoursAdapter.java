package com.myscrap.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.myscrap.CompanyWorkingHoursActivity;
import com.myscrap.R;
import com.myscrap.model.MyDialogCloseListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

/**
 * Created by Ms2 on 10/26/2016.
 */

public class CompanyWorkingHoursAdapter extends RecyclerView.Adapter<CompanyWorkingHoursAdapter.ViewHolder>{

    private Context mContext;
    private ArrayList<Integer> mSelectedItems;
    private AlertDialog mAlertDialog = null;
    private TimePickerDialog finalOpenTimePickerDialog = null;
    private TimePickerDialog finalCloseTimePickerDialog = null;
    private boolean isTwentyFourHours = false;
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.company_working_hours_activity_item, parent, false);
        return new CompanyWorkingHoursAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mHoursTiTle, mWeekDays, mOpenTime, mCloseTime, mAddSetOfHours;
        private ImageView mCancelDate;
        private LinearLayout mOpenCloseTimeLayout, totalLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            mCancelDate = (ImageView) itemView.findViewById(R.id.cancel_date);
            mWeekDays = (TextView) itemView.findViewById(R.id.week_days);
            mHoursTiTle = (TextView) itemView.findViewById(R.id.hours);
            mOpenTime = (TextView) itemView.findViewById(R.id.open_days_selector);
            mCloseTime = (TextView) itemView.findViewById(R.id.close_days_selector);
            mAddSetOfHours = (TextView) itemView.findViewById(R.id.add_set_of_hours_text_view);
            mOpenCloseTimeLayout = (LinearLayout) itemView.findViewById(R.id.open_close_layout);
            totalLayout = (LinearLayout) itemView.findViewById(R.id.hours_layout);
        }
    }

        private void openWeekDaysDialogFragment() {
            mSelectedItems = new ArrayList<>();
            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Select days")
                    .setCancelable(false)
                    .setMultiChoiceItems(R.array.week_days_array, null,
                            (dialog, which, isChecked) -> {
                                if (which == 7 && !mSelectedItems.isEmpty()) {
                                    if (isChecked) {
                                        // If the user checked the item, add it to the selected items
                                        mSelectedItems.add(which);
                                    } else if (mSelectedItems.contains(which)) {
                                        // Else, if the item is already in the array, remove it
                                        mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(R.string.confirm);
                                        mSelectedItems.remove(Integer.valueOf(which));
                                        if (mSelectedItems.size()==1){
                                            if (mSelectedItems.get(0).toString().equalsIgnoreCase("7")) {
                                                ((AlertDialog) dialog).getListView().setItemChecked(7, false);
                                                mSelectedItems.clear();
                                            }
                                        }
                                    }
                                } else if (which != 7) {
                                    if (isChecked) {
                                        // If the user checked the item, add it to the selected items
                                        mSelectedItems.add(which);
                                    } else if (mSelectedItems.contains(which)) {
                                        // Else, if the item is already in the array, remove it
                                        mSelectedItems.remove(Integer.valueOf(which));
                                        mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(R.string.confirm);
                                        if (mSelectedItems.size()==1){
                                            if (mSelectedItems.get(0).toString().equalsIgnoreCase("7")) {
                                                ((AlertDialog) dialog).getListView().setItemChecked(7, false);
                                                mSelectedItems.clear();
                                            }
                                        }
                                    }
                                } else {
                                    ((AlertDialog) dialog).getListView().setItemChecked(which, false);
                                }


                                if (mSelectedItems.size() == 8 && mAlertDialog != null) {
                                    mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(R.string.confirm);
                                } else if (mSelectedItems.size() >= 2 && mSelectedItems.contains(7)) {
                                    mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(R.string.confirm);
                                } else if (mAlertDialog != null) {
                                    mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(R.string.next_caps);
                                }
                            })
                    // Set the action buttons
                    .setPositiveButton(R.string.next, (dialog, id) -> {
                        // User clicked OK, so save the mSelectedItems results somewhere
                        // or return them to the component that opened the dialog
                        if (!mSelectedItems.isEmpty()) {
                            if (mSelectedItems.size() == 8) {
                                isTwentyFourHours = true;
                            }
                            if (mSelectedItems.contains(7)) {
                                // Else, if the item is already in the array, remove it
                                mSelectedItems.remove(Integer.valueOf(7));
                                isTwentyFourHours = true;
                            }
                            Collections.sort(mSelectedItems);
                            String daysString = "";
                            String daysSubString = "";
                            for (int i : mSelectedItems) {
                                daysString += mContext.getResources().getStringArray(R.array.week_days_array)[i];
                                daysString += "\n";
                                daysSubString += mContext.getResources().getStringArray(R.array.week_days_array)[i].substring(0,3);
                                if (mSelectedItems.size() == 1) {
                                    daysSubString += "";
                                    daysString += "";
                                } else {
                                    if (i == mSelectedItems.size()-1) {
                                        daysSubString += "";
                                        daysString += "";
                                    }
                                    else {
                                        daysSubString += "-";
                                        daysString += "-";

                                    }
                                }
                            }
                            CompanyWorkingHoursActivity.mSelectedItems = mSelectedItems;
                            CompanyWorkingHoursActivity.mWeekDaysString = daysString;
                            CompanyWorkingHoursActivity.mWeekDaysSubString = daysSubString;
                            if (isTwentyFourHours) {
                                if (mAlertDialog != null) {
                                    mAlertDialog.dismiss();
                                    mAlertDialog.cancel();
                                    mAlertDialog = null;
                                }
                                isTwentyFourHours = false;
                                dialog.dismiss();
                                confirmWorkingHours();
                            } else if (mSelectedItems.size() > 0 && mSelectedItems.size() != 8) {
                                dialog.dismiss();
                                //addWorkingHours();
                                openTimeDialogFragment(0, false);
                            } else {
                                Toast.makeText(mContext, "Select days", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss());
            mAlertDialog  = builder.create();
            mAlertDialog.show();
        }

    private void confirmWorkingHours() {
        Activity activity = (AppCompatActivity)mContext;
            if (activity instanceof MyDialogCloseListener)
                ((MyDialogCloseListener) activity).handleTwentyFourHours();
    }

        private void openTimeDialogFragment(final int adapterPosition, final boolean b) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            TimePickerDialog mTimeOpenPickerDialog;
            mTimeOpenPickerDialog = new TimePickerDialog(mContext, (timePicker, hourOfDay, minute1) -> {
                CompanyWorkingHoursActivity.mWeekDaysOpenTime = updateTime(hourOfDay, minute1);
                finalOpenTimePickerDialog.setTitle("Opens at");
                if (b) {
                    finalOpenTimePickerDialog.dismiss();
                } else {
                    if (finalOpenTimePickerDialog != null) {
                        finalOpenTimePickerDialog.dismiss();
                        finalOpenTimePickerDialog.cancel();
                    }
                    closeTimeDialogFragment(0, false);
                }
            }, hour, minute, DateFormat.is24HourFormat(mContext));
            LayoutInflater inflater = ((AppCompatActivity) mContext).getLayoutInflater();
            @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.time_picker_custom_title_layout, null);
            TextView texts=(TextView) dialogView.findViewById(R.id.custom_title);
            texts.setText(R.string.opens_at);
            mTimeOpenPickerDialog.setCustomTitle(dialogView);
            mTimeOpenPickerDialog.show();
            finalOpenTimePickerDialog = mTimeOpenPickerDialog ;
            mTimeOpenPickerDialog.show();

            finalOpenTimePickerDialog.setOnDismissListener(dialog -> {
                Activity activity = (AppCompatActivity)mContext;
                if (b && CompanyWorkingHoursActivity.mWeekDaysOpenTime != null && !CompanyWorkingHoursActivity.mWeekDaysOpenTime.equalsIgnoreCase("")) {
                    if (activity instanceof MyDialogCloseListener)
                        ((MyDialogCloseListener) activity).handleOpenTimeDialogClose(dialog,adapterPosition,CompanyWorkingHoursActivity.mWeekDaysOpenTime );
                }
            });
        }

        private void closeTimeDialogFragment(final int adapterPosition, final boolean b) {
            final Calendar c = Calendar.getInstance();
            int innerHour = c.get(Calendar.HOUR_OF_DAY);
            int innerMinute = c.get(Calendar.MINUTE);
            TimePickerDialog mTimeClosePickerDialog;

            mTimeClosePickerDialog = new TimePickerDialog(mContext, (timePicker, hour, minute) -> {
                finalCloseTimePickerDialog.setTitle("Closes at");
                CompanyWorkingHoursActivity.mWeekDaysCloseTime = updateTime(hour,minute);
                finalCloseTimePickerDialog.dismiss();
            }, innerHour, innerMinute, DateFormat.is24HourFormat(mContext));
            mTimeClosePickerDialog.setTitle("Closes at");
            finalCloseTimePickerDialog = mTimeClosePickerDialog ;
            LayoutInflater inflater = ((AppCompatActivity) mContext).getLayoutInflater();
            @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.time_picker_custom_title_layout, null);
            TextView texts=(TextView) dialogView.findViewById(R.id.custom_title);
            texts.setText(R.string.closes_at);
            mTimeClosePickerDialog.setCustomTitle(dialogView);
            mTimeClosePickerDialog.show();

            finalCloseTimePickerDialog.setOnDismissListener(dialog -> {
                Activity activity = (AppCompatActivity)mContext;
                if(activity instanceof MyDialogCloseListener) {
                    if (b && CompanyWorkingHoursActivity.mWeekDaysCloseTime != null && !CompanyWorkingHoursActivity.mWeekDaysCloseTime.equalsIgnoreCase("")) {
                        ((MyDialogCloseListener) activity).handleCloseTimeDialogClose(dialog, adapterPosition, CompanyWorkingHoursActivity.mWeekDaysCloseTime);
                    } else if (CompanyWorkingHoursActivity.mWeekDaysCloseTime != null && !CompanyWorkingHoursActivity.mWeekDaysCloseTime.equalsIgnoreCase("")){
                        ((MyDialogCloseListener) activity).handleDialogClose(dialog);
                    } else {
                        finalCloseTimePickerDialog.dismiss();
                    }
                }
            });
        }

    private String updateTime(int hours, int min) {
        String timeSet;
        if (hours > 12) {
            hours -= 12;
            timeSet = "PM";
        } else if (hours == 0) {
            hours += 12;
            timeSet = "AM";
        } else if (hours == 12)
            timeSet = "PM";
        else
            timeSet = "AM";

        String minutes = "";
        if (min < 10)
            minutes = "0" + min;
        else
            minutes = String.valueOf(min);
        return String.valueOf(hours) + ':' + minutes + " " + timeSet;
    }

}
