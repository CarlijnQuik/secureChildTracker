package com.example.carli.mychildtrackerdisplay.Model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;

@IgnoreExtraProperties
public class UserEntry {
    private String userType;
    private String partnerID;
    private Map<Integer, Location> locationList;
    private Map<Integer, String> data;
    private Integer interval;
    private String securityCheck;


    private boolean sos;

    public UserEntry() {

    }


    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getPartnerID() {
        return partnerID;
    }

    public void setPartnerID(String partnerID) {
        this.partnerID = partnerID;
    }

    public Map<Integer, Location> getLocationList() {
        return locationList;
    }

    public void setLocationList(Map<Integer, Location> locationList) {
        this.locationList = locationList;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public String getSecurityCheck() {
        return securityCheck;
    }

    public void setSecurityCheck(String securityCheck) {
        this.securityCheck = securityCheck;
    }

    public Map<Integer, String> getData() {
        return data;
    }

    public void setData(Map<Integer, String> data) {
        this.data = data;
    }


    public boolean isSos() {
        return sos;
    }

    public void setSos(boolean sos) {
        this.sos = sos;
    }

    public UserEntry(String userType, String partnerID, Map<Integer, Location> locationList, Map<Integer, String> data, Integer interval, String securityCheck, boolean sos) {
        this.userType = userType;
        this.partnerID = partnerID;
        this.locationList = locationList;
        this.data = data;
        this.interval = interval;
        this.securityCheck = securityCheck;
        this.sos = sos;
    }
}
