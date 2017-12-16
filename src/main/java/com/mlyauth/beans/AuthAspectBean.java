package com.mlyauth.beans;

import com.mlyauth.constants.AuthAspectType;

import java.util.Collection;
import java.util.HashSet;

public class AuthAspectBean extends AuthEntity{

    private AuthAspectType type;
    private Collection<AttributeBean> settings = new HashSet<>();


    public AuthAspectType getType() {
        return type;
    }

    public void setType(AuthAspectType type) {
        this.type = type;
    }

    public Collection<AttributeBean> getSettings() {
        return settings;
    }

    public void setSettings(Collection<AttributeBean> settings) {
        this.settings = settings;
    }
}
