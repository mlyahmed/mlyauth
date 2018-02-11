package com.mlyauth.domain;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "APPLICATION_ASPECT_ATTR")
public class ApplicationAspectAttribute {

    @EmbeddedId
    private ApplicationAspectAttributeId id;

    @Column(name = "ATTRIBUTE_VALUE", nullable = false, unique = true)
    private String value;

    public static ApplicationAspectAttribute newInstance() {
        return new ApplicationAspectAttribute();
    }

    public ApplicationAspectAttributeId getId() {
        return id;
    }

    public ApplicationAspectAttribute setId(ApplicationAspectAttributeId id) {
        this.id = id;
        return this;
    }

    public String getValue() {
        return value;
    }

    public ApplicationAspectAttribute setValue(String value) {
        this.value = value;
        return this;
    }
}


