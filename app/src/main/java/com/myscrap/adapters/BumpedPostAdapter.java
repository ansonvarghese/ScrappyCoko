package com.myscrap.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.myscrap.R;
import com.myscrap.UserFriendProfileActivity;
import com.myscrap.activity.XMPPChatRoomActivity;
import com.myscrap.model.Bumped;
import com.myscrap.xmppresources.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ms3 on 11/28/2017.
 */

public class BumpedPostAdapter extends RecyclerView.Adapter<BumpedPostAdapter.ViewHolder>{

    private Context mContext;
    private List<Bumped.BumpedPostItem> mBumpedPost = new ArrayList<>();
    private BumpedPostAdapter.BumpedPostAdapterListener mListener;




    public BumpedPostAdapter(Context mContext, List<Bumped.BumpedPostItem> bumpedPost, BumpedPostAdapter.BumpedPostAdapterListener listener) {
        this.mContext = mContext;
        this.mListener = listener;
        this.mBumpedPost = bumpedPost;
    }

    @Override
    public BumpedPostAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bumped_post_card, parent, false);

        return new BumpedPostAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final BumpedPostAdapter.ViewHolder holder, int position) {
        final Bumped.BumpedPostItem bumpedPost = mBumpedPost.get(position);
        if (bumpedPost.getName() != null && !bumpedPost.getName().equalsIgnoreCase(""))
            holder.name.setText(bumpedPost.getName());

        if (bumpedPost.getDesignation() != null && !bumpedPost.getDesignation().equalsIgnoreCase(""))
            holder.designation.setText(bumpedPost.getDesignation());


        if (bumpedPost.getProfilePic() != null )
        {
            if(!bumpedPost.getProfilePic().equalsIgnoreCase("") && !bumpedPost.getProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                    && !bumpedPost.getProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")) {
                Uri uri = Uri.parse(bumpedPost.getProfilePic());
                holder.profilePic.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.FOCUS_CROP);
                holder.profilePic.setImageURI(uri);
            } else {
                Uri uri = new Uri.Builder()
                        .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                        .path(String.valueOf(R.drawable.profile_anonymous_user))
                        .build();
                holder.profilePic.setImageURI(uri);
            }
        }

        if (bumpedPost.getTimeStamp() != null && !bumpedPost.getTimeStamp().equalsIgnoreCase("")
                && !bumpedPost.getTimeStamp().equalsIgnoreCase("true")
                && !bumpedPost.getTimeStamp().equalsIgnoreCase("false")) {
            holder.timeStamp.setText(bumpedPost.getTimeStamp());
            holder.timeStamp.setVisibility(View.VISIBLE);
        }
        else {
            holder.timeStamp.setVisibility(View.GONE);
        }


        if (bumpedPost.getNew() != null && bumpedPost.getNew() == true)
        {
            holder.title.setVisibility(View.GONE);
            holder.isNew.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.isNew.setVisibility(View.GONE);
            holder.title.setVisibility(View.VISIBLE);
            holder.title.setText("Last Bumped");
        }

/*
        if (bumpedPost.getTitle() != null && !bumpedPost.getTitle().equalsIgnoreCase(""))
        {
            holder.title.setText(bumpedPost.getTitle());
            holder.title.setVisibility(View.VISIBLE);
        } else {
            holder.title.setVisibility(View.GONE);
        }*/

        holder.name.setOnClickListener(v -> goToUserFriendProfile(bumpedPost.getUserId()));
        holder.designation.setOnClickListener(v -> goToUserFriendProfile(bumpedPost.getUserId()));
        holder.profilePic.setOnClickListener(v -> goToUserFriendProfile(bumpedPost.getUserId()));

        holder.deletePost.setOnClickListener(v -> mListener.onRemovePost(bumpedPost.getUserId(), holder.getAdapterPosition()));

        holder.chatLayout.setOnClickListener(v ->
        {
            if (bumpedPost.getUserId() != null && !bumpedPost.getUserId().equalsIgnoreCase("")
                    && bumpedPost.getProfilePic() != null && !bumpedPost.getProfilePic().equalsIgnoreCase("")
                    && bumpedPost.getName() != null && !bumpedPost.getName().equalsIgnoreCase("")
                    && bumpedPost.getColorCode() != null && !bumpedPost.getColorCode().equalsIgnoreCase("")
                    && bumpedPost.getjId() != null && !bumpedPost.getjId().equalsIgnoreCase(""))
                    goToChat(bumpedPost.getjId(),bumpedPost.getUserId(),bumpedPost.getName(), bumpedPost.getProfilePic(), bumpedPost.getColorCode());
            //      goToChat(bumpedPost.getUserId(),bumpedPost.getName(), bumpedPost.getProfilePic(), bumpedPost.getColorCode());
        });
    }



    @Override
    public int getItemCount() {
        return mBumpedPost.size();
    }

    private void goToUserFriendProfile(String friendId) {
        if (friendId != null && !friendId.equalsIgnoreCase("")){
            Intent intent = new Intent(mContext, UserFriendProfileActivity.class);
            intent.putExtra("friendId", friendId);
            mContext.startActivity(intent);
        }
    }



/*

    private void goToChat(String chatRoomId, String from, String chatRoomProfilePic, String color)
    {
        Intent intent = new Intent(AppController.getInstance(), ChatRoomActivity.class);
        intent.putExtra("chatRoomId", chatRoomId);
        intent.putExtra("color", color);
        intent.putExtra("chatRoomName", from);
        intent.putExtra("chatRoomProfilePic", chatRoomProfilePic);
        intent.putExtra("online", "0");
        mContext.startActivity(intent);
    }

*/

    private void goToChat(String jid,String userId, String name, String chatRoomProfilePic, String color)
    {
        if (jid != null)
        {
            Intent intent = new Intent(mContext, XMPPChatRoomActivity.class);

            intent.putExtra(Constant.FRIENDS_JID, jid);
            intent.putExtra(Constant.FRIENDS_ID, userId);
            intent.putExtra(Constant.FRIENDS_NAME, name);
            intent.putExtra(Constant.FRIENDS_URL, chatRoomProfilePic);
            intent.putExtra(Constant.FRIENDS_COLOR, color);

            mContext.startActivity(intent);
        }
    }




    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView name, title, timeStamp, designation ,isNew;
        public SimpleDraweeView profilePic;
        public ImageView deletePost;
        RelativeLayout chatLayout;
        FrameLayout imageLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            title = itemView.findViewById(R.id.title);
            deletePost = itemView.findViewById(R.id.deletePost);
            timeStamp = itemView.findViewById(R.id.time);
            designation = itemView.findViewById(R.id.designation);
            profilePic = itemView.findViewById(R.id.profile);
            chatLayout = itemView.findViewById(R.id.message_to_user);
            imageLayout = itemView.findViewById(R.id.image_layout);
            isNew = itemView.findViewById(R.id.is_new);
        }
    }




    public interface BumpedPostAdapterListener
    {
        void onRemovePost(String id, int position);
    }

}
