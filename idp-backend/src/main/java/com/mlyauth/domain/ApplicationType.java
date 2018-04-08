package com.mlyauth.domain;

import com.mlyauth.constants.ApplicationTypeCode;

import javax.persistence.*;

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

    public ApplicationType setCode(ApplicationTypeCode code) {
        this.code = code;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ApplicationType setDescription(String description) {
        this.description = description;
        return this;
    }

}
