package com.myscrap.webservice;

import com.myscrap.model.ActiveFriends;
import com.myscrap.model.ActiveUser;
import com.myscrap.model.AddMessage;
import com.myscrap.model.Bumped;
import com.myscrap.model.ChangePassword;
import com.myscrap.model.ChatRoomResponse;
import com.myscrap.model.Comment;
import com.myscrap.model.CompanyEditProfile;
import com.myscrap.model.CompanyProfile;
import com.myscrap.model.CompanyProfilePicture;
import com.myscrap.model.Connect;
import com.myscrap.model.Contact;
import com.myscrap.model.Count;
import com.myscrap.model.CreateEvent;
import com.myscrap.model.DeletePost;
import com.myscrap.model.DeviceModel;
import com.myscrap.model.EditProfile;
import com.myscrap.model.Employee;
import com.myscrap.model.EmployeeRequest;
import com.myscrap.model.EnableNotification;
import com.myscrap.model.Event;
import com.myscrap.model.EventDelete;
import com.myscrap.model.EventGoing;
import com.myscrap.model.EventInterest;
import com.myscrap.model.EventInterestList;
import com.myscrap.model.EventInvitations;
import com.myscrap.model.EventReport;
import com.myscrap.model.Exchange;
import com.myscrap.model.Favourite;
import com.myscrap.model.Feed;
import com.myscrap.model.ForgotPassword;
import com.myscrap.model.Like;
import com.myscrap.model.LikedData;
import com.myscrap.model.LogOut;
import com.myscrap.model.LoginResponse;
import com.myscrap.model.Markers;
import com.myscrap.model.MessageCount;
import com.myscrap.model.Moderator;
import com.myscrap.model.NearFriends;
import com.myscrap.model.News;
import com.myscrap.model.Notification;
import com.myscrap.model.Online;
import com.myscrap.model.OwnIt;
import com.myscrap.model.PeopleNearBy;
import com.myscrap.model.Post;
import com.myscrap.model.ProfilePicture;
import com.myscrap.model.Report;
import com.myscrap.model.Search;
import com.myscrap.model.ShakeFriend;
import com.myscrap.model.SingleNews;
import com.myscrap.model.SinglePost;
import com.myscrap.model.SinglePostDetails;
import com.myscrap.model.UserFriendProfile;
import com.myscrap.model.Viewers;

import org.json.JSONObject;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;
import rx.Observable;


public interface ApiInterface {


    @FormUrlEncoded
    @POST("myScrapMobileSecurity")
    Observable<DeviceModel> updateDeviceDetails(@Field("apiKey") String apiKey, @Field("mobileDevice") String mobileDevice, @Field("mobileBrand") String mobileBrand);

    @GET("test")
    Call<String> test();

    @FormUrlEncoded
    @POST("register")
    Observable<LoginResponse> doSignUp(@Field("email") String email, @Field("password") String password, @Field("firstName") String firstName, @Field("lastName") String lastName, @Field("ipAddress") String ipAddress);

