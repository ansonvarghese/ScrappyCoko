package com.myscrap.model;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.myscrap.utils.UserUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Ms2 on 4/7/2016.
 */
public class ChatRoom implements Comparable<ChatRoom>, Serializable {

    private String chat_room_id, my_chat_room_id, profilePic, name, online, lastMessage, messageId, created_at/*, seenTime*/;


    private boolean isRead = true;
    private boolean isTyping = false;
    private boolean isSeen = false;

    @SerializedName("data")
    private  List<ChatRoom> data = new ArrayList<>();

    @SerializedName("id")
    private Integer id;
    @SerializedName("msg_to_id")
    private String to;
    @SerializedName("msg_from_id")
    private String from;
    @SerializedName("message")
    private String message;
    @SerializedName("sent")
    private String timeStamp;
    @SerializedName("seenTime")
    private String seenT;
    @SerializedName("from")
    private String messageFrom;
    @SerializedName("status")
    private String status;
    @SerializedName("colorCode")
    private String color;
    @SerializedName("messageCount")
    private int unReadMessageCount;

    private String messageDateFormatted;
    private String messageType;
    private String messageChat;
    private String messageChatId;
    private String messageLinkImage;
    private String messageLinkTitle;
    private String messageLinkSubTitle;
    private String messageLinkContent;

    public ChatRoom() {

    }

    @Override
    public int compareTo(@NonNull ChatRoom another) {
        if (chat_room_id != null && created_at != null){
            return Integer.parseInt(UserUtils.parsingInteger(this.chat_room_id)) - Integer.parseInt(UserUtils.parsingInteger(another.chat_room_id));
        } else {
            return Integer.parseInt(UserUtils.parsingInteger(this.from)) - Integer.parseInt(UserUtils.parsingInteger(another.timeStamp));
        }
    }

    public static final Comparator<ChatRoom> timeStampComparator = new Comparator<ChatRoom>(){

        @Override
        public int compare(ChatRoom o1, ChatRoom o2) {
            if (o1.created_at != null && o2.created_at != null ){
                return o1.created_at.compareTo(o2.created_at);
            } else {
                return o1.timeStamp.compareTo(o2.timeStamp);
            }

        }

    };

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ChatRoom other = (ChatRoom) obj;
        if (this.created_at != null && other.created_at != null ){
            return !(this.created_at != other.created_at && !this.created_at.equals(other.created_at));
        } else {
            return !(this.timeStamp != other.timeStamp && (this.timeStamp == null || !this.timeStamp.equals(other.timeStamp)));
        }

    }


    public ChatRoom(int id, String to, String from, String message, String messageType, String messageChat, String messageId, String messageLinkImage, String messageLinkTitle, String messageLinkSubTitle, String messageLinkContent, String timeStamp, String messageFrom, String status, String seenTime, String profilePicture, int msgCount, String color) {


        this.setId(id);
        this.setTo(to);
        this.setFrom(from);
        this.setMessage(message);
        this.setMessageType(messageType);
        this.setMessageChatImage(messageChat);
        this.setMessageChatId(messageId);
        this.setMessageLinkImage(messageLinkImage);
        this.setMessageLinkTitle(messageLinkTitle);
        this.setMessageLinkSubTitle(messageLinkSubTitle);
        this.setMessageLinkContent(messageLinkContent);
        this.setTimeStamp(timeStamp);
        this.setMessageFrom(messageFrom);
        this.setStatus(status);
        this.setSeenT(seenTime);
        this.setProfilePic(profilePicture);
        this.setUnReadMessageCount(msgCount);
        this.setColor(color);
    }

    public ChatRoom(String messageType){
        this.setMessageType(messageType);
    }


    public String getChat_room_id() {
        return chat_room_id;
    }

    public void setChat_room_id(String chat_room_id) {
        this.chat_room_id = chat_room_id;
    }

    public String getMy_chat_room_id() {
        return my_chat_room_id;
    }

    public void setMy_chat_room_id(String my_chat_room_id) {
        this.my_chat_room_id = my_chat_room_id;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ChatRoom> getData() {
        return data;
    }

    public void setData(List<ChatRoom> data) {
        this.data = data;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getMessageFrom() {
        return messageFrom;
    }

    public void setMessageFrom(String messageFrom) {
        this.messageFrom = messageFrom;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isTyping() {
        return isTyping;
    }

    public void setTyping(boolean typing) {
        isTyping = typing;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    /*public String getSeenTime() {
        return seenTime;
    }

    public void setSeenTime(String seenTime) {
        this.seenTime = seenTime;
    }*/

    public String getSeenT() {
        return seenT;
    }

    public void setSeenT(String seenT) {
        this.seenT = seenT;
    }

    public int getUnReadMessageCount() {
        return unReadMessageCount;
    }

    public void setUnReadMessageCount(int unReadMessageCount) {
        this.unReadMessageCount = unReadMessageCount;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageChatImage() {
        return messageChat;
    }

    public void setMessageChatImage(String messageChat) {
        this.messageChat = messageChat;
    }

    public String getMessageChatId() {
        return messageChatId;
    }

    public void setMessageChatId(String messageChatId) {
        this.messageChatId = messageChatId;
    }

    public String getMessageLinkImage() {
        return messageLinkImage;
    }

    public void setMessageLinkImage(String messageLinkImage) {
        this.messageLinkImage = messageLinkImage;
    }

    public String getMessageLinkTitle() {
        return messageLinkTitle;
    }

    public void setMessageLinkTitle(String messageLinkTitle) {
        this.messageLinkTitle = messageLinkTitle;
    }

    public String getMessageLinkSubTitle() {
        return messageLinkSubTitle;
    }

    public void setMessageLinkSubTitle(String messageLinkSubTitle) {
        this.messageLinkSubTitle = messageLinkSubTitle;
    }

    public String getMessageLinkContent() {
        return messageLinkContent;
    }

    public void setMessageLinkContent(String messageLinkContent) {
        this.messageLinkContent = messageLinkContent;
    }

    public String getMessageDateFormatted() {
        return messageDateFormatted;
    }

    public void setMessageDateFormatted(String messageDateFormatted) {
        this.messageDateFormatted = messageDateFormatted;
    }
}
