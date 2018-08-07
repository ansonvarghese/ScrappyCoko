package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ms3 on 8/10/2017.
 */

public class Event {

    @SerializedName("status")
    private String status;
    @SerializedName("error")
    private boolean errorStatus;

    @SerializedName("eventData")
    private List<EventData>  eventDataList = new ArrayList<>();

    @SerializedName("feedsData")
    private List<Feed.FeedItem> data = new ArrayList<>();

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

    public List<EventData> getEventDataList() {
        return eventDataList;
    }

    public void setEventDataList(List<EventData> eventDataList) {
        this.eventDataList = eventDataList;
    }

    public List<Feed.FeedItem> getData() {
        return data;
    }

    public void setData(List<Feed.FeedItem> data) {
        this.data = data;
    }

    public class EventData {
        private String userId;
        private String eventId;
        private String eventPostedId;
        private String eventName;
        private String eventPicture;
        private String startDate;
        private String eventShare;
        private String endDate;
        private String startTime;
        private String endTime;
        private String eventLocation;
        private String eventDetail;
        private int eventPrivacy = 0;
        private int eventGuest = 0;
        private String eventTime;
        private boolean isGoing;
        private int goingCount;
        private boolean isInterested ;
        private int interestedCount;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getEventName() {
            return eventName;
        }

        public void setEventName(String eventName) {
            this.eventName = eventName;
        }

        public String getEventPicture() {
            return eventPicture;
        }

        public void setEventPicture(String eventPicture) {
            this.eventPicture = eventPicture;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public String getEventLocation() {
            return eventLocation;
        }

        public void setEventLocation(String eventLocation) {
            this.eventLocation = eventLocation;
        }

        public String getEventDetail() {
            return eventDetail;
        }

        public void setEventDetail(String eventDetail) {
            this.eventDetail = eventDetail;
        }

        public int getEventPrivacy() {
            return eventPrivacy;
        }

        public void setEventPrivacy(int eventPrivacy) {
            this.eventPrivacy = eventPrivacy;
        }

        public int getEventGuest() {
            return eventGuest;
        }

        public void setEventGuest(int eventGuest) {
            this.eventGuest = eventGuest;
        }

        public String getEventTime() {
            return eventTime;
        }

        public void setEventTime(String eventTime) {
            this.eventTime = eventTime;
        }

        public String getEventId() {
            return eventId;
        }

        public void setEventId(String eventId) {
            this.eventId = eventId;
        }

        public String getEventPostedId() {
            return eventPostedId;
        }

        public void setEventPostedId(String eventPostedId) {
            this.eventPostedId = eventPostedId;
        }

        public boolean isGoing() {
            return isGoing;
        }

        public void setGoing(boolean going) {
            isGoing = going;
        }

        public boolean isInterested() {
            return isInterested;
        }

        public void setInterested(boolean interested) {
            isInterested = interested;
        }

        public int getGoingCount() {
            return goingCount;
        }

        public void setGoingCount(int goingCount) {
            this.goingCount = goingCount;
        }

        public int getInterestedCount() {
            return interestedCount;
        }

        public void setInterestedCount(int interestedCount) {
            this.interestedCount = interestedCount;
        }

        public String getEventShare() {
            return eventShare;
        }

        public void setEventShare(String eventShare) {
            this.eventShare = eventShare;
        }
    }
}
