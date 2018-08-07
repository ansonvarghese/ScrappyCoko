package com.myscrap.xmppdata;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.myscrap.xmppmodel.ChatContactModel;
import com.myscrap.xmppmodel.XMPPChatMessageModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by darshan on 17/4/17.
 */

public class ChatContactTable extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME="MYSCRAP_XMPP_STORAGE";
    private static final int DATABASE_VERSION=1;



    private static final String TABLE_NAME = "ChatContactTable";
    private static final String KEY_ID = "key_id";
    private static final String USER_ID = "userId";
    private static final String FRIENDS_USER_ID = "friendsUserId";
    private static final String USER_JID = "userJid";
    private static final String FRIENDS_JID = "friendsJid";
    private static final String PROFILE_PICTURE = "profilePicture";
    private static final String NAME = "name";
    private static final String LAST_MESSAGE = "lastMessage";
    private static final String CREATED_AT = "createdAt";
    private static final String SEEN_TIME = "seenTime";
    private static final String COLOR_CODE = "colorCode";
    private static final String MESSAGE_COUNT = "messageCount";
    private static final String MESSAGE_TYPE = "messageType";
    private static final String MESSAGE_LINK_IMAGE = "messageLinkImage";
    private static final String MESSAGE_LINK_TITLE = "messageLinkTitle";
    private static final String MESSAGE_LINK_SUBTITLE = "messageLinkSubTitle";
    private static final String MESSAGE_LINK_CONTENT = "messageLinkContent";
    private static final String MESSAGE_ID = "messageId";
    private static final String READ_STATUS = "readStatus";
    private static final String IS_SENDER = "isSender";




    private SQLiteDatabase database;
    private String SqlQuery;
    private Context mContext;



    public ChatContactTable(Context context)
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
                +USER_ID+ " varchar not null unique,"
                +FRIENDS_USER_ID+ " text,"
                +USER_JID+ " text,"
                +FRIENDS_JID+ " text,"
                +PROFILE_PICTURE+ " text,"
                +NAME+ " text,"
                +LAST_MESSAGE+ " text,"
                +CREATED_AT+ " text,"
                +SEEN_TIME+ " text,"
                +COLOR_CODE+ " text,"
                +MESSAGE_COUNT+ " text,"
                +MESSAGE_TYPE+ " text,"
                +MESSAGE_LINK_IMAGE+ " text,"
                +MESSAGE_LINK_TITLE+ " text,"
                +MESSAGE_LINK_SUBTITLE+ " text,"
                +MESSAGE_LINK_CONTENT+ " text,"
                +MESSAGE_ID+ " text,"
                +READ_STATUS+ " text,"
                +IS_SENDER+" text)";
        sqLiteDatabase.execSQL(SqlQuery);
    }



    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int newVersion, int oldVersion)
    {
        onCreate(sqLiteDatabase);
    }




    public void insertChat(ChatContactModel model)
    {
        database = this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put(USER_ID,model.getUserId());
        cv.put(FRIENDS_USER_ID, model.getFriendsUserId());
        cv.put(USER_JID, model.getUserJid());
        cv.put(FRIENDS_JID, model.getFriendsJid());
        cv.put(PROFILE_PICTURE, model.getProfilePic());
        cv.put(NAME, model.getName());
        cv.put(LAST_MESSAGE, model.getLastMessage());
        cv.put(CREATED_AT, model.getCreatedAt());
        cv.put(SEEN_TIME,model.getSeenTime());
        cv.put(COLOR_CODE,model.getColorCode());
        cv.put(MESSAGE_COUNT,model.getMessageCount());
        cv.put(MESSAGE_TYPE,model.getMessageType());
        cv.put(MESSAGE_LINK_IMAGE,model.getMessageLinkImage());
        cv.put(MESSAGE_LINK_SUBTITLE,model.getMessageLinkSubtitle());
        cv.put(MESSAGE_LINK_TITLE, model.getMessageLinkTitle());
        cv.put(MESSAGE_LINK_CONTENT,model.getMessageLinkContent());
        cv.put(MESSAGE_ID,model.getMessageId());
        cv.put(READ_STATUS,model.getReadStatus());
        cv.put(IS_SENDER,model.getIsSender());


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


  /*  public boolean isChatAvailable(String chatId)
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

    }*/


    public void dropTable()
    {
        database=this.getWritableDatabase();
        database.execSQL("drop table "+TABLE_NAME);
    }

   /* public void updateSeenStatus(String textId)
    {
        database = getWritableDatabase();
        String strSQL = "UPDATE "+TABLE_NAME+" SET "+READ_STATUS+" ='1'  WHERE "+TEXT_ID+" ='"+ textId+"'";
        database.execSQL(strSQL);
    }
*/
    public String getUnreadCount(String friendsJid)
    {

        database = getWritableDatabase();
        String query = "SELECT * FROM "+TABLE_NAME+" WHERE "+FRIENDS_JID+"='"+friendsJid+"' AND "+READ_STATUS+"='0'";
        Cursor cursor = database.rawQuery(query,null);

        if (cursor != null && cursor.getCount() >0)
        {
            return String.valueOf(cursor.getCount());
        }
        else
        {
            return "0";
        }

    }


    public String getTotalCount()
    {

        database = getWritableDatabase();
        String query = "SELECT * FROM "+TABLE_NAME+" WHERE "+READ_STATUS+"='0'";
        Cursor cursor = database.rawQuery(query,null);

        if (cursor != null && cursor.getCount()>0)
        {
            return String.valueOf(cursor.getCount());
        }
        else
        {
            return "0";
        }

    }

    public String getUserCount()
    {

        database = getWritableDatabase();
        String query = "SELECT DISTINCT "+FRIENDS_JID+" FROM "+TABLE_NAME+" WHERE "+READ_STATUS+"='0'";
        Cursor cursor = database.rawQuery(query,null);

        if (cursor != null && cursor.getCount()>0)
        {
            return String.valueOf(cursor.getCount());
        }
        else
        {
            return "0";
        }

    }


}
