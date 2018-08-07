package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ms3 on 12/4/2017.
 */

public class EventInvitations {
    @SerializedName("status")
    private String status;
    @SerializedName("error")
    private boolean errorStatus;

    @SerializedName("eventData")
    private List<EventInvitationsData> eventInvitationsDataList = new ArrayList<>();

    public List<EventInvitationsData> getEventInvitationsDataList() {
        return eventInvitationsDataList;
    }

    public void setEventInvitationsDataList(List<EventInvitationsData> eventInvitationsDataList) {
        this.eventInvitationsDataList = eventInvitationsDataList;
    }

    public boolean isErrorStatus() {
        return errorStatus;
    }

    public void setErrorStatus(boolean errorStatus) {
        this.errorStatus = errorStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public class EventInvitationsData {
        private String eventName;
        private String eventId;
        private String eventImage;
        private String eventInvitedUserName;
        private String eventInvitedUserId;
        private String eventPostedUserId;
        private boolean isEventAccepted;

        public String getEventName() {
            return eventName;
        }

        public void setEventName(String eventName) {
            this.eventName = eventName;
        }

        public String getEventId() {
            return eventId;
        }

        public void setEventId(String eventId) {
            this.eventId = eventId;
        }

        public String getEventImage() {
            return eventImage;
        }

        public void setEventImage(String eventImage) {
            this.eventImage = eventImage;
        }

        public String getEventInvitedUserName() {
            return eventInvitedUserName;
        }

        public void setEventInvitedUserName(String eventInvitedUserName) {
            this.eventInvitedUserName = eventInvitedUserName;
        }

        public String getEventInvitedUserId() {
            return eventInvitedUserId;
        }

        public void setEventInvitedUserId(String eventInvitedUserId) {
            this.eventInvitedUserId = eventInvitedUserId;
        }

        public String getEventPostedUserId() {
            return eventPostedUserId;
        }

        public void setEventPostedUserId(String eventPostedUserId) {
            this.eventPostedUserId = eventPostedUserId;
        }

        public boolean isEventAccepted() {
            return isEventAccepted;
        }

        public void setEventAccepted(boolean eventAccepted) {
            isEventAccepted = eventAccepted;
        }
    }
}
