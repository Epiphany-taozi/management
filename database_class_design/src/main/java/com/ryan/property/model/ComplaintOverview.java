package com.ryan.property.model;

import java.sql.Timestamp;

public class ComplaintOverview {

    private long id;
    private String status;
    private Timestamp createdAt;
    private Timestamp latestReplyAt;
    private String latestReplyContent;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getLatestReplyAt() {
        return latestReplyAt;
    }

    public void setLatestReplyAt(Timestamp latestReplyAt) {
        this.latestReplyAt = latestReplyAt;
    }

    public String getLatestReplyContent() {
        return latestReplyContent;
    }

    public void setLatestReplyContent(String latestReplyContent) {
        this.latestReplyContent = latestReplyContent;
    }

    public String getLatestReplySummary() {
        if (latestReplyContent == null || latestReplyContent.isBlank()) {
            return "";
        }
        String trimmed = latestReplyContent.trim();
        int maxLen = 40;
        if (trimmed.length() <= maxLen) {
            return trimmed;
        }
        return trimmed.substring(0, maxLen) + "...";
    }
}
