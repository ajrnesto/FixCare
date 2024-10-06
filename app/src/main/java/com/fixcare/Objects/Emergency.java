package com.fixcare.Objects;

public class Emergency {
    String customerUid;
    String vehicleClass;
    String model;
    String plateNumber;
    double latitude;
    double longitude;
    String selectedServices;

    public Emergency() {
    }

    public Emergency(String customerUid, String vehicleClass, String model, String plateNumber, double latitude, double longitude, String selectedServices) {
        this.customerUid = customerUid;
        this.vehicleClass = vehicleClass;
        this.model = model;
        this.plateNumber = plateNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.selectedServices = selectedServices;
    }

    public String getCustomerUid() {
        return customerUid;
    }

    public void setCustomerUid(String customerUid) {
        this.customerUid = customerUid;
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

    public String getSelectedServices() {
        return selectedServices;
    }

    public void setSelectedServices(String selectedServices) {
        this.selectedServices = selectedServices;
    }
}
