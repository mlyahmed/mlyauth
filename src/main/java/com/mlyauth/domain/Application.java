package com.mlyauth.domain;

import com.mlyauth.constants.AuthAspectType;

import javax.persistence.*;

@Entity
@Table(name="APPLICATION")
public class Application {

    @Id
    @Column(name = "ID", nullable = false)
    private long id;


    @Column(name = "APP_NAME", nullable = false, unique = true)
    private String appname;

    @Column(name = "TITLE", nullable = false)
    private String title;

    @Column(name = "AUTH_ASPECT", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthAspectType authAspect;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAppname() {
        return appname;
    }

    public void setAppname(String appname) {
        this.appname = appname;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public AuthAspectType getAuthAspect() {
        return authAspect;
    }

    public void setAuthAspect(AuthAspectType authAspect) {
        this.authAspect = authAspect;
    }
}
