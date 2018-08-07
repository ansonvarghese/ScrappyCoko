package com.myscrap;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.adapters.FavouriteFragmentAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.Favourite;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.ProgressBarDialog;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.view.DividerItemDecoration;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class ContactFavourites extends Fragment implements FavouriteFragmentAdapter.FavouriteFragmentAdapterListener, SearchView.OnQueryTextListener{

    private FavouriteFragmentAdapter mContactsFragmentAdapter;
    private List<Favourite.FavouriteData> mFavouriteData = new ArrayList<>();
    private SwipeRefreshLayout swipe;
    private RecyclerView mFavouriteRecyclerView;
    private boolean isSearchViewOpen;
    private List<Favourite.FavouriteData> filteredModelListCopy = new ArrayList<>();
    FavouriteFragmentAdapter.FavouriteFragmentAdapterListener listener;
    private Tracker mTracker;
    private View emptyView;

    public ContactFavourites() {
        setHasOptionsMenu(false);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        mTracker = AppController.getInstance().getDefaultTracker();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mFavouritesFragment = inflater.inflate(R.layout.fragment_contact_favourites, container, false);
        mFavouriteRecyclerView = (RecyclerView) mFavouritesFragment.findViewById(R.id.recycler_view_contact_favourite);
        swipe = (SwipeRefreshLayout) mFavouritesFragment.findViewById(R.id.swipe);
        emptyView = mFavouritesFragment.findViewById(R.id.empty);
        UserUtils.setEmptyView(emptyView, R.drawable.ic_favorites_empty, "No Favourite Members", false);
        listener = this;
        swipe.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
        swipe.setDistanceToTriggerSync(30);
        swipe.setOnRefreshListener(() -> {
            if(CheckNetworkConnection.isConnectionAvailable(getActivity())){
                if(swipe != null)
                    swipe.setRefreshing(true);
                loadFavouriteContacts();
            }
        });
        mFavouriteRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mFavouriteRecyclerView.setLayoutManager(layoutManager);
        mFavouriteRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mFavouriteRecyclerView.setHasFixedSize(true);
        mFavouriteRecyclerView.setNestedScrollingEnabled(false);
        mContactsFragmentAdapter = new FavouriteFragmentAdapter(getActivity(), mFavouriteData, listener);
        mFavouriteRecyclerView.setAdapter(mContactsFragmentAdapter);
        return mFavouritesFragment;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

    }

    private void loadData() {
        if(CheckNetworkConnection.isConnectionAvailable(getActivity())){
            if(swipe != null){
                swipe.post(() -> {
                    swipe.setRefreshing(true);
                    loadFavouriteContacts();
                });
            }
        } else {
            SnackBarDialog.showNoInternetError(mFavouriteRecyclerView);
        }
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        isSearchViewOpen = newText != null && !newText.trim().equalsIgnoreCase("");
        final List<Favourite.FavouriteData> filteredModelList = filter(mFavouriteData, newText);
        filteredModelListCopy.clear();
        filteredModelListCopy.addAll(filteredModelList);
        if (filteredModelList.size() > 0) {
            mContactsFragmentAdapter.setFilter(filteredModelList);
            return true;
        } else {
            Toast.makeText(AppController.getInstance(), "Not Found", Toast.LENGTH_SHORT).show();
            return false;
        }
    }


    private List<Favourite.FavouriteData> filter(List<Favourite.FavouriteData> favoriteData, String query) {
        final List<Favourite.FavouriteData> favoriteDataCopy = new ArrayList<>();
        if(!TextUtils.isEmpty(query)){
            for (Favourite.FavouriteData data : favoriteData){
                final String text = data.getName().toLowerCase().toLowerCase();
                if ( text.contains(query.toLowerCase())) {
                    favoriteDataCopy.add(data);
                }
            }
            return favoriteDataCopy;
        } else {
            return mFavouriteData;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        HomeActivity.isContactFavourites = true;
        loadData();
        if(mTracker != null){
            mTracker.setScreenName("Contact Favourite Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        HomeActivity.isContactFavourites = true;
    }

    private void scrollToBottom() {
        if(mFavouriteRecyclerView != null && getActivity() != null) {
            AppController.runOnUIThread(() -> {
                if(mContactsFragmentAdapter != null) {
                    mContactsFragmentAdapter.swap(mFavouriteData);
                }
            });
        }

    }

    private void loadFavouriteContacts() {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(getActivity());
            apiService.favourites(userId, apiKey)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<Favourite>() {
                @Override
                public void onCompleted() {
                    Log.d("loadFavouriteContacts", "onSuccess");
                }

                @Override
                public void onError(Throwable e) {
                    ProgressBarDialog.dismissLoader();
                    Log.d("loadFavouriteContacts", "onFailure");
                    if(swipe != null)
                        swipe.setRefreshing(false);
                }

                @Override
                public void onNext(Favourite mFavourite) {
                    ProgressBarDialog.dismissLoader();
                    if (mFavouriteData != null)
                        mFavouriteData.clear();
                    if(swipe != null)
                        swipe.setRefreshing(false);
                    if (mFavourite != null) {
                        parseData(mFavourite);
                    }
                }
            });
        } else {
            if(swipe != null)
                SnackBarDialog.showNoInternetError(swipe);
        }
    }

    private void parseData(Favourite mFavourite) {
        if(!mFavourite.isErrorStatus()){
            if (mFavourite.getData() != null) {
                mFavouriteData.addAll(mFavourite.getData());
                if(mContactsFragmentAdapter != null){
                    mContactsFragmentAdapter.notifyDataSetChanged();
                }
            } else {
                if(swipe != null)
                    SnackBarDialog.show(swipe, "No favourite members");
            }
            if (!mFavouriteData.isEmpty()) {
                swipe.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            } else {
                swipe.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }
        } else {
            if(swipe != null)
                SnackBarDialog.show(swipe, mFavourite.getStatus());
        }
    }

    @Override
    public void onFavouritesAdapterClicked(int position) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if(isSearchViewOpen) {
            if(filteredModelListCopy != null && filteredModelListCopy.size() > 0) {
                Favourite.FavouriteData mData = filteredModelListCopy.get(position);
                if(mData != null) {
                    if(mData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                        goToUserProfile();
                    } else {
                        goToUserFriendProfile(mData.getUserId());
                    }
                }
            }
        } else {
            if(mFavouriteData != null && mFavouriteData.size() > 0) {
                Favourite.FavouriteData mData = mFavouriteData.get(position);
                if(mData != null) {
                    if(mData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                        goToUserProfile();
                    } else {
                        goToUserFriendProfile(mData.getUserId());
                    }
                }
            }
        }


    }

    private void goToUserProfile() {
        Intent i = new Intent(getActivity(), UserProfileActivity.class);
        startActivity(i);
        if (CheckOsVersion.isPreLollipop() && getActivity() != null) {
            getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    private void goToUserFriendProfile(String postedUserId) {
        final Intent intent = new Intent(getActivity(), UserFriendProfileActivity.class);
        intent.putExtra("friendId", postedUserId);
        startActivity(intent);
        if(CheckOsVersion.isPreLollipop())
            if(getActivity() != null)
                getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public void onStarClicked(int position, boolean isStarred) {
        if(mFavouriteData != null && mFavouriteData.size() > 0) {
            Favourite.FavouriteData mData = mFavouriteData.get(position);
            if(mData != null){
                if(mContactsFragmentAdapter != null){
                    mFavouriteData.remove(position);
                    mContactsFragmentAdapter.notifyItemRemoved(position);
                }
                addToFavourites(mData);
            }

        }
    }

    private void addToFavourites(Favourite.FavouriteData mData) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(getActivity());
            String friendId = mData.getUserId();
            Call<JSONObject> call = apiService.addToContacts(userId,friendId, apiKey);
            call.enqueue(new Callback<JSONObject>() {
                @Override
                public void onResponse(@NonNull Call<JSONObject> call, @NonNull Response<JSONObject> response) {
                    Log.d("addToFavourites", "onSuccess");
                    if(swipe != null)
                        swipe.setRefreshing(false);
                }
                @Override
                public void onFailure(@NonNull Call<JSONObject> call, @NonNull Throwable t) {
                    Log.d("addToFavourites", "onFailure");
                    if(swipe != null)
                        swipe.setRefreshing(false);
                }
            });
        } else {
            if(swipe != null)
                SnackBarDialog.showNoInternetError(swipe);
        }
    }
}
