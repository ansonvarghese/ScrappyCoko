package com.myscrap.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ms3 on 6/14/2017.
 */

public class Report  {

    @SerializedName("status")
    private String status;
    @SerializedName("error")
    private boolean errorStatus;

    @SerializedName("reportData")
    private ReportData reportData;

    public ReportData getReportData() {
        return reportData;
    }

    public void setReportData(ReportData reportData) {
        this.reportData = reportData;
    }

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

    public class ReportData {
     private boolean isReported;

        private String reportedUserId;

        private String reportId;

        public boolean isReported() {
            return isReported;
        }

        public void setReported(boolean reported) {
            isReported = reported;
        }

        public String getReportedUserId() {
            return reportedUserId;
        }

        public void setReportedUserId(String reportedUserId) {
            this.reportedUserId = reportedUserId;
        }

        public String getReportId() {
            return reportId;
        }

        public void setReportId(String reportId) {
            this.reportId = reportId;
        }
    }
}
