package com.myscrap.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.myscrap.CreateNewsActivity;
import com.myscrap.NewsViewActivity;
import com.myscrap.R;
import com.myscrap.application.AppController;
import com.myscrap.model.DeletePost;
import com.myscrap.model.News;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by ms3 on 5/18/2017.
 */

public class NewsFragmentAdapter extends RecyclerView.Adapter<NewsFragmentAdapter.ItemViewHolder>{
    private Context mContext;
    private List<News.NewsData> mNewsDataList = new ArrayList<>();

    public NewsFragmentAdapter(Context context, List<News.NewsData> shakeFriendList, NewsFragmentAdapter.NewsFragmentAdapterListener contactsFragmentAdapterListener){
        this.mContext = context;
        this.mNewsDataList = shakeFriendList;
    }

    @Override
    public NewsFragmentAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_list_row, parent, false);
        return new NewsFragmentAdapter.ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final NewsFragmentAdapter.ItemViewHolder holder, int position) {
        final News.NewsData newsData = mNewsDataList.get(position);
        if(newsData != null) {

            if(newsData.getProfilePic() != null){
                Uri uri = Uri.parse(newsData.getProfilePic());
                holder.profileImage.setImageURI(uri);
            }

            if(newsData.getStatus() != null){
                holder.status.setText(newsData.getStatus());
            }

            if(newsData.getTimeStamp() != null && !newsData.getTimeStamp().equalsIgnoreCase("")){
                String timeAgo = UserUtils.getNewsTime(Long.parseLong(UserUtils.parsingLong(newsData.getTimeStamp())));
                if(timeAgo != null){
                    holder.profileTimeStamp.setText(timeAgo);
                    holder.profileTimeStamp.setVisibility(View.VISIBLE);
                } else {
                    holder.profileTimeStamp.setVisibility(View.GONE);
                }
            }


            if(newsData.isEditShow()){
                holder.overflow.setVisibility(View.VISIBLE);
            } else {
                holder.overflow.setVisibility(View.GONE);
            }

            holder.overflow.setOnClickListener(v -> showPopupMenu(holder.overflow, newsData, holder.getAdapterPosition(), true));


            holder.profileImage.setOnClickListener(v -> {
                Intent i = new Intent(mContext, NewsViewActivity.class);
                i.putExtra("newsId", newsData.getPostId());
                mContext.startActivity(i);
            });
            holder.status.setOnClickListener(v -> {
                Intent i = new Intent(mContext, NewsViewActivity.class);
                i.putExtra("newsId", newsData.getPostId());
                mContext.startActivity(i);
            });
            holder.profileTimeStamp.setOnClickListener(v -> {
                Intent i = new Intent(mContext, NewsViewActivity.class);
                i.putExtra("newsId", newsData.getPostId());
                mContext.startActivity(i);
            });

        }
    }

    @Override
    public int getItemCount() {
        if(mNewsDataList != null)
            return mNewsDataList.size();
        else
            return  0;
    }

    public void swap(List<News.NewsData> mContactList) {
        this.mNewsDataList = mContactList;
        this.notifyDataSetChanged();
    }

    private void showPopupMenu(View v, News.NewsData feedItem, int itemPosition, boolean isEditShow) {
        PopupMenu popup = new PopupMenu(mContext, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.over_flow, popup.getMenu());
        Menu popupMenu = popup.getMenu();
        if(!isEditShow)
            popupMenu.findItem(R.id.action_edit).setVisible(false);
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener(feedItem, itemPosition));
        popup.show();
    }

    private class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        private News.NewsData newsItem;
        private int itemPosition;
        public MyMenuItemClickListener(News.NewsData mFeedItem, int position) {
            this.newsItem = mFeedItem;
            this.itemPosition = position;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_edit:
                    Intent i = new Intent(mContext, CreateNewsActivity.class);
                    i.putExtra("page", "news");
                    i.putExtra("editPost", newsItem.getStatus());
                    Gson gson = new Gson();
                    String userData = gson.toJson(newsItem);
                    i.putExtra("tagData", userData);
                    i.putExtra("postId", ""+newsItem.getPostId());
                    mContext.startActivity(i);
                    return true;
                case R.id.action_delete:
                    if (newsItem != null ) {
                        if(mNewsDataList != null && mNewsDataList.size() > 0){
                            showDeletePostDialog(mContext, newsItem.getPostedUserId(), newsItem.getPostId(), newsItem.getAlbumId(), itemPosition);
                        }
                    }
                    return true;
                default:
            }
            return false;
        }
    }


    private void showDeletePostDialog(Context context, final String id, final String postId, final String albumID, final int itemPosition){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
        dialogBuilder.setMessage("Are you sure you want to delete this news?");
        dialogBuilder.setCancelable(true);
        dialogBuilder.setPositiveButton("DELETE", (dialog, which) -> {
            mNewsDataList.remove(itemPosition);
            notifyDataSetChanged();
            deletingPost(id, postId, albumID);
        });

        dialogBuilder.setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    private void deletingPost(String id, String postId, String albumID) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        Call<DeletePost> call = apiService.deletePost(userId, postId, albumID,apiKey);
        call.enqueue(new Callback<DeletePost>() {
            @Override
            public void onResponse(@NonNull Call<DeletePost> call, @NonNull retrofit2.Response<DeletePost> response) {

                if(response.body() != null && response.isSuccessful()){
                    DeletePost mDeletePost = response.body();
                    if(mDeletePost != null && !mDeletePost.isErrorStatus()){
                        Log.d("deletingPost", "onSuccess");
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<DeletePost> call, @NonNull Throwable t) {
                Log.d("deletingPost", "onFailure");
            }
        });
    }


    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private SimpleDraweeView profileImage;
        private ImageView overflow;
        private TextView profileName;
        private TextView profileTimeStamp;
        private TextView status;

        public ItemViewHolder(View itemView) {
            super(itemView);
            profileImage = (SimpleDraweeView) itemView.findViewById(R.id.icon_profile);
            overflow = (ImageView) itemView.findViewById(R.id.overflow);
            profileName = (TextView) itemView.findViewById(R.id.name);
            profileTimeStamp = (TextView) itemView.findViewById(R.id.timeStamp);
            status = (TextView) itemView.findViewById(R.id.status);
            profileImage.setOnClickListener(this);
            profileName.setOnClickListener(this);
            profileTimeStamp.setOnClickListener(this);
            itemView.setOnClickListener(this);
            itemView.setOnTouchListener((v, event) -> true);
        }

        @Override
        public void onClick(View v) {
        }
    }

    public interface NewsFragmentAdapterListener {
        void onAdapterClicked(int position);
    }

}
