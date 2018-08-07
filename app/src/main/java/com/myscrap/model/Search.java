package com.myscrap.model;

import java.util.List;

/**
 * Created by ms3 on 6/6/2017.
 */

public class Search  {
    private String error;

    private String status;

    private SearchData searchData;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public SearchData getSearchData() {
        return searchData;
    }

    public void setSearchData(SearchData searchData) {
        this.searchData = searchData;
    }


    public class SearchData {


        private List<Users> user;

        public List<Users> getUser() {
            return user;
        }

        public void setUser(List<Users> user) {
            this.user = user;
        }

        public class Users {

            private String id;

            private String name;

            private String profilePic;

            private String type;

            private String city;

            private String country;

            private String colorCode;

            private String userCompany;

            private String userDesignation;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
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

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getCity() {
                return city;
            }

            public void setCity(String city) {
                this.city = city;
            }

            public String getCountry() {
                return country;
            }

            public void setCountry(String country) {
                this.country = country;
            }

            public String getUserCompany() {
                return userCompany;
            }

            public void setUserCompany(String userCompany) {
                this.userCompany = userCompany;
            }

            public String getUserDesignation() {
                return userDesignation;
            }

            public void setUserDesignation(String userDesignation) {
                this.userDesignation = userDesignation;
            }

            public String getColorCode() {
                return colorCode;
            }

            public void setColorCode(String colorCode) {
                this.colorCode = colorCode;
            }

            public Users(String id, String name){
                this.setId(id);
                this.setName(name);
            }
        }
    }
}
