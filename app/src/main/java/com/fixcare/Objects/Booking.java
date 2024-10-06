package com.fixcare.Objects;

public class Booking {
    String uid;
    String vehicleClass;
    String model;
    String plateNumber;
    double latitude;
    double longitude;
    long schedule;
    String selectedServices;
    String customerUid;
    String workshopUid;
    int status;

    public Booking() {
    }

    public Booking(String uid, String vehicleClass, String model, String plateNumber, double latitude, double longitude, long schedule, String selectedServices, String customerUid, String workshopUid, int status) {
        this.uid = uid;
        this.vehicleClass = vehicleClass;
        this.model = model;
        this.plateNumber = plateNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.schedule = schedule;
        this.selectedServices = selectedServices;
        this.customerUid = customerUid;
        this.workshopUid = workshopUid;
        this.status = status;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getVehicleClass() {
        return vehicleClass;
    }

    public void setVehicleClass(String vehicleClass) {
        this.vehicleClass = vehicleClass;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getSchedule() {
        return schedule;
    }

    public void setSchedule(long schedule) {
        this.schedule = schedule;
    }

    public String getSelectedServices() {
        return selectedServices;
    }

    public void setSelectedServices(String selectedServices) {
        this.selectedServices = selectedServices;
    }

    public String getCustomerUid() {
        return customerUid;
    }

    public void setCustomerUid(String customerUid) {
        this.customerUid = customerUid;
    }

    public String getWorkshopUid() {
        return workshopUid;
    }

    public void setWorkshopUid(String workshopUid) {
        this.workshopUid = workshopUid;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
