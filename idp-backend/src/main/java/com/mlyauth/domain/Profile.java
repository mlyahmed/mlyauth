package com.mlyauth.domain;

import com.mlyauth.constants.ProfileCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "PROFILE")
public class Profile {


    @Id
    @Column(name = "CODE", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProfileCode code;

    @Column(name = "DESCRIPTION")
    private String description;

    public static Profile newInstance() {
        return new Profile();
    }

    public ProfileCode getCode() {
        return code;
    }

    public Profile setCode(ProfileCode code) {
        this.code = code;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
