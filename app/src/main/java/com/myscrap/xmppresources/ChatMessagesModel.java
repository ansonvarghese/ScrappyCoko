package com.myscrap.xmppresources;

import android.content.Context;

import com.myscrap.xmppdata.ChatMessagesTable;
import com.myscrap.xmppmodel.XMPPChatMessageModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gakwaya on 2017/12/31.
 */

public class ChatMessagesModel
{


    private static ChatMessagesModel sChatMessagesModel;
    private Context context;
    private List<XMPPChatMessageModel> messages;
    private ChatMessagesTable chatTable;



    public static ChatMessagesModel get(Context context)
    {
        if( sChatMessagesModel == null)
        {
            sChatMessagesModel = new ChatMessagesModel(context);
        }
        return sChatMessagesModel;
    }



    private ChatMessagesModel(Context context)
    {
        this.context = context;
        chatTable = new ChatMessagesTable(context);
    }


    public List<XMPPChatMessageModel> getMessages(String friendsJid)
    {
        chatTable = new ChatMessagesTable(context);
        messages = new ArrayList<>();
        messages = chatTable.getChatData(friendsJid);
        return messages;
    }


    public void addMessage(XMPPChatMessageModel message)
    {
        chatTable.insertChat(message);
    }


}
