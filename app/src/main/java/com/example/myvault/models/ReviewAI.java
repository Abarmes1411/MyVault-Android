package com.example.myvault.models;

import java.io.Serializable;

public class ReviewAI extends DomainEntity implements Serializable {

    private String contentID;
    private String summaryGenerated;
    private String updateTime;


    public ReviewAI() {
    }

    public ReviewAI(String contentID, String summaryGenerated, String updateTime) {
        super();
        this.contentID = contentID;
        this.summaryGenerated = summaryGenerated;
        this.updateTime = updateTime;
    }


    public String getContentID() {
        return contentID;
    }

    public void setContentID(String contentID) {
        this.contentID = contentID;
    }

    public String getSummaryGenerated() {
        return summaryGenerated;
    }

    public void setSummaryGenerated(String summaryGenerated) {
        this.summaryGenerated = summaryGenerated;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
