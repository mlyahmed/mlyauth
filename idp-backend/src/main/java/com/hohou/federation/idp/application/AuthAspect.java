package com.hohou.federation.idp.application;

import com.hohou.federation.idp.constants.AspectType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "AUTH_ASPECT")
public class AuthAspect implements Serializable {

    @Id
    @Column(name = "TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private AspectType type;

    @Column(name = "TITLE", nullable = false)
    private String title;

    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    public AspectType getType() {
        return type;
    }

    public void setType(final AspectType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
