package com.primasolutions.idp.application;

import com.primasolutions.idp.authentication.AuthenticationInfo;
import com.primasolutions.idp.authentication.Profile;
import com.primasolutions.idp.constants.AspectType;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "APPLICATION")
public class Application  implements Serializable {

    public static final int ID_INIT_VALUE = 9999;
    public static final int ID_INC_STEP = 1;


    @Id
    @Column(name = "ID", nullable = false)
    @TableGenerator(name = "APPLICATION_ID", table = "IDS_SEQUENCES", pkColumnName = "SEQUENCENAME",
            valueColumnName = "SEQUENCEVALUE", pkColumnValue = "APPLICATION_ID",
            initialValue = ID_INIT_VALUE, allocationSize = ID_INC_STEP)
    @GeneratedValue(generator = "APPLICATION_ID", strategy = GenerationType.TABLE)
    private long id;


    @Column(name = "APP_NAME", nullable = false, unique = true)
    private String appname;

    @Column(name = "TITLE", nullable = false)
    private String title;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "TYPE")
    private ApplicationType type;

    @ElementCollection(targetClass = AspectType.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "APPLICATION_ASPECT", joinColumns = @JoinColumn(name = "APPLICATION_ID"))
    @Column(name = "ASPECT_CODE")
    private Set<AspectType> aspects;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "AUTHENTICATION_INFO_ID")
    private AuthenticationInfo authenticationInfo;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "APPLICATION_PROFILE", joinColumns = @JoinColumn(name = "APPLICATION_ID"),
            inverseJoinColumns = @JoinColumn(name = "PROFILE_CODE"))
    private Set<Profile> profiles;

    public static Application newInstance() {
        return new Application();
    }

    public long getId() {
        return id;
    }

    public Application setId(final long id) {
        this.id = id;
        return this;
    }

    public String getAppname() {
        return appname;
    }

    public Application setAppname(final String appname) {
        this.appname = appname;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Application setTitle(final String title) {
        this.title = title;
        return this;
    }

    public ApplicationType getType() {
        return type;
    }

    public Application setType(final ApplicationType type) {
        this.type = type;
        return this;
    }

    public Set<AspectType> getAspects() {
        return aspects;
    }

    public Application setAspects(final Set<AspectType> aspects) {
        this.aspects = aspects;
        return this;
    }

    public AuthenticationInfo getAuthenticationInfo() {
        return authenticationInfo;
    }

    public Application setAuthenticationInfo(final AuthenticationInfo authenticationInfo) {
        this.authenticationInfo = authenticationInfo;
        return this;
    }

    public Set<Profile> getProfiles() {
        return profiles;
    }

    public Application setProfiles(final Set<Profile> profiles) {
        this.profiles = profiles;
        return this;
    }
}
