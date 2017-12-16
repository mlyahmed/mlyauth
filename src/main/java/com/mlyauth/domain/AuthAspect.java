package com.mlyauth.domain;

import com.mlyauth.constants.AuthAspectType;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="AUTH_ASPECT")
public class AuthAspect implements Serializable {

    @Id
    @Column(name = "TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthAspectType type;

    @Column(name = "TITLE", nullable = false)
    private String title;

    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    public AuthAspectType getType() {
        return type;
    }

    public void setType(AuthAspectType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
