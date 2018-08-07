package com.myscrap.xmppmodel;

/**
 * Created by ms3 Android MyScrap.
 */

public class ChatContactModel
{
    String userId;
    String friendsUserId;
    String friendsJid;
    String userJid;
    String profilePic;
    String name;
    String lastMessage;
    String createdAt;
    String seenTime;
    String colorCode;
    String messageCount;
    String messageType;
    String messageLinkImage;
    String messageChat;
    String messageLinkTitle;
    String messageLinkSubtitle;
    String messageLinkContent;
    String messageId;
    String readStatus;
    String isSender;


    public ChatContactModel(String userId, String friendsUserId, String friendsJid, String userJid,
                            String profilePic, String name, String lastMessage, String createdAt, String seenTime,
                            String colorCode, String messageCount, String messageType, String messageLinkImage,
                            String messageChat, String messageLinkTitle, String messageLinkSubtitle,
                            String messageLinkContent, String messageId, String readStatus, String isSender)
    {
        this.userId = userId;
        this.friendsUserId = friendsUserId;
        this.friendsJid = friendsJid;
        this.userJid = userJid;
        this.profilePic = profilePic;
        this.name = name;
        this.lastMessage = lastMessage;
        this.createdAt = createdAt;
        this.seenTime = seenTime;
        this.colorCode = colorCode;
        this.messageCount = messageCount;
        this.messageType = messageType;
        this.messageLinkImage = messageLinkImage;
        this.messageChat = messageChat;
        this.messageLinkTitle = messageLinkTitle;
        this.messageLinkSubtitle = messageLinkSubtitle;
        this.messageLinkContent = messageLinkContent;
        this.messageId = messageId;
        this.readStatus = readStatus;
        this.isSender = isSender;
    }


    public String getMessageLinkSubtitle() {
        return messageLinkSubtitle;
    }

    public void setMessageLinkSubtitle(String messageLinkSubtitle) {
        this.messageLinkSubtitle = messageLinkSubtitle;
    }

    public String getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(String readStatus) {
        this.readStatus = readStatus;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFriendsUserId() {
        return friendsUserId;
    }

    public void setFriendsUserId(String friendsUserId) {
        this.friendsUserId = friendsUserId;
    }

    public String getFriendsJid() {
        return friendsJid;
    }

    public void setFriendsJid(String friendsJid) {
        this.friendsJid = friendsJid;
    }

    public String getUserJid() {
        return userJid;
    }

    public void setUserJid(String userJid) {
        this.userJid = userJid;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getSeenTime() {
        return seenTime;
    }

    public void setSeenTime(String seenTime) {
        this.seenTime = seenTime;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(String messageCount) {
        this.messageCount = messageCount;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageLinkImage() {
        return messageLinkImage;
    }

    public void setMessageLinkImage(String messageLinkImage) {
        this.messageLinkImage = messageLinkImage;
    }

    public String getMessageChat() {
        return messageChat;
    }

    public void setMessageChat(String messageChat) {
        this.messageChat = messageChat;
    }

    public String getMessageLinkTitle() {
        return messageLinkTitle;
    }

    public void setMessageLinkTitle(String messageLinkTitle) {
        this.messageLinkTitle = messageLinkTitle;
    }

    public String getMessageLinkContent() {
        return messageLinkContent;
    }

    public void setMessageLinkContent(String messageLinkContent) {
        this.messageLinkContent = messageLinkContent;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getIsSender() {
        return isSender;
    }

    public void setIsSender(String isSender) {
        this.isSender = isSender;
    }
}
