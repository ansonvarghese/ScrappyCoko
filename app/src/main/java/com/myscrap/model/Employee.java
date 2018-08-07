package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by ms3 on 5/30/2017.
 */

public class Employee {

    @SerializedName("status")
    private String status;
    @SerializedName("error")
    private boolean errorStatus;

    private boolean empStatus;

    private boolean joinStatus;

    private boolean likeStatus;

    private EmployeeData employeeData;


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

    public boolean isJoinStatus() {
        return joinStatus;
    }

    public void setJoinStatus(boolean joinStatus) {
        this.joinStatus = joinStatus;
    }

    public boolean isLikeStatus() {
        return likeStatus;
    }

    public void setLikeStatus(boolean likeStatus) {
        this.likeStatus = likeStatus;
    }

    public EmployeeData getEmployeeData() {
        return employeeData;
    }

    public void setEmployeeData(EmployeeData employeeData) {
        this.employeeData = employeeData;
    }

    public boolean isEmpStatus() {
        return empStatus;
    }

    public void setEmpStatus(boolean empStatus) {
        this.empStatus = empStatus;
    }

    public class EmployeeData {
        @SerializedName("Employees")
        private List<Employees> employees;
        @SerializedName("Admin")
        private Admin admin;



        public Admin getAdmin() {
            return admin;
        }

        public void setAdmin(Admin admin) {
            this.admin = admin;
        }

        public List<Employees> getEmployees() {
            return employees;
        }

        public void setEmployees(List<Employees> employees) {
            this.employees = employees;
        }

        public class Employees {


            private String name;

            private String userId;

            private String profilePic;

            private String designation;

            private String country;

            private String userCompany;

            private String colorCode;

            private String friendCount;

            private String points;

            private int moderator;

            private String rank;

            private boolean neJoined;

            private String friendStatus;

            public String getRank() {
                return rank;
            }

            public void setRank(String rank) {
                this.rank = rank;
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

            public String getFriendCount() {
                return friendCount;
            }

            public void setFriendCount(String friendCount) {
                this.friendCount = friendCount;
            }

            public String getPoints() {
                return points;
            }

            public void setPoints(String points) {
                this.points = points;
            }

            public String getFriendStatus() {
                return friendStatus;
            }

            public void setFriendStatus(String friendStatus) {
                this.friendStatus = friendStatus;
            }

            public String getCountry() {
                return country;
            }

            public void setCountry(String country) {
                this.country = country;
            }

            public String getColorCode() {
                return colorCode;
            }

            public void setColorCode(String colorCode) {
                this.colorCode = colorCode;
            }

            public String getUserCompany() {
                return userCompany;
            }

            public void setUserCompany(String userCompany) {
                this.userCompany = userCompany;
            }

            public boolean isNeJoined() {
                return neJoined;
            }

            public void setNeJoined(boolean neJoined) {
                this.neJoined = neJoined;
            }

            public int getModerator() {
                return moderator;
            }

            public void setModerator(int moderator) {
                this.moderator = moderator;
            }
        }

        public class Admin {
            private String admin;

            private String name;

            private String profilePic;

            private String userCompany;

            private String designation;

            private String userid;

            private String country;

            private String colorCode;

            public String getAdmin() {
                return admin;
            }

            public void setAdmin(String admin) {
                this.admin = admin;
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

            public String getUserid() {
                return userid;
            }

            public void setUserid(String userid) {
                this.userid = userid;
            }

            public String getCountry() {
                return country;
            }

            public void setCountry(String country) {
                this.country = country;
            }

            public String getColorCode() {
                return colorCode;
            }

            public void setColorCode(String colorCode) {
                this.colorCode = colorCode;
            }

            public String getDesignation() {
                return designation;
            }

            public void setDesignation(String designation) {
                this.designation = designation;
            }

            public String getUserCompany() {
                return userCompany;
            }

            public void setUserCompany(String userCompany) {
                this.userCompany = userCompany;
            }
        }
    }
}
