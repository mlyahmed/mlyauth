package com.primasolutions.idp.application;

import com.primasolutions.idp.constants.AspectAttribute;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "APPLICATION_ASPECT_ATTR")
public class AppAspAttr {

    @EmbeddedId
    private ApplicationAspectAttributeId id;

    @Column(name = "ATTRIBUTE_VALUE", nullable = false, unique = true)
    @Lob
    private String value;

    public static AppAspAttr newInstance() {
        return new AppAspAttr();
    }

    public ApplicationAspectAttributeId getId() {
        return id;
    }

    public AppAspAttr setId(final ApplicationAspectAttributeId id) {
        this.id = id;
        return this;
    }

    public String getValue() {
        return value;
    }

    public AppAspAttr setValue(final String value) {
        this.value = value;
        return this;
    }

    @Transient
    public AspectAttribute getAttributeCode() {
        return AspectAttribute.create(this.getId().getAttributeCode());
    }

}


