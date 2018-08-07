package com.myscrap;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.adapters.InvitationAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.Contact;
import com.myscrap.model.EventInvitations;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.view.DividerItemDecoration;
import com.myscrap.view.PreCachingLayoutManager;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class EventInvitationsFragment extends Fragment implements InvitationAdapter.InvitationAdapterListener{

    private SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView mRecyclerView;
    private List<EventInvitations.EventInvitationsData> mEventInvitations = new ArrayList<>();
    private InvitationAdapter mInvitationAdapter;
    private Tracker mTracker;
    private View emptyView;
    public EventInvitationsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = AppController.getInstance().getDefaultTracker();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_event_invitations, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe);
        emptyView = v.findViewById(R.id.empty);
        UserUtils.setEmptyView(emptyView, R.drawable.ic_no_invitation, "No Invitations", true);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.invitations_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        PreCachingLayoutManager linearLayoutManager = new PreCachingLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setExtraLayoutSpace(DeviceUtils.getScreenHeight(getActivity()));
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
        mSwipeRefreshLayout.setDistanceToTriggerSync(30);

        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        InvitationAdapter.InvitationAdapterListener listener = this;
        mInvitationAdapter = new InvitationAdapter(getActivity(), mEventInvitations, listener);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(AppController.getInstance(), LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mInvitationAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if(getActivity() != null && CheckNetworkConnection.isConnectionAvailable(getActivity())){
                loadEventInvitations();
            } else {
                SnackBarDialog.showNoInternetError(mSwipeRefreshLayout);
            }

        });
        return v;
    }

    private void loadEventInvitations() {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            if(mSwipeRefreshLayout != null && !mSwipeRefreshLayout.isRefreshing())
                mSwipeRefreshLayout.setRefreshing(true);
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(getActivity());
            Call<EventInvitations> call = apiService.invitations(userId, apiKey);
            call.enqueue(new Callback<EventInvitations>() {
                @Override
                public void onResponse(@NonNull Call<EventInvitations> call, @NonNull Response<EventInvitations> response) {
                    Log.d("loadEventInvitations", "onSuccess");
                    if (response.body() != null) {
                        if (mEventInvitations != null)
                            mEventInvitations.clear();
                        if(mSwipeRefreshLayout != null)
                            mSwipeRefreshLayout.setRefreshing(false);
                        EventInvitations eventInvitations = response.body();
                        if (eventInvitations != null) {
                            if(!eventInvitations.isErrorStatus()){
                                if (eventInvitations.getEventInvitationsDataList() != null){
                                    if (eventInvitations.getEventInvitationsDataList().isEmpty()) {
                                        emptyView.setVisibility(View.VISIBLE);
                                        mSwipeRefreshLayout.setVisibility(View.GONE);
                                    } else {
                                        mEventInvitations.addAll(eventInvitations.getEventInvitationsDataList());
                                        mInvitationAdapter.notifyDataSetChanged();
                                        emptyView.setVisibility(View.GONE);
                                        mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    emptyView.setVisibility(View.VISIBLE);
                                    mSwipeRefreshLayout.setVisibility(View.GONE);
                                }
                            } else {
                                if(mSwipeRefreshLayout != null)
                                    SnackBarDialog.show(mSwipeRefreshLayout, eventInvitations.getStatus());
                            }
                        }
                    }
                }
                @Override
                public void onFailure(@NonNull Call<EventInvitations> call, @NonNull Throwable t) {
                    Log.d("loadEventInvitations", "onFailure");
                    if(mSwipeRefreshLayout != null)
                        mSwipeRefreshLayout.setRefreshing(false);
                    emptyView.setVisibility(View.VISIBLE);
                    if(mSwipeRefreshLayout != null)
                        mSwipeRefreshLayout.setVisibility(View.GONE);
                }
            });
        } else {
            if(mSwipeRefreshLayout != null)
                SnackBarDialog.showNoInternetError(mSwipeRefreshLayout);
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("Invite Accept Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        loadEventInvitations();
        //UserOnlineStatus.setUserOnline(AppController.getInstance(), UserOnlineStatus.ONLINE);
    }

    @Override
    public void onPause() {
        super.onPause();
        //UserOnlineStatus.setUserOnline(this, UserOnlineStatus.OFFLINE);
    }


    private void inviteContacts(Contact.ContactData mData) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(getActivity());
            String friendId = mData.getUserId();
            Call<JSONObject> call = apiService.inviteContacts(userId,friendId, apiKey);
            call.enqueue(new Callback<JSONObject>() {
                @Override
                public void onResponse(@NonNull Call<JSONObject> call, @NonNull Response<JSONObject> response) {
                    Log.d("addToContacts", "onSuccess");
                }
                @Override
                public void onFailure(@NonNull Call<JSONObject> call, @NonNull Throwable t) {
                    Log.d("addToContacts", "onFailure");
                }
            });
        } else {

        }
    }

    @Override
    public void onStarClicked(EventInvitations.EventInvitationsData mEventInvitationsData, boolean isStarred) {

    }
}
