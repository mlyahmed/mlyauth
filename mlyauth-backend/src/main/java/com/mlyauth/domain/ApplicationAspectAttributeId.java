package com.mlyauth.domain;

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

    public long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(long applicationId) {
        this.applicationId = applicationId;
    }

    public String getAspectCode() {
        return aspectCode;
    }

    public void setAspectCode(String aspectCode) {
        this.aspectCode = aspectCode;
    }

    public String getAttributeCode() {
        return attributeCode;
    }

    public void setAttributeCode(String attributeCode) {
        this.attributeCode = attributeCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationAspectAttributeId that = (ApplicationAspectAttributeId) o;
        return applicationId == that.applicationId &&
                Objects.equals(aspectCode, that.aspectCode) &&
                Objects.equals(attributeCode, that.attributeCode);
    }

    @Override
    public int hashCode() {

        return Objects.hash(applicationId, aspectCode, attributeCode);
    }
}
