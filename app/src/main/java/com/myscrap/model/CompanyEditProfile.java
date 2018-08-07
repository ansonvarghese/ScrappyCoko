package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ms3 on 5/19/2017.
 */

public class CompanyEditProfile {

    @SerializedName("status")
    private String status;

    @SerializedName("error")
    private boolean errorStatus;

    @SerializedName("EditCompanyProfileData")
    private CompanyEditProfile.CompanyEditProfileData data ;

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

    public CompanyEditProfileData getData() {
        return data;
    }

    public void setData(CompanyEditProfileData data) {
        this.data = data;
    }


    public class CompanyEditProfileData {

        private String code;

        private String phoneNo;
        @SerializedName("owerName")
        private String ownerName;
        @SerializedName("owerId")
        private String ownerId;

        private String website;

        private String companyInterest;

        private String companyAffiliation;

        private String userInterestRoles;

        private String email;

        private String companyType;

        private String companyLocation;

        private String companyProfilePic;

        private String companyName;

        private String companyBio;

        private String companyLatitude;

        private String companyLongitude;

        public String getPhoneNo() {
            return phoneNo;
        }

        public void setPhoneNo(String phoneNo) {
            this.phoneNo = phoneNo;
        }

        public String getOwnerName() {
            return ownerName;
        }

        public void setOwnerName(String ownerName) {
            this.ownerName = ownerName;
        }

        public String getOwnerId() {
            return ownerId;
        }

        public void setOwnerId(String ownerId) {
            this.ownerId = ownerId;
        }

        public String getWebsite() {
            return website;
        }

        public void setWebsite(String website) {
            this.website = website;
        }

        public String getCompanyInterest() {
            return companyInterest;
        }

        public void setCompanyInterest(String companyInterest) {
            this.companyInterest = companyInterest;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getCompanyType() {
            return companyType;
        }

        public void setCompanyType(String companyType) {
            this.companyType = companyType;
        }

        public String getCompanyLocation() {
            return companyLocation;
        }

        public void setCompanyLocation(String companyLocation) {
            this.companyLocation = companyLocation;
        }

        public String getCompanyProfilePic() {
            return companyProfilePic;
        }

        public void setCompanyProfilePic(String companyProfilePic) {
            this.companyProfilePic = companyProfilePic;
        }

        public String getCompanyName() {
            return companyName;
        }

        public void setCompanyName(String companyName) {
            this.companyName = companyName;
        }

        public String getCompanyBio() {
            return companyBio;
        }

        public void setCompanyBio(String companyBio) {
            this.companyBio = companyBio;
        }

        public String getCompanyLatitude() {
            return companyLatitude;
        }

        public void setCompanyLatitude(String companyLatitude) {
            this.companyLatitude = companyLatitude;
        }

        public String getCompanyLongitude() {
            return companyLongitude;
        }

        public void setCompanyLongitude(String companyLongitude) {
            this.companyLongitude = companyLongitude;
        }

        public String getUserInterestRoles() {
            return userInterestRoles;
        }

        public void setUserInterestRoles(String userInterestRoles) {
            this.userInterestRoles = userInterestRoles;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getCompanyAffiliation() {
            return companyAffiliation;
        }

        public void setCompanyAffiliation(String companyAffiliation) {
            this.companyAffiliation = companyAffiliation;
        }
    }
}
