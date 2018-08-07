package com.myscrap;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.CheckedTextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.adapters.ContactsFragmentAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.Contact;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ContactsFragment extends Fragment implements ContactsFragmentAdapter.ContactsFragmentAdapterListener, SearchView.OnQueryTextListener{

    private ContactsFragmentAdapter mContactsFragmentAdapter;
    private List<Contact.ContactData> mContactDataList = new ArrayList<>();
    private SwipeRefreshLayout swipe;
    private AlertDialog.Builder dialog;
    private RecyclerView mRecyclerView;
    private boolean isSearchViewOpen;
    private List<Contact.ContactData> filteredModelListCopy = new ArrayList<>();
    final String[] filters = new String[]{
            "Score", "First Name","Last Name","Company", "New"
    };
    private Tracker mTracker;
    Bundle savedInstanceState;
    public ContactsFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        setHasOptionsMenu(true);
        mTracker = AppController.getInstance().getDefaultTracker();

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mContactsFragment = inflater.inflate(R.layout.fragment_contacts, container, false);
        mRecyclerView = (RecyclerView) mContactsFragment.findViewById(R.id.recycler_view);
        swipe = (SwipeRefreshLayout) mContactsFragment.findViewById(R.id.swipe);
        swipe.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
        swipe.setDistanceToTriggerSync(30);//
        swipe.setOnRefreshListener(() -> {
            if(getActivity() != null && CheckNetworkConnection.isConnectionAvailable(getActivity())){
                load();
            } else {
                if(swipe != null) {
                    SnackBarDialog.showNoInternetError(swipe);
                }
            }
        });
        if (getActivity() != null)
        dialog = new AlertDialog.Builder(getActivity());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        ContactsFragmentAdapter.ContactsFragmentAdapterListener listener = this;
        mContactsFragmentAdapter = new ContactsFragmentAdapter(getActivity(), mContactDataList, listener);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mContactsFragmentAdapter);


        if(swipe != null){
            swipe.post(this::load);
        }
        return mContactsFragment;
    }

    private void load(){
        if(getActivity() != null && CheckNetworkConnection.isConnectionAvailable(getActivity())){
            swipe.setRefreshing(true);
            loadContactsToAdd();
        } else {
            if(swipe != null) {
                SnackBarDialog.showNoInternetError(swipe);
            }
        }
    }

    private boolean isNeverShowAgain;

    private void showAlertDialog(){
        if(getActivity() != null){
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
            mBuilder.setTitle("Earn Points");
            LayoutInflater inflater;
            inflater = getActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.points, null);
            mBuilder.setView(dialogView);
            mBuilder.setPositiveButton("OK", (dialog, which) -> {
                if(isNeverShowAgain){
                    UserUtils.saveTipsEnable(getActivity(), "1");
                } else {
                    UserUtils.saveTipsEnable(getActivity(), "0");
                }
                if(dialog != null)
                    dialog.dismiss();
            });
            final CheckedTextView neverShowAgain = (CheckedTextView) dialogView.findViewById(R.id.never_show_again);
            neverShowAgain.setOnClickListener(v -> {
                if(neverShowAgain.isChecked()){
                    neverShowAgain.setChecked(false);
                    isNeverShowAgain = false;
                } else {
                    isNeverShowAgain = true;
                    neverShowAgain.setChecked(true);
                }
            });
            mBuilder.show();
        }

    }

    private void loadContactsToAdd() {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            if(swipe != null && !swipe.isRefreshing())
                ProgressBarDialog.showLoader(getActivity(), false);
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(getActivity());
            apiService.contacts(userId, apiKey)
                    .subscribeOn(Schedulers.newThread())
                    .retry(3)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Contact>() {
                        @Override
                        public void onCompleted() {
                            Log.d("loadContacts", "onCompleted");
                        }

                        @Override
                        public void onError(Throwable e) {
                            ProgressBarDialog.dismissLoader();
                            Log.d("loadContacts", "onFailure");
                            if(swipe != null)
                                swipe.setRefreshing(false);
                        }

                        @Override
                        public void onNext(Contact mContact) {
                            ProgressBarDialog.dismissLoader();
                            if(!UserUtils.isTipsEnabled(AppController.getInstance()))
                                showAlertDialog();
                            Log.d("loadContacts", "onSuccess");
                            if (mContact != null) {
                                if(swipe != null)
                                    swipe.setRefreshing(false);
                                if (mContactDataList != null)
                                    mContactDataList.clear();
                                parseData(mContact);
                            }
                        }
                    });
        } else {
            if(swipe != null)
                SnackBarDialog.showNoInternetError(swipe);
        }

    }

    private void parseData(Contact mContact) {
        if(!mContact.isErrorStatus()){
            mContactDataList = mContact.getData();
            if(mContactDataList != null && mContactsFragmentAdapter != null){
                mContactsFragmentAdapter.swap(mContactDataList);
            }
        } else {
            if(swipe != null)
                SnackBarDialog.show(swipe, mContact.getStatus());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.member_filter, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_member_search:
                if (getActivity() != null) {
                    SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
                    SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
                    searchView.setOnQueryTextListener(this);
                    if (searchManager != null) {
                        searchView.setSearchableInfo( searchManager.getSearchableInfo(getActivity().getComponentName()));
                    }
                }
                return true;
            case R.id.action_member_filter:
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
    public boolean onQueryTextChange(String newText) {
        isSearchViewOpen = newText != null && !newText.trim().equalsIgnoreCase("");
        final List<Contact.ContactData> filteredModelList = filter(mContactDataList, newText);
        filteredModelListCopy.clear();
        filteredModelListCopy.addAll(filteredModelList);
        mContactsFragmentAdapter.setFilter(filteredModelList);
        return true;
    }

    private List<Contact.ContactData> filter(List<Contact.ContactData> favoriteData, String query) {
        final List<Contact.ContactData> favoriteDataCopy = new ArrayList<>();
        for (Contact.ContactData data : favoriteData){
            final String text = data.getName().toLowerCase().toLowerCase();
            if (text.contains(query.toLowerCase())) {
                favoriteDataCopy.add(data);
            }
            if(data.getFirstName() != null){
                final String textFirst = data.getFirstName().toLowerCase().toLowerCase();
                if (textFirst.contains(query.toLowerCase())) {
                    favoriteDataCopy.add(data);
                }
            }

            if(data.getLastName() != null){
                final String textLast = data.getLastName().toLowerCase().toLowerCase();
                if (textLast.contains(query.toLowerCase())) {
                    favoriteDataCopy.add(data);
                }
            }

            if(data.getDesignation() != null) {
                final String textDesignation = data.getDesignation().toLowerCase().toLowerCase();
                if (textDesignation.contains(query.toLowerCase())) {
                    favoriteDataCopy.add(data);
                }
            }
            if(data.getUserLocation() != null) {
                final String textUserLocation = data.getUserLocation().toLowerCase().toLowerCase();
                if (textUserLocation.contains(query.toLowerCase())) {
                    favoriteDataCopy.add(data);
                }
            }

            if(data.getCountry() != null) {
                final String textCountry = data.getCountry().toLowerCase().toLowerCase();
                if (textCountry.contains(query.toLowerCase())) {
                    favoriteDataCopy.add(data);
                }
            }

            if(data.getUserCompany() != null) {
                final String textCompany = data.getUserCompany().toLowerCase().toLowerCase();
                if (textCompany.contains(query.toLowerCase())) {
                    favoriteDataCopy.add(data);
                }
            }

            Set<Contact.ContactData> hs = new HashSet<>();
            hs.addAll(favoriteDataCopy);
            favoriteDataCopy.clear();
            favoriteDataCopy.addAll(hs);

        }
        return favoriteDataCopy;
    }


    public void showPopUpMenu(){
        if(dialog == null)
            if (getActivity() != null)
            dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("Sort By");

        dialog.setItems(filters, (dialog, which) -> {

            switch (which){
                case 0:
                    if(dialog != null)
                        dialog.dismiss();
                    filterByToppers();
                    break;
                case 1:

                    if(dialog != null)
                        dialog.dismiss();
                    filterByFirstName();
                    break;
                case 2:

                    if(dialog != null)
                        dialog.dismiss();
                    filterByLastName();
                    break;
                case 3:

                    if(dialog != null)
                        dialog.dismiss();
                    filterByCompanyName();
                    break;
                case 4:
                    filterByNew();
                    if(dialog != null)
                        dialog.dismiss();
                    break;
            }

        });
        dialog.show();
    }

    private void filterByCompanyName() {
        if(mContactDataList != null && mContactDataList.size() > 0){
            Collections.sort(mContactDataList, (contactOne, contactTwo) -> (contactOne.getUserCompany() != null ? contactOne.getUserCompany() : "").compareTo((contactTwo.getUserCompany() != null ? contactTwo.getUserCompany() : "")));
            Collections.reverse(mContactDataList);
            List<Contact.ContactData> mCompanyName = new ArrayList<>();
            List<Contact.ContactData> mCompanyNameLess = new ArrayList<>();
            if(mContactDataList.size() > 0) {
                for(Contact.ContactData mContact : mContactDataList){
                    if(mContact.getUserCompany()!= null && !mContact.getUserCompany().equalsIgnoreCase("")){
                        mCompanyName.add(mContact);
                    } else {
                        mCompanyNameLess.add(mContact);
                    }
                }
            }

            if(mCompanyName.size() > 0 && mCompanyNameLess.size() > 0){
                mContactDataList.clear();
                Collections.sort(mCompanyName, (o1, o2) -> (o1.getUserCompany() != null ? o1.getUserCompany() : "").compareTo((o2.getUserCompany() != null ? o2.getUserCompany() :"")));
                mContactDataList.addAll(mCompanyName);
                mContactDataList.addAll(mCompanyNameLess);
            }

            if(mContactsFragmentAdapter != null){
                mContactsFragmentAdapter.swap(mContactDataList);
                scrollToBottom();
            }
        }
    }

    private void scrollToBottom() {
        if(mRecyclerView != null && getActivity() != null) {
            getActivity().runOnUiThread(() -> mRecyclerView.getLayoutManager().scrollToPosition(0));
        }
    }

    private void filterByToppers() {
        if(mContactDataList != null && mContactDataList.size() > 0){
            Collections.sort(mContactDataList, (mFavouriteDataOne, mFavouriteDataTwo) -> {
                return mFavouriteDataOne.getRank() - mFavouriteDataTwo.getRank(); // Ascending
            });

            if(mContactsFragmentAdapter != null){
                mContactsFragmentAdapter.swap(mContactDataList);
                scrollToBottom();
            }
        }
    }

    private void filterByNew() {
        if(mContactDataList != null && mContactDataList.size() > 0){
            Collections.sort(mContactDataList, (mFavouriteDataOne, mFavouriteDataTwo) -> {
                int b1 = mFavouriteDataOne.isNewJoined() ? 1 : 0;
                int b2 = mFavouriteDataTwo.isNewJoined() ? 1 : 0;
                return b2 - b1; // Ascending
            });

            Collections.sort(mContactDataList, Contact.ContactData.timeStampComparator);
            if(mContactsFragmentAdapter != null && mContactDataList != null){
                mContactsFragmentAdapter.swap(mContactDataList);
                scrollToBottom();
            }
        }
    }

    private void filterByFirstName() {
        if(mContactDataList != null && mContactDataList.size() > 0){
            Collections.sort(mContactDataList, (contactOne, contactTwo) -> (contactOne.getFirstName() != null ? contactOne.getFirstName() : "").compareTo((contactTwo.getFirstName() != null ? contactTwo.getFirstName() : "")));
            if(mContactsFragmentAdapter != null){
                mContactsFragmentAdapter.swap(mContactDataList);
                scrollToBottom();
            }
        }
    }

    private void filterByLastName() {
        if(mContactDataList != null && mContactDataList.size() > 0){
            Collections.sort(mContactDataList, (contactOne, contactTwo) -> (contactOne.getLastName() != null ? contactOne.getLastName() : "").compareTo((contactTwo.getLastName() != null ? contactTwo.getLastName() : "")));
            if(mContactsFragmentAdapter != null){
                mContactsFragmentAdapter.swap(mContactDataList);
                scrollToBottom();
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        HomeActivity.isContactFavourites = true;
        if(mTracker != null){
            mTracker.setScreenName("Members Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        HomeActivity.isContactFavourites = true;
    }

    @Override
    public void onContactsAdapterClicked(int position) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if(isSearchViewOpen) {
            if(filteredModelListCopy != null && filteredModelListCopy.size() > 0) {
                Contact.ContactData mContactData  = filteredModelListCopy.get(position);
                if(mContactData != null){
                    if(mContactData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                        goToUserProfile();
                    } else {
                        goToUserFriendProfile(mContactData.getUserId());
                    }
                }
            }
        } else {
            if(mContactDataList != null && mContactDataList.size() > 0) {
                Contact.ContactData mContactData = mContactDataList.get(position);
                if(mContactData != null){
                    if(mContactData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                        goToUserProfile();
                    } else {
                        goToUserFriendProfile(mContactData.getUserId());
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
        if(mContactDataList != null && mContactDataList.size() > 0) {
            Contact.ContactData mData = mContactDataList.get(position);
            if(mData != null){
                if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())) {
                    addToContacts(mData);
                    if(isStarred){
                        Toast.makeText(AppController.getInstance(), "Added to favourites", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AppController.getInstance(), "Removed from favourites", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if(swipe != null)
                        SnackBarDialog.showNoInternetError(swipe);
                }
            }
        }
    }

    private void addToContacts(Contact.ContactData mData) {
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
                    ProgressBarDialog.dismissLoader();
                    Log.d("addToContacts", "onSuccess");
                }
                @Override
                public void onFailure(@NonNull Call<JSONObject> call, @NonNull Throwable t) {
                    ProgressBarDialog.dismissLoader();
                    Log.d("addToContacts", "onFailure");
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
