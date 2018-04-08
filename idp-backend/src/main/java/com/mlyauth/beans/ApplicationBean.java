package com.mlyauth.beans;

import com.mlyauth.constants.ApplicationTypeCode;
import com.mlyauth.constants.AspectType;

import java.util.LinkedHashMap;
import java.util.Map;

public class ApplicationBean {

    private long id;
    private ApplicationTypeCode type;
    private String appname;
    private String title;
    private AspectType authAspect;
    private Map<String, AttributeBean> authSettings = new LinkedHashMap<>();

    public static ApplicationBean newInstance(){
        return new ApplicationBean();
    }

    public long getId() {
        return id;
    }

    public ApplicationBean setId(long id) {
        this.id = id;
        return this;
    }

    public ApplicationTypeCode getType() {
        return type;
    }

    public ApplicationBean setType(ApplicationTypeCode type) {
        this.type = type;
        return this;
    }

    public String getAppname() {
        return appname;
    }

    public ApplicationBean setAppname(String appname) {
        this.appname = appname;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public ApplicationBean setTitle(String title) {
        this.title = title;
        return this;
    }

    public AspectType getAuthAspect() {
        return authAspect;
    }

    public ApplicationBean setAuthAspect(AspectType authAspect) {
        this.authAspect = authAspect;
        return this;
    }

    public Map<String, AttributeBean> getAuthSettings() {
        return authSettings;
    }

    public void setAuthSettings(Map<String, AttributeBean> authSettings) {
        this.authSettings = authSettings;
    }
}
