package com.fixcare.Dialogs;

public class BookingReference {
    String uid;
    String workshopUid;

    public BookingReference() {
    }

    public BookingReference(String uid, String workshopUid) {
        this.uid = uid;
        this.workshopUid = workshopUid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getWorkshopUid() {
        return workshopUid;
    }

    public void setWorkshopUid(String workshopUid) {
        this.workshopUid = workshopUid;
    }
}
