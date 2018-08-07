package com.myscrap.xmppdata;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.myscrap.xmppmodel.XMPPChatMessageModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by darshan on 17/4/17.
 */

public class ChatHistoryTable extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME="MYSCRAP_XMPP_STORAGE";
    private static final int DATABASE_VERSION=1;

    private static final String TABLE_NAME="chatHistoryTable";
    private static final String KEY_ID="key_id";
    private static final String TEXT_ID="text_id";
    private static final String FRIENDS_JID = "friendsJid";
    private static final String FRIENDS_USER_ID = "friends_user_id";
    private static final String FRIENDS_NAME = "friends_name";
    private static final String FRIENDS_URL = "friends_url";
    private static final String CHAT="chat";
    private static final String TEXT_TYPE="text_type";
    private static final String TIME_STAMP="time_stamp";
    private static final String READ_STATUS="read_status";
    private static final String COLOR="color";



    private SQLiteDatabase database;
    private List<XMPPChatMessageModel> textList;
    private XMPPChatMessageModel textModel;
    private String SqlQuery;
    private Context mContext;



    public ChatHistoryTable(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext=context;
        database=this.getWritableDatabase();
        onCreate(database);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {

        SqlQuery="create table if not exists "+TABLE_NAME+"("+KEY_ID+" integer primary key autoincrement ,"
                +TEXT_ID+ " text not null unique,"
                +FRIENDS_JID+ " text,"
                +FRIENDS_USER_ID+ " text,"
                +FRIENDS_NAME+ " text,"
                +FRIENDS_URL+ " text,"
                +CHAT+ " text,"
                +TEXT_TYPE+ " text,"
                +TIME_STAMP+ " text,"
                +READ_STATUS+ " text,"
                +COLOR+" text)";
        sqLiteDatabase.execSQL(SqlQuery);
    }



    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int newVersion, int oldVersion)
    {
        onCreate(sqLiteDatabase);
    }




    public void insertChat(XMPPChatMessageModel model)
    {
        database = this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put(TEXT_ID,model.getTextId());
        cv.put(FRIENDS_JID,model.getFriendsJid());
        cv.put(FRIENDS_USER_ID, model.getFriendsUserId());
        cv.put(FRIENDS_NAME, model.getFriendsName());
        cv.put(FRIENDS_URL, model.getFriendsUrl());
        cv.put(CHAT, model.getChat());
        cv.put(TEXT_TYPE, model.getTextType());
        cv.put(TIME_STAMP, model.getTimestamp());
        cv.put(READ_STATUS, model.getReadStatus());
        cv.put(COLOR,model.getColor());
        database.insert(TABLE_NAME,null,cv);
    }


    public List<XMPPChatMessageModel> getChatData(String friendsJid)
    {
        List<XMPPChatMessageModel> chatList = new ArrayList<>();
        String selectQuery = "SELECT * FROM "+TABLE_NAME+" WHERE "+FRIENDS_JID+"='"+friendsJid+"'";
        database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery(selectQuery,null);
        if (cursor != null && cursor.getCount() >  0)
        {
            while (cursor.moveToNext())
            {
                String textId = cursor.getString(1);
                String frndJid = cursor.getString(2);
                String frindsUserId = cursor.getString(3);
                String friendsName = cursor.getString(4);
                String friendsUrl = cursor.getString(5);
                String chat  = cursor.getString(6);
                String textType = cursor.getString(7);
                String timestamp = cursor.getString(8);
                String readStatus = cursor.getString(9);
                String color = cursor.getString(10);
                chatList.add(new XMPPChatMessageModel(textId,frndJid,frindsUserId,friendsName,friendsUrl,chat,textType,timestamp,readStatus,color));
            }
            return chatList;
        }
        else
        return null;
    }


    public List<String> distinctJid()
    {

        List<String> jidList = new ArrayList<>();
        String selectQuery = "SELECT DISTINCT("+FRIENDS_JID+") FROM "+TABLE_NAME+" ORDER BY "+KEY_ID+" DESC";
        database = this.getWritableDatabase();
        Cursor cursor  = database.rawQuery(selectQuery,null);
        if (cursor!= null && cursor.getCount() > 0)
        {
            while (cursor.moveToNext())
            {
                jidList.add(cursor.getString(0));
            }
            return jidList;
        }
        else
            return null;

    }

    public List<XMPPChatMessageModel> getContactList()
    {

        List<XMPPChatMessageModel> contactList = new ArrayList<>();
        List<String> Jids = distinctJid();
        database = this.getWritableDatabase();
        if (Jids != null)
        {
            for (int i = 0; i < Jids.size(); i++)
            {
                String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + FRIENDS_JID + "='" + Jids.get(i) + "' ORDER BY " + KEY_ID + " DESC LIMIT 1";
                Cursor cursor = database.rawQuery(selectQuery, null);
                if (cursor != null && cursor.getCount() >0)
                {
                    cursor.moveToFirst();
                    String textId = cursor.getString(1);
                    String frndJid = cursor.getString(2);
                    String frindsUserId = cursor.getString(3);
                    String friendsName = cursor.getString(4);
                    String friendsUrl = cursor.getString(5);
                    String chat  = cursor.getString(6);
                    String textType = cursor.getString(7);
                    String timestamp = cursor.getString(8);
                    String readStatus = cursor.getString(9);
                    String color = cursor.getString(10);
                    contactList.add(new XMPPChatMessageModel(textId,frndJid,frindsUserId,friendsName,friendsUrl,chat,textType,timestamp,readStatus,color));
                }
            }

            return contactList;
        }

        else

            return null;
    }


    public boolean isChatAvailable(String chatId)
    {

        String selectQuery = "SELECT * FROM "+TABLE_NAME+" WHERE "+TEXT_ID+"='"+chatId+"'";
        database = this.getWritableDatabase();
        Cursor cursor  = database.rawQuery(selectQuery,null);
        if (cursor!= null && cursor.getCount() > 0)
        {
            return true;
        }
        else
            return false;

    }


    public void dropTable()
    {
        database=this.getWritableDatabase();
        database.execSQL("drop table "+TABLE_NAME);
    }
}
