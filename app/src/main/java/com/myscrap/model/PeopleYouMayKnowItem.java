package com.myscrap.model;


import java.util.List;

/**
 * Created by Ms2 on 4/5/2016.
 */
public class PeopleYouMayKnowItem  {

    private int friendCount, followerCount, likeCount, friendConnectionStatus;
    private String friendId, colorCode, friendName, friendOnline,friendEmail, friendDesignation, friendDOB, friendGender, friendFirstAddress, friendSecondAddress, friendMobile, friendCity,friendWeb, friendCountry, friendDOJ, friendJIP, friendProfilePic, friendCoverPic , connectionStatus, type, lastLoggedIn;
    private boolean isFriend = false;
    private List<PeopleYouMayKnowItem> mFriendDetailedList;
   // private List<FeedItemComment> listOfComments;
  //  private List<FeedItemLike> listOfLikes;
    private Feed.FeedItem listOfFeed;
    private boolean isShowTitle = false;
    boolean isUserFollowingStatus = false;
    private  int userOnline, privacyCity, privacyPhone, privacyCountry, privacyDOB, privacyWeb, privacyEmail, privacyDesignation;

    public PeopleYouMayKnowItem() {
    }

    public PeopleYouMayKnowItem(int friendCount, String friendId, String friendName, String friendEmail, String friendDesignation, String friendDOB, String friendGender, String friendFirstAddress, String friendSecondAddress, String friendMobile, String friendCity, String friendCountry, String friendDOJ, String friendJIP, String friendProfilePic, String friendCoverPic) {
        this.friendCount = friendCount;
        this.friendId = friendId;
        this.friendName = friendName;
        this.friendEmail = friendEmail;
        this.friendDesignation = friendDesignation;
        this.friendDOB = friendDOB ;
        this.friendGender = friendGender;
        this.friendFirstAddress = friendFirstAddress;
        this.friendSecondAddress = friendSecondAddress;
        this.friendMobile = friendMobile;
        this.friendCity = friendCity;
        this.friendCountry = friendCountry;
        this.friendDOJ = friendDOJ;
        this.friendJIP = friendJIP;
        this.friendProfilePic = friendProfilePic;
        this.friendCoverPic = friendCoverPic;
    }

    public boolean isShowTitle() {
        return isShowTitle;
    }

    public void setShowTitle(boolean showTitle) {
        isShowTitle = showTitle;
    }

    public int getFriendCount() {
        return friendCount;
    }

