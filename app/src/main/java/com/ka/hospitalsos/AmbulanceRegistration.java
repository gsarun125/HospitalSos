package com.ka.hospitalsos;

public class AmbulanceRegistration {

    private String serviceName;
    private String contact;
    private String address;
    private int numAmbulances;
    private String fcmToken;


// Required public no-argument constructor


    public AmbulanceRegistration(String serviceName, String contact, String address, int numAmbulances,   String fcmToken) {
        this.serviceName = serviceName;
        this.contact = contact;
        this.address = address;
        this.numAmbulances = numAmbulances;
        this.fcmToken=fcmToken;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getNumAmbulances() {
        return numAmbulances;
    }

    public void setNumAmbulances(int numAmbulances) {
        this.numAmbulances = numAmbulances;
    }
}
