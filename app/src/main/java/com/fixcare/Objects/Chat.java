package com.fixcare.Objects;

public class Chat {
    String uid;
    String message;
    String authorUid;
    long timestamp;

    public Chat() {
    }

    public Chat(String uid, String message, String authorUid, long timestamp) {
        this.uid = uid;
        this.message = message;
        this.authorUid = authorUid;
        this.timestamp = timestamp;
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
}
