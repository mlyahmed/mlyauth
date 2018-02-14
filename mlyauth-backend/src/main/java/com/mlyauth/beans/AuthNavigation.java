package com.mlyauth.beans;

import java.util.Collection;

public class AuthNavigation {


    private String target;
    private Collection<AttributeBean> attributes;


    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Collection<AttributeBean> getAttributes() {
        return attributes;
    }

    public void setAttributes(Collection<AttributeBean> attributes) {
        this.attributes = attributes;
    }


}
