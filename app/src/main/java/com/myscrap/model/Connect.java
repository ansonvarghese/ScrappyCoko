package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ms3 on 5/18/2017.
 */

public class Connect {

    @SerializedName("status")
    private String status;
    @SerializedName("error")
    private boolean errorStatus;

    @SerializedName("connectData")
    private List<ConnectData> data = new ArrayList<>();

    private UpdatedChat updatedchat;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isErrorStatus() {
        return errorStatus;
    }

    public void setErrorStatus(boolean errorStatus) {
        this.errorStatus = errorStatus;
    }

    public List<ConnectData> getData() {
        return data;
    }

    public void setData(List<ConnectData> data) {
        this.data = data;
    }

    public UpdatedChat getUpdatedchat() {
        return updatedchat;
    }

    public void setUpdatedchat(UpdatedChat updatedchat) {
        this.updatedchat = updatedchat;
    }


    public class ConnectData {
        @SerializedName("id")
        private Integer id;
        @SerializedName("message")
        private String message;
        @SerializedName("fromid")
        private String from;
        @SerializedName("userid")
        private String to;
        @SerializedName("userName")
        private String userName;
        @SerializedName("userDP")
        private String userProfile;
        @SerializedName("timeStamp")
        private String timeStamp;
        @SerializedName("colorCode")
        private String color;
        @SerializedName("messageCount")
        private int unReadMessageCount;

        @SerializedName("seenTime")
        private String seenT;

        private String messageStatus;
        private String messageType;
        private String messageChat;
        private String messageChatId;
        private String messageLinkImage;
        private String messageLinkTitle;
        private String messageLinkSubTitle;
        private String messageLinkContent;


        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getUserProfile() {
            return userProfile;
        }

        public void setUserProfile(String userProfile) {
            this.userProfile = userProfile;
        }

        public String getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
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


        public String getMessageStatus() {
            return messageStatus;
        }

        public void setMessageStatus(String messageStatus) {
            this.messageStatus = messageStatus;
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

        public String getSeenT() {
            return seenT;
        }

        public void setSeenT(String seenT) {
            this.seenT = seenT;
        }
    }

    public class UpdatedChat {

        private int count;

        @SerializedName("data")
        private List<UpdatedChatData> updatedChatData = new ArrayList<>();

        public List<UpdatedChatData> getUpdatedChat() {
            return updatedChatData;
        }

        public void setUpdatedChat(List<UpdatedChatData> updatedChat) {
            updatedChatData = updatedChat;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public class UpdatedChatData {

            private String delivered;

            private String chatRoomId;

            @SerializedName("userid")
            private String userId;

            private String seenTime;

            private boolean isSeen;

            public String getDelivered() {
                return delivered;
            }

            public void setDelivered(String delivered) {
                this.delivered = delivered;
            }

            public String getChatRoomId() {
                return chatRoomId;
            }

            public void setChatRoomId(String chatRoomId) {
                this.chatRoomId = chatRoomId;
            }

            public String getUserId() {
                return userId;
            }

            public void setUserId(String userId) {
                this.userId = userId;
            }

            public String getSeenTime() {
                return seenTime;
            }

            public void setSeenTime(String seenTime) {
                this.seenTime = seenTime;
            }

            public boolean isSeen() {
                return isSeen;
            }

            public void setSeen(boolean seen) {
                isSeen = seen;
            }
        }
    }
}
