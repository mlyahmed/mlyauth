package com.mlyauth.domain;

import com.mlyauth.constants.RoleCode;

import javax.persistence.*;

@Entity
@Table(name = "ROLE")
public class Role {

    @Id
    @Column(name = "CODE", nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleCode code;

    @Column(name = "DESCRIPTION")
    private String description;


    public static Role newInstance() {
        return new Role();
    }

    public RoleCode getCode() {
        return code;
    }

    public Role setCode(RoleCode code) {
        this.code = code;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Role setDescription(String description) {
        this.description = description;
        return this;
    }
}
