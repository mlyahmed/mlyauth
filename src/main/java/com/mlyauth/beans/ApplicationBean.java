package com.mlyauth.beans;

import com.mlyauth.constants.AuthAspectType;

import java.util.LinkedHashSet;
import java.util.Set;

public class ApplicationBean {

    private String appname;
    private String title;
    private AuthAspectType authAspect;
    private Set<AttributeMap> authSettings = new LinkedHashSet<>();

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

    public AuthAspectType getAuthAspect() {
        return authAspect;
    }

    public void setAuthAspect(AuthAspectType authAspect) {
        this.authAspect = authAspect;
    }

    public Set<AttributeMap> getAuthSettings() {
        return authSettings;
    }

    public void setAuthSettings(Set<AttributeMap> authSettings) {
        this.authSettings = authSettings;
    }
}
