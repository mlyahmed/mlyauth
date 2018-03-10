package com.mlyauth.beans;

import com.mlyauth.constants.AspectType;

import java.util.LinkedHashMap;
import java.util.Map;

public class ApplicationBean {

    private long id;
    private String appname;
    private String title;
    private AspectType authAspect;
    private Map<String, AttributeBean> authSettings = new LinkedHashMap<>();


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAppname() {
        return appname;
    }

    public void setAppname(String appname) {
        this.appname = appname;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public AspectType getAuthAspect() {
        return authAspect;
    }

    public void setAuthAspect(AspectType authAspect) {
        this.authAspect = authAspect;
    }

    public Map<String, AttributeBean> getAuthSettings() {
        return authSettings;
    }

    public void setAuthSettings(Map<String, AttributeBean> authSettings) {
        this.authSettings = authSettings;
    }
}
