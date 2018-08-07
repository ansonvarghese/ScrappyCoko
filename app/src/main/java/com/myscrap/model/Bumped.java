package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ms3 on 5/11/2017.
 */

public class Bumped {

    @SerializedName("status")
    private String status;

    @SerializedName("error")
    private boolean errorStatus;

    @SerializedName("bumpedCount")
    private int bumpedCount;

    @SerializedName("bumpPostsData")
    private List<BumpedPostItem> data = new ArrayList<>();

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

    public List<BumpedPostItem> getData() {
        return data;
    }

    public void setData(List<BumpedPostItem> data) {
        this.data = data;
    }

    public int getBumpedCount() {
        return bumpedCount;
    }

    public void setBumpedCount(int bumpedCount) {
        this.bumpedCount = bumpedCount;
    }

    public class BumpedPostItem
    {

        private String title;
        private String name;
        private String colorCode ;
        private String userId;
        private String profilePic;
        private String timeStamp;
        private String designation;
        private String jId;
        private Boolean isNew;

        public BumpedPostItem(String name, String profilePic, String designation)
        {
            this.name = name;
            this.designation = designation;
            this.profilePic = profilePic;
        }

        public Boolean getNew()
        {
            return isNew;
        }

        public void setNew(Boolean aNew) {
            isNew = aNew;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getProfilePic() {
            return profilePic;
        }

        public void setProfilePic(String profilePic) {
            this.profilePic = profilePic;
        }

        public String getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
        }

        public String getDesignation() {
            return designation;
        }

        public void setDesignation(String designation) {
            this.designation = designation;
        }

        public String getColorCode() {
            return colorCode;
        }

        public void setColorCode(String colorCode) {
            this.colorCode = colorCode;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }


        public String getjId() {
            return jId;
        }

        public void setjId(String jId) {
            this.jId = jId;
        }
    }
}
