package com.example.carli.mychildtrackerdisplay.Model;

import java.util.Map;

public class UserEntry {
    private String usertype;
    private String partner_id;
    private Map<Integer, Location> locationList;
    private Integer interval;
    private String security_check;

    public UserEntry() {

    }

    public String getUsertype() {
        return usertype;
    }

    public void setUsertype(String usertype) {
        this.usertype = usertype;
    }

    public String getPartner_id() {
        return partner_id;
    }

    public void setPartner_id(String partner_id) {
        this.partner_id = partner_id;
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

    public String getSecurity_check() {
        return security_check;
    }

    public void setSecurity_check(String security_check) {
        this.security_check = security_check;
    }

    public UserEntry(String usertype, String partner_id, Map<Integer, Location> locationList, Integer interval, String security_check) {
        this.usertype = usertype;
        this.partner_id = partner_id;
        this.locationList = locationList;
        this.interval = interval;
        this.security_check = security_check;
    }
}
