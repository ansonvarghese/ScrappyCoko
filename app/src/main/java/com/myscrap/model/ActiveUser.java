package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by ms3 on 6/3/2017.
 */

public class ActiveUser{

    @SerializedName("status")
    private String status;
    @SerializedName("error")
    private boolean errorStatus;

    private List<ActiveUserData> activeUserData;

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

    public List<ActiveUserData> getActiveUserData() {
        return activeUserData;
    }

    public void setActiveUserData(List<ActiveUserData> activeUserData) {
        this.activeUserData = activeUserData;
    }

    public class ActiveUserData {
        private String userId;
        private String name;
        private String profilePic;
        private String colorCode;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getProfilePic() {
            return profilePic;
        }

        public void setProfilePic(String profilePic) {
            this.profilePic = profilePic;
        }

        public String getColorCode() {
            return colorCode;
        }

        public void setColorCode(String colorCode) {
            this.colorCode = colorCode;
        }
    }
}
