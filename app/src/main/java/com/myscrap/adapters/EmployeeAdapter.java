package com.myscrap.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.myscrap.R;
import com.myscrap.UserFriendProfileActivity;
import com.myscrap.UserProfileActivity;
import com.myscrap.application.AppController;
import com.myscrap.model.Employee;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.CheckNetworkConnection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ms3 on 5/16/2017.
 */

public class EmployeeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Context mContext;
    private List<Employee.EmployeeData.Employees> employees = new ArrayList<>();
    private Employee.EmployeeData.Admin admin;
    private boolean isMyCompany;
    private EmployeeAdapter.OnItemClickListener listener;

    public EmployeeAdapter(Context context, Employee.EmployeeData employeeData, OnItemClickListener mListener){
        this.mContext = context;
        this.listener = mListener;
        if(employeeData != null){
            this.employees = employeeData.getEmployees();
            this.admin = employeeData.getAdmin();

        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_employee, parent, false);
        return new LikeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof LikeViewHolder) {

            if (position == 0) {
                final Employee.EmployeeData.Admin mAdmin = admin;
                if (mAdmin != null) {
                    ((LikeViewHolder) holder).overflow.setVisibility(View.GONE);
                    ((LikeViewHolder) holder).profileName.setText(mAdmin.getName());
                    ((LikeViewHolder) holder).top.setVisibility(View.VISIBLE);
                    ((LikeViewHolder) holder).top.setText("ADMIN");

                    if(mAdmin.getCountry() != null && !mAdmin.getCountry().equalsIgnoreCase("")){
                        ((LikeViewHolder) holder).company.setVisibility(View.VISIBLE);
                        ((LikeViewHolder) holder).company.setText(mAdmin.getCountry());
                    } else {
                        ((LikeViewHolder) holder).company.setVisibility(View.INVISIBLE);
                    }


                    String userPosition;
                    if(mAdmin.getDesignation() != null && !mAdmin.getDesignation().equalsIgnoreCase("")){
                        userPosition = mAdmin.getDesignation().trim();
                    } else {
                        userPosition = "Trader";
                    }

                    String userCompany;
                    if(mAdmin.getUserCompany() != null && !mAdmin.getUserCompany().equalsIgnoreCase("")){
                        userCompany = mAdmin.getUserCompany().trim();
                    } else {
                        userCompany = "";
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        SpannableStringBuilder spannedDetails;
                        if(!userPosition.equalsIgnoreCase("") && !userCompany.equalsIgnoreCase("")){
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+  "&#160"+"&#160"+"&#8226"+"&#160"+"&#160"+userCompany+"&#160" + "</font>", Html.FROM_HTML_MODE_LEGACY));
                        } else if (!userPosition.equalsIgnoreCase("") && userCompany.equalsIgnoreCase("")) {
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "&#160"+"</font>", Html.FROM_HTML_MODE_LEGACY));
                        }else if (userPosition.equalsIgnoreCase("") && !userCompany.equalsIgnoreCase("")) {
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                        } else if(!userPosition.equalsIgnoreCase("")){
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                        } else if(!userCompany.equalsIgnoreCase("")){
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                        } else {
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("", Html.FROM_HTML_MODE_LEGACY));
                        }
                        ((LikeViewHolder) holder).designation.setText(spannedDetails);
                        ((LikeViewHolder) holder).designation.setVisibility(View.VISIBLE);
                    } else {
                        SpannableStringBuilder spannedDetails;
                        if(!userPosition.equalsIgnoreCase("") && !userCompany.equalsIgnoreCase("")){
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+  "&#160"+"&#160"+ "&#8226"+"&#160"+"&#160"+userCompany+"&#160" + "</font>"));
                        } else if (!userPosition.equalsIgnoreCase("") && userCompany.equalsIgnoreCase("")) {
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "</font>"));
                        }else if (userPosition.equalsIgnoreCase("") && !userCompany.equalsIgnoreCase("")) {
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "</font>"));
                        } else if(!userPosition.equalsIgnoreCase("")){
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "</font>"));
                        } else if(!userCompany.equalsIgnoreCase("")){
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "</font>"));
                        } else {
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml(""));
                        }
                        ((LikeViewHolder) holder).designation.setText(spannedDetails);
                        ((LikeViewHolder) holder).designation.setVisibility(View.VISIBLE);
                    }



                    String[] split = mAdmin.getName().split("\\s+");
                    String profilePic = mAdmin.getProfilePic();
                    if (profilePic != null && !profilePic.equalsIgnoreCase("")){
                        if(profilePic.equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                                || profilePic.equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                            ((LikeViewHolder) holder).profile.setImageResource(R.drawable.bg_circle);
                            if(mAdmin.getColorCode() != null && !mAdmin.getColorCode().equalsIgnoreCase("") && mAdmin.getColorCode().startsWith("#")){
                                ((LikeViewHolder) holder).profile.setColorFilter(Color.parseColor(mAdmin.getColorCode()));
                            } else {
                                ((LikeViewHolder) holder).profile.setColorFilter(DeviceUtils.getRandomMaterialColor(mContext, "400"));
                            }

                            ((LikeViewHolder) holder).iconText.setVisibility(View.VISIBLE);
                            if (mAdmin.getName() != null && !mAdmin.getName().equalsIgnoreCase("")){
                                if (split.length > 1){
                                    String first = split[0].substring(0,1);
                                    String last = split[1].substring(0,1);
                                    String initial = first + ""+ last ;
                                    ((LikeViewHolder) holder).iconText.setText(initial.toUpperCase());
                                } else {
                                    if (split[0] != null && split[0].trim().length() >= 1) {
                                        String first = split[0].substring(0, 1);
                                        ((LikeViewHolder) holder).iconText.setText(first.toUpperCase());
                                    }
                                }
                            }
                        } else {

                            Uri uri = Uri.parse(profilePic);
                            RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                            ((LikeViewHolder) holder).profile.setHierarchy(new GenericDraweeHierarchyBuilder(mContext.getResources())
                                    .setRoundingParams(roundingParams)
                                    .build());
                            roundingParams.setRoundAsCircle(true);
                            ((LikeViewHolder) holder).profile.setImageURI(uri);

                            ((LikeViewHolder) holder).profile.setColorFilter(null);
                            ((LikeViewHolder) holder).iconText.setVisibility(View.GONE);
                        }
                    }

                    ((LikeViewHolder) holder).overflow.setVisibility(View.GONE);
                    ((LikeViewHolder) holder).profileName.setOnClickListener(v -> {
                        if (mAdmin.getUserid().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                            goToUserProfile();
                        } else {
                            goToUserFriendProfile(mAdmin.getUserid());
                        }
                    });
                    ((LikeViewHolder) holder).designation.setOnClickListener(v -> {
                        if (mAdmin.getUserid().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                            goToUserProfile();
                        } else {
                            goToUserFriendProfile(mAdmin.getUserid());
                        }
                    });
                    ((LikeViewHolder) holder).profile.setOnClickListener(v -> {
                        if (mAdmin.getUserid().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                            goToUserProfile();
                        } else {
                            goToUserFriendProfile(mAdmin.getUserid());
                        }
                    });
                } else {
                    final Employee.EmployeeData.Employees employeesData = employees.get(position);
                    ((LikeViewHolder) holder).profileName.setText(employeesData.getName());
                    ((LikeViewHolder) holder).overflow.setVisibility(View.GONE);

                    String userPosition;
                    if(employeesData.getDesignation() != null && !employeesData.getDesignation().equalsIgnoreCase("")){
                        userPosition = employeesData.getDesignation().trim();
                    } else {
                        userPosition = "Trader";
                    }

                    String userCompany;
                    if(employeesData.getUserCompany() != null && !employeesData.getUserCompany().equalsIgnoreCase("")){
                        userCompany = employeesData.getUserCompany().trim();
                    } else {
                        userCompany = "";
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        SpannableStringBuilder spannedDetails;
                        if(!userPosition.equalsIgnoreCase("") && !userCompany.equalsIgnoreCase("")){
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+  "&#160"+"&#160"+"&#8226"+"&#160"+"&#160"+userCompany+"&#160" + "</font>", Html.FROM_HTML_MODE_LEGACY));
                        } else if (!userPosition.equalsIgnoreCase("") && userCompany.equalsIgnoreCase("")) {
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "&#160"+"</font>", Html.FROM_HTML_MODE_LEGACY));
                        }else if (userPosition.equalsIgnoreCase("") && !userCompany.equalsIgnoreCase("")) {
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                        } else if(!userPosition.equalsIgnoreCase("")){
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                        } else if(!userCompany.equalsIgnoreCase("")){
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                        } else {
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("", Html.FROM_HTML_MODE_LEGACY));
                        }
                        ((LikeViewHolder) holder).designation.setText(spannedDetails);
                        ((LikeViewHolder) holder).designation.setVisibility(View.VISIBLE);
                    } else {
                        SpannableStringBuilder spannedDetails;
                        if(!userPosition.equalsIgnoreCase("") && !userCompany.equalsIgnoreCase("")){
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+  "&#160"+"&#160"+ "&#8226"+"&#160"+"&#160"+userCompany+"&#160" + "</font>"));
                        } else if (!userPosition.equalsIgnoreCase("") && userCompany.equalsIgnoreCase("")) {
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "</font>"));
                        }else if (userPosition.equalsIgnoreCase("") && !userCompany.equalsIgnoreCase("")) {
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "</font>"));
                        } else if(!userPosition.equalsIgnoreCase("")){
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "</font>"));
                        } else if(!userCompany.equalsIgnoreCase("")){
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "</font>"));
                        } else {
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml(""));
                        }
                        ((LikeViewHolder) holder).designation.setText(spannedDetails);
                        ((LikeViewHolder) holder).designation.setVisibility(View.VISIBLE);
                    }

                    if(employeesData.getModerator() == 1) {
                        ((LikeViewHolder) holder).top.setText(R.string.mod);
                        ((LikeViewHolder) holder).top.setVisibility(View.VISIBLE);
                        ((LikeViewHolder) holder).top.setBackground(ContextCompat.getDrawable(mContext, R.drawable.top_mod));
                    } else {
                        if (Integer.parseInt(UserUtils.parsingInteger(employeesData.getRank())) >= 1 && Integer.parseInt(UserUtils.parsingInteger(employeesData.getRank())) <=10) {
                            ((LikeViewHolder) holder).top.setVisibility(View.VISIBLE);
                            ((LikeViewHolder) holder).top.setText("TOP "+employeesData.getRank());
                        } else {
                            if(employeesData.isNeJoined()){
                                ((LikeViewHolder) holder).top.setVisibility(View.VISIBLE);
                                ((LikeViewHolder) holder).top.setBackground(ContextCompat.getDrawable(mContext, R.drawable.top_red));
                            } else {
                                ((LikeViewHolder) holder).top.setVisibility(View.GONE);
                                ((LikeViewHolder) holder).top.setBackground(null);
                            }
                        }
                    }




                    ((LikeViewHolder) holder).designation.setTextColor(ContextCompat.getColor(mContext,R.color.msSecondaryTextColor));
                    if(employeesData.getCountry() != null && !employeesData.getCountry().equalsIgnoreCase("")){
                        ((LikeViewHolder) holder).company.setVisibility(View.VISIBLE);
                        ((LikeViewHolder) holder).company.setText(employeesData.getCountry());
                    } else {
                        ((LikeViewHolder) holder).company.setVisibility(View.INVISIBLE);
                    }


                    String[] split = employeesData.getName().split("\\s+");
                    String profilePic = employeesData.getProfilePic();
                    if (profilePic != null && !profilePic.equalsIgnoreCase("")){
                        if(profilePic.equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                                || profilePic.equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                            ((LikeViewHolder) holder).profile.setImageResource(R.drawable.bg_circle);
                            ((LikeViewHolder) holder).profile.setColorFilter(DeviceUtils.getRandomMaterialColor(mContext, "400"));
                            ((LikeViewHolder) holder).iconText.setVisibility(View.VISIBLE);
                            if (employeesData.getName() != null && !employeesData.getName().equalsIgnoreCase("")){
                                if (split.length > 1){
                                    String first = split[0].substring(0,1);
                                    String last = split[1].substring(0,1);
                                    String initial = first + " "+ last ;
                                    ((LikeViewHolder) holder).iconText.setText(initial.toUpperCase());
                                } else {
                                    if (split[0] != null && split[0].trim().length() >= 1) {
                                        String first = split[0].substring(0, 1);
                                        ((LikeViewHolder) holder).iconText.setText(first.toUpperCase());
                                    }
                                }
                            }
                        } else {
                            /*Glide.with(AppController.getInstance()).load(profilePic)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .transform(new CircleTransform(mContext))
                                    .into(((LikeViewHolder) holder).profile);*/

                            ((LikeViewHolder) holder).profile.post(() -> {
                                Uri uri = Uri.parse(profilePic);
                                RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                                ((LikeViewHolder) holder).profile.setHierarchy(new GenericDraweeHierarchyBuilder(mContext.getResources())
                                        .setRoundingParams(roundingParams)
                                        .build());
                                roundingParams.setRoundAsCircle(true);
                                ((LikeViewHolder) holder).profile.setImageURI(uri);
                            });

                            ((LikeViewHolder) holder).profile.setColorFilter(null);
                            ((LikeViewHolder) holder).iconText.setVisibility(View.GONE);
                        }
                    }


                    ((LikeViewHolder) holder).profileName.setOnClickListener(v -> {
                        if (employeesData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                            goToUserProfile();
                        } else {
                            goToUserFriendProfile(employeesData.getUserId());
                        }
                    });
                    ((LikeViewHolder) holder).designation.setOnClickListener(v -> {
                        if (employeesData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                            goToUserProfile();
                        } else {
                            goToUserFriendProfile(employeesData.getUserId());
                        }
                    });
                    ((LikeViewHolder) holder).profile.setOnClickListener(v -> {
                        if (employeesData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                            goToUserProfile();
                        } else {
                            goToUserFriendProfile(employeesData.getUserId());
                        }
                    });
                }
            } else {
                final Employee.EmployeeData.Admin mAdmin = admin;
                final Employee.EmployeeData.Employees employeesData;
                if(mAdmin != null){
                    employeesData = employees.get(position - 1);
                } else {
                    employeesData = employees.get(position);
                }
                ((LikeViewHolder) holder).profileName.setText(employeesData.getName());

                if(isMyCompany){
                    ((LikeViewHolder) holder).overflow.setVisibility(View.VISIBLE);
                } else {
                    ((LikeViewHolder) holder).overflow.setVisibility(View.GONE);
                }

                ((LikeViewHolder) holder).overflow.setOnClickListener(v -> {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
                    LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.custom_confirm_dialog, null);
                    dialogBuilder.setView(dialogView);

                    final AlertDialog alertDialog = dialogBuilder.create();
                    Window window = alertDialog.getWindow();
                    if (window != null) {
                        window.setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        window.setGravity(Gravity.CENTER);
                    }

                    Button remove = (Button) dialogView.findViewById(R.id.remove);
                    Button cancel = (Button) dialogView.findViewById(R.id.cancel);
                    SimpleDraweeView profile = (SimpleDraweeView) dialogView.findViewById(R.id.icon_profile);
                    TextView iconText = (TextView) dialogView.findViewById(R.id.icon_text);

                    String[] split = employeesData.getName().split("\\s+");
                    String profilePic = employeesData.getProfilePic();
                    if (profilePic != null && !profilePic.equalsIgnoreCase("")){
                        if(profilePic.equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                                || profilePic.equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                            profile.setImageResource(R.drawable.bg_circle);
                            profile.setColorFilter(DeviceUtils.getRandomMaterialColor(mContext, "400"));
                            iconText.setVisibility(View.VISIBLE);
                            if (employeesData.getName() != null && !employeesData.getName().equalsIgnoreCase("")){
                                if (split.length > 1){
                                    String first = split[0].substring(0,1);
                                    String last = split[1].substring(0,1);
                                    String initial = first + " "+ last ;
                                    iconText.setText(initial.toUpperCase());
                                } else {
                                    if (split[0] != null && split[0].trim().length() >= 1) {
                                        String first = split[0].substring(0, 1);
                                        iconText.setText(first.toUpperCase());
                                    }
                                }
                            }
                        } else {
                            /*Glide.with(AppController.getInstance()).load(profilePic)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .transform(new CircleTransform(mContext))
                                    .into(profile);*/

                            profile.post(() -> {
                                Uri uri = Uri.parse(profilePic);
                                RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                                profile.setHierarchy(new GenericDraweeHierarchyBuilder(mContext.getResources())
                                        .setRoundingParams(roundingParams)
                                        .build());
                                roundingParams.setRoundAsCircle(true);
                                profile.setImageURI(uri);
                            });


                            profile.setColorFilter(null);
                            iconText.setVisibility(View.GONE);
                        }
                    }
                    cancel.setOnClickListener(v12 -> alertDialog.hide());
                    remove.setOnClickListener(v1 -> {
                        if(CheckNetworkConnection.isConnectionAvailable(mContext)){
                            if(listener != null)
                                listener.onOverFlow(v1, holder.getAdapterPosition());
                        } else {
                            SnackBarDialog.showNoInternetError(v1);
                        }

                        alertDialog.hide();

                    });

                    alertDialog.show();
                });

                String userPosition;
                if(employeesData.getDesignation() != null && !employeesData.getDesignation().equalsIgnoreCase("")){
                    userPosition = employeesData.getDesignation().trim();
                } else {
                    userPosition = "Trader";
                }

                String userCompany;
                if(employeesData.getUserCompany() != null && !employeesData.getUserCompany().equalsIgnoreCase("")){
                    userCompany = employeesData.getUserCompany().trim();
                } else {
                    userCompany = "";
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    SpannableStringBuilder spannedDetails;
                    if(!userPosition.equalsIgnoreCase("") && !userCompany.equalsIgnoreCase("")){
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+  "&#160"+"&#160"+"&#8226"+"&#160"+"&#160"+userCompany+"&#160" + "</font>", Html.FROM_HTML_MODE_LEGACY));
                    } else if (!userPosition.equalsIgnoreCase("") && userCompany.equalsIgnoreCase("")) {
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "&#160"+"</font>", Html.FROM_HTML_MODE_LEGACY));
                    }else if (userPosition.equalsIgnoreCase("") && !userCompany.equalsIgnoreCase("")) {
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                    } else if(!userPosition.equalsIgnoreCase("")){
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                    } else if(!userCompany.equalsIgnoreCase("")){
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                    } else {
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("", Html.FROM_HTML_MODE_LEGACY));
                    }
                    ((LikeViewHolder) holder).designation.setText(spannedDetails);
                    ((LikeViewHolder) holder).designation.setVisibility(View.VISIBLE);
                } else {
                    SpannableStringBuilder spannedDetails;
                    if(!userPosition.equalsIgnoreCase("") && !userCompany.equalsIgnoreCase("")){
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+  "&#160"+"&#160"+ "&#8226"+"&#160"+"&#160"+userCompany+"&#160" + "</font>"));
                    } else if (!userPosition.equalsIgnoreCase("") && userCompany.equalsIgnoreCase("")) {
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "</font>"));
                    }else if (userPosition.equalsIgnoreCase("") && !userCompany.equalsIgnoreCase("")) {
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "</font>"));
                    } else if(!userPosition.equalsIgnoreCase("")){
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "</font>"));
                    } else if(!userCompany.equalsIgnoreCase("")){
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "</font>"));
                    } else {
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml(""));
                    }
                    ((LikeViewHolder) holder).designation.setText(spannedDetails);
                    ((LikeViewHolder) holder).designation.setVisibility(View.VISIBLE);
                }

                if(employeesData.getModerator() == 1) {
                    ((LikeViewHolder) holder).top.setText(R.string.mod);
                    ((LikeViewHolder) holder).top.setVisibility(View.VISIBLE);
                    ((LikeViewHolder) holder).top.setBackground(ContextCompat.getDrawable(mContext, R.drawable.top_mod));
                } else {
                    if (Integer.parseInt(UserUtils.parsingInteger(employeesData.getRank())) >= 1 && Integer.parseInt(UserUtils.parsingInteger(employeesData.getRank())) <=10) {
                        ((LikeViewHolder) holder).top.setVisibility(View.VISIBLE);
                        ((LikeViewHolder) holder).top.setText("TOP "+employeesData.getRank());

                    } else {
                        if(employeesData.isNeJoined()){
                            ((LikeViewHolder) holder).top.setVisibility(View.VISIBLE);
                            ((LikeViewHolder) holder).top.setBackground(ContextCompat.getDrawable(mContext, R.drawable.top_red));
                        } else {
                            ((LikeViewHolder) holder).top.setVisibility(View.GONE);
                            ((LikeViewHolder) holder).top.setBackground(null);
                        }
                    }
                }



                ((LikeViewHolder) holder).designation.setTextColor(ContextCompat.getColor(mContext,R.color.msSecondaryTextColor));
                if(employeesData.getCountry() != null && !employeesData.getCountry().equalsIgnoreCase("")){
                    ((LikeViewHolder) holder).company.setVisibility(View.VISIBLE);
                    ((LikeViewHolder) holder).company.setText(employeesData.getCountry());
                } else {
                    ((LikeViewHolder) holder).company.setVisibility(View.INVISIBLE);
                }


                String[] split = employeesData.getName().split("\\s+");
                String profilePic = employeesData.getProfilePic();
                if (profilePic != null && !profilePic.equalsIgnoreCase("")){
                    if(profilePic.equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                            || profilePic.equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                        ((LikeViewHolder) holder).profile.setImageResource(R.drawable.bg_circle);
                        ((LikeViewHolder) holder).profile.setColorFilter(DeviceUtils.getRandomMaterialColor(mContext, "400"));
                        ((LikeViewHolder) holder).iconText.setVisibility(View.VISIBLE);
                        if (employeesData.getName() != null && !employeesData.getName().equalsIgnoreCase("")){
                            if (split.length > 1){
                                String first = split[0].substring(0,1);
                                String last = split[1].substring(0,1);
                                String initial = first + " "+ last ;
                                ((LikeViewHolder) holder).iconText.setText(initial.toUpperCase());
                            } else {
                                if (split[0] != null && split[0].trim().length() >= 1) {
                                    String first = split[0].substring(0, 1);
                                    ((LikeViewHolder) holder).iconText.setText(first.toUpperCase());
                                }
                            }
                        }
                    } else {
                        /*Glide.with(AppController.getInstance()).load(profilePic)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .transform(new CircleTransform(mContext))
                                .into(((LikeViewHolder) holder).profile);*/
                        ((LikeViewHolder) holder).profile.post(() -> {
                            Uri uri = Uri.parse(profilePic);
                            RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                            ((LikeViewHolder) holder).profile.setHierarchy(new GenericDraweeHierarchyBuilder(mContext.getResources())
                                    .setRoundingParams(roundingParams)
                                    .build());
                            roundingParams.setRoundAsCircle(true);
                            ((LikeViewHolder) holder).profile.setImageURI(uri);
                        });

                        ((LikeViewHolder) holder).profile.setColorFilter(null);
                        ((LikeViewHolder) holder).iconText.setVisibility(View.GONE);
                    }
                }


                ((LikeViewHolder) holder).profileName.setOnClickListener(v -> {
                    if (employeesData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                        goToUserProfile();
                    } else {
                        goToUserFriendProfile(employeesData.getUserId());
                    }
                });

                ((LikeViewHolder) holder).designation.setOnClickListener(v -> {
                    if (employeesData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                        goToUserProfile();
                    } else {
                        goToUserFriendProfile(employeesData.getUserId());
                    }
                });

                ((LikeViewHolder) holder).profile.setOnClickListener(v -> {
                    if (employeesData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                        goToUserProfile();
                    } else {
                        goToUserFriendProfile(employeesData.getUserId());
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {

        if(admin != null){
            if(employees != null){
                if(employees.size() > 0){
                    return employees.size() + 1;
                } else {
                    return 0;
                }
            } else {
                return 1;
            }
        } else {
            if(employees.size() > 0) {
                return employees.size();
            }
        }
        return 0;
    }

    private void goToUserProfile() {
        Intent i = new Intent(mContext, UserProfileActivity.class);
        mContext.startActivity(i);
        if (CheckOsVersion.isPreLollipop()) {
            if(mContext != null)
                ((Activity)mContext).overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    private void goToUserFriendProfile(String postedUserId) {
        final Intent intent = new Intent(mContext, UserFriendProfileActivity.class);
        intent.putExtra("friendId", postedUserId);
        mContext.startActivity(intent);
        if(CheckOsVersion.isPreLollipop())
            if(mContext != null)
                ((Activity)mContext).overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    public void swap(Employee.EmployeeData employeeData, boolean isMyCompany) {
        if(employeeData != null){
            this.employees = employeeData.getEmployees();
            this.admin = employeeData.getAdmin();
            this.isMyCompany = isMyCompany;
            this.notifyDataSetChanged();
        }
    }

    private class LikeViewHolder extends RecyclerView.ViewHolder {
        private SimpleDraweeView profile;
        private ImageView overflow;
        private TextView profileName, iconText, top, points, designation, company;
        public LikeViewHolder(View view) {
            super(view);
            profile = (SimpleDraweeView)view.findViewById(R.id.profile_photo);
            overflow = (ImageView)view.findViewById(R.id.overflow);
            iconText = (TextView)view.findViewById(R.id.icon_text);
            top = (TextView)view.findViewById(R.id.top);
            points = (TextView)view.findViewById(R.id.points);
            profileName = (TextView)view.findViewById(R.id.name);
            designation = (TextView)view.findViewById(R.id.designation);
            company = (TextView)view.findViewById(R.id.company);
        }
    }

    public interface OnItemClickListener {
        void  onOverFlow(View v, int position);
    }
}
