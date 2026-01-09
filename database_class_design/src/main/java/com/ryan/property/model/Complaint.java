package com.ryan.property.model;

import java.sql.Timestamp;

public class Complaint {
    private long id;
    private long ownerId;
    private String title;
    private String content;
    private String status;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String latestReplyContent;
    private Timestamp latestReplyTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getLatestReplyContent() {
        return latestReplyContent;
    }

    public void setLatestReplyContent(String latestReplyContent) {
        this.latestReplyContent = latestReplyContent;
    }

    public Timestamp getLatestReplyTime() {
        return latestReplyTime;
    }

    public void setLatestReplyTime(Timestamp latestReplyTime) {
        this.latestReplyTime = latestReplyTime;
    }
}
