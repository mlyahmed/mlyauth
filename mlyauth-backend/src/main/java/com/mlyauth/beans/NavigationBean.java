package com.mlyauth.beans;

import java.util.Collection;

public class NavigationBean {


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


    public AttributeBean getAttribute(String code){
        return attributes.stream().filter(attr -> attr.getCode().equals(code)).findFirst().orElse(null);
    }
}
