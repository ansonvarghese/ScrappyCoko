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
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.adapters.CompanyFragmentAdapter;
import com.myscrap.application.AppController;
import com.myscrap.database.MyScrapSQLiteDatabase;
import com.myscrap.model.CountryList;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import in.myinnos.alphabetsindexfastscrollrecycler.IndexFastScrollRecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class CompanyFragment extends Fragment implements CompanyFragmentAdapter.CompanyFragmentAdapterListener, SearchView.OnQueryTextListener{


    private IndexFastScrollRecyclerView mCompanyRecyclerView;
    private SwipeRefreshLayout swipe;
    private CompanyFragmentAdapter mCompanyFragmentAdapter;
    private AlertDialog.Builder dialog;
    private List<MyItem> mCompanyData = new ArrayList<>();
    private MyScrapSQLiteDatabase mMyScrapSQLiteDatabase;
    private ArrayList<MyItem> markerList = new ArrayList<>();
    private boolean isSearchViewOpen;

    final String[] filters = new String[]{
            "Name", "Country"
    };

    private Tracker mTracker;
    private MenuItem searchItem;


    public CompanyFragment() {
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_company, container, false);
        mCompanyRecyclerView = view.findViewById(R.id.recycler_view_company);
        swipe = view.findViewById(R.id.swipe);
        CompanyFragmentAdapter.CompanyFragmentAdapterListener listener = this;
        swipe.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
        swipe.setDistanceToTriggerSync(30);
        swipe.setOnRefreshListener(() -> {
            if(!isSearchViewOpen){
                if(CheckNetworkConnection.isConnectionAvailable(getActivity())){
                    if(swipe != null)
                        swipe.setRefreshing(true);
                    loadCompanies();
                }
            } else {
                if(swipe != null)
                    swipe.setRefreshing(false);
            }

        });
        if (getActivity() != null)
            dialog = new AlertDialog.Builder(getActivity());
        mCompanyRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mCompanyRecyclerView.setLayoutManager(layoutManager);
        mCompanyRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mCompanyRecyclerView.setHasFixedSize(true);
        mCompanyRecyclerView.setIndexTextSize(8);
        mCompanyRecyclerView.setNestedScrollingEnabled(false);
        mCompanyRecyclerView.setIndexBarCornerRadius(10);
        mCompanyFragmentAdapter = new CompanyFragmentAdapter(mCompanyRecyclerView, getActivity(), mCompanyData, listener);
        mCompanyRecyclerView.setAdapter(mCompanyFragmentAdapter);
        if (mMyScrapSQLiteDatabase == null)
            mMyScrapSQLiteDatabase = MyScrapSQLiteDatabase.getInstance(AppController.getInstance());
        markerList = new ArrayList<>();
        markerList = mMyScrapSQLiteDatabase.getMarkerList();
        loadCompanies();

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mTracker = AppController.getInstance().getDefaultTracker();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.company_filter, menu);
        searchItem = menu.findItem(R.id.search);
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
                SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
                SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
                searchView.setOnQueryTextListener(this);
                if (searchManager != null) {
                    searchView.setSearchableInfo( searchManager.getSearchableInfo(getActivity().getComponentName()));
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
        isSearchViewOpen = !newText.trim().equalsIgnoreCase("");
        mCompanyFragmentAdapter.getFilter().filter(newText);
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
        if(dialog == null){
            if(getActivity() == null)
                return;
            dialog = new AlertDialog.Builder(getActivity());
        }

        dialog.setTitle("Sort by");

        dialog.setItems(filters, (dialog, which) -> {

            switch (which){
                case 0:
                    if(dialog != null)
                        dialog.dismiss();
                    filterByName();
                    if(searchItem != null)
                     MenuItemCompat.collapseActionView(searchItem);
                    if(mCompanyRecyclerView != null)
                        mCompanyRecyclerView.setIndexBarVisibility(true);
                    break;
                case 1:
                    if(searchItem != null)
                        MenuItemCompat.collapseActionView(searchItem);
                    if(mCompanyRecyclerView != null)
                        mCompanyRecyclerView.setIndexBarVisibility(false);
                    if(dialog != null)
                        dialog.dismiss();
                    filterByCountry();
                    doFilterByCountryName();
                    break;
            }

        });
        dialog.show();
    }

    private void showCountryDialog(Map<String, Integer> setCountryList) {
        final List<CountryList> mCountryLists = new ArrayList<>();
        final List<String> mCountryName = new ArrayList<>();
        if(setCountryList != null) {

            for (Map.Entry<String, Integer> entry : setCountryList.entrySet()) {
                mCountryLists.add(new CountryList(entry.getKey(), String.valueOf(entry.getValue())));
                String capitalizeFirstChar = UserUtils.capitalizeFirst(entry.getKey().toLowerCase());
                String count = "["+entry.getValue()+"]";
                mCountryName.add(capitalizeFirstChar + " "+ count);
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.title_bar, null);
            ImageView iv = view.findViewById(R.id.close_indicator);
            builder.setCustomTitle(view);
            CharSequence[] cs = mCountryName.toArray(new CharSequence[mCountryName.size()]);
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(getContext(),R.layout.custom_item_single_choice, cs);
            builder.setSingleChoiceItems(adapter, 0, (dialog, which) -> {
                filteringCompany(mCountryLists.get(which).getCountryName());
                dialog.dismiss();
            });
            final AlertDialog dialog = builder.create();
            iv.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
            Display display;
            if ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE) != null) {
                display = ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                int width = display.getWidth();
                int height = (int)(getResources().getDisplayMetrics().heightPixels*0.90);
                Log.v("width", width+"");
                dialog.getWindow().setLayout((6*width)/7,height);
            }

        }
    }


    private void filteringCompany(String selectedCountry) {
        if(mCompanyData != null && mCompanyData.size() > 0){
            final List<MyItem> dataCountry = new ArrayList<>();
            for(MyItem item : mCompanyData){
                if(item.getCompanyName() != null && !item.getCompanyName().equalsIgnoreCase("") && item.getCompanyCountry() != null && !item.getCompanyCountry().equalsIgnoreCase("")){
                    if(item.getCompanyCountry().trim().toLowerCase().equalsIgnoreCase(selectedCountry.trim().toLowerCase())){
                        item.setCompanyCountry(UserUtils.capitalizeFirst(item.getCompanyCountry().trim().toLowerCase()));
                        dataCountry.add(item);
                    }

                }
            }
            Set<MyItem> hs = new HashSet<>();
            hs.addAll(dataCountry);
            dataCountry.clear();
            dataCountry.addAll(hs);
            if(mCompanyFragmentAdapter != null){
                if(mCompanyRecyclerView != null && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        mCompanyFragmentAdapter.swap(dataCountry, "COUNTRY");
                        mCompanyRecyclerView.getLayoutManager().scrollToPosition(0);
                    });
                }
            }

        }
    }

    private void filterByName() {

        if(mCompanyData != null && markerList != null && markerList.size() > 0) {
            mCompanyData.clear();
            mCompanyData.addAll(markerList);
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
                if(item.getCompanyName() != null && !item.getCompanyName().equalsIgnoreCase("") && item.getCompanyCountry() != null && !item.getCompanyCountry().equalsIgnoreCase("")){
                    dataName.add(item);
                }
            }

            Set<MyItem> hs = new HashSet<>();
            hs.addAll(dataName);
            dataName.clear();
            dataName.addAll(hs);

            if(mCompanyFragmentAdapter != null && dataName.size() > 0){
                mCompanyFragmentAdapter.swap(dataName, "NAME");
                scrollToBottom();
            }
        }
    }

    private void filterByCountry() {
        if(mCompanyData != null && mCompanyData.size() > 0){
            final List<MyItem> data = new ArrayList<>();
            for(MyItem item : mCompanyData){
                if(item.getCompanyName() != null && !item.getCompanyName().equalsIgnoreCase("") && item.getCompanyCountry() != null && !item.getCompanyCountry().equalsIgnoreCase("")){
                    data.add(item);
                }
            }

            Collections.sort(data, (itemOne, itemTwo) -> {
                if (itemOne.getCompanyCountry() == null) {
                    if (itemTwo.getCompanyCountry() == null) {
                        return 0;
                    }
                    return -1;
                } else if (itemTwo.getCompanyCountry() == null) {
                    return 1;
                }
                return itemOne.getCompanyCountry().toLowerCase().trim().compareTo(itemTwo.getCompanyCountry().toLowerCase().trim());

            });

            Set<MyItem> hs = new HashSet<>();
            hs.addAll(data);
            data.clear();
            data.addAll(hs);

            if(mCompanyFragmentAdapter != null){
                if(mCompanyRecyclerView != null && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        mCompanyFragmentAdapter.swap(data, "COUNTRY");
                        mCompanyRecyclerView.getLayoutManager().scrollToPosition(0);
                    });
                }
            }
        }
    }

    private void doFilterByCountryName() {
        if(mCompanyData != null && mCompanyData.size() > 0){
            final List<String> dataCountry = new ArrayList<>();
            for(MyItem item : mCompanyData){
                if(item.getCompanyName() != null && !item.getCompanyName().equalsIgnoreCase("") && item.getCompanyCountry() != null && !item.getCompanyCountry().equalsIgnoreCase("")){
                    dataCountry.add(item.getCompanyCountry().trim().toUpperCase());
                }
            }

            Map<String, Integer> map = new HashMap<>();

            for (String temp : dataCountry) {
                Integer count = map.get(temp);
                map.put(temp, (count == null) ? 1 : count + 1);
            }
            Map<String, Integer> treeMap = new TreeMap<>(map);
            showCountryDialog(treeMap);
        }
    }

    private void scrollToBottom() {
        if(mCompanyRecyclerView != null && getActivity() != null) {
            getActivity().runOnUiThread(() -> mCompanyRecyclerView.getLayoutManager().scrollToPosition(0));
        }
    }

    private void loadCompanies() {

        if(swipe != null){
            swipe.setRefreshing(true);
            swipe.post(() -> {
                if(markerList != null && markerList.size() > 0){
                    mCompanyData.clear();
                    for(MyItem item : markerList){
                        if(item.getCompanyName() != null && !item.getCompanyName().equalsIgnoreCase("") && item.getCompanyCountry()!= null && !item.getCompanyCountry().equalsIgnoreCase("") ){
                            mCompanyData.add(item);
                        }
                    }
                    //mCompanyData.addAll(markerList);
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
                            //return (itemOne.getCompanyName() != null ? itemTwo.getCompanyName() : "").compareTo((itemTwo.getCompanyName() != null ? itemTwo.getCompanyName() : ""));
                        });

                        Collections.sort(mCompanyData, (o1, o2) -> o1.getCompanyName().trim().compareTo(o2.getCompanyName().trim()));

                        List<MyItem> dataName = new ArrayList<>();
                        for(MyItem item : mCompanyData){
                            if(item.getCompanyName() != null && !item.getCompanyName().equalsIgnoreCase("") && item.getCompanyCountry()!= null && !item.getCompanyCountry().equalsIgnoreCase("") ){
                                dataName.add(item);
                            }
                        }
                        Set<MyItem> hs = new HashSet<>();
                        hs.addAll(dataName);
                        dataName.clear();
                        dataName.addAll(hs);
                        if(mCompanyFragmentAdapter != null && dataName.size() > 0){
                            mCompanyFragmentAdapter.swap(dataName, "NAME");
                        }
                    }
                    swipe.setRefreshing(false);
                }
            });
        }


        if(CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            if(markerList != null && markerList.size() == 0) {
                getCompany();
            }
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
            String apiKey = UserUtils.getApiKey(AppController.getInstance());
            Call<Markers> call = apiService.discover(apiKey);
            call.enqueue(new Callback<Markers>() {
                @Override
                public void onResponse(@NonNull Call<Markers> call, @NonNull Response<Markers> response) {
                    if(swipe != null)
                        swipe.setRefreshing(false);
                    if (response.body() != null) {
                        Markers markers = response.body();
                        if(markers != null) {
                            if(!markers.isErrorStatus()) {
                                List<Markers.MarkerData> data = markers.getData();
                                if(data != null && data.size() > 0){
                                    mCompanyData.clear();
                                    if(mMyScrapSQLiteDatabase == null)
                                        mMyScrapSQLiteDatabase = new MyScrapSQLiteDatabase(AppController.getInstance());
                                    mMyScrapSQLiteDatabase.deleteMarkerList();
                                    for(Markers.MarkerData marker  : data){
                                        if (!marker.getLatitude().equalsIgnoreCase("") && !marker.getLongitude().equalsIgnoreCase("")) {
                                            double offsetItemLatitude = Double.parseDouble(marker.getLatitude());
                                            double offsetItemLongitude = Double.parseDouble(marker.getLongitude());
                                            MyItem offsetItem = new MyItem(offsetItemLatitude, offsetItemLongitude,marker.getName(), marker.getCompanyType(), marker.getIsNew(), marker.getState(),marker.getCountry(),marker.getImage(),marker.getId());
                                            mMyScrapSQLiteDatabase.addMarker(offsetItem);
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
                                            //return (itemOne.getCompanyName() != null ? itemTwo.getCompanyName() : "").compareTo((itemTwo.getCompanyName() != null ? itemTwo.getCompanyName() : ""));
                                        });

                                        Collections.sort(mCompanyData, (o1, o2) -> o1.getCompanyName().trim().compareTo(o2.getCompanyName().trim()));

                                        List<MyItem> dataName = new ArrayList<>();
                                        for(MyItem item : mCompanyData){
                                            if(item.getCompanyName() != null && !item.getCompanyName().equalsIgnoreCase("")){
                                                dataName.add(item);
                                            }
                                        }

                                        Set<MyItem> hs = new HashSet<>();
                                        hs.addAll(dataName);
                                        dataName.clear();
                                        dataName.addAll(hs);

                                        if(mCompanyFragmentAdapter != null && dataName.size() > 0){
                                            mCompanyFragmentAdapter.swap(dataName, "NAME");
                                        }
                                    }

                                }
                            }
                        }
                        if(swipe != null)
                            swipe.setRefreshing(false);
                        Log.d("MarkerList", "onSuccess");
                    }
                }
                @Override
                public void onFailure(@NonNull Call<Markers> call, @NonNull Throwable t) {
                    Log.d("MarkerList", "onFailure");
                    if(swipe != null)
                        swipe.setRefreshing(false);

                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("Company List Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onFavouritesAdapterClicked(List<MyItem> originalList, int position, String companyId) {
        if(swipe != null && !swipe.isRefreshing()){
            goToCompany(companyId);
        }
    }

    private void goToCompany(String companyId) {
        if(CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            Intent i = new Intent(getActivity(), CompanyProfileActivity.class);
            i.putExtra("companyId", companyId);
            startActivity(i);
        } else {
            if(swipe != null)
                SnackBarDialog.showNoInternetError(swipe);
        }
    }

    @Override
    public void onStarClicked(int position, final boolean isStarred) {
        if(swipe != null){
            swipe.post(() -> swipe.setRefreshing(isStarred));
        }

    }

}
