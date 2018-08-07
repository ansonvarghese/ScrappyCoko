package com.myscrap.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.myscrap.R;
import com.myscrap.application.AppController;
import com.myscrap.model.NearFriends;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.view.PreCachingLayoutManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Ms2 on 6/5/2016.
 */
public class NearestRecyclerViewDataAdapter extends RecyclerView.Adapter<NearestRecyclerViewDataAdapter.ItemRowHolder> {

    private List<NearFriends.NearFriendsData>  dataListFriends = new ArrayList<>();
    private Context mContext;
    private NearestRecyclerViewDataAdapter mRecyclerViewDataAdapter;
    private boolean isActiveNowList = false;
    private NearestSectionListDataAdapter itemListDataAdapter;
    private int VIEW_TYPE;
    public NearestRecyclerViewDataAdapter(Context context, List<NearFriends.NearFriendsData> dataListFriends, boolean isActiveNowList, int viewType)
    {
        this.mContext = context;
        this.VIEW_TYPE = viewType;
        this.isActiveNowList = isActiveNowList;
        this.dataListFriends.clear();
        this.dataListFriends = dataListFriends;
        this.mRecyclerViewDataAdapter = this;

    }

    @Override
    public NearestRecyclerViewDataAdapter.ItemRowHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        @SuppressLint("InflateParams")
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_feeds_top_header, null, false);
        return new ItemRowHolder(v);
        }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(final NearestRecyclerViewDataAdapter.ItemRowHolder itemRowHolder, int i) {
        if (itemRowHolder != null) {
            if (!dataListFriends.isEmpty()) {
                itemRowHolder.mLinearLayout.setVisibility(View.VISIBLE);
            }
            if (itemListDataAdapter == null)
            {
                itemListDataAdapter = new NearestSectionListDataAdapter(dataListFriends,mContext, isActiveNowList,this.VIEW_TYPE);
                itemListDataAdapter.setHasStableIds(true);
                PreCachingLayoutManager layoutManager = new PreCachingLayoutManager(mContext);
                if (this.VIEW_TYPE == 2){
                    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                } else {
                    layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                }
                layoutManager.setExtraLayoutSpace(DeviceUtils.getScreenWidth(mContext));
                itemRowHolder.recycler_view_list.setHasFixedSize(true);
                itemRowHolder.recycler_view_list.setLayoutManager(layoutManager);
                itemRowHolder.recycler_view_list.setNestedScrollingEnabled(false);
                itemRowHolder.recycler_view_list.setAdapter(itemListDataAdapter);
                if (itemRowHolder.recycler_view_list != null && itemRowHolder.recycler_view_list.getLayoutManager() != null) {
                    itemRowHolder.recycler_view_list.smoothScrollToPosition(0);
                }
            } else {
                itemListDataAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void swap(final List<NearFriends.NearFriendsData> mUserSearchModelLists) {
        AppController.runOnUIThread(() -> {
            dataListFriends.clear();
            dataListFriends.addAll(mUserSearchModelLists);
            if (mRecyclerViewDataAdapter != null)
                mRecyclerViewDataAdapter.notifyDataSetChanged();
        });

    }

    public void setFilter(List<NearFriends.NearFriendsData> filteredModelList) {
        this.dataListFriends = filteredModelList;
        if (dataListFriends != null) {
            Set<NearFriends.NearFriendsData> hs = new HashSet<>();
            hs.addAll(dataListFriends);
            this.dataListFriends.clear();
            this.dataListFriends.addAll(hs);
            swap(dataListFriends);
        }

    }

    static class ItemRowHolder extends RecyclerView.ViewHolder {
        RecyclerView recycler_view_list;
        LinearLayout mLinearLayout;

        ItemRowHolder(View view) {
        super(view);
        this.recycler_view_list = (RecyclerView) view.findViewById(R.id.recycler_view_list);
        this.mLinearLayout= (LinearLayout) view.findViewById(R.id.total_linear_layout);
            recycler_view_list.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                }
            });
        }

    }

}
