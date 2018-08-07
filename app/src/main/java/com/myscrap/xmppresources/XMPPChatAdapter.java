package com.myscrap.xmppresources;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myscrap.R;
import com.myscrap.xmppdata.ChatMessagesTable;
import com.myscrap.xmppmodel.XMPPChatMessageModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ms3 Android MyScrap.
 */

public class XMPPChatAdapter extends RecyclerView.Adapter<XMPPChatAdapter.MyViewHolder>
{


    public interface OnInformRecyclerViewToScrollDownListener
    {
        public void onInformRecyclerViewToScrollDown( int size);
    }

    private Context context;
    public List<XMPPChatMessageModel> chatList = new ArrayList<>();
    public XMPPChatMessageModel xmppChatModel;
    public String friendsJid;



    private OnInformRecyclerViewToScrollDownListener mOnInformRecyclerViewToScrollDownListener;


    public void setmOnInformRecyclerViewToScrollDownListener(OnInformRecyclerViewToScrollDownListener mOnInformRecyclerViewToScrollDownListener) {
        this.mOnInformRecyclerViewToScrollDownListener = mOnInformRecyclerViewToScrollDownListener;
    }

    public XMPPChatAdapter(Context context,String friendsJid)
    {
        this.context = context;
        this.friendsJid = friendsJid;
        chatList = ChatMessagesModel.get(context).getMessages(friendsJid);
    }




    public void informRecyclerViewToScrollDown()
    {
        if (chatList != null)
        {
            mOnInformRecyclerViewToScrollDownListener.onInformRecyclerViewToScrollDown(chatList.size());
        }
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {

        MyViewHolder myViewHolder = null;

        if (viewType == Constant.SEND)
        {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_item_message, parent, false);
            myViewHolder = new  MyViewHolder(v);
        }

        else if (viewType == Constant.RECEIVE)
        {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_item_message_other, parent, false);
            myViewHolder = new  MyViewHolder(v);
        }

         return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position)
    {
        xmppChatModel = chatList.get(position);
        holder.bindChat(xmppChatModel);
    }

    @Override
    public int getItemViewType(int position)
    {

        xmppChatModel = chatList.get(position);

        if (xmppChatModel.getTextType().equalsIgnoreCase("send"))
        {
            return Constant.SEND;
        }
        else if (xmppChatModel.getTextType().equalsIgnoreCase("receive"))
        {
            return Constant.RECEIVE;
        }
        else
        {
            return Constant.RECEIVE;
        }
    }

    @Override
    public int getItemCount()
    {
        if (chatList != null)
        {
            return chatList.size();
        }
        else
            return 0;
    }







    public class MyViewHolder extends RecyclerView.ViewHolder
    {

        TextView my_text;
        TextView my_timestamp_text;
        TextView other_text;
        TextView other_timestamptext;

        public MyViewHolder(View itemView)
        {
            super(itemView);

            my_text = itemView.findViewById(R.id.message);
            my_timestamp_text = itemView.findViewById(R.id.timestamp);



        }

        public void bindChat(XMPPChatMessageModel chatModel)
        {
            ChatMessagesTable chatMessagesTable = new ChatMessagesTable(context);
            chatMessagesTable.updateSeenStatus(chatModel.getTextId());
            my_text.setText(chatModel.getChat());


        }
    }




    public void onMessageAdd()
    {
        chatList = ChatMessagesModel.get(context).getMessages(friendsJid);
        notifyDataSetChanged();
        informRecyclerViewToScrollDown();
    }



}
