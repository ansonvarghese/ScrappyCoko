package com.myscrap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.adapters.NotificationFragmentAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.Notification;
import com.myscrap.notification.Config;
import com.myscrap.notification.NotificationUtils;
import com.myscrap.utils.ProgressBarDialog;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.view.RecyclerTouchListener;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class NotificationFragment extends Fragment
{

    private View mNotificationFragment;
    private SwipeRefreshLayout swipe;
    private NotificationFragmentAdapter mNotificationFragmentAdapter;
    private List<Notification.NotificationData> mNotificationData = new ArrayList<>();
    private LinearLayout empty;
    private boolean  hasNotification;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private Tracker mTracker;
    public static NotificationFragmentAdapter adapter;

    Call<Notification> call;

    public NotificationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mTracker = AppController.getInstance().getDefaultTracker();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        mNotificationFragment = inflater.inflate(R.layout.fragment_notification, container, false);
        RecyclerView mRecyclerView = (RecyclerView) mNotificationFragment.findViewById(R.id.recycler_view);
        empty = (LinearLayout) mNotificationFragment.findViewById(R.id.notification_empty);
        swipe = (SwipeRefreshLayout) mNotificationFragment.findViewById(R.id.swipe);
        swipe.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
        swipe.setDistanceToTriggerSync(30);//
        swipe.setOnRefreshListener(() ->
        {
            if(CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
                if(swipe != null)
                    swipe.setRefreshing(true);
                loadNotifications();
            }
        });

        //mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mNotificationFragmentAdapter = new NotificationFragmentAdapter(getActivity(), mNotificationData);
   //     adapter = new NotificationFragmentAdapter(getActivity(),mNotificationData);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mNotificationFragmentAdapter);

        // here I made changes
  //      UserUtils.setFRNotificationCount(getActivity(), "0");
 //       UserUtils.setNotificationCount(getActivity(), "0");
 //       HomeActivity.clearALLNotification();

        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position)
            {
                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;
                if(mNotificationData != null && mNotificationData.size() > 0)
                {
                    final Notification.NotificationData notificationItem = mNotificationData.get(position);
                    if (notificationItem.getType() != null && notificationItem.getType().equalsIgnoreCase("post"))
                    {
                        Intent intent = new Intent(getActivity(), DetailedPostActivity.class);
                        intent.putExtra("notId", notificationItem.getNot_id());
                        intent.putExtra("postId", notificationItem.getPostId());
                        intent.putExtra("userId", AppController.getInstance().getPrefManager().getUser().getId());
                        startActivity(intent);
                    }

                    else if(notificationItem.getType() != null && notificationItem.getType().equalsIgnoreCase("doubleComment"))
                    {
                        Intent intent = new Intent(getActivity(), DetailedPostActivity.class);
                        intent.putExtra("notId", notificationItem.getNot_id());
                        intent.putExtra("postId", notificationItem.getPostId());
                        intent.putExtra("userId", AppController.getInstance().getPrefManager().getUser().getId());
                        startActivity(intent);
                    }
                    else if (notificationItem.getType() != null && notificationItem.getType().equalsIgnoreCase("user")) {
                        Intent i = new Intent(AppController.getInstance(), UserFriendProfileActivity.class);
                        i.putExtra("friendId", "" + notificationItem.getPostUserId());
                        i.putExtra("notId", notificationItem.getNot_id());
                        startActivity(i);
                    }
                    else if (notificationItem.getType() != null && notificationItem.getType().equalsIgnoreCase("bumped"))
                    {
                        Intent i = new Intent(AppController.getInstance(), UserFriendProfileActivity.class);
                        i.putExtra("friendId", "" + notificationItem.getPostUserId());
                        i.putExtra("notId", notificationItem.getNot_id());
                        startActivity(i);
                    }
                    else if (notificationItem.getType() != null && notificationItem.getType().equalsIgnoreCase("company"))
                    {
                        if(notificationItem.getCompanyId() != null && !notificationItem.getCompanyId().equalsIgnoreCase("") && !notificationItem.getCompanyId().equalsIgnoreCase("0"))
                        {
                            Intent i = new Intent(AppController.getInstance(), CompanyProfileActivity.class);
                            i.putExtra("companyId", notificationItem.getCompanyId());
                            i.putExtra("notId", notificationItem.getNot_id());
                            startActivity(i);
                        }
                    }
                    else if (notificationItem.getType() != null && notificationItem.getType().equalsIgnoreCase("event"))
                    {
                        if(notificationItem.getPostId() != null && !notificationItem.getPostId().equalsIgnoreCase("") && !notificationItem.getPostId().equalsIgnoreCase("0")){
                            Intent i = new Intent(AppController.getInstance(), EventDetailActivity.class);
                            i.putExtra("eventId", notificationItem.getPostId());
                            i.putExtra("notId", notificationItem.getNot_id());
                            startActivity(i);
                        }
                    }
                    else if (notificationItem.getType() != null && notificationItem.getType().equalsIgnoreCase("missedActivity"))
                    {
                        /*if(notificationItem.getPostId() != null && !notificationItem.getPostId().equalsIgnoreCase("") && !notificationItem.getPostId().equalsIgnoreCase("0")){
                            Intent i = new Intent(AppController.getInstance(), EventDetailActivity.class);
                            i.putExtra("eventId", notificationItem.getPostId());
                            i.putExtra("notId", notificationItem.getNot_id());
                            startActivity(i);
                        }*/
                    }
                }
            }


            @Override
            public void onLongClick(View view, int position)
            {

            }
        }));
        //NotificationUtils.clearNotifications();
        mRegistrationBroadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, final Intent intent)
            {
                if (intent != null && intent.getAction() != null)
                {
                    if (intent.getAction().equalsIgnoreCase(Config.NOTIFICATION))
                    {
                        int count = Integer.parseInt(UserUtils.parsingInteger(intent.getStringExtra("notificationCount")));
                        UserUtils.setNotificationCount(getActivity(), String.valueOf(count));
                        HomeActivity.notification();
                        loadNotifications();
                    }
                    else if (intent.getAction().equalsIgnoreCase(Config.FRIEND_REQUEST_NOTIFICATION))
                    {
                        int count = Integer.parseInt(UserUtils.parsingInteger(intent.getStringExtra("friendNotificationCount")));
                        UserUtils.setFRNotificationCount(getActivity(), String.valueOf(count));
                        HomeActivity.notification();
                    }
                }
            }
        };


        return mNotificationFragment;
    }


    @Override
    public void onDestroy() {
        if (call != null) {
            call.cancel();
        }
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        if(hasNotification){
            inflater.inflate(R.menu.notification, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("Notification Fragment Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        //NotificationUtils.clearNotifications();
        EventBus.getDefault().register(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.NOTIFICATION);
        if(getActivity() != null)
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mRegistrationBroadcastReceiver, filter);
        if(swipe != null) {
            swipe.post(() -> {
                if(CheckNetworkConnection.isConnectionAvailable(getActivity())){
                    if(swipe != null)
                        swipe.setRefreshing(true);
                    loadNotifications();
                } else {
                    if(swipe != null)
                        swipe.setRefreshing(false);
                    if(mNotificationFragment != null) {
                        SnackBarDialog.showNoInternetError(mNotificationFragment);
                    }
                }
            });
        }
    }

    @Subscribe
    public void getMessage(String  load) {
        loadNotifications();
    }
        @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
            if(getActivity() != null)
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mRegistrationBroadcastReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        if(item.getItemId() == R.id.clear)
        {
            clearNotification();
            HomeActivity.clearALLNotification();
        }
        return true;
    }


    public  void clearNotification()
    {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            ProgressBarDialog.showLoader(getActivity(), false);
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(getActivity());

            Toast.makeText(getActivity(),userId+"\n"+apiKey, Toast.LENGTH_SHORT).show();

            Call<Notification> call = apiService.clearNotification(userId, apiKey);
            call.enqueue(new Callback<Notification>()
            {
                @Override
                public void onResponse(@NonNull Call<Notification> call, @NonNull Response<Notification> response)
                {
                    ProgressBarDialog.dismissLoader();
                    if(swipe != null)
                        swipe.setRefreshing(false);
                    Log.d("clearNotification", "onSuccess");
                    if (response.body() != null && response.isSuccessful())
                    {
                       Notification mNotification = response.body();
                        if(mNotification != null && !mNotification.isErrorStatus()){
                            if(mNotificationData != null && mNotificationFragmentAdapter != null){
                                mNotificationData.clear();
                                mNotificationFragmentAdapter.notifyDataSetChanged();
                                if(swipe != null) {
                                    swipe.setVisibility(View.GONE);
                                    swipe.setEnabled(false);
                                }
                                if(empty != null)
                                empty.setVisibility(View.VISIBLE);
                                hasNotification = false;
                                if(getActivity() != null)
                                getActivity().invalidateOptionsMenu();
                            }
                        }
                        if(mNotificationData != null && mNotificationFragmentAdapter != null){
                            mNotificationData.clear();
                            mNotificationFragmentAdapter.notifyDataSetChanged();
                            if(swipe != null){
                                swipe.setVisibility(View.GONE);
                                swipe.setEnabled(false);
                            }
                            if(empty != null)
                            empty.setVisibility(View.VISIBLE);
                            hasNotification = false;
                            if(getActivity() != null)
                            getActivity().invalidateOptionsMenu();
                        }
                    }
                }
                @Override
                public void onFailure(@NonNull Call<Notification> call, @NonNull Throwable t) {
                    Log.d("clearNotification", "onFailure");
                    if(swipe != null)
                        swipe.setRefreshing(false);
                }
            });
        } else {
            if(mNotificationFragment != null)
                SnackBarDialog.showNoInternetError(mNotificationFragment);
        }
    }

    public void loadNotifications()
    {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            /*if(swipe != null && !swipe.isRefreshing())
                ProgressBarDialog.showLoader(getActivity(), false);*/
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(getActivity());
            call = apiService.notification(userId,"0", apiKey);
            call.enqueue(new Callback<Notification>() {
                @Override
                public void onResponse(@NonNull Call<Notification> call, @NonNull Response<Notification> response) {
                    //ProgressBarDialog.dismissLoader();
                    UserUtils.setNotificationCount(getActivity(), "0");
                    HomeActivity.notification();
                    NotificationUtils.clearNotifications();
                    Log.d("loadNotifications", "onSuccess");
                    if (response.body() != null) {
                        Notification mNotification = response.body();
                        if(mNotification != null && !mNotification.isErrorStatus()) {
                            List<Notification.NotificationData> data = mNotification.getData();
                            if(mNotificationData != null && mNotificationFragmentAdapter != null) {
                                if (data != null && data.size() > 0) {
                                    if (swipe != null) {
                                        swipe.setVisibility(View.VISIBLE);
                                        swipe.setEnabled(true);
                                    }
                                    if (empty != null)
                                        empty.setVisibility(View.GONE);
                                    mNotificationData.clear();
                                    hasNotification = true;
                                    if(getActivity() != null)
                                        getActivity().invalidateOptionsMenu();
                                    mNotificationData.addAll(filtering(data));
                                    mNotificationFragmentAdapter.notifyDataSetChanged();
                                } else {
                                    hasNotification = false;
                                    if (swipe != null) {
                                        swipe.setVisibility(View.GONE);
                                        swipe.setEnabled(false);
                                    }
                                    if (empty != null)
                                        empty.setVisibility(View.VISIBLE);
                                }
                            }
                        } else {
                            hasNotification = false;
                            if(swipe != null) {
                                swipe.setVisibility(View.GONE);
                                swipe.setEnabled(false);
                            }
                            if(empty != null)
                            empty.setVisibility(View.VISIBLE);
                        }
                        if(swipe != null)
                            swipe.setRefreshing(false);
                    }
                }
                @Override
                public void onFailure(@NonNull Call<Notification> call, @NonNull Throwable t) {
                    //ProgressBarDialog.dismissLoader();
                    Log.d("loadNotifications", "onFailure");
                    if(swipe != null)
                        swipe.setRefreshing(false);
                }
            });
        } else {
            if(mNotificationFragment != null)
                SnackBarDialog.showNoInternetError(mNotificationFragment);
        }
    }

    public static void refreshAdapter()
    {
        adapter.notifyDataSetChanged();
    }


    private List<Notification.NotificationData> filtering(List<Notification.NotificationData> notificationDataList) {
        if (notificationDataList == null)
            return null;
        List<Notification.NotificationData> newNotifications = new ArrayList<>();
        List<Notification.NotificationData> oldNotifications = new ArrayList<>();
        List<Notification.NotificationData> notifications = new ArrayList<>();

        for (Notification.NotificationData data : notificationDataList) {
            if (data.isNew()){
                newNotifications.add(data);
            } else {
                oldNotifications.add(data);
            }
        }

        if (!newNotifications.isEmpty()){
            newNotifications.get(0).setTitle(true);
        }

        if (!oldNotifications.isEmpty()){
            oldNotifications.get(0).setTitle(true);
        }

        if(!newNotifications.isEmpty())
           notifications.addAll(newNotifications);
        if(!oldNotifications.isEmpty())
            notifications.addAll(oldNotifications);
        return notifications;
    }


    public static NotificationFragment newInstance()
    {
        return  new NotificationFragment();
    }
}
