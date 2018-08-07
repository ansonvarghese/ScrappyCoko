package com.myscrap.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.myscrap.R;
import com.myscrap.model.PeopleYouMayKnowItem;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.view.PreCachingLayoutManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ms2 on 6/5/2016.
 */
public class RecyclerViewDataAdapter extends RecyclerView.Adapter<RecyclerViewDataAdapter.ItemRowHolder> {

    private List<PeopleYouMayKnowItem>  dataListFriends = new ArrayList<>();
    private Context mContext;
    private RecyclerViewDataAdapter mRecyclerViewDataAdapter;
    private static int displayedPosition;
    private boolean isActiveNowList = false;
    private SectionListDataAdapter itemListDataAdapter;
    public RecyclerViewDataAdapter(Context context, List<PeopleYouMayKnowItem> dataListFriends, boolean isActiveNowList) {
        this.mContext = context;
        this.isActiveNowList = isActiveNowList;
        this.dataListFriends = new ArrayList<>();
        this.dataListFriends = dataListFriends;
        this.mRecyclerViewDataAdapter = this;
    }

    @Override
    public ItemRowHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        @SuppressLint("InflateParams") View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_feeds_top_header, null);
        return new ItemRowHolder(v);
        }

    @Override
    public void onBindViewHolder(final ItemRowHolder itemRowHolder, int i) {
        if (!dataListFriends.isEmpty()) {
            if(itemRowHolder != null)
                itemRowHolder.mLinearLayout.setVisibility(View.VISIBLE);
        }

        if (itemListDataAdapter == null) {
            itemListDataAdapter = new SectionListDataAdapter(dataListFriends,mContext, isActiveNowList);
            PreCachingLayoutManager layoutManager = new PreCachingLayoutManager(mContext);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            layoutManager.setExtraLayoutSpace(DeviceUtils.getScreenWidth(mContext));
            if (itemRowHolder != null) {
                itemRowHolder.recycler_view_list.setHasFixedSize(true);
                itemRowHolder.recycler_view_list.setLayoutManager(layoutManager);
                itemRowHolder.recycler_view_list.setNestedScrollingEnabled(false);
                itemRowHolder.recycler_view_list.setAdapter(itemListDataAdapter);
                if (itemRowHolder.recycler_view_list != null && itemRowHolder.recycler_view_list.getLayoutManager() != null) {
                    itemRowHolder.recycler_view_list.scrollToPosition(displayedPosition);
                }
            }
        } else {
            itemListDataAdapter.notifyDataSetChanged();
        }


    }

    @Override
    public int getItemCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public void swap(final List<PeopleYouMayKnowItem> mUserSearchModelLists) {
        new Handler().post(() -> {
            dataListFriends.clear();
            dataListFriends.addAll(mUserSearchModelLists);
            if (mRecyclerViewDataAdapter != null)
                mRecyclerViewDataAdapter.notifyDataSetChanged();
        });

    }

    class ItemRowHolder extends RecyclerView.ViewHolder {
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
                    LinearLayoutManager llm = (LinearLayoutManager) recycler_view_list.getLayoutManager();
                    displayedPosition = llm.findFirstVisibleItemPosition();
                }
            });
        }

    }

}
