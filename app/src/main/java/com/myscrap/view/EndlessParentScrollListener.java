package com.myscrap.view;

import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by ms3 on 5/24/2017.
 */

public abstract class EndlessParentScrollListener implements NestedScrollView.OnScrollChangeListener {
    private int currentPage = 0;
    private int previousTotalItemCount = 0;
    private boolean loading = true;

    private RecyclerView.LayoutManager mLayoutManager;

    protected EndlessParentScrollListener(RecyclerView.LayoutManager layoutManager) {
        this.mLayoutManager = layoutManager;
    }

    @Override
    public void onScrollChange(NestedScrollView scrollView, int x, int y, int oldx, int oldy) {
        View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
        int distanceToEnd = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));

        int totalItemCount = mLayoutManager.getItemCount();
        if (totalItemCount < previousTotalItemCount) {
            this.currentPage = 0;
            this.previousTotalItemCount = totalItemCount;
            if (totalItemCount == 0) {
                this.loading = true;
            }
        }

        if (y > oldy) {
            onScrollDown();
        }
        if (y < oldy) {
            onScrollUp();
        }

        if (y == 0) {
            onScrollTopReached();
        }

        if (y == (scrollView.getChildAt(0).getMeasuredHeight() - scrollView.getMeasuredHeight())) {
            onScrollBottomReached();
        }

        if (loading && (totalItemCount > previousTotalItemCount)) {
            loading = false;
            previousTotalItemCount = totalItemCount;
        }

        int visibleThresholdDistance = 300;
        if (!loading && distanceToEnd <= visibleThresholdDistance) {
            currentPage++;
            onLoadMore(currentPage, totalItemCount);
            loading = true;
        }
    }

    public void setLoading(boolean loading){
        this.loading = loading;
    }

    public abstract void onLoadMore(int page, int totalItemsCount);
    public abstract void onScrollUp();
    public abstract void onScrollDown();
    public abstract void onScrollTopReached();
    public abstract void onScrollBottomReached();
}
