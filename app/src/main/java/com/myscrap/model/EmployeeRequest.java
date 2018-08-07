package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by ms3 on 5/30/2017.
 */

public class EmployeeRequest {

    @SerializedName("status")
    private String status;

    @SerializedName("error")
    private boolean errorStatus;

    @SerializedName("RequestData")
    private List<EmployeeRequestData> employeeRequestData;

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

    public List<EmployeeRequestData> getEmployeeRequestData() {
        return employeeRequestData;
    }

    public void setEmployeeRequestData(List<EmployeeRequestData> employeeRequestData) {
        this.employeeRequestData = employeeRequestData;
    }


    public class EmployeeRequestData {

        private String name;

        private String userId;

        private String profilePic;

        private String designation;

        private String country;

        private String colorCode;

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
    }
}
