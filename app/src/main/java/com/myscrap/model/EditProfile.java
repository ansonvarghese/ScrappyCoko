package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ms3 on 5/19/2017.
 */

public class EditProfile {

    @SerializedName("status")
    private String status;

    @SerializedName("error")
    private boolean errorStatus;

    @SerializedName("EditProfileData")
    private EditProfile.EditProfileData data ;

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

    public EditProfileData getData() {
        return data;
    }

    public void setData(EditProfileData data) {
        this.data = data;
    }


    public class EditProfileData {

        private String phoneNo;

        private String code;

        private String lastName;

        private String userBio;

        private String userInterest;

        private String userInterestRoles;

        private String website;

        private String email;

        private String userLocation;

        private String company;

        private String companyId;

        private String profilePic;

        private String designation;

        private String firstName;

        private boolean isProfilePictureNeed;

        private boolean isWebsiteNeed;

        private boolean isCompanyNeed;

        private boolean isPhoneNeed;

        private boolean isRolesNeed;

        private boolean isInterestsNeed;

        private boolean isBioNeed;

        private boolean isDesignationNeed;

        private boolean isCountryNeed;

        public String getPhoneNo() {
            return phoneNo;
        }

        public void setPhoneNo(String phoneNo) {
            this.phoneNo = phoneNo;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getUserBio() {
            return userBio;
        }

        public void setUserBio(String userBio) {
            this.userBio = userBio;
        }

        public String getUserInterest() {
            return userInterest;
        }

        public void setUserInterest(String userInterest) {
            this.userInterest = userInterest;
        }

        public String getWebsite() {
            return website;
        }

        public void setWebsite(String website) {
            this.website = website;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getUserLocation() {
            return userLocation;
        }

        public void setUserLocation(String userLocation) {
            this.userLocation = userLocation;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
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

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
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

        public String getCompanyId() {
            return companyId;
        }

        public void setCompanyId(String companyId) {
            this.companyId = companyId;
        }

        public boolean isProfilePictureNeed() {
            return isProfilePictureNeed;
        }

        public void setProfilePictureNeed(boolean profilePictureNeed) {
            isProfilePictureNeed = profilePictureNeed;
        }

        public boolean isWebsiteNeed() {
            return isWebsiteNeed;
        }

        public void setWebsiteNeed(boolean websiteNeed) {
            isWebsiteNeed = websiteNeed;
        }

        public boolean isCompanyNeed() {
            return isCompanyNeed;
        }

        public void setCompanyNeed(boolean companyNeed) {
            isCompanyNeed = companyNeed;
        }

        public boolean isPhoneNeed() {
            return isPhoneNeed;
        }

        public void setPhoneNeed(boolean phoneNeed) {
            isPhoneNeed = phoneNeed;
        }

        public boolean isRolesNeed() {
            return isRolesNeed;
        }

        public void setRolesNeed(boolean rolesNeed) {
            isRolesNeed = rolesNeed;
        }

        public boolean isInterestsNeed() {
            return isInterestsNeed;
        }

        public void setInterestsNeed(boolean interestsNeed) {
            isInterestsNeed = interestsNeed;
        }

        public boolean isBioNeed() {
            return isBioNeed;
        }

        public void setBioNeed(boolean bioNeed) {
            isBioNeed = bioNeed;
        }

        public boolean isDesignationNeed() {
            return isDesignationNeed;
        }

        public void setDesignationNeed(boolean designationNeed) {
            isDesignationNeed = designationNeed;
        }

        public boolean isCountryNeed() {
            return isCountryNeed;
        }

        public void setCountryNeed(boolean countryNeed) {
            isCountryNeed = countryNeed;
        }
    }
}
