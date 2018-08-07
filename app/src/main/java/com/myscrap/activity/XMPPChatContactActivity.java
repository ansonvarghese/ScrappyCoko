package com.myscrap.activity;

import android.app.ActivityManager;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.clans.fab.FloatingActionMenu;
import com.google.gson.Gson;
import com.myscrap.ActiveListActivity;
import com.myscrap.R;
import com.myscrap.SearchViewActivity;
import com.myscrap.adapters.NearestRecyclerViewDataAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.ActiveFriends;
import com.myscrap.model.ChatRoom;
import com.myscrap.model.NearFriends;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.ProgressBarDialog;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;
import com.myscrap.xmpp.RoosterConnectionService;
import com.myscrap.xmppadapter.XMPPContactListAdapter;
import com.myscrap.xmppdata.ChatMessagesTable;
import com.myscrap.xmppmodel.XMPPChatMessageModel;
import com.myscrap.xmppresources.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class XMPPChatContactActivity extends AppCompatActivity  implements SearchView.OnQueryTextListener,SwipeRefreshLayout.OnRefreshListener
{

    private RecyclerView contactRecycler;
    private RecyclerView.LayoutManager layoutManager;
    private XMPPContactListAdapter contactListAdapter;
    private List<XMPPChatMessageModel> contactList;
    private ChatMessagesTable chatMessagesTable;

    private FloatingActionMenu menuLabelsRight;
    private SearchView searchView;
    private MenuItem actionViewItem;
    private ActiveFriends mActiveFriends, mFriendLists;
    private Subscription activeFriendsSubscription, updateProfilePicturesSubscription;

    private RecyclerView recyclerViewHeader;
    private List<NearFriends.NearFriendsData> mNearFriendsLists = new ArrayList<>();
    private static List<ChatRoom> chatRooms;
    private NearestRecyclerViewDataAdapter mScrollRecyclerViewAdapter;
    private boolean isRefreshing;
    private LinearLayout activeLayout;
    private TextView activeSeeAllLayout;
    private static SwipeRefreshLayout swipeRefreshLayout;
    private BroadcastReceiver mReceiveMessageBroadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xmppchat_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        chatMessagesTable = new ChatMessagesTable(getApplicationContext());
      /*  if (chatMessagesTable.isEmpty())
        {
            String userJid = UserUtils.getUserJid(XMPPChatContactActivity.this)+"@s192-169-189-223.secureserver.net";
            String apiKey = UserUtils.getApiKey(XMPPChatContactActivity.this);
            String[] strs = {userJid,apiKey};

     //       new LoadChatHistoty().execute(strs);

        }
        else
        {
          */
            contactRecycler = findViewById(R.id.recycler_view);
            layoutManager = new LinearLayoutManager(getApplicationContext());
            contactList = chatMessagesTable.getContactList();
    //    }


        activeLayout = (LinearLayout) findViewById(R.id.active_now_layout);
        activeSeeAllLayout = (TextView) findViewById(R.id.active_now_see_all_layout);


        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccent);
        swipeRefreshLayout.setDistanceToTriggerSync(30);// in dips

        recyclerViewHeader = (RecyclerView) findViewById(R.id.active_now_recycler_view);
        recyclerViewHeader.setHasFixedSize(true);
        recyclerViewHeader.setNestedScrollingEnabled(false);
        recyclerViewHeader.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(AppController.getInstance(), LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setInitialPrefetchItemCount(4);
        recyclerViewHeader.setLayoutManager(linearLayoutManager);
        mNearFriendsLists = new ArrayList<>();

        mScrollRecyclerViewAdapter = new NearestRecyclerViewDataAdapter(AppController.getInstance(), mNearFriendsLists, true, NearFriends.VIEW_TYPE_ACTIVE);
        mScrollRecyclerViewAdapter.setHasStableIds(true);
        recyclerViewHeader.setAdapter(mScrollRecyclerViewAdapter);





        menuLabelsRight = (FloatingActionMenu) findViewById(R.id.menu_labels_right);
        com.github.clans.fab.FloatingActionButton search = (com.github.clans.fab.FloatingActionButton) menuLabelsRight.findViewById(R.id.search);
        com.github.clans.fab.FloatingActionButton friends = (com.github.clans.fab.FloatingActionButton) menuLabelsRight.findViewById(R.id.friends);

        search.setOnClickListener(v ->
        {
            if (searchView != null)
            {
                openSearch();
            }
            if (menuLabelsRight != null)
            {
                menuLabelsRight.close(true);
            }
        });


        friends.setOnClickListener(v ->
        {
            if (menuLabelsRight != null)
            {
                menuLabelsRight.close(true);
                String getFriendsList = UserUtils.getUserFriendLists(XMPPChatContactActivity.this);
                Gson gson = new Gson();
                mFriendLists = gson.fromJson(getFriendsList, ActiveFriends.class);
                if (mFriendLists != null)
                {
                    goToSearchActivity(mFriendLists, "Members");
                }

                else
                    activeFriends();

            }
        });



        if (contactList!= null)
        {
            contactListAdapter = new XMPPContactListAdapter(XMPPChatContactActivity.this,contactList);
            contactRecycler.setLayoutManager(layoutManager);
            contactRecycler.setAdapter(contactListAdapter);
        }


        if (activeSeeAllLayout != null)
        {
            activeSeeAllLayout.setOnClickListener(v -> goToList(mActiveFriends, "Active Users"));
        }






        SwipeRefreshLayout.OnRefreshListener swipeRefreshListner = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh()
            {
                activeFriends();
            }
        };

        swipeRefreshLayout.post(new Runnable() {
            @Override public void run()
            {
                if (!swipeRefreshLayout.isRefreshing())
                {
                    swipeRefreshLayout.setRefreshing(true);
                }
                swipeRefreshListner.onRefresh();
            }
        });


    }






    private void goToSearchActivity(ActiveFriends mActiveFriends, String mPageName)
    {
        Intent intent = new Intent(AppController.getInstance(), SearchViewActivity.class);
        Gson gson = new Gson();
        String activeUsersData = gson.toJson(mActiveFriends, ActiveFriends.class);
        intent.putExtra("activeUsersList", activeUsersData);
        intent.putExtra("pageName", mPageName);
        startActivity(intent);
        if (CheckOsVersion.isPreLollipop()) {
            this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }



    private void goToList(ActiveFriends mActiveFriends, String mPageName)
    {
        Intent intent = new Intent(AppController.getInstance(), ActiveListActivity.class);
        Gson gson = new Gson();
        String activeUsersData = gson.toJson(mActiveFriends, ActiveFriends.class);
        intent.putExtra("activeUsersList", activeUsersData);
        intent.putExtra("pageName", mPageName);
        startActivity(intent);
        if (CheckOsVersion.isPreLollipop())
        {
            this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }





    private void openSearch()
    {
        if(searchView != null && actionViewItem != null){
            actionViewItem.expandActionView();
            searchView.requestFocus();
        }
    }


    private void activeFriends()
    {

        if (!CheckNetworkConnection.isConnectionAvailable(XMPPChatContactActivity.this))
            return;

        ApiInterface apiService = ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        activeFriendsSubscription = apiService.getActiveFriends(userId, apiKey)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ActiveFriends>()
                {
                    @Override
                    public void onCompleted()
                    {
                        Log.d("activeFriends", "onCompleted: ");
                        if (isRefreshing)
                        {
                            isRefreshing = false;
                        }

                        if (swipeRefreshLayout.isRefreshing())
                        {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Log.e("activeFriends", "onError: ");
                        if (isRefreshing)
                        {
                            isRefreshing = false;
                        }
                    }

                    @Override
                    public void onNext(ActiveFriends activeFriends)
                    {
                        if (activeFriends != null)
                        {
                            mActiveFriends = activeFriends;
                            List<NearFriends.NearFriendsData> activeFriendsData = activeFriends.getActiveFriendsData();
                            if (activeFriendsData != null)
                            {
                                if (isRefreshing)
                                {
                                    isRefreshing = false;
                                    update(activeFriendsData);
                                }
                                else
                                {
                                    mNearFriendsLists.clear();
                                    mNearFriendsLists.addAll(activeFriendsData);
                                    doLayoutChanges(mNearFriendsLists);
                                    if (swipeRefreshLayout.isRefreshing())
                                    {
                                        swipeRefreshLayout.setRefreshing(false);
                                    }

                                }
                            }
                        }
                        Log.d("activeFriends", "onNext: ");
                        if (swipeRefreshLayout.isRefreshing())
                        {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                });
    }




    private void doLayoutChanges(List<NearFriends.NearFriendsData> mNearFriendsLists)
    {
        if (mNearFriendsLists == null)
            return;

        if (mNearFriendsLists.size() > 0)
        {
            if (activeLayout != null && !activeLayout.isShown())
                activeLayout.setVisibility(View.VISIBLE);
            if (mNearFriendsLists.size() >= 6 )
            {
                activeSeeAllLayout.setVisibility(View.VISIBLE);
            }
            else
            {
                activeSeeAllLayout.setVisibility(View.GONE);
            }
        }
        else
        {
            mNearFriendsLists.clear();
            activeLayout.setVisibility(View.GONE);
            activeSeeAllLayout.setVisibility(View.GONE);
        }
        if (mScrollRecyclerViewAdapter != null)
            mScrollRecyclerViewAdapter.notifyDataSetChanged();
        if (contactListAdapter != null)
            contactListAdapter.notifyDataSetChanged();

        if (swipeRefreshLayout.isRefreshing())
        {
            swipeRefreshLayout.setRefreshing(false);
        }

    }




    private void update(List<NearFriends.NearFriendsData> activeFriends)
    {
        new Handler().post(() ->
        {
            if (mScrollRecyclerViewAdapter != null){
                if (activeLayout != null) {
                    if (activeFriends != null) {
                        mNearFriendsLists.clear();
                        mNearFriendsLists.addAll(activeFriends);
                        setScrollAdapter(mNearFriendsLists);
                        if (swipeRefreshLayout.isRefreshing())
                        {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                    else {
                        activeLayout.setVisibility(View.GONE);
                        activeSeeAllLayout.setVisibility(View.GONE);
                    }
                }
            }

            if (swipeRefreshLayout.isRefreshing())
            {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }



    private void setScrollAdapter(List<NearFriends.NearFriendsData> mNearFriendsLists)
    {
        if (recyclerViewHeader != null && mNearFriendsLists != null)
        {
            mScrollRecyclerViewAdapter = new NearestRecyclerViewDataAdapter(AppController.getInstance(), mNearFriendsLists, true, NearFriends.VIEW_TYPE_ACTIVE);
            mScrollRecyclerViewAdapter.setHasStableIds(true);
            recyclerViewHeader.setAdapter(mScrollRecyclerViewAdapter);
            doLayoutChanges(mNearFriendsLists);
        }

    }







    @Override
    protected void onRestart()
    {
        super.onRestart();

        if (!isMyServiceRunning(RoosterConnectionService.class))
        {
            // start service to get server connection
            // start service to get server connection
            Intent i1 = new Intent(XMPPChatContactActivity.this, RoosterConnectionService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                startForegroundService(i1);
            }
            else
            {
                startService(i1);
            }
        }

        finish();
        startActivity(new Intent(getApplicationContext(),XMPPChatContactActivity.class));

    }


    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(mReceiveMessageBroadcastReceiver);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        mReceiveMessageBroadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();
                switch (action)
                {
                    case Constant.BroadCastMessages.UI_NEW_MESSAGE_FLAG:
                        {
                        contactList = chatMessagesTable.getContactList();
                        contactListAdapter = new XMPPContactListAdapter(XMPPChatContactActivity.this, contactList);
                        contactRecycler.setLayoutManager(layoutManager);
                        contactRecycler.setAdapter(contactListAdapter);
                      }

                        return;
                }

            }
        };

        IntentFilter filter = new IntentFilter(Constant.BroadCastMessages.UI_NEW_MESSAGE_FLAG);
        registerReceiver(mReceiveMessageBroadcastReceiver,filter);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return super.onSupportNavigateUp();
    }







    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        actionViewItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(actionViewItem);
        searchView.setOnQueryTextListener(this);
        return super.onPrepareOptionsMenu(menu);
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.action_search)
        {
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            searchView = (SearchView) MenuItemCompat.getActionView(item);
            searchView.setOnQueryTextListener(this);
            if (searchManager != null)
            {
                searchView.setSearchableInfo( searchManager.getSearchableInfo(getComponentName()));
            }
            return true;
        } else if (id == android.R.id.home) {
            this.finish();
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }








    private boolean isMyServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }



    @Override
    public boolean onQueryTextSubmit(String query)
    {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText)
    {


        if(contactList != null && contactList.size() > 0)
        {
            final List<XMPPChatMessageModel> filteredModelList = filter(contactList, newText);

            if (filteredModelList != null && filteredModelList.size() > 0)
            {
                contactListAdapter.setFilter(filteredModelList, newText);
                return true;
            }
            else
            {
                if(contactRecycler != null){
                    SnackBarDialog.show(contactRecycler, "No user found");
                }
                return false;
            }
        }
        return true;
    }




    private List<XMPPChatMessageModel> filter(List<XMPPChatMessageModel> chatRooms, String query) {

        final List<XMPPChatMessageModel> filteredName = new ArrayList<>();

        if(chatRooms == null)
            return filteredName;

        for (XMPPChatMessageModel chatRoom : chatRooms)
        {
            final String text = chatRoom.getFriendsName().toLowerCase().toLowerCase();
            if (text.contains(query.toLowerCase())) {
                filteredName.add(chatRoom);
            }
        }
        return filteredName;
    }



    @Override
    public void onRefresh()
    {
        AppController.getInstance().getPreferenceManager().clearMessageRead();
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance()))
        {
            isRefreshing = true;
            new Handler().postDelayed(this::activeFriends, 1000);
        } else
            Toast.makeText(AppController.getInstance(), "No internet connections available", Toast.LENGTH_SHORT).show();
    }



    private void showProgress()
    {
        runOnUiThread(() -> ProgressBarDialog.showLoader(XMPPChatContactActivity.this, false));
    }

    private void hideProgress() {
        runOnUiThread(ProgressBarDialog::dismissLoader);
    }





    //  new method created by me on 9th of April
    public class LoadChatHistoty extends AsyncTask<String, Void, Void>
    {

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            showProgress();
        }

        protected Void doInBackground(String... strings)
        {

            Response.Listener<String> jsonListener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response)
                {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run()
                        {
                            Toast.makeText(getApplicationContext(),"we are getting response", Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            };

            Response.ErrorListener errorListener = new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    Log.e("ERROR SERVER ", error.toString());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"error", Toast.LENGTH_SHORT).show();
                        }
                    });
                    hideProgress();
                }
            };


            StringRequest chatHistoryRequest = new StringRequest(Request.Method.POST, "https://myscrap.com/android/msChatsRooms", jsonListener, errorListener) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError
                {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("jId", strings[0]);
                    params.put("apiKey", strings[1]);
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError
                {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    return headers;
                }


            };
            chatHistoryRequest.setRetryPolicy(new DefaultRetryPolicy(
                    5000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            Volley.newRequestQueue(getApplicationContext()).add(chatHistoryRequest);
            return null;

        }


    }




}
