package com.mlyauth.domain;

import javax.persistence.*;

@Entity
@Table(name = "APPLICATION_ASPECT_ATTR")
public class ApplicationAspectAttribute {

    @EmbeddedId
    private ApplicationAspectAttributeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APPLICATION_ID", insertable = false, updatable = false)
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ASPECT_CODE", insertable = false, updatable = false)
    private AuthAspect aspect;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ATTRIBUTE_CODE", insertable = false, updatable = false)
    private Attribute attribute;

    private String value;


    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public AuthAspect getAspect() {
        return aspect;
    }

    public void setAspect(AuthAspect aspect) {
        this.aspect = aspect;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}


