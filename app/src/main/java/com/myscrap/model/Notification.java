package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by ms3 on 5/19/2017.
 */

public class Notification  {

    @SerializedName("status")
    private String status;
    @SerializedName("error")
    private boolean errorStatus;

    @SerializedName("notificationData")
    private List<Notification.NotificationData> data;


    public Notification(){}


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

    public List<NotificationData> getData() {
        return data;
    }

    public void setData(List<NotificationData> data) {
        this.data = data;
    }

    public class NotificationData {
        private String bumpedId;

        private String Count;

        private String colorCode;

        private String postUserId;

        private String profilePic;

        private String not_id;

        private String active;

        private boolean isNew;

        private boolean isTitle;

        @SerializedName("posttype")
        private String type;

        private String NotificationTime;

        private String notificationMessage;

        private String postUserName;

        private String postId;

        private String companyId;

        public NotificationData() {}

        public String getBumpedId() {
            return bumpedId;
        }

        public void setBumpedId(String bumpedId) {
            this.bumpedId = bumpedId;
        }

        public String getCount() {
            return Count;
        }

        public void setCount(String count) {
            Count = count;
        }

        public String getPostUserId() {
            return postUserId;
        }

        public void setPostUserId(String postUserId) {
            this.postUserId = postUserId;
        }

        public String getProfilePic() {
            return profilePic;
        }

        public void setProfilePic(String profilePic) {
            this.profilePic = profilePic;
        }

        public String getNot_id() {
            return not_id;
        }

        public void setNot_id(String not_id) {
            this.not_id = not_id;
        }

        public String getActive() {
            return active;
        }

        public void setActive(String active) {
            this.active = active;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getNotificationTime() {
            return NotificationTime;
        }

        public void setNotificationTime(String notificationTime) {
            NotificationTime = notificationTime;
        }

        public String getNotificationMessage() {
            return notificationMessage;
        }

        public void setNotificationMessage(String notificationMessage) {
            this.notificationMessage = notificationMessage;
        }

        public String getPostUserName() {
            return postUserName;
        }

        public void setPostUserName(String postUserName) {
            this.postUserName = postUserName;
        }

        public String getPostId() {
            return postId;
        }

        public void setPostId(String postId) {
            this.postId = postId;
        }

        public String getCompanyId() {
            return companyId;
        }

        public void setCompanyId(String companyId) {
            this.companyId = companyId;
        }

        public String getColorCode() {
            return colorCode;
        }

        public void setColorCode(String colorCode) {
            this.colorCode = colorCode;
        }

        public boolean isNew() {
            return isNew;
        }

        public void setNew(boolean aNew) {
            isNew = aNew;
        }

        public boolean isTitle() {
            return isTitle;
        }

        public void setTitle(boolean title) {
            isTitle = title;
        }
    }
}
