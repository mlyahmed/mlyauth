package com.mlyauth.domain;

import com.mlyauth.constants.AuthAspectType;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="APPLICATION")
public class Application  implements Serializable {

    @Id
    @Column(name = "ID", nullable = false)
    @TableGenerator(name = "APPLICATION_ID", table = "IDS_SEQUENCES", pkColumnName = "SEQUENCENAME", valueColumnName = "SEQUENCEVALUE", pkColumnValue = "APPLICATION_ID", initialValue = 9999, allocationSize=1)
    @GeneratedValue(generator = "APPLICATION_ID", strategy = GenerationType.TABLE)
    private long id;


    @Column(name = "APP_NAME", nullable = false, unique = true)
    private String appname;

    @Column(name = "TITLE", nullable = false)
    private String title;

    @Column(name = "AUTH_ASPECT", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthAspectType authAspect;


    public static Application newInstance(){
        return new Application();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAppname() {
        return appname;
    }

    public Application setAppname(String appname) {
        this.appname = appname;
        return this;
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
