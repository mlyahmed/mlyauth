package com.mlyauth.domain;

import com.mlyauth.constants.ProfileCode;

import javax.persistence.*;

@Entity
@Table(name = "PROFILE")
public class Profile {


    @Id
    @Column(name = "CODE", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProfileCode code;

    @Column(name = "DESCRIPTION")
    private String description;


    public ProfileCode getCode() {
        return code;
    }

    public void setCode(ProfileCode code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
