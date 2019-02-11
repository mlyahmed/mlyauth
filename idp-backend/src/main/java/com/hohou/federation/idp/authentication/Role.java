package com.hohou.federation.idp.authentication;

import com.hohou.federation.idp.constants.RoleCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

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

    public Role setCode(final RoleCode code) {
        this.code = code;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Role setDescription(final String description) {
        this.description = description;
        return this;
    }
}