    public void setFriendCount(int friendCount) {
        this.friendCount = friendCount;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public String getFriendEmail() {
        return friendEmail;
    }

    public void setFriendEmail(String friendEmail) {
        this.friendEmail = friendEmail;
    }

    public String getFriendDesignation() {
        return friendDesignation;
    }

    public void setFriendDesignation(String friendDesignation) {
        this.friendDesignation = friendDesignation;
    }

    public String getFriendDOB() {
        return friendDOB;
    }

    public void setFriendDOB(String friendDOB) {
        this.friendDOB = friendDOB;
    }

    public String getFriendGender() {
        return friendGender;
    }

    public void setFriendGender(String friendGender) {
        this.friendGender = friendGender;
    }

    public String getFriendFirstAddress() {
        return friendFirstAddress;
    }

    public void setFriendFirstAddress(String friendFirstAddress) {
        this.friendFirstAddress = friendFirstAddress;
    }

    public String getFriendSecondAddress() {
        return friendSecondAddress;
    }

    public void setFriendSecondAddress(String friendSecondAddress) {
        this.friendSecondAddress = friendSecondAddress;
    }

    public String getFriendMobile() {
        return friendMobile;
    }

    public void setFriendMobile(String friendMobile) {
        this.friendMobile = friendMobile;
    }

    public String getFriendCity() {
        return friendCity;
    }

    public void setFriendCity(String friendCity) {
        this.friendCity = friendCity;
    }

    public String getFriendCountry() {
        return friendCountry;
    }

    public void setFriendCountry(String friendCountry) {
        this.friendCountry = friendCountry;
    }

    public String getFriendDOJ() {
        return friendDOJ;
    }

    public void setFriendDOJ(String friendDOJ) {
        this.friendDOJ = friendDOJ;
    }

    public String getFriendJIP() {
        return friendJIP;
    }

    public void setFriendJIP(String friendJIP) {
        this.friendJIP = friendJIP;
    }

    public String getFriendProfilePic() {
        return friendProfilePic;
    }

    public void setFriendProfilePic(String friendProfilePic) {
        this.friendProfilePic = friendProfilePic;
    }

    public String getFriendCoverPic() {
        return friendCoverPic;
    }

    public void setFriendCoverPic(String friendCoverPic) {
        this.friendCoverPic = friendCoverPic;
    }


    public List<PeopleYouMayKnowItem> getFriendDetailedList() {
        return mFriendDetailedList;
    }

    public void setFriendDetailedList(List<PeopleYouMayKnowItem> mFriendDetailedList) {
        this.mFriendDetailedList = mFriendDetailedList;
    }

    /*public List<FeedItemComment> getListOfComments() {
        return listOfComments;
    }

    public void setListOfComments(List<FeedItemComment> mListOfComments) {
        listOfComments = new ArrayList<>();
        this.listOfComments = mListOfComments;
    }

    public List<FeedItemLike> getListOfLikes() {
        return listOfLikes;
    }

    public void setListOfLikes(List<FeedItemLike> mListOfLikes) {
        listOfLikes = new ArrayList<>();
        this.listOfLikes = mListOfLikes;
    }

    public FeedItem getListOfFeeds() {
        return listOfFeed;
    }

    public void setListOfFeeds(FeedItem listOfFeed) {
        this.listOfFeed = listOfFeed;
    }*/

    public String getFriendWeb() {
        return friendWeb;
    }

    public void setFriendWeb(String friendWeb) {
        this.friendWeb = friendWeb;
    }

    public int getFriendConnectionStatus() {
        return friendConnectionStatus;
    }

    public void setFriendConnectionStatus(int friendConnectionStatus) {
        this.friendConnectionStatus = friendConnectionStatus;
    }

    public String getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPrivacyCity() {
        return privacyCity;
    }

    public void setPrivacyCity(int privacyCity) {
        this.privacyCity = privacyCity;
    }

    public int getPrivacyPhone() {
        return privacyPhone;
    }

    public void setPrivacyPhone(int privacyPhone) {
        this.privacyPhone = privacyPhone;
    }

    public int getPrivacyCountry() {
        return privacyCountry;
    }

    public void setPrivacyCountry(int privacyCountry) {
        this.privacyCountry = privacyCountry;
    }

    public int getPrivacyDOB() {
        return privacyDOB;
    }

    public void setPrivacyDOB(int privacyDOB) {
        this.privacyDOB = privacyDOB;
    }

    public int getPrivacyWeb() {
        return privacyWeb;
    }

    public void setPrivacyWeb(int privacyWeb) {
        this.privacyWeb = privacyWeb;
    }

    public int getPrivacyEmail() {
        return privacyEmail;
    }

    public void setPrivacyEmail(int privacyEmail) {
        this.privacyEmail = privacyEmail;
    }

    public int getPrivacyDesignation() {
        return privacyDesignation;
    }

    public void setPrivacyDesignation(int privacyDesignation) {
        this.privacyDesignation = privacyDesignation;
    }

    public int getUserOnline() {
        return userOnline;
    }

    public void setUserOnline(int userOnline) {
        this.userOnline = userOnline;
    }

    public String getLastLoggedIn() {
        return lastLoggedIn;
    }

    public void setLastLoggedIn(String lastLoggedIn) {
        this.lastLoggedIn = lastLoggedIn;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(int followerCount) {
        this.followerCount = followerCount;
    }

    public boolean isUserFollowingStatus() {
        return isUserFollowingStatus;
    }

    public void setUserFollowingStatus(boolean userFollowingStatus) {
        isUserFollowingStatus = userFollowingStatus;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }

    public String getFriendOnline() {
        return friendOnline;
    }

    public void setFriendOnline(String friendOnline) {
        this.friendOnline = friendOnline;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }
}
