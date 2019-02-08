package com.hohou.federation.idp.application;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ApplicationAspectAttributeId implements Serializable {

    @Column(name = "APPLICATION_ID")
    private long applicationId;

    @Column(name = "ASPECT_CODE")
    private String aspectCode;

    @Column(name = "ATTRIBUTE_CODE")
    private String attributeCode;

    public static ApplicationAspectAttributeId newInstance() {
        return new ApplicationAspectAttributeId();
    }

    public long getApplicationId() {
        return applicationId;
    }

    public ApplicationAspectAttributeId setApplicationId(final long applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    public String getAspectCode() {
        return aspectCode;
    }

    public ApplicationAspectAttributeId setAspectCode(final String aspectCode) {
        this.aspectCode = aspectCode;
        return this;
    }

    public String getAttributeCode() {
        return attributeCode;
    }

    public ApplicationAspectAttributeId setAttributeCode(final String attributeCode) {
        this.attributeCode = attributeCode;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationAspectAttributeId that = (ApplicationAspectAttributeId) o;
        return applicationId == that.applicationId
                && Objects.equals(aspectCode, that.aspectCode)
                && Objects.equals(attributeCode, that.attributeCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationId, aspectCode, attributeCode);
    }
}
