package com.myscrap;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.adapters.CompanyFavoriteFragmentAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.Favourite;
import com.myscrap.model.Markers;
import com.myscrap.model.MyItem;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.view.DividerItemDecoration;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * A simple {@link Fragment} subclass.
 */
public class FavouriteCompanyFragment extends Fragment implements CompanyFavoriteFragmentAdapter.CompanyFragmentAdapterListener, SearchView.OnQueryTextListener{


    private RecyclerView mCompanyRecyclerView;
    private SwipeRefreshLayout swipe;
    private CompanyFavoriteFragmentAdapter mCompanyFragmentAdapter;
    private AlertDialog.Builder dialog;
    private List<MyItem> mCompanyData = new ArrayList<>();
    final String[] filters = new String[]{
            "Name", "Country"
    };

    private Tracker mTracker;
    private View emptyView;

    public FavouriteCompanyFragment() {
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fav_company, container, false);
        mCompanyRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_company);
        swipe = (SwipeRefreshLayout) view.findViewById(R.id.swipe);
        emptyView = view.findViewById(R.id.empty);
        UserUtils.setEmptyView(emptyView, R.drawable.ic_favorites_empty, "No Favourite Companies", false);
        CompanyFavoriteFragmentAdapter.CompanyFragmentAdapterListener listener = this;
        swipe.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
        swipe.setDistanceToTriggerSync(30);
        swipe.setOnRefreshListener(() -> {
            if(CheckNetworkConnection.isConnectionAvailable(getActivity())){
                if(swipe != null)
                    swipe.setRefreshing(true);
                loadCompanies();
            }
        });
        if (getActivity() != null)
        dialog = new AlertDialog.Builder(getActivity());
        mCompanyRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mCompanyRecyclerView.setLayoutManager(layoutManager);
        mCompanyRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mCompanyRecyclerView.setHasFixedSize(true);
        mCompanyRecyclerView.setNestedScrollingEnabled(false);
        mCompanyFragmentAdapter = new CompanyFavoriteFragmentAdapter(mCompanyRecyclerView, getActivity(), mCompanyData, listener);
        mCompanyRecyclerView.setAdapter(mCompanyFragmentAdapter);
        loadCompanies();
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        mTracker = AppController.getInstance().getDefaultTracker();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                if (getActivity() != null) {
                    SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
                    SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
                    searchView.setOnQueryTextListener(this);
                    if (searchManager != null) {
                        searchView.setSearchableInfo( searchManager.getSearchableInfo(getActivity().getComponentName()));
                    }
                }
                return true;
            case R.id.filter:
                showPopUpMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(final String newText) {
        boolean isSearchViewOpen = !newText.trim().equalsIgnoreCase("");
        return true;
    }

    private List<MyItem> filter(List<MyItem> mCompanyData, String query) {
        final List<MyItem> mCompanyDataCopy = new ArrayList<>();
        for(MyItem item : mCompanyData){
            final String text = item.getCompanyName().toLowerCase().toLowerCase();
            if (text.contains(query.toLowerCase())) {
                mCompanyDataCopy.add(item);
            }
            if(item.getCompanyCountry()!= null){
                final  String countryName = item.getCompanyCountry().toLowerCase();
                if(countryName.contains(query)){
                    mCompanyDataCopy.add(item);
                }
            }
            Set<MyItem> hs = new HashSet<>();
            hs.addAll(mCompanyDataCopy);
            mCompanyDataCopy.clear();
            mCompanyDataCopy.addAll(hs);
        }
        return mCompanyDataCopy;
    }

    private void showPopUpMenu() {
        if(dialog == null && getActivity() != null)
            dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("Sort by");

        dialog.setItems(filters, (dialog, which) -> {

            switch (which){
                case 0:
                    if(dialog != null)
                        dialog.dismiss();
                    filterByName();
                    break;
                case 1:

                    if(dialog != null)
                        dialog.dismiss();
                    filterByCountry();
                    break;
            }

        });
        dialog.show();
    }

    private void filterByName() {

        if(mCompanyData != null &&mCompanyData.size() > 0) {
            mCompanyData.clear();
            mCompanyData.addAll(mCompanyData);
        }

        if(mCompanyData != null && mCompanyData.size() > 0){
            Collections.sort(mCompanyData, (itemOne, itemTwo) -> {

                if (itemOne.getCompanyName() == null) {
                    if (itemTwo.getCompanyName() == null) {
                        return 0;
                    }
                    return -1;
                } else if (itemTwo.getCompanyName() == null) {
                    return 1;
                }
                return itemOne.getCompanyName().trim().compareTo(itemTwo.getCompanyName().trim());
            });

            Collections.sort(mCompanyData, (o1, o2) -> o1.getCompanyName().trim().compareTo(o2.getCompanyName().trim()));

            List<MyItem> dataName = new ArrayList<>();
            for(MyItem item : mCompanyData){
                if(item.getCompanyName() != null && !item.getCompanyName().equalsIgnoreCase("")){
                    dataName.add(item);
                }
            }

            if(mCompanyFragmentAdapter != null && dataName.size() > 0){
                mCompanyFragmentAdapter.swap(dataName);
                scrollToBottom();
            }
        }
    }

    private void filterByCountry() {
        if(mCompanyData != null && mCompanyData.size() > 0){
            Collections.sort(mCompanyData, (itemOne, itemTwo) -> {
                if (itemOne.getCompanyCountry() == null) {
                    if (itemTwo.getCompanyCountry() == null) {
                        return 0;
                    }
                    return -1;
                } else if (itemTwo.getCompanyCountry() == null) {
                    return 1;
                }
                return itemOne.getCompanyCountry().trim().compareTo(itemTwo.getCompanyCountry().trim());

            });

            List<MyItem> data = new ArrayList<>();
            for(MyItem item : mCompanyData){
                if(item.getCompanyName() != null && !item.getCompanyName().equalsIgnoreCase("") && item.getCompanyCountry() != null && !item.getCompanyCountry().equalsIgnoreCase("")){
                    data.add(item);
                }
            }

            if(mCompanyFragmentAdapter != null){
                if(data.size() > 0){
                    Collections.sort(data, (o1, o2) -> o1.getCompanyCountry().trim().compareTo(o2.getCompanyCountry().trim()));
                    mCompanyFragmentAdapter.swap(data);
                }
                scrollToBottom();
            }
        }
    }

    private void scrollToBottom() {
        if(mCompanyRecyclerView != null && getActivity() != null) {
            getActivity().runOnUiThread(() -> mCompanyRecyclerView.getLayoutManager().scrollToPosition(0));
        }
    }

    private void loadCompanies() {
        if(CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
                getCompany();
        } else {
            if(mCompanyRecyclerView != null){
                SnackBarDialog.showNoInternetError(mCompanyRecyclerView);
            }
        }
    }

    private void getCompany() {
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())) {
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(AppController.getInstance());
                    apiService.favouritedCompany(userId,apiKey)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Markers>() {
                        @Override
                        public void onCompleted() {
                            Log.d("MarkerList", "onSuccess");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d("MarkerList", "onFailure");
                            if(swipe != null)
                                swipe.setRefreshing(false);
                        }

                        @Override
                        public void onNext(Markers markers) {
                            if(swipe != null)
                                swipe.setRefreshing(false);
                            parseData(markers);
                        }
                    });
        }
    }

    private void parseData(Markers markers) {
        if(markers != null) {
            if(!markers.isErrorStatus()) {
                List<Markers.MarkerData> data = markers.getData();
                if(data != null && data.size() > 0){
                    mCompanyData.clear();
                    for(Markers.MarkerData marker  : data){
                        if (!marker.getLatitude().equalsIgnoreCase("") && !marker.getLongitude().equalsIgnoreCase("")) {
                            double offsetItemLatitude = Double.parseDouble(marker.getLatitude());
                            double offsetItemLongitude = Double.parseDouble(marker.getLongitude());
                            MyItem offsetItem = new MyItem(offsetItemLatitude, offsetItemLongitude,marker.getName(), marker.getCompanyType(),marker.getIsNew(), marker.getState(),marker.getCountry(),marker.getImage(),marker.getId());
                            offsetItem.setFavourite(marker.isFavourite());
                            mCompanyData.add(offsetItem);
                        }
                    }
                    if(mCompanyData != null && mCompanyData.size() > 0) {
                        Collections.sort(mCompanyData, (itemOne, itemTwo) -> {

                            if (itemOne.getCompanyName() == null) {
                                if (itemTwo.getCompanyName() == null) {
                                    return 0;
                                }
                                return -1;
                            } else if (itemTwo.getCompanyName() == null) {
                                return 1;
                            }
                            return itemOne.getCompanyName().trim().compareTo(itemTwo.getCompanyName().trim());
                        });

                        Collections.sort(mCompanyData, (o1, o2) -> o1.getCompanyName().trim().compareTo(o2.getCompanyName().trim()));

                        List<MyItem> dataName = new ArrayList<>();
                        for(MyItem item : mCompanyData){
                            if(item.getCompanyName() != null && !item.getCompanyName().equalsIgnoreCase("")){
                                dataName.add(item);
                            }
                        }
                        if(mCompanyFragmentAdapter != null && dataName.size() > 0){
                            mCompanyFragmentAdapter.swap(dataName);
                        }
                        if (!dataName.isEmpty()) {
                            swipe.setVisibility(View.VISIBLE);
                            emptyView.setVisibility(View.GONE);
                        } else {
                            swipe.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        }
                    }

                } else {
                    swipe.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("Favourite Company Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onFavouritesAdapterClicked(List<MyItem> originalList, int position) {
        if(swipe != null && !swipe.isRefreshing()){
            if(originalList != null && originalList.size() > 0){
                MyItem mData = originalList.get(position);
                if(mData != null){
                    goToCompany(mData.getMarkerId());
                }
            }
        }
    }

    private void goToCompany(String companyId) {
        if(CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            Intent i = new Intent(getActivity(), CompanyProfileActivity.class);
            i.putExtra("companyId", companyId);
            startActivity(i);
        }
    }

    @Override
    public void onStarClicked(MyItem mData ,int position, final boolean isStarred) {
        if(CheckNetworkConnection.isConnectionAvailable(getContext())){
            doFavourite(mData, position);
        } else{
            if(swipe != null){
                SnackBarDialog.showNoInternetError(swipe);
            }
        }
    }


    private void doFavourite(MyItem mData, int position) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        Call<Favourite> call = apiService.insertCompanyFavourite(userId, mData.getMarkerId(),apiKey);
        call.enqueue(new Callback<Favourite>() {
            @Override
            public void onResponse(@NonNull Call<Favourite> call, @NonNull retrofit2.Response<Favourite> response) {

                if(response.body() != null && response.isSuccessful()){
                    Favourite mFavouriteData = response.body();
                    if(mFavouriteData != null && !mFavouriteData.isErrorStatus()){
                        Log.d("doFavourite", "onSuccess");
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<Favourite> call, @NonNull Throwable t) {
                Log.d("doFavourite", "onFailure");
            }
        });
    }

}
