package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by ms3 on 10/7/2017.
 */

public class Exchange {

    @SerializedName("status")
    private String status;

    @SerializedName("error")
    private boolean errorStatus;

    private String londonTime;

    private String shanghaiTime;

    private String newYorkTime;

    @SerializedName("lmeData")
    private List<LMEData> lmeData;

    @SerializedName("shanghaiData")
    private List<ShanghaiData> shanghaiData;

    @SerializedName("comexData")
    private List<ComexData> comexData;


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

    public List<LMEData> getLmeData() {
        return lmeData;
    }

    public void setLmeData(List<LMEData> lmeData) {
        this.lmeData = lmeData;
    }

    public List<ShanghaiData> getShanghaiData() {
        return shanghaiData;
    }

    public void setShanghaiData(List<ShanghaiData> shanghaiData) {
        this.shanghaiData = shanghaiData;
    }

    public List<ComexData> getComexData() {
        return comexData;
    }

    public void setComexData(List<ComexData> comexData) {
        this.comexData = comexData;
    }

    public String getLondonTime() {
        return londonTime;
    }

    public void setLondonTime(String londonTime) {
        this.londonTime = londonTime;
    }

    public String getShanghaiTime() {
        return shanghaiTime;
    }

    public void setShanghaiTime(String shanghaiTime) {
        this.shanghaiTime = shanghaiTime;
    }

    public String getNewYorkTime() {
        return newYorkTime;
    }

    public void setNewYorkTime(String newYorkTime) {
        this.newYorkTime = newYorkTime;
    }

    public class ShanghaiData {

        private String title;
        private String month;
        private String last;
        private String change;
        private String symbol;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getMonth() {
            return month;
        }

        public void setMonth(String month) {
            this.month = month;
        }

        public String getLast() {
            return last;
        }

        public void setLast(String last) {
            this.last = last;
        }

        public String getChange() {
            return change;
        }

        public void setChange(String change) {
            this.change = change;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }
    }

    public class ComexData {
        private String title;
        private String month;
        private String last;
        private String change;
        private String symbol;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getMonth() {
            return month;
        }

        public void setMonth(String month) {
            this.month = month;
        }

        public String getLast() {
            return last;
        }

        public void setLast(String last) {
            this.last = last;
        }

        public String getChange() {
            return change;
        }

        public void setChange(String change) {
            this.change = change;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }
    }

    public class LMEData {

        private String title;
        private String contract;
        private String last;
        private String change;
        private String symbol;


        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }


        public String getLast() {
            return last;
        }

        public void setLast(String last) {
            this.last = last;
        }

        public String getChange() {
            return change;
        }

        public void setChange(String change) {
            this.change = change;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getContract() {
            return contract;
        }

        public void setContract(String contract) {
            this.contract = contract;
        }
    }
}
