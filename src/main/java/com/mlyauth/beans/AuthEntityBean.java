package com.mlyauth.beans;

import java.util.HashMap;
import java.util.Map;

public abstract class AuthEntityBean {

    private Map<AttributeBean, String> attributes = new HashMap<>();

    public Map<AttributeBean, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<AttributeBean, String> attributes) {
        this.attributes = attributes;
    }
}
