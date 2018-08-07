package com.myscrap.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.myscrap.ChatRoomActivity;
import com.myscrap.MainChatRoomActivity;
import com.myscrap.application.AppController;
import com.myscrap.model.ChatRoom;
import com.myscrap.model.MyItem;

import java.util.ArrayList;

/**
 * Created by Ms2 on 2/20/2016.
 */
public class  MyScrapSQLiteDatabase extends SQLiteOpenHelper implements MarkerListener{

    private static final String DB_NAME = "myscrap";
    /* OLD VERSION 14 05-03-2018*/
    private static final int DB_VERSION = 15;

    /*MARKER_TABLE*/
    private static final String MARKER_TABLE = "marker";

    /*MARKER_TABLE_COLUMN_NAMES*/
    private static final String KEY_ID = "id";
    private static final String KEY_MARKER_ID = "marker_id";
    private static final String KEY_MARKER_NAME = "name";
    private static final String KEY_MARKER_TYPE = "type";
    private static final String KEY_MARKER_JOIN = "is_new";
    private static final String KEY_MARKER_STATE = "state";
    private static final String KEY_MARKER_COUNTRY = "country";
    private static final String KEY_MARKER_IMAGE_URL = "image";
    private static final String KEY_MARKER_LATITUDE = "latitude";
    private static final String KEY_MARKER_LONGITUDE = "longitude";

    /*MARKER TABLE CREATE STATEMENT*/
    private static final String CREATE_MARKER_TABLE = "CREATE TABLE " + MARKER_TABLE + "(" + KEY_ID + " INTEGER PRIMARY KEY, " +
            KEY_MARKER_ID + " TEXT, " + KEY_MARKER_NAME + " TEXT, " + KEY_MARKER_TYPE + " TEXT, " + KEY_MARKER_JOIN + " TEXT, " + KEY_MARKER_STATE + " TEXT, " + KEY_MARKER_COUNTRY + " TEXT, "  + KEY_MARKER_IMAGE_URL + " TEXT, " +
            KEY_MARKER_LATITUDE + " REAL, " + KEY_MARKER_LONGITUDE + " REAL " + ")" ;


    private static MyScrapSQLiteDatabase instance;
    private static Context mContext;


    public static synchronized MyScrapSQLiteDatabase getInstance(Context context) {
        mContext = context;
        if (instance == null)
            instance = new MyScrapSQLiteDatabase(context);
        return instance;
    }

