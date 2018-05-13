package com.primasolutions.idp.domain;

import com.primasolutions.idp.constants.ApplicationTypeCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "APPLICATION_TYPE")
public class ApplicationType {

    @Id
    @Column(name = "CODE", nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplicationTypeCode code;

    @Column(name = "DESCRIPTION")
    private String description;

    public static ApplicationType newInstance() {
        return new ApplicationType();
    }

    public ApplicationTypeCode getCode() {
        return code;
    }

    public ApplicationType setCode(final ApplicationTypeCode code) {
        this.code = code;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ApplicationType setDescription(final String description) {
        this.description = description;
        return this;
    }

}
