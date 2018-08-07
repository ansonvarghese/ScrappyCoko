package com.myscrap.xmppadapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.myscrap.R;
import com.myscrap.activity.XMPPChatRoomActivity;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.xmppdata.ChatMessagesTable;
import com.myscrap.xmppmodel.XMPPChatMessageModel;
import com.myscrap.xmppresources.Constant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by ms3 Android MyScrap.
 */

public class XMPPContactListAdapter extends RecyclerView.Adapter<XMPPContactListAdapter.MyViewHolder>
{


    public Context context;
    public List<XMPPChatMessageModel> chatList;
    public XMPPChatMessageModel chatModel;
    public ChatMessagesTable chatMessagesTable;



    public XMPPContactListAdapter(Context context, List<XMPPChatMessageModel> chatList)
    {
        this.context = context;
        this.chatList = chatList;
        chatMessagesTable = new ChatMessagesTable(context);
    }




    public void setFilter(List<XMPPChatMessageModel> filteredModelList, String searchQueryString)
    {
        this.chatList = filteredModelList;
        this.notifyDataSetChanged();
    }



    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.xmpp_chat_contact_row,parent,false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position)
    {

        chatModel = chatList.get(position);

        String count = chatMessagesTable.getUnreadCount(chatModel.getFriendsJid());

        if (count != null && !count.equalsIgnoreCase("0"))
        {
            holder.unread_count_text.setVisibility(View.VISIBLE);
            holder.unread_count_text.setText(count);
        }

        if (chatModel.getReadStatus().equalsIgnoreCase("0"))
        {
            holder.time_stamp.setTypeface(holder.time_stamp.getTypeface(), Typeface.BOLD);
            holder.time_stamp.setTextColor(context.getResources().getColor(R.color.accent));
            holder.last_text.setTypeface(holder.last_text.getTypeface(), Typeface.BOLD);
            holder.from_text.setTypeface(holder.from_text.getTypeface(), Typeface.BOLD);

        }

        holder.time_stamp.setText(getChatRoomTime(Long.parseLong(chatModel.getTimestamp())));
        holder.last_text.setText(chatModel.getChat());
        holder.from_text.setText(chatModel.getFriendsName());



        if (chatModel.getFriendsUrl().equalsIgnoreCase("") ||
                chatModel.getFriendsUrl().equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                || chatModel.getFriendsUrl().equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png"))
        {

            holder.icon_profile.setImageResource(R.drawable.bg_circle);
            if (chatModel.getColor() != null && !chatModel.getColor().equalsIgnoreCase("") && chatModel.getColor().startsWith("#"))
            {
                holder.icon_profile.setColorFilter(Color.parseColor(chatModel.getColor()));
            }
            else
            {
                holder.icon_profile.setColorFilter(DeviceUtils.getRandomMaterialColor(context, "400"));
            }

            String acronyms = getFriendsNameAcronyms(chatModel.getFriendsName());
            holder.icon_text.setText(acronyms);
            holder.icon_text.setVisibility(View.VISIBLE);

        }
        else
        {

            holder.icon_text.setVisibility(View.GONE);

            Uri uri = Uri.parse(chatModel.getFriendsUrl());
            RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
            holder.icon_profile.setHierarchy(new GenericDraweeHierarchyBuilder(context.getResources())
                    .setRoundingParams(roundingParams)
                    .build());
            roundingParams.setRoundAsCircle(true);
            holder.icon_profile.setImageURI(uri);
            holder.icon_profile.setColorFilter(null);
        }






    }

    @Override
    public int getItemCount()
    {
        return chatList.size();
    }






    private String getFriendsNameAcronyms(String friendsName)
    {

        String initial = "";
        String[] split = friendsName.split("\\s+");
        if (split.length > 1){
            String first = split[0].substring(0,1);
            String last = split[1].substring(0,1);
            initial = first + ""+ last ;
        }
        else
        {
            if (split[0] != null && split[0].trim().length() == 1)
            {
                initial = split[0].substring(0,1);
            }
        }

        return initial;
    }





    private static String getChatRoomTime(long time)
    {
        if (time < 1000000000000L)
        {
            time *= 1000;
        }

        SimpleDateFormat mSimpleDateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        mSimpleDateFormat.setTimeZone(tz);
        String dateString = mSimpleDateFormat.format(time);
        return getChatTimeStamp(dateString);
    }

    private static String getChatTimeStamp(String dateStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String timestamp = "";
        Calendar calendar = Calendar.getInstance();
        String today = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        today = today.length() < 2 ? "0" + today : today;
        try {
            Date date = format.parse(dateStr);
            SimpleDateFormat todayFormat = new SimpleDateFormat("dd", Locale.getDefault());
            String dateToday = todayFormat.format(date);
            format = dateToday.equals(today) ? new SimpleDateFormat("HH:mm", Locale.getDefault()) : new SimpleDateFormat("dd MMM", Locale.getDefault());
            timestamp = format.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }





    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {


        RelativeLayout rootview;
        RelativeLayout icon_back_relative_layout;
        TextView icon_text;
        RelativeLayout icon_front_relative_layout;
        SimpleDraweeView icon_profile;

        TextView from_text;
        TextView last_text;
        TextView unread_count_text;
        TextView time_stamp;



        //  seen layout
        FrameLayout seen_layout;
        SimpleDraweeView icon_star;
        TextView count;

        LinearLayout parent_layout;


        public MyViewHolder(View itemView)
        {
            super(itemView);


            rootview = itemView.findViewById(R.id.root_view);
            icon_back_relative_layout = itemView.findViewById(R.id.icon_back);
            icon_front_relative_layout = itemView.findViewById(R.id.icon_front);
            from_text = itemView.findViewById(R.id.from);
            last_text = itemView.findViewById(R.id.txt_primary);
            icon_profile = itemView.findViewById(R.id.icon_profile);
            time_stamp = itemView.findViewById(R.id.timestamp);
            parent_layout = itemView.findViewById(R.id.parent_layout);
            icon_text = itemView.findViewById(R.id.icon_text);
            unread_count_text = itemView.findViewById(R.id.count);


            itemView.setOnClickListener(this);
            icon_profile.setOnClickListener(this);
            parent_layout.setOnClickListener(this);

        }

        @Override
        public void onClick(View view)
        {
            XMPPChatMessageModel model = chatList.get(getAdapterPosition());
            switch(view.getId())
            {
                case  R.id.icon_profile :
                {

                }
                break;

                default:
                {

                    String friendsId = model.getFriendsUserId();
                    String friendsJID = model.getFriendsJid();
                    String friendsName = model.getFriendsName();
                    String friendsUrl = model.getFriendsUrl();
                    String friendsColor = model.getColor();



                    Intent intent = new Intent(context, XMPPChatRoomActivity.class);
                    intent.putExtra(Constant.FRIENDS_ID, friendsId);
                    intent.putExtra(Constant.FRIENDS_JID, friendsJID);
                    intent.putExtra(Constant.FRIENDS_NAME, friendsName);
                    intent.putExtra(Constant.FRIENDS_URL, friendsUrl);
                    intent.putExtra(Constant.FRIENDS_COLOR, friendsColor);
                    context.startActivity(intent);
                }
                    break;
            }
        }
    }





}
