package com.myscrap.database;

import android.provider.BaseColumns;

/**
 * Created by ms3 on 3/28/2017.
 */

public abstract class DbEntries implements BaseColumns {
    public static final String TABLE_NAME = "messages";
    public static final String COLUMN_NAME_FROM = "`from`";
    public static final String COLUMN_NAME_TO = "`to`";
    public static final String COLUMN_NAME_MESSAGE = "message";
    public static final String COLUMN_NAME_MESSAGE_TYPE = "messageType";
    public static final String COLUMN_NAME_CHAT_MESSAGE = "messageChat";
    public static final String COLUMN_NAME_MESSAGE_ID = "messageId";
    public static final String COLUMN_NAME_MESSAGE_LINK_IMAGE = "messageLinkImage";
    public static final String COLUMN_NAME_MESSAGE_LINK_TITLE = "messageLinkTitle";
    public static final String COLUMN_NAME_MESSAGE_LINK_SUB_TITLE = "messageLinkSubTitle";
    public static final String COLUMN_NAME_MESSAGE_LINK_CONTENT = "messageLinkContent";
    public static final String COLUMN_NAME_ID = "id";
    public static final String COLUMN_NAME_TIME = "t";
    public static final String COLUMN_NAME_SEEN = "seen";
    public static final String COLUMN_NAME_SEEN_TIME = "seenTime";
    public static final String COLUMN_NAME_CHAT_NAME = "name";
    public static final String COLUMN_NAME_CHAT_PROFILE = "profile";
    public static final String COLUMN_NAME_CHAT_COLOR = "color";
    public static final String COLUMN_NAME_COUNT = "unReadCount";
    public static final String COLUMN_NAME_NULLABLE = null;
}