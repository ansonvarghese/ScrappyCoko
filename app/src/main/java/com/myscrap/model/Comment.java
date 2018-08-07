package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ms3 on 5/16/2017.
 */

public class Comment {
    @SerializedName("status")
    private String status;
    @SerializedName("error")
    private boolean errorStatus;

    @SerializedName("commentData")
    private List<Comment.CommentData> data = new ArrayList<>();

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

    public List<CommentData> getData() {
        return data;
    }

    public void setData(List<CommentData> data) {
        this.data = data;
    }

    public class CommentData {

        private String timeStamp;

        private String name;

        private String userId;

        private String profilePic;

        private String designation;

        private String comment;

        private String colorCode;

        private String commentCount;

        private String postId;

        private String commentId;

        private boolean likeStatus;

        private String friendStatus;

        /*@SerializedName("commentLikeDetails")
        private List<Comment.CommentData.CommentLikeDetails> dataLikeDetails = new ArrayList<>();*/

        public String getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
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

        public String getDesignation() {
            return designation;
        }

        public void setDesignation(String designation) {
            this.designation = designation;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getCommentCount() {
            return commentCount;
        }

        public void setCommentCount(String commentCount) {
            this.commentCount = commentCount;
        }

        public String getPostId() {
            return postId;
        }

        public void setPostId(String postId) {
            this.postId = postId;
        }

        public String getCommentId() {
            return commentId;
        }

        public void setCommentId(String commentId) {
            this.commentId = commentId;
        }

        public boolean isLikeStatus() {
            return likeStatus;
        }

        public void setLikeStatus(boolean likeStatus) {
            this.likeStatus = likeStatus;
        }

        public String getFriendStatus() {
            return friendStatus;
        }

        public void setFriendStatus(String friendStatus) {
            this.friendStatus = friendStatus;
        }

        public String getColorCode() {
            return colorCode;
        }

        public void setColorCode(String colorCode) {
            this.colorCode = colorCode;
        }


        public class CommentLikeDetails {
            private String commentpostId;

            private String commentname;

            private String commentuserId;

            private String commentlikeProfilePic;

            private String commentlikeTimeStamp;

            private String friendStatus;

            public String getCommentpostId() {
                return commentpostId;
            }

            public void setCommentpostId(String commentpostId) {
                this.commentpostId = commentpostId;
            }

            public String getCommentname() {
                return commentname;
            }

            public void setCommentname(String commentname) {
                this.commentname = commentname;
            }

            public String getCommentuserId() {
                return commentuserId;
            }

            public void setCommentuserId(String commentuserId) {
                this.commentuserId = commentuserId;
            }

            public String getCommentlikeProfilePic() {
                return commentlikeProfilePic;
            }

            public void setCommentlikeProfilePic(String commentlikeProfilePic) {
                this.commentlikeProfilePic = commentlikeProfilePic;
            }

            public String getCommentlikeTimeStamp() {
                return commentlikeTimeStamp;
            }

            public void setCommentlikeTimeStamp(String commentlikeTimeStamp) {
                this.commentlikeTimeStamp = commentlikeTimeStamp;
            }

            public String getFriendStatus() {
                return friendStatus;
            }

            public void setFriendStatus(String friendStatus) {
                this.friendStatus = friendStatus;
            }
        }

    }
}
