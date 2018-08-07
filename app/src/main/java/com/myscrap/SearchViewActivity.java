package com.myscrap;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.myscrap.activity.XMPPChatRoomActivity;
import com.myscrap.adapters.SearchViewAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.ActiveFriends;
import com.myscrap.model.NearFriends;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.view.DividerItemDecoration;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;
import com.myscrap.xmppresources.Constant;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SearchViewActivity extends AppCompatActivity implements SearchViewAdapter.SearchViewAdapterListener, SearchView.OnQueryTextListener
{


    private RecyclerView mRecyclerView;
    private SearchViewAdapter mSearchViewAdapter;
    private List<NearFriends.NearFriendsData> mNearFriendsDataList = new ArrayList<>();
    private ActiveFriends mFriendLists;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            if (getIntent().getStringExtra("pageName") != null) {
                if (!getIntent().getStringExtra("pageName").equalsIgnoreCase("")) {
                    getSupportActionBar().setTitle(getIntent().getStringExtra("pageName"));
                }
                else
                {
                    getSupportActionBar().setTitle("Users");
                }
            }
        }


        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        SearchViewAdapter.SearchViewAdapterListener listener = this;
        mSearchViewAdapter = new SearchViewAdapter(this, mNearFriendsDataList, listener);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mSearchViewAdapter);

        ActiveFriends data = fromString(getIntent().getStringExtra("activeUsersList"));
        if (data != null) {
            List<NearFriends.NearFriendsData> activeFriendsData = data.getActiveFriendsData();
            if (activeFriendsData != null && !activeFriendsData.isEmpty()) {
                if (filterByName(activeFriendsData) !=null)
                {
                    mNearFriendsDataList.addAll( filterByName(activeFriendsData));
                }
                mSearchViewAdapter.notifyDataSetChanged();
            } else {
                update();
            }
        } else {
            update();
        }

    }

    private void update() {
        if (!CheckNetworkConnection.isConnectionAvailable(this))
            return;

        new Handler().post(() -> {
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(AppController.getInstance());
            apiService.getFriendsList(userId, apiKey)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<ActiveFriends>() {
                        @Override
                        public void onCompleted() {
                            Log.d("FriendsList", "onCompleted: ");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("FriendsList", "onError: ");
                        }

                        @Override
                        public void onNext(ActiveFriends activeFriends) {
                            if (activeFriends != null) {
                                mFriendLists = activeFriends;
                                parse(mFriendLists);
                            }
                            Log.d("FriendsList", "onNext: ");
                        }
                    });
        });
    }

    private void parse(ActiveFriends mFriendLists) {
        Gson gson = new Gson();
        String mFriendList = gson.toJson(mFriendLists);
        UserUtils.saveUserFriendLists(AppController.getInstance(), mFriendList);
        if (mFriendLists != null) {
            List<NearFriends.NearFriendsData> activeFriendsData = mFriendLists.getActiveFriendsData();
            if (activeFriendsData != null && !activeFriendsData.isEmpty()) {
                if (mNearFriendsDataList != null) {
                    mNearFriendsDataList.clear();
                    mNearFriendsDataList.addAll(filterByName(activeFriendsData));
                    mSearchViewAdapter.notifyDataSetChanged();
                }
            }
        }
    }


    public static ActiveFriends fromString(String value)
    {
        Type listType = new TypeToken<ActiveFriends>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
            searchView.setOnQueryTextListener(this);
            if (searchManager != null) {
                searchView.setSearchableInfo( searchManager.getSearchableInfo(getComponentName()));
            }
            return true;
        } else if (id == android.R.id.home) {
            this.finish();
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText)
    {
        if(mNearFriendsDataList != null && !mNearFriendsDataList.isEmpty()) {
            final List<NearFriends.NearFriendsData> filteredModelList = filter(mNearFriendsDataList, newText);
            if (filteredModelList != null && filteredModelList.size() > 0) {
                mSearchViewAdapter.setFilter(filteredModelList);
                return true;
            }
            else
            {
                if(mRecyclerView != null && !newText.isEmpty())
                {
                    SnackBarDialog.show(mRecyclerView, "No user found");
                }
                return false;
            }
        }
        return true;
    }

    private List<NearFriends.NearFriendsData> filter(List<NearFriends.NearFriendsData> nearFriendsData, String query) {

        final List<NearFriends.NearFriendsData> nearFriendsDataCopy = new ArrayList<>();

        if(nearFriendsData == null)
            return nearFriendsDataCopy;

            for (NearFriends.NearFriendsData nearFriendData : nearFriendsData){
                final String text = nearFriendData.getName().toLowerCase().toLowerCase();
                if (text.contains(query.toLowerCase())) {
                    nearFriendsDataCopy.add(nearFriendData);
                }
            }

            Set<NearFriends.NearFriendsData> hs = new HashSet<>();
            hs.addAll(nearFriendsDataCopy);
            nearFriendsDataCopy.clear();
            nearFriendsDataCopy.addAll(hs);
            return nearFriendsDataCopy;
    }


    private List<NearFriends.NearFriendsData> filterByName(List<NearFriends.NearFriendsData> nearFriendsDataCopy) {
        if(nearFriendsDataCopy != null && nearFriendsDataCopy.size() > 0){
            Collections.sort(nearFriendsDataCopy, (contactOne, contactTwo) -> (contactOne.getName() != null ? contactOne.getName() : "").compareTo((contactTwo.getName() != null ? contactTwo.getName() : "")));
        }
        return nearFriendsDataCopy;
    }

   /* private void goToChat(String id, String from, String chatRoomProfilePic, String color)
    {

        Intent intent = new Intent(AppController.getInstance(), ChatRoomActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("chatRoomId", id);
        intent.putExtra("color", color);
        intent.putExtra("chatRoomName", from);
        intent.putExtra("chatRoomProfilePic", chatRoomProfilePic);
        intent.putExtra("online", "0");
        startActivity(intent);
    }
*/


    private void goToChat(String jid,String userId, String name, String chatRoomProfilePic, String color)
    {
        if (jid != null)
        {
            Intent intent = new Intent(AppController.getInstance(), XMPPChatRoomActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Constant.FRIENDS_JID, jid);
            intent.putExtra(Constant.FRIENDS_ID, userId);
            intent.putExtra(Constant.FRIENDS_NAME, name);
            intent.putExtra(Constant.FRIENDS_URL, chatRoomProfilePic);
            intent.putExtra(Constant.FRIENDS_COLOR, color);
            AppController.getInstance().startActivity(intent);
        }
    }




    @Override
    public void onContactsAdapterClicked(NearFriends.NearFriendsData nearFriendsData, int position)
    {
        if(nearFriendsData != null )
        {
        //    goToChat(nearFriendsData.getUserid(),nearFriendsData.getName(), nearFriendsData.getProfilePic(), nearFriendsData.getColorCode());
            goToChat(nearFriendsData.getjId(),nearFriendsData.getUserid(),nearFriendsData.getName(), nearFriendsData.getProfilePic(), nearFriendsData.getColorCode());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

}