    public  MyScrapSQLiteDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_MARKER_TABLE);
        db.execSQL("CREATE TABLE messages (_id INTEGER PRIMARY KEY AUTOINCREMENT, `from` TEXT, `to` TEXT, message TEXT, messageType TEXT, messageChat TEXT, messageId TEXT, messageLinkImage TEXT, messageLinkTitle TEXT, messageLinkSubTitle TEXT, messageLinkContent TEXT, id TEXT, t TEXT, seen TEXT, seenTime TEXT, name TEXT, profile TEXT, unReadCount TEXT, color TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MARKER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS messages");
        onCreate(db);
    }

    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

    @Override
    public void addMarker(MyItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues cv = new ContentValues();
            cv.put(KEY_MARKER_ID, item.getMarkerId());
            cv.put(KEY_MARKER_NAME, item.getCompanyName());
            cv.put(KEY_MARKER_TYPE, item.getCompanyType());
            cv.put(KEY_MARKER_JOIN, item.getIsNew());
            cv.put(KEY_MARKER_STATE, item.getCompanyAddress());
            cv.put(KEY_MARKER_COUNTRY, item.getCompanyCountry());
            cv.put(KEY_MARKER_IMAGE_URL, item.getCompanyImage());
            cv.put(KEY_MARKER_LATITUDE, item.getLatitude());
            cv.put(KEY_MARKER_LONGITUDE, item.getLongitude());
            db.insert(MARKER_TABLE,null,cv);
        } catch (Exception e) {
            Log.e("database" +MARKER_TABLE, e.toString());
        } finally {
            /*if (db != null)
                db.close();*/
        }
    }


    @Override
    public void deleteMarkerList() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + MARKER_TABLE);
        db.execSQL(CREATE_MARKER_TABLE);
        db.close();
    }

    @Override
    public void updateMarker(MyItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        try {
            cv.put(KEY_MARKER_ID, item.getMarkerId());
            cv.put(KEY_MARKER_NAME, item.getCompanyName());
            cv.put(KEY_MARKER_TYPE, item.getCompanyType());
            cv.put(KEY_MARKER_JOIN, item.getIsNew());
            cv.put(KEY_MARKER_STATE, item.getCompanyAddress());
            cv.put(KEY_MARKER_COUNTRY, item.getCompanyCountry());
            cv.put(KEY_MARKER_IMAGE_URL, item.getCompanyImage());
            cv.put(KEY_MARKER_LATITUDE, item.getLatitude());
            cv.put(KEY_MARKER_LONGITUDE, item.getLongitude());
            db.update(MARKER_TABLE, cv, KEY_MARKER_ID+" =?",new String[]{String.valueOf(item.getMarkerId())});
            Log.d("updateMarker" , KEY_MARKER_ID+""+String.valueOf(item.getMarkerId()));
        } catch (Exception e) {
            Log.e("database" +MARKER_TABLE, e.toString());
        } finally {
            if (db != null)
                db.close();
        }
    }

    @Override
    public ArrayList<MyItem> getMarkerList() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<MyItem> markerList = null;
        Cursor cursor = null;
        try {
            markerList = new ArrayList<>();
            String query = "SELECT * FROM " + MARKER_TABLE;
            cursor = db.rawQuery(query, null);
            if (!cursor.isLast()) {
                while(cursor.moveToNext()) {
                    MyItem myItem = new MyItem();
                    myItem.setId(cursor.getInt(0));
                    myItem.setMarkerId(cursor.getString(1));
                    myItem.setCompanyName(cursor.getString(2));
                    myItem.setCompanyType(cursor.getString(3));
                    myItem.setIsNew(cursor.getString(4));
                    myItem.setCompanyAddress(cursor.getString(5));
                    myItem.setCompanyCountry(cursor.getString(6));
                    myItem.setCompanyImage(cursor.getString(7));
                    myItem.setLatitude(cursor.getDouble(8));
                    myItem.setLongitude(cursor.getDouble(9));
                    markerList.add(myItem);
                }
            }

        } catch (Exception e) {
            Log.e("db getMarkerList" +MARKER_TABLE, e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }if (db != null) {
                db.close();
            }
        }
        return markerList;
    }

    @Override
    public int getMarkerCount() {
        return getMarkerList().size();
    }

    public void deleteChatRoomTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS messages");
        db.execSQL("CREATE TABLE messages (_id INTEGER PRIMARY KEY AUTOINCREMENT, `from` TEXT, `to` TEXT, message TEXT, messageType TEXT, messageChat TEXT, messageId TEXT, messageLinkImage TEXT, messageLinkTitle TEXT, messageLinkSubTitle TEXT, messageLinkContent TEXT, id TEXT, t TEXT, seen TEXT, seenTime TEXT, name TEXT, profile TEXT, unReadCount TEXT, color TEXT);");
        db.close();
    }

    public void deleteChatRoomMessages(int chatRoomId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("messages", "`from`" + " = ? OR "+"`to`" + " = ? ", new String[] { String.valueOf(chatRoomId) ,String.valueOf(chatRoomId) });
        db.close();
    }

    public ArrayList<ChatRoom> getChatRoomMessage(String number) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<ChatRoom> chatRoomMessagesList = null;
        Cursor cursor = null;
        try {
            chatRoomMessagesList = new ArrayList<>();
            String selectQuery = "SELECT _id, `from`,`to`,  message, messageType, messageChat, messageId, messageLinkImage, messageLinkTitle, messageLinkSubTitle, messageLinkContent, id, t, seen, seenTime, name, profile, unReadCount, color FROM messages WHERE `from` = " + DatabaseUtils.sqlEscapeString(number) + " OR `to` = " + DatabaseUtils.sqlEscapeString(number)+" ORDER BY t ASC;";
            cursor = db.rawQuery(selectQuery, null);
            if (!cursor.isLast()) {
                while(cursor.moveToNext()) {
                    ChatRoom chatRoom = new ChatRoom(cursor.getInt(11),cursor.getString(2),cursor.getString(1),cursor.getString(3),cursor.getString(4),cursor.getString(5),cursor.getString(6),cursor.getString(7),cursor.getString(8),cursor.getString(9),cursor.getString(10),cursor.getString(12),cursor.getString(15),cursor.getString(13),cursor.getString(14),cursor.getString(16),cursor.getInt(17),cursor.getString(18));
                    chatRoomMessagesList.add(chatRoom);
                }
            }
        } catch (Exception e) {
            Log.e("db getChatRoomMessages" ,e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null)
                db.close();
        }
        return chatRoomMessagesList;
    }

    private ChatRoom getChatRoomMessageByTime(String time) {
        SQLiteDatabase db = this.getReadableDatabase();
        ChatRoom chatRoomMessagesList = null;
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT _id, `from`,`to`,  message, messageType, messageChat, messageId, messageLinkImage, messageLinkTitle, messageLinkSubTitle, messageLinkContent, id, t, seen, seenTime, name, profile, unReadCount, color FROM messages WHERE t = " + DatabaseUtils.sqlEscapeString(time);
            cursor = db.rawQuery(selectQuery, null);
            if (!cursor.isLast()) {
                while(cursor.moveToNext()) {
                    chatRoomMessagesList = new ChatRoom(cursor.getInt(11),cursor.getString(2),cursor.getString(1),cursor.getString(3),cursor.getString(4),cursor.getString(5),cursor.getString(6),cursor.getString(7),cursor.getString(8),cursor.getString(9),cursor.getString(10),cursor.getString(12),cursor.getString(15),cursor.getString(13),cursor.getString(14),cursor.getString(16),cursor.getInt(17),cursor.getString(18));
                }
            }
        } catch (Exception e) {
            Log.e("database" ,e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null)
                db.close();
        }
        return chatRoomMessagesList;
    }

    public void updateSeenStatus(String number, String messageSeen) {
        if(AppController.getInstance().getPrefManager().getUser() != null){
            String s = "3";
            SQLiteDatabase db = this.getReadableDatabase();
            try {
                String updateQuery = "UPDATE "+"messages" +" SET " + "seen"+ " = '"+s+"'" + " , "+"seenTime"+" = '"+messageSeen+"'"+" WHERE "+"`from`"+ " = "+DatabaseUtils.sqlEscapeString(number) + " AND `to` = " + DatabaseUtils.sqlEscapeString(AppController.getInstance().getPrefManager().getUser().getId())+" OR "+"`from`"+ " = "+DatabaseUtils.sqlEscapeString(AppController.getInstance().getPrefManager().getUser().getId()) + " AND `to` = " + DatabaseUtils.sqlEscapeString(number);
                db.execSQL(updateQuery);
            } catch (Exception e) {
                Log.e("database" ,e.toString());
            } finally {
                if (db != null)
                    db.close();
            }
        }

    }

    public void updateProfilePicture(String number, String profilePicture, String colorCode) {
        if(AppController.getInstance().getPrefManager().getUser() != null){
            SQLiteDatabase db = this.getReadableDatabase();
            try {
                String updateQuery = "UPDATE "+"messages" +" SET "  +"profile"+" = '"+profilePicture+"'"+" , "+"color"+" = '"+colorCode+"'"+" WHERE "+"`from`"+ " = "+DatabaseUtils.sqlEscapeString(number) + " AND `to` = " + DatabaseUtils.sqlEscapeString(AppController.getInstance().getPrefManager().getUser().getId())+" OR "+"`from`"+ " = "+DatabaseUtils.sqlEscapeString(AppController.getInstance().getPrefManager().getUser().getId()) + " AND `to` = " + DatabaseUtils.sqlEscapeString(number);
                db.execSQL(updateQuery);
                Log.e(number+"'s UpdateProfile","DONE");
            } catch (Exception e) {
                Log.e("database updateProfile", e.toString());
            } finally {
                if (db != null)
                    db.close();
            }
        }
    }

    public void updateMessageCount(String number, int count) {
        if(AppController.getInstance().getPrefManager().getUser() != null){
            SQLiteDatabase db = this.getReadableDatabase();
            try {
                String updateQuery = "UPDATE "+"messages" +" SET " + "unReadCount"+ " = '"+count+"'" + " WHERE "+"`from`"+ " = "+DatabaseUtils.sqlEscapeString(number) + " AND `to` = " + DatabaseUtils.sqlEscapeString(AppController.getInstance().getPrefManager().getUser().getId())+" OR "+"`from`"+ " = "+DatabaseUtils.sqlEscapeString(AppController.getInstance().getPrefManager().getUser().getId()) + " AND `to` = " + DatabaseUtils.sqlEscapeString(number);
                db.execSQL(updateQuery);
            } catch (Exception e) {
                Log.e("database" ,e.toString());
            } finally {
                if (db != null)
                    db.close();
            }
        }
    }

    public void updateMessageDeliveredStatus(String number) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if(AppController.getInstance().getPrefManager().getUser() != null) {
            String s = "2";
            SQLiteDatabase db = this.getReadableDatabase();
            try {
                String updateQuery = "UPDATE "+"messages" +" SET " + "seen"+ " = '"+s+"' WHERE "+"`from`"+ " = "+DatabaseUtils.sqlEscapeString(number) + " AND `to` = " + DatabaseUtils.sqlEscapeString(AppController.getInstance().getPrefManager().getUser().getId())+" AND seen=1"+" OR "+"`from`"+ " = "+DatabaseUtils.sqlEscapeString(AppController.getInstance().getPrefManager().getUser().getId()) + " AND `to` = " + DatabaseUtils.sqlEscapeString(number)+" AND seen=1";
                db.execSQL(updateQuery);
            } catch (Exception e) {
                Log.e("database" ,e.toString());
            } finally {
                if (db != null)
                    db.close();
            }
        }
    }

    public void updateMessageTime(String messageId, String messageTime) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if(AppController.getInstance().getPrefManager().getUser() != null) {
            SQLiteDatabase db = this.getReadableDatabase();
            try {
                String updateQuery = "UPDATE "+"messages" +" SET " + "t"+ " = '"+messageTime+"' WHERE "+"messageId"+ " = "+DatabaseUtils.sqlEscapeString(messageId);
                db.execSQL(updateQuery);
            } catch (Exception e) {
                Log.e("database" ,e.toString());
            } finally {
                if (db != null)
                    db.close();
            }
        }
    }

    public ArrayList<ChatRoom> getChatRoomList() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<ChatRoom> chatRoomList = null;
        Cursor cursor = null;
        try {
            chatRoomList = new ArrayList<>();
            String query = "SELECT *, MAX(t) FROM messages GROUP BY MIN(`from`, `to`), MAX(`from`, `to`) ORDER BY t DESC;";
            cursor = db.rawQuery(query, null);
            if (!cursor.isLast()) {
                while(cursor.moveToNext()) {
                    ChatRoom chatRoom = new ChatRoom(cursor.getInt(11),cursor.getString(2),cursor.getString(1),cursor.getString(3),cursor.getString(4),cursor.getString(5),cursor.getString(6),cursor.getString(7),cursor.getString(8),cursor.getString(9),cursor.getString(10),cursor.getString(12),cursor.getString(15),cursor.getString(13),cursor.getString(14),cursor.getString(16),cursor.getInt(17),cursor.getString(18));
                    chatRoomList.add(chatRoom);
                }
            }
        } catch (Exception e) {
            Log.e("database", e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if(db != null)
                db.close();
        }
        return chatRoomList;
    }


    public void removeDuplicate() {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            db.execSQL("SELECT DISTINCT messageId from messages");
        } catch (Exception e) {
            Log.e("removeDuplicate", e.toString());
        } finally {
            if(db != null)
                db.close();
        }
    }

    public void deleteDuplicates(){
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            //db.execSQL("SELECT DISTINCT messageId from messages");
            db.execSQL("delete from messages where _id not in (SELECT MIN(_id ) FROM messages GROUP BY messageId)");
        } catch (Exception e) {
            Log.e("removeDuplicate", e.toString());
        } finally {
            if(db != null)
                db.close();
        }

    }


    public int getChatRoomMessageCount(String number) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int count = 0;
        try {
            String query = "SELECT `unReadCount` FROM messages WHERE "+"`from`"+ " = "+DatabaseUtils.sqlEscapeString(number) + " AND `to` = " + DatabaseUtils.sqlEscapeString(AppController.getInstance().getPrefManager().getUser().getId())+" OR "+"`from`"+ " = "+DatabaseUtils.sqlEscapeString(AppController.getInstance().getPrefManager().getUser().getId()) + " AND `to` = " + DatabaseUtils.sqlEscapeString(number);
            cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()){
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e("database", e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }

    public void addMessage(String from, String to, String message, String messageType, String messageChat, String messageId, String messageLinkImage, String messageLinkTitle, String messageLinkSubTitle, String messageLinkContent, String id, String time, String seen, String seenTime, String name, String profilePicture, String color, int unReadCount){
        SQLiteDatabase db = null;
        try {
            MyScrapSQLiteDatabase mDbHelper = new MyScrapSQLiteDatabase(mContext);
            db = mDbHelper.getWritableDatabase();
            String query = "INSERT INTO " + DbEntries.TABLE_NAME + " ("
                    + DbEntries.COLUMN_NAME_FROM + ", "
                    + DbEntries.COLUMN_NAME_TO + ", "
                    + DbEntries.COLUMN_NAME_MESSAGE + ", "
                    + DbEntries.COLUMN_NAME_MESSAGE_TYPE + ", "

                    + DbEntries.COLUMN_NAME_CHAT_MESSAGE + ", "
                    + DbEntries.COLUMN_NAME_MESSAGE_ID + ", "
                    + DbEntries.COLUMN_NAME_MESSAGE_LINK_IMAGE + ", "
                    + DbEntries.COLUMN_NAME_MESSAGE_LINK_TITLE + ", "
                    + DbEntries.COLUMN_NAME_MESSAGE_LINK_SUB_TITLE + ", "
                    + DbEntries.COLUMN_NAME_MESSAGE_LINK_CONTENT + ", "


                    + DbEntries.COLUMN_NAME_ID + ", "
                    + DbEntries.COLUMN_NAME_TIME + ", "
                    + DbEntries.COLUMN_NAME_SEEN + ", "
                    + DbEntries.COLUMN_NAME_SEEN_TIME + ", "
                    + DbEntries.COLUMN_NAME_CHAT_NAME + ", "
                    + DbEntries.COLUMN_NAME_CHAT_PROFILE + ", "
                    + DbEntries.COLUMN_NAME_COUNT + ", "
                    + DbEntries.COLUMN_NAME_CHAT_COLOR + ") VALUES ("+" "
                    + DatabaseUtils.sqlEscapeString(from)+", "
                    + DatabaseUtils.sqlEscapeString(to) + ", "
                    + DatabaseUtils.sqlEscapeString(message) + ", "
                    + DatabaseUtils.sqlEscapeString(messageType) + ", "

                    + DatabaseUtils.sqlEscapeString(messageChat) + ", "
                    + DatabaseUtils.sqlEscapeString(messageId) + ", "
                    + DatabaseUtils.sqlEscapeString(messageLinkImage) + ", "
                    + DatabaseUtils.sqlEscapeString(messageLinkTitle) + ", "
                    + DatabaseUtils.sqlEscapeString(messageLinkSubTitle) + ", "
                    + DatabaseUtils.sqlEscapeString(messageLinkContent) + ", "

                    + DatabaseUtils.sqlEscapeString(id) + ", "
                    + DatabaseUtils.sqlEscapeString(time) + ", "
                    + DatabaseUtils.sqlEscapeString(seen) + ", "
                    + DatabaseUtils.sqlEscapeString(seenTime) + ", "
                    + DatabaseUtils.sqlEscapeString(name) + ", "
                    + DatabaseUtils.sqlEscapeString(profilePicture) + ", "
                    + unReadCount + ", "
                    + DatabaseUtils.sqlEscapeString(color) + ")";
            db.execSQL(query);
        } catch (Exception e) {
            Log.e("Insert Error", e.toString());
        } finally {
            if(db != null)
                db.close();
        }
    }

    public void addSingleMessage(String from, String to, String message, String messageType, String messageChat, String messageId, String messageLinkImage, String messageLinkTitle, String messageLinkSubTitle, String messageLinkContent, String id, String time, String seen, String seenTime, String name, String profilePicture, String color, int unReadCount){
        SQLiteDatabase db = null;
        try {
            MyScrapSQLiteDatabase mDbHelper = new MyScrapSQLiteDatabase(mContext);
            db = mDbHelper.getWritableDatabase();
            String query = "INSERT INTO " + DbEntries.TABLE_NAME + " ("
                    + DbEntries.COLUMN_NAME_FROM + ", "
                    + DbEntries.COLUMN_NAME_TO + ", "
                    + DbEntries.COLUMN_NAME_MESSAGE + ", "
                    + DbEntries.COLUMN_NAME_MESSAGE_TYPE + ", "

                    + DbEntries.COLUMN_NAME_CHAT_MESSAGE + ", "
                    + DbEntries.COLUMN_NAME_MESSAGE_ID + ", "
                    + DbEntries.COLUMN_NAME_MESSAGE_LINK_IMAGE + ", "
                    + DbEntries.COLUMN_NAME_MESSAGE_LINK_TITLE + ", "
                    + DbEntries.COLUMN_NAME_MESSAGE_LINK_SUB_TITLE + ", "
                    + DbEntries.COLUMN_NAME_MESSAGE_LINK_CONTENT + ", "

                    + DbEntries.COLUMN_NAME_ID + ", "
                    + DbEntries.COLUMN_NAME_TIME + ", "
                    + DbEntries.COLUMN_NAME_SEEN + ", "
                    + DbEntries.COLUMN_NAME_SEEN_TIME + ", "
                    + DbEntries.COLUMN_NAME_CHAT_NAME + ", "
                    + DbEntries.COLUMN_NAME_CHAT_PROFILE + ", "
                    + DbEntries.COLUMN_NAME_COUNT + ", "
                    + DbEntries.COLUMN_NAME_CHAT_COLOR + ") VALUES ("+" "
                    + DatabaseUtils.sqlEscapeString(from)+", "
                    + DatabaseUtils.sqlEscapeString(to) + ", "
                    + DatabaseUtils.sqlEscapeString(message) + ", "
                    + DatabaseUtils.sqlEscapeString(messageType) + ", "
                    + DatabaseUtils.sqlEscapeString(messageChat) + ", "
                    + DatabaseUtils.sqlEscapeString(messageId) + ", "
                    + DatabaseUtils.sqlEscapeString(messageLinkImage) + ", "
                    + DatabaseUtils.sqlEscapeString(messageLinkTitle) + ", "
                    + DatabaseUtils.sqlEscapeString(messageLinkSubTitle) + ", "
                    + DatabaseUtils.sqlEscapeString(messageLinkContent) + ", "
                    + DatabaseUtils.sqlEscapeString(id) + ", "
                    + DatabaseUtils.sqlEscapeString(time) + ", "
                    + DatabaseUtils.sqlEscapeString(seen) + ", "
                    + DatabaseUtils.sqlEscapeString(seenTime) + ", "
                    + DatabaseUtils.sqlEscapeString(name) + ", "
                    + DatabaseUtils.sqlEscapeString(profilePicture) + ", "
                    + unReadCount + ", "
                    + DatabaseUtils.sqlEscapeString(color) + ")";
            db.execSQL(query);
            Intent intent = new Intent(ChatRoomActivity.SET_NOTIFY);
            Bundle bundle = new Bundle();
            bundle.putSerializable("updateByTime", instance.getChatRoomMessageByTime(time));
            intent.putExtra("updateByTime", bundle);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            Intent intent1 = new Intent(MainChatRoomActivity.SET_NOTIFY);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent1);
        } catch (Exception e) {
            Log.e("Insert SingleMessage", e.toString());
        } finally {
            if(db != null)
                db.close();
        }
    }

    public void updateSingleMessage(String from, String to, String message, String messageType, String messageChat, String messageId,
                                    String messageLinkImage, String messageLinkTitle, String messageLinkSubTitle, String messageLinkContent,
                                    String id, String time, String seen, String seenTime, String name, String profilePicture, String color, int unReadCount){
        SQLiteDatabase db = null;
        try {
            MyScrapSQLiteDatabase mDbHelper = new MyScrapSQLiteDatabase(mContext);
            db = mDbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(DbEntries.COLUMN_NAME_FROM, from);
            cv.put(DbEntries.COLUMN_NAME_TO, to);
            cv.put(DbEntries.COLUMN_NAME_MESSAGE, message);
            cv.put(DbEntries.COLUMN_NAME_MESSAGE_TYPE, messageType);
            cv.put(DbEntries.COLUMN_NAME_CHAT_MESSAGE, messageChat);
            cv.put(DbEntries.COLUMN_NAME_MESSAGE_ID, messageId);
            cv.put(DbEntries.COLUMN_NAME_MESSAGE_LINK_IMAGE, messageLinkImage);
            cv.put(DbEntries.COLUMN_NAME_MESSAGE_LINK_TITLE, messageLinkTitle);
            cv.put(DbEntries.COLUMN_NAME_MESSAGE_LINK_SUB_TITLE, messageLinkSubTitle);
            cv.put(DbEntries.COLUMN_NAME_MESSAGE_LINK_CONTENT, messageLinkContent);
            cv.put(DbEntries.COLUMN_NAME_ID, id);
            cv.put(DbEntries.COLUMN_NAME_TIME, time);
            cv.put(DbEntries.COLUMN_NAME_SEEN, seen);
            cv.put(DbEntries.COLUMN_NAME_SEEN_TIME, seenTime);
            cv.put(DbEntries.COLUMN_NAME_CHAT_NAME, name);
            cv.put(DbEntries.COLUMN_NAME_CHAT_PROFILE, profilePicture);
            cv.put(DbEntries.COLUMN_NAME_COUNT, unReadCount);
            cv.put(DbEntries.COLUMN_NAME_CHAT_COLOR, color);
            db.update(DbEntries.TABLE_NAME, cv, DbEntries.COLUMN_NAME_TIME+" =?",new String[]{String.valueOf(time)});
            Log.d("SingleMessage ", "updated");
            Intent intent = new Intent(ChatRoomActivity.SET_UPDATE_NOTIFY);
            Bundle bundle = new Bundle();
            bundle.putSerializable("updateByTime", instance.getChatRoomMessageByTime(time));
            intent.putExtra("updateByTime", bundle);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            Intent intent1 = new Intent(MainChatRoomActivity.SET_UPDATE_NOTIFY);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent1);
        } catch (Exception e) {
            Log.e("SingleMessage ", "failed");
        } finally {
            if(db != null)
                db.close();
        }
    }

}