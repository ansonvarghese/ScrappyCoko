package com.myscrap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.myscrap.adapters.NearestRecyclerViewDataAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.ActiveFriends;
import com.myscrap.model.NearFriends;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ActiveListActivity extends AppCompatActivity {
    private List<NearFriends.NearFriendsData> mActiveUsersLists = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            if (getIntent().getStringExtra("pageName") != null) {
                if (!getIntent().getStringExtra("pageName").equalsIgnoreCase("")) {
                    getSupportActionBar().setTitle(getIntent().getStringExtra("pageName"));
                } else {
                    getSupportActionBar().setTitle("Users");
                }
            }
        }
        //Gson gson = new Gson();
        //ActiveFriends data = gson.fromJson(getIntent().getStringExtra("activeUsersList"), ActiveFriends.class);
        ActiveFriends data = fromString(getIntent().getStringExtra("activeUsersList"));
        if (data != null) {
            List<NearFriends.NearFriendsData> activeFriendsData = data.getActiveFriendsData();
            if (activeFriendsData != null) {
                mActiveUsersLists.addAll(activeFriendsData);
            }
        }
        RecyclerView recyclerViewHeader = (RecyclerView) findViewById(R.id.active_now_recycler_view);
        recyclerViewHeader.setHasFixedSize(true);
        recyclerViewHeader.setNestedScrollingEnabled(false);
        recyclerViewHeader.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(AppController.getInstance(), LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setInitialPrefetchItemCount(4);
        recyclerViewHeader.setLayoutManager(linearLayoutManager);

        if (mActiveUsersLists == null)
            mActiveUsersLists = new ArrayList<>();

        NearestRecyclerViewDataAdapter mScrollRecyclerViewAdapter = new NearestRecyclerViewDataAdapter(AppController.getInstance(), mActiveUsersLists, true, NearFriends.VIEW_TYPE_ACTIVE_LIST);
        recyclerViewHeader.setAdapter(mScrollRecyclerViewAdapter);
    }


    public static ActiveFriends fromString(String value) {
        Type listType = new TypeToken<ActiveFriends>() {}.getType();
        return new Gson().fromJson(value, listType);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
