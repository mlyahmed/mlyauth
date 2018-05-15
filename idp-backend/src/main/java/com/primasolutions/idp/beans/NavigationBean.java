package com.primasolutions.idp.beans;

import com.primasolutions.idp.application.AttributeBean;

import java.util.Collection;

public class NavigationBean {

    private long id;
    private long tokenId;
    private String target;
    private Collection<AttributeBean> attributes;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public long getTokenId() {
        return tokenId;
    }

    public void setTokenId(final long tokenId) {
        this.tokenId = tokenId;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(final String target) {
        this.target = target;
    }

    public Collection<AttributeBean> getAttributes() {
        return attributes;
    }

    public void setAttributes(final Collection<AttributeBean> attributes) {
        this.attributes = attributes;
    }

    public AttributeBean getAttribute(final String code) {
        return attributes.stream().filter(attr -> attr.getCode().equals(code)).findFirst().orElse(null);
    }
}