    @FormUrlEncoded
    @POST("msLogin")
    Call<LoginResponse> doLogin(@Field("userName") String email, @Field("password") String password, @Field("device") String device, @Field("ipAddress") String ipAddress, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("myScrapResetPassword")
    Call<String> doResetPassword(@Field("email") String email);

    //////////////////////////////////////////////////////////////////////////



    /////////////////////////////////////////////////////////////////////////

    @FormUrlEncoded
    @POST("msChatRooms")
    Observable<ChatRoomResponse> getChatRoom(@Field("userId") String userId, @Field("apiKey") String apiKey);
//    Call<ChatRoomResponse> getChatRoom(@Field("userId") String userId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msAddChatMessage")
    Call<AddMessage> addMessage(@Field("userId") String userId, @Field("senderId") String from, @Field("receiverId") String to, @Field("message") String message, @Field("timeStamp") String timeStamp,
    @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msMessageSeen")
    Observable<String> sendSeen(@Field("from") String from, @Field("to") String to, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msDeleteChatRooms")
    Call<String> deleteChatRooms(@Field("userId") String userId, @Field("friendId") String chatRoomId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msMessageReceived")
    Observable<String> sendMessageReceived(@Field("from") String from, @Field("to") String to, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msConnected")
    Observable<Connect> connected(@Field("from") String from, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msNotificationsSeen")
    Call<String> notificationsSeen(@Field("userId") String userId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msEditProfilePic")
    Call<ProfilePicture> changeProfile(@Field("userId") String userId, @Field("profilePic") String profilePic, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msEditCompanyProfilePic")
    Call<CompanyProfilePicture> changeCompanyProfile(@Field("userId") String userId, @Field("companyId") String companyId, @Field("profilePic") String profilePic, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msGcmUpdate")
    Observable<String> updateGcmId(@Field("userId") String userId, @Field("gcmCode") String gcmCode, @Field("apiKey") String apiKey);


    @FormUrlEncoded
    @POST("msLogout")
    Observable<LogOut> logout(@Field("userId") String userId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msUserOnlineStatus")
    Call<Online> userOnline(@Field("userId") String userId, @Field("chatRoomId") String chatRoomId , @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msDiscover")
    Call<Markers> discover(@Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msUpdateLocations")
    Observable<Markers> updateMarkers(@Field("companyId") String companyId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msUpdateCompany")
    Observable<Markers> updatingMarkers(@Field("userId") String userId, @Field("apiKey") String apiKey);
    //Call<Markers> updatingMarkers(@Field("userId") String userId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msUserUpdates")
    Call<Markers> updatedMarkersAck(@Field("userId") String userId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msModerator")
    Call<Moderator> moderator(@Field("userId") String userId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msFavouriteCompany")
    Observable<Markers> favouritedCompany(@Field("userId") String userId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msShake")
    Call<ShakeFriend> shake(@Field("userId") String userId , @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msPeopleNearBy")
    Observable<PeopleNearBy> peopleNearBy(@Field("userId") String userId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msViewers")
    Observable<Viewers> visitors(@Field("userId") String userId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msCompanyViewers")
    Call<Viewers> companyVisitors(@Field("userId") String userId, @Field("companyId") String companyId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msUpdateViewers")
    Observable<Viewers> visitorsSeen(@Field("userId") String userId, @Field("apiKey") String apiKey);


    @FormUrlEncoded
    @POST("msCompanyUpdateViewers")
    Call<Viewers> companyVisitorsSeen(@Field("userId") String userId, @Field("companyId") String companyId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msViewersCount")
    Observable<Viewers> visitorsCount(@Field("userId") String userId, @Field("apiKey") String apiKey);
    //Call<Viewers> visitorsCount(@Field("userId") String userId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msNotificationCounts")
    Observable<Count> notificationsCount(@Field("userId") String userId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msMessageCount")
    Observable<MessageCount> messageCount(@Field("from") String from, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msBumpedCount")
    Observable<Bumped> bumpedCount(@Field("userId") String userId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msUpdateUserLocation")
    Call<String> updateUserLocation(@Field("userId") String userId, @Field("lat") String lat, @Field("lng") String lng, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msBumpPosts")
    Observable<Bumped> bumped(@Field("userId") String userId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msUpdateBumpers")
    Observable<Bumped> bumperSeen(@Field("userId") String userId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msBumpPostRemove")
    Observable<Bumped> bumpedPostRemove(@Field("userId") String userId, @Field("friendId") String friendId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("myScrapUserLogin")
    Call<String> login(@Field("userName") String userName, @Field("password") String password, @Field("device") String device, @Field("ipAddress") String ipAddress, @Field("apiKey")  String apiKey);

    /*@FormUrlEncoded
    @POST("test")
    Call<LoginResponse> doLogin(@Field("userName") String userName, @Field("password") String password, @Field("apiKey") String apiKey, @Field("ipAddress") String ipAddress, @Field("device") String device);*/


    @FormUrlEncoded
    @POST("msChatRoomMessages")
    Observable<ChatRoomResponse> getChatRoomMessages(@Field("userId") String userId, @Field("chatRoomId") String chatRoomId, @Field("messageId") String messageId, @Field("load") String load, @Field("apiKey") String apiKey);


    @FormUrlEncoded
    @POST("msActiveFriends")
    Observable<ActiveFriends> getActiveFriends(@Field("userId") String userId, @Field("apiKey") String apiKey);



    @FormUrlEncoded
    @POST("msUsers")
    Observable<ActiveFriends> getFriendsList(@Field("userId") String userId, @Field("apiKey") String apiKey);


    @FormUrlEncoded
    @POST("msChatRoomsProfilePicture")
    Observable<ChatRoomResponse> getProfilePictures(@Field("userId") String userId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msUserFeeds")
    Observable<Feed> getFeeds(@Field("pageLoad") String pageLoad, @Field("userId") String userId, @Field("friendId") String friendId, @Field("companyId") String companyId, @Field("apiKey") String apiKey);

    @GET("customfeed")
    Observable<Feed> getFeed();

    @FormUrlEncoded
    @POST("msFavouritePosts")
    Observable<Feed> getUserFavouritedPosts(@Field("pageLoad") String pageLoad, @Field("userId") String userId, @Field("friendId") String friendId, @Field("companyId") String companyId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msReportedPosts")
    Observable<Feed> getReportedUserPosts(@Field("userId") String userId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msLike")
    Call<Like> loadLikeDetails(@Field("userId") String userId, @Field("postId") String postId, @Field("postedUserId") String postedUserId, @Field("apiKey") String apiKey);


    @FormUrlEncoded
    @POST("msCompanyLikesDetails")
    Call<Like> loadCompanyLikesDetails(@Field("userId") String userId, @Field("companyId") String companyId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msEmployeeJoin")
    Call<Employee> joinAsEmployee(@Field("userId") String userId, @Field("companyId") String companyId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msOwnCompany")
    Call<OwnIt> companyOwnIt(@Field("userId") String userId, @Field("companyId") String companyId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msDisOwned")
    Call<OwnIt> companyDisOwnIt(@Field("userId") String userId, @Field("companyId") String companyId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msEmployeeDetails")
    Call<Employee> companyEmployee(@Field("userId") String userId, @Field("companyId") String companyId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msRemoveEmployee")
    Call<Employee> removeEmployee(@Field("userId") String userId, @Field("companyId") String companyId,  @Field("empId") String empId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msRequestDetails")
    Call<EmployeeRequest> companyEmployeeRequest(@Field("userId") String userId, @Field("companyId") String companyId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msRequestStatus")
    Call<EmployeeRequest> companyEmployeeRequest(@Field("companyId") String companyId, @Field("acceptId") String acceptId, @Field("rejectId") String rejectId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msCompanyLike")
    Call<Employee> doCompanyLike(@Field("userId") String userId, @Field("companyId") String companyId, @Field("apiKey") String apiKey);


    @FormUrlEncoded
    @POST("msComment")
    Call<Comment> loadCommentDetails(@Field("userId") String userId, @Field("postId") String postId, @Field("postedUserId") String postedUserId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msInsertLike")
    Call<LikedData> insertLike(@Field("userId") String userId, @Field("postId") String postId, @Field("friendId") String friendId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msInsertComment")
    Call<Comment> insertComment(@Field("userId") String userId, @Field("postId") String postId, @Field("friendId") String friendId, @Field("comment") String comment, @Field("timeStamp") String timeStamp, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msUserPostInsert")
    Observable<Post> insertPostInsert(@Field("userId") String userId, @Field("friendId") String friendId, @Field("companyId") String companyId, @Field("editPostId") String editPostId, @Field("eventId") String eventId, @Field("eventPost") String eventPost, @Field("content") String content, @Field("timeStamp") String timeStamp, @Field("feedImage") String feedImage, @Field("imageCount") int imageCount, @Field("device") String device, @Field("ipAddress") String ipAddress, @Field("apiKey") String apiKey, @Field("tagged") JSONObject taggingObject, @Field("taggedUserIdList") List<String> taggedUserIdList, @Field("taggedUserNameList") List<String> taggedUserNameList, @Field("newsHeading") String heading, @Field("subNewsHeading") String subHeading, @Field("location") String location, @Field("multiImage") JSONObject multiImageObject);

     @FormUrlEncoded
    @POST("msUserPostInsert")
    Call<Post> insertCompanyImage(@Field("userId") String userId, @Field("friendId") String friendId, @Field("timeStamp") String timeStamp, @Field("feedImage") String feedImage, @Field("apiKey") String apiKey, @Field("companyId") String companyId, @Field("imageCount") String imageCount, @Field("editPostId") String editPostId, @Field("tagged") JSONObject taggingObject,@Field("taggedUserIdList") List<String> taggedUserIdList, @Field("taggedUserNameList") List<String> taggedUserNameList, @Field("content") String content);

    @FormUrlEncoded
    @Headers("Cache-Control:public ,max-age=60")
    @POST("msAddContacts")
    Observable<Contact> contacts(@Field("userId") String userId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    //@POST("msLme")
    @POST("msExchange")
    Observable<Exchange> exchange(@Field("userId") String userId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msFavouriteDetails")
    Observable<Favourite> favourites(@Field("userId") String userId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msModerators")
    Observable<Favourite> moderators(@Field("userId") String userId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msNews")
    Observable<News> news(@Field("userId") String userId, @Field("companyId") String companyId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msSingleNews")
    Call<SingleNews> singleNews(@Field("userId") String userId, @Field("newsId") String newsId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msSinglePostView")
    Call<SinglePost> singleFeeds(@Field("userId") String userId, @Field("postId") String postId, @Field("notificationId") String notificationId, @Field("apiKey") String apiKey);


    @FormUrlEncoded
    @POST("msPostDetails")
    Observable<SinglePostDetails> singlePostDetails(@Field("userId") String userId, @Field("postId") String postId, @Field("notId") String  notId,@Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msUserProfileFeeds")
    Observable<UserFriendProfile> userProfile(@Field("userId") String userId, @Field("pageLoad") String pageLoad, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msFriendProfileFeeds")
    Observable<UserFriendProfile> userFriendProfile(@Field("userId") String userId, @Field("friendId") String friendId, @Field("pageLoad") String pageLoad, @Field("notId") String notId, @Field("userView") String userView, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msConnectPeople")
    Call<JSONObject> addToContacts(@Field("userId") String userId, @Field("friendId") String friendId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msEditProfile")
    Call<EditProfile> editProfile(@Field("userId") String userId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msCompanyEditProfile")
    Call<CompanyEditProfile> companyEditProfile(@Field("userId") String userId, @Field("companyId") String companyId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msForgotPassword")
    Call<ForgotPassword> forgotPassword(@Field("reg_email") String email, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msNotifications")
    Call<Notification> notification(@Field("userId") String userId, @Field("pageLoad") String pageLoad, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msEnableNotification")
    Observable<EnableNotification> enableNotification(@Field("userId") String userId, @Field("notification") String notification,@Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msOnline")
    Call<Online> online(@Field("userId") String userId, @Field("status") String status,@Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msClearNotifications")
    Call<Notification> clearNotification(@Field("userId") String userId, @Field("apiKey") String apiKey);


    @FormUrlEncoded
    @POST("msSearch")
    Observable<Search> search(@Field("userId") String userId, @Field("word") String word, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msNearFriends")
    Observable<NearFriends> nearestFriends(@Field("userId") String userId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msActiveUsers")
    Call<ActiveUser> activeUsers(@Field("userId") String userId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msEditProfile")
    Call<EditProfile> editProfile(@Field("userId") String userId, @Field("firstName") String firstName, @Field("lastName") String lastName, @Field("email") String email, @Field("designation") String designation, @Field("company") String company, @Field("companyId")  String companyId, @Field("userBio") String userBio, @Field("profilePic") String profilePic, @Field("userInterest") String userInterest, @Field("userRoles") String roles ,@Field("phoneNo") String phoneNo, @Field("code") String code, @Field("website") String website, @Field("userLocation") String userLocation, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msPostDelete")
    Call<DeletePost> deletePost(@Field("userId")String userId, @Field("postId") String postId, @Field("albumId") String albumID, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msCommentDelete")
    Call<Comment> deleteComment(@Field("userId")String userId, @Field("postId") String postId, @Field("commentId") String commentId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msInsertComment")
    Call<Comment> editComment(@Field("userId")String userId, @Field("postId") String postId, @Field("friendId") String friendId, @Field("editId") String editId, @Field("comment") String comment, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msCompanyFeeds")
    Observable<CompanyProfile> companyProfile(@Field("pageLoad") String pageLoad, @Field("userId") String userId, @Field("companyId") String companyId, @Field("notId") String notId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msCompanyImages")
    Call<CompanyProfile> companyGallery(@Field("userId") String userId, @Field("companyId") String companyId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msChangePassword")
    Call<ChangePassword> changePassword(@Field("userId") String userId, @Field("curPassword") String curPassword, @Field("newPassword") String newPassword, @Field("apiKey") String apiKey);


    @FormUrlEncoded
    @POST("msInsertFavouritePost")
    Call<Favourite> insertFavourite(@Field("userId") String userId, @Field("postId") String postId, @Field("friendId") String friendId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msInsertFavouriteCompany")
    Call<Favourite> insertCompanyFavourite(@Field("userId") String userId, @Field("companyId") String companyId, @Field("apiKey") String apiKey);


    @FormUrlEncoded
    @POST("msCompanyEditProfile")
    Call<CompanyEditProfile> companyEditProfile(@Field("userId") String userId, @Field("companyId") String companyId, @Field("companyName") String companyName, @Field("phoneNo") String phoneNo, @Field("code") String code, @Field("owerName") String ownerName, @Field("owerId") String ownerId, @Field("website") String website, @Field("companyInterest") String companyInterest, @Field("userRoles") String roles , @Field("companyAffiliations") String affiliation ,@Field("email") String email, @Field("companyType") String companyType, @Field("companyLocation") String companyLocation,
    @Field("companyBio") String companyBio, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msCompanyEditProfile")
    Call<CompanyEditProfile> companyEditProfileLocation(@Field("userId") String userId, @Field("companyId") String companyId,  @Field("companyLatitude") String companyLatitude,  @Field("companyLongitude") String companyLongitude,  @Field("apiKey") String apiKey);


    @FormUrlEncoded
    @POST("msUserOnlineStatus")
    Call<Online> userOnlineStatus(@Field("userId") String userId, @Field("chatRoomId") String chatRoomId, @Field("timeZone") String timeZone , @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msReport")
    Call<Report> reportPost(@Field("userId") String userId, @Field("postId") String postId, @Field("friendId")  String postedUserId, @Field("companyId")  String companyId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msReport")
    Call<Report> undoReportPost(@Field("reportId") String reportId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msDeleteReportedPost")
    Call<Report> deleteReportPost(@Field("postId") String postId, @Field("reportId") String reportId ,@Field("userId") String userId, @Field("apiKey") String apiKey);


    @FormUrlEncoded
    @POST("msCreateEvents")
    Observable<CreateEvent> doCreateEvent(@Field("userId") String userId, @Field("eventId") String eventId , @Field("ipAddress") String ipAddress, @Field("eventName") String eventName, @Field("startDate") String startDate, @Field("startTime") String startTime, @Field("endDate") String endDate, @Field("endTime") String endTime, @Field("eventLocation") String eventLocation, @Field("eventDetail") String eventDetail, @Field("eventPrivacy") String privacy, @Field("eventPicture") String eventPicture, @Field("eventGuest") boolean checked, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msEvents")
    Observable<Event> getEventList(@Field("userId") String userId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msEventDetails")
    Observable<Event> getEventDetails(@Field("userId") String userId, @Field("eventId") String eventId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msEventInterest")
    Observable<EventInterest> eventInterest(@Field("userId") String userId, @Field("eventId") String eventId, @Field("eventInterest") int eventInterest, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msEventGoing")
    Observable<EventGoing> eventGoing(@Field("userId") String userId, @Field("eventId") String eventId, @Field("eventGoing") int eventGoing, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msEventDelete")
    Observable<EventDelete> eventDelete(@Field("userId") String userId, @Field("eventId") String eventId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msEventReport")
    Observable<EventReport> eventReport(@Field("userId") String userId, @Field("eventId") String eventId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msEventDetailFeeds")
    Observable<Event> getEventDetailFeeds(@Field("pageLoad") String pageLoad, @Field("userId") String userId, @Field("eventId") String eventId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msGoing")
    Observable<EventInterestList> getEventGoingList(@Field("userId") String userId, @Field("eventId") String eventId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msInterested")
    Observable<EventInterestList> getEventInterestList(@Field("userId") String userId, @Field("eventId") String eventId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msEventInvites")
    Call<EventInvitations> invitations(@Field("userId") String userId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @Headers("Cache-Control:public ,max-age=60")
    @POST("msAddContacts")
    Call<Contact> contacts(@Field("userId") String userId, @Field("eventId") String eventId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msInvitePeople")
    Call<JSONObject> inviteToEvent(@Field("userId") String userId, @Field("friendId") String friendId, @Field("eventId") String eventId, @Field("apiKey") String apiKey);

    @FormUrlEncoded
    @POST("msInvitePeople")
    Call<JSONObject> inviteContacts(@Field("userId") String userId, @Field("friendId") String friendId, @Field("apiKey") String apiKey);

    @GET
    Call<ResponseBody> fetchCaptcha(@Url String url);

}
