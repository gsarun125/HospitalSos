package com.ka.hospitalsos;

public class Agency {

    private String id;
    private String serviceName;
    private String contact;
    private String address;
    private long numAmbulances;

    public Agency(String id, String serviceName, String contact, String address, long numAmbulances) {
        this.id = id;
        this.serviceName = serviceName;
        this.contact = contact;
        this.address = address;
        this.numAmbulances = numAmbulances;
    }

    public String getId() {
        return id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getContact() {
        return contact;
    }

    public String getAddress() {
        return address;
    }

    public long getNumAmbulances() {
        return numAmbulances;
    }
}
