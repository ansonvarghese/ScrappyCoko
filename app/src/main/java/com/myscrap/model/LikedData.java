package com.myscrap.model;

import java.util.List;

/**
 * Created by ms3 on 5/18/2017.
 */

public class LikedData  {
    private boolean error;

    private String status;

    private InsertLikeData insertLikeData;

    public boolean getError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public InsertLikeData getInsertLikeData() {
        return insertLikeData;
    }

    public void setInsertLikeData(InsertLikeData insertLikeData) {
        this.insertLikeData = insertLikeData;
    }

    public class InsertLikeData {
        private String postedUserId;

        private String postType;

        private String timeStamp;

        private String likeCount;

        private String status;

        private String postedUserDesignation;

        private String commentCount;

        private String postId;

        private String postedUserName;

        private boolean likeStatus;

        private String postBy;

        private List<InsertLikeData.PictureUrl> pictureUrl;

        public String getPostedUserId() {
            return postedUserId;
        }

        public void setPostedUserId(String postedUserId) {
            this.postedUserId = postedUserId;
        }

        public String getPostType() {
            return postType;
        }

        public void setPostType(String postType) {
            this.postType = postType;
        }

        public String getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
        }

        public String getLikeCount() {
            return likeCount;
        }

        public void setLikeCount(String likeCount) {
            this.likeCount = likeCount;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getPostedUserDesignation() {
            return postedUserDesignation;
        }

        public void setPostedUserDesignation(String postedUserDesignation) {
            this.postedUserDesignation = postedUserDesignation;
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

        public String getPostedUserName() {
            return postedUserName;
        }

        public void setPostedUserName(String postedUserName) {
            this.postedUserName = postedUserName;
        }

        public boolean getLikeStatus() {
            return likeStatus;
        }

        public void setLikeStatus(boolean likeStatus) {
            this.likeStatus = likeStatus;
        }

        public String getPostBy() {
            return postBy;
        }

        public void setPostBy(String postBy) {
            this.postBy = postBy;
        }

        public List<PictureUrl> getPictureUrl() {
            return pictureUrl;
        }

        public void setPictureUrl(List<PictureUrl> pictureUrl) {
            this.pictureUrl = pictureUrl;
        }

        public class PictureUrl {
            private String postid;

            private String likeCount;

            private String images;

            private String commentCount;

            private boolean likeStatus;

            public String getLikeCount() {
                return likeCount;
            }

            public void setLikeCount(String likeCount) {
                this.likeCount = likeCount;
            }

            public String getImages() {
                return images;
            }

            public void setImages(String images) {
                this.images = images;
            }

            public String getCommentCount() {
                return commentCount;
            }

            public void setCommentCount(String commentCount) {
                this.commentCount = commentCount;
            }

            public boolean isLikeStatus() {
                return likeStatus;
            }

            public void setLikeStatus(boolean likeStatus) {
                this.likeStatus = likeStatus;
            }
        }
    }
}
