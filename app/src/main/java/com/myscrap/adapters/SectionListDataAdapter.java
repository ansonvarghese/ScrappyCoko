package com.myscrap.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.myscrap.ChatRoomActivity;
import com.myscrap.R;
import com.myscrap.application.AppController;
import com.myscrap.model.PeopleYouMayKnowItem;
import com.myscrap.utils.DeviceUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ms2 on 6/5/2016.
 */
public class SectionListDataAdapter extends RecyclerView.Adapter<SectionListDataAdapter.SingleItemRowHolder> {

    private List<PeopleYouMayKnowItem> peopleYouMayKnowItemList;
    private Context mContext;
    private boolean isActiveList = false;

    SectionListDataAdapter(List<PeopleYouMayKnowItem> peopleYouMayKnowItemList, Context context, boolean isActiveList) {
        this.peopleYouMayKnowItemList = new ArrayList<>();
        this.peopleYouMayKnowItemList = peopleYouMayKnowItemList;
        this.isActiveList = isActiveList;
        this.mContext = context;
    }

    @Override
    public SingleItemRowHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        @SuppressLint("InflateParams") View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_single_card, null);
        return new SingleItemRowHolder(v);
    }

    @Override
    public void onBindViewHolder(final SingleItemRowHolder holder, int i) {
        final PeopleYouMayKnowItem mPeopleYouMayKnowItem = peopleYouMayKnowItemList.get(i);
        if (mPeopleYouMayKnowItem.getFriendProfilePic() != null){
            if(mPeopleYouMayKnowItem.getFriendProfilePic().equalsIgnoreCase("") || mPeopleYouMayKnowItem.getFriendProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                    || mPeopleYouMayKnowItem.getFriendProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                holder.iconProfile.setImageResource(R.drawable.bg_circle);
                if(mPeopleYouMayKnowItem.getColorCode() != null && !mPeopleYouMayKnowItem.getColorCode().equalsIgnoreCase("") && mPeopleYouMayKnowItem.getColorCode().startsWith("#")){
                    holder.iconProfile.setColorFilter(Color.parseColor(mPeopleYouMayKnowItem.getColorCode()));
                } else {
                    holder.iconProfile.setColorFilter(DeviceUtils.getRandomMaterialColor(mContext, "400"));
                }

                holder.iconText.setVisibility(View.VISIBLE);
                if (mPeopleYouMayKnowItem.getFriendName() != null && !mPeopleYouMayKnowItem.getFriendName().equalsIgnoreCase("")){
                    String[] split = mPeopleYouMayKnowItem.getFriendName().split("\\s+");
                    if (split.length > 1){
                        String first = split[0].substring(0,1);
                        String last = split[1].substring(0,1);
                        String initial = first + ""+ last ;
                        holder.iconText.setText(initial.toUpperCase().trim());
                    } else {
                        if (split[0] != null && split[0].trim().length() >= 1) {
                            String first = split[0].substring(0, 1);
                            holder.iconText.setText(first.toUpperCase().trim());
                        }
                    }
                }
            } else {
                Uri uri = Uri.parse(mPeopleYouMayKnowItem.getFriendProfilePic());
                RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                holder.iconProfile.setHierarchy(new GenericDraweeHierarchyBuilder(mContext.getResources())
                        .setRoundingParams(roundingParams)
                        .build());
                roundingParams.setRoundAsCircle(true);
                holder.iconProfile.setImageURI(uri);
                holder.iconProfile.setColorFilter(null);
                holder.iconText.setVisibility(View.GONE);
            }
        }


        if (isActiveList) {
            holder.online.setVisibility(View.VISIBLE);
            holder.tvTitle.setVisibility(View.VISIBLE);
            if (mPeopleYouMayKnowItem.getFriendName() != null && !mPeopleYouMayKnowItem.getFriendName().equalsIgnoreCase("")){
                String firstName = mPeopleYouMayKnowItem.getFriendName();
                String[] name = firstName.split(" ");
                holder.tvTitle.setText(name[0]);
            } else {
                holder.tvTitle.setText(mPeopleYouMayKnowItem.getFriendName());
            }
        } else {
            holder.online.setVisibility(View.GONE);
            holder.tvTitle.setVisibility(View.GONE);
        }

    }


    @Override
    public int getItemCount() {
        return (null != peopleYouMayKnowItemList ? peopleYouMayKnowItemList.size() : 0);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }



    class SingleItemRowHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, online;
        private  SimpleDraweeView iconProfile;
        private  TextView iconText;
        SingleItemRowHolder(View view) {
            super(view);
            this.tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            this.online = (TextView) view.findViewById(R.id.online);
            this.iconProfile = (SimpleDraweeView) view.findViewById(R.id.icon_profile);
            this.iconText = (TextView) view.findViewById(R.id.icon_text);
            view.setOnClickListener(v -> {
                int position = getAdapterPosition();
                goToChat(peopleYouMayKnowItemList.get(position).getFriendId(),peopleYouMayKnowItemList.get(position).getFriendName(),peopleYouMayKnowItemList.get(position).getFriendProfilePic(), peopleYouMayKnowItemList.get(position).getColorCode());
            });
        }
    }

    private void goToChat(String id, String from, String chatRoomProfilePic, String color) {
        Intent intent = new Intent(AppController.getInstance(), ChatRoomActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("chatRoomId", id);
        intent.putExtra("color", color);
        intent.putExtra("chatRoomName", from);
        intent.putExtra("chatRoomProfilePic", chatRoomProfilePic);
        mContext.startActivity(intent);
    }
}
