package com.myscrap.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.myscrap.R;
import com.myscrap.UserFriendProfileActivity;
import com.myscrap.UserProfileActivity;
import com.myscrap.application.AppController;
import com.myscrap.model.EmployeeRequest;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by ms3 on 5/16/2017.
 */

public class EmployeeRequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Context mContext;
    private String companyId;
    private List<EmployeeRequest.EmployeeRequestData> employees = new ArrayList<>();
    private EmployeeRequestAdapter mEmployeeRequestAdapter;

    public EmployeeRequestAdapter(Context context, String mCompanyId, List<EmployeeRequest.EmployeeRequestData> employeeData){
        this.mContext = context;
        this.companyId = mCompanyId;
        this.mEmployeeRequestAdapter = this;
        if(employeeData != null){
            this.employees = employeeData;
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_employee_request, parent, false);
        return new LikeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof LikeViewHolder) {

            final EmployeeRequest.EmployeeRequestData employeesData = employees.get(position);
            ((LikeViewHolder) holder).profileName.setText(employeesData.getName());

            if (employeesData.getDesignation() != null && !employeesData.getDesignation().equalsIgnoreCase("")) {
                ((LikeViewHolder) holder).designation.setText(employeesData.getDesignation());
                ((LikeViewHolder) holder).designation.setVisibility(View.VISIBLE);
            } else {
                ((LikeViewHolder) holder).designation.setText("Trader");
                ((LikeViewHolder) holder).designation.setVisibility(View.VISIBLE);
            }

            String[] split = employeesData.getName().split("\\s+");
            String profilePic = employeesData.getProfilePic();
            if (profilePic != null && !profilePic.equalsIgnoreCase("")){
                if(profilePic.equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                        || profilePic.equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                    ((LikeViewHolder) holder).profile.setImageResource(R.drawable.bg_circle);
                    if(employeesData.getColorCode() != null && !employeesData.getColorCode().equalsIgnoreCase("") && employeesData.getColorCode().startsWith("#")){
                        ((LikeViewHolder) holder).profile.setColorFilter(Color.parseColor(employeesData.getColorCode()));
                    } else {
                        ((LikeViewHolder) holder).profile.setColorFilter(DeviceUtils.getRandomMaterialColor(mContext, "400"));
                    }

                    ((LikeViewHolder) holder).iconText.setVisibility(View.VISIBLE);
                    if (employeesData.getName() != null && !employeesData.getName().equalsIgnoreCase("")){
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
                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;
                if (employeesData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                    goToUserProfile();
                } else {
                    goToUserFriendProfile(employeesData.getUserId());
                }
            });
            ((LikeViewHolder) holder).designation.setOnClickListener(v -> {
                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;
                if (employeesData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                    goToUserProfile();
                } else {
                    goToUserFriendProfile(employeesData.getUserId());
                }
            });
            ((LikeViewHolder) holder).profile.setOnClickListener(v -> {
                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;
                if (employeesData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                    goToUserProfile();
                } else {
                    goToUserFriendProfile(employeesData.getUserId());
                }
            });

            ((LikeViewHolder) holder).accept.setOnClickListener(v -> {
                doAcceptReject("acceptId", employeesData.getUserId());
                if(employees != null){
                    employees.remove(holder.getAdapterPosition());
                    notifyItemRemoved(holder.getAdapterPosition());
                    mEmployeeRequestAdapter.swap(employees, companyId);
                }

            });

            ((LikeViewHolder) holder).reject.setOnClickListener(v -> {
                doAcceptReject(employeesData.getUserId(), "removeId");
                if(employees != null){
                    employees.remove(holder.getAdapterPosition());
                    notifyItemRemoved(holder.getAdapterPosition());
                    mEmployeeRequestAdapter.swap(employees, companyId);
                }
            });
        }
    }

    private void doAcceptReject(String acceptId, String removeId) {
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());

        if(acceptId.equalsIgnoreCase("acceptId")){
            acceptId = removeId;
            removeId = "";
        } else if(removeId.equalsIgnoreCase("removeId")){
            removeId = acceptId;
            acceptId = "";
        }

        Call<EmployeeRequest> call = apiService.companyEmployeeRequest(companyId, acceptId, removeId, apiKey);
        call.enqueue(new Callback<EmployeeRequest>() {
            @Override
            public void onResponse(@NonNull Call<EmployeeRequest> call, @NonNull retrofit2.Response<EmployeeRequest> response) {
                Log.d("loadEmployeeDetails", "onSuccess");
                if(response.body() != null && response.isSuccessful()){
                    EmployeeRequest mEmployee = response.body();
                    if (mEmployee != null && !mEmployee.isErrorStatus()) {
                        employees = mEmployee.getEmployeeRequestData();
                        if (employees != null)
                            mEmployeeRequestAdapter.swap(employees, companyId);
                        else {
                            mEmployeeRequestAdapter.swap(new ArrayList<>(), companyId);
                        }
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<EmployeeRequest> call, @NonNull Throwable t) {
                Log.d("loadEmployeeDetails", "onFailure");
            }
        });
    }

    @Override
    public int getItemCount() {
        return employees.size();
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

    public void swap(List<EmployeeRequest.EmployeeRequestData> employeeData, String mCompanyId) {
        if(employeeData != null){
            this.employees = employeeData;
            this.companyId = mCompanyId;
            this.notifyDataSetChanged();
        }
    }

    private class LikeViewHolder extends RecyclerView.ViewHolder {
        private SimpleDraweeView profile;
        private TextView profileName, iconText, designation, company, accept, reject;
        public LikeViewHolder(View view) {
            super(view);
            profile = (SimpleDraweeView) view.findViewById(R.id.profile_photo);
            iconText = (TextView)view.findViewById(R.id.icon_text);
            profileName = (TextView)view.findViewById(R.id.name);
            designation = (TextView)view.findViewById(R.id.designation);
            company = (TextView)view.findViewById(R.id.company);
            accept = (TextView)view.findViewById(R.id.accept);
            reject = (TextView)view.findViewById(R.id.reject);
        }
    }
}
