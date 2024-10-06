package com.fixcare.Objects;

public class ChatReference {
    String uid;
    String message;
    String authorUid;
    long timestamp;
    String contactUid;

    public ChatReference() {
    }

    public ChatReference(String uid, String message, String authorUid, long timestamp, String contactUid) {
        this.uid = uid;
        this.message = message;
        this.authorUid = authorUid;
        this.timestamp = timestamp;
        this.contactUid = contactUid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuthorUid() {
        return authorUid;
    }

    public void setAuthorUid(String authorUid) {
        this.authorUid = authorUid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getContactUid() {
        return contactUid;
    }

    public void setContactUid(String contactUid) {
        this.contactUid = contactUid;
    }
}
