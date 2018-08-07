package com.myscrap.xmppmodel;

/**
 * Created by ms3 Android MyScrap.
 */

public class XMPPChatMessageModel
{
    String textId;
    String friendsJid;
    String friendsUserId;
    String friendsName;
    String friendsUrl;
    String chat;
    String textType;
    String timestamp;
    String readStatus;
    String color;


    public XMPPChatMessageModel(String textId, String friendsJid, String friendsUserId, String friendsName, String friendsUrl,
                                String chat, String textType, String timestamp, String readStatus, String color)
    {
        this.textId = textId;
        this.friendsJid = friendsJid;
        this.friendsUserId = friendsUserId;
        this.friendsName = friendsName;
        this.friendsUrl = friendsUrl;
        this.chat = chat;
        this.textType = textType;
        this.timestamp = timestamp;
        this.readStatus = readStatus;
        this.color = color;
    }


    public String getTextId() {
        return textId;
    }

    public void setTextId(String textId) {
        this.textId = textId;
    }

    public String getFriendsJid() {
        return friendsJid;
    }

    public void setFriendsJid(String friendsJid) {
        this.friendsJid = friendsJid;
    }

    public String getFriendsUserId() {
        return friendsUserId;
    }

    public void setFriendsUserId(String friendsUserId) {
        this.friendsUserId = friendsUserId;
    }

    public String getFriendsName() {
        return friendsName;
    }

    public void setFriendsName(String friendsName) {
        this.friendsName = friendsName;
    }

    public String getFriendsUrl() {
        return friendsUrl;
    }

    public void setFriendsUrl(String friendsUrl) {
        this.friendsUrl = friendsUrl;
    }

    public String getChat() {
        return chat;
    }

    public void setChat(String chat) {
        this.chat = chat;
    }

    public String getTextType() {
        return textType;
    }

    public void setTextType(String textType) {
        this.textType = textType;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(String readStatus) {
        this.readStatus = readStatus;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
