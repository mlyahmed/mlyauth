package com.mlyauth.domain;

import com.mlyauth.constants.NavigationDirection;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "NAVIGATION")
public class Navigation {

    @Id
    @Column(name = "ID", nullable = false)
    @TableGenerator(name = "NAVIGATION_ID", table = "IDS_SEQUENCES", pkColumnName = "SEQUENCENAME", pkColumnValue = "NAVIGATION_ID", valueColumnName = "SEQUENCEVALUE", initialValue = 9999, allocationSize = 1)
    @GeneratedValue(generator = "NAVIGATION_ID", strategy = GenerationType.TABLE)
    private long id;

    @Column(name = "TARGET_URL", nullable = false)
    private String targetURL;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "DIRECTION", nullable = false)
    @Enumerated(EnumType.STRING)
    private NavigationDirection direction;

    @Column(name = "TIME_CONSUMED", nullable = false)
    private long timeConsumed;

    @OneToMany(mappedBy = "navigation", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<NavigationAttribute> attributes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "AUTHENTICATION_SESSION_ID", nullable = false, updatable = false)
    private AuthenticationSession session;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "TOKEN_ID", nullable = false, updatable = false)
    private Token token;

    public static Navigation newInstance() {
        return new Navigation();
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTargetURL() {
        return targetURL;
    }

    public Navigation setTargetURL(String targetURL) {
        this.targetURL = targetURL;
        return this;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Navigation setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public NavigationDirection getDirection() {
        return direction;
    }

    public Navigation setDirection(NavigationDirection direction) {
        this.direction = direction;
        return this;
    }

    public long getTimeConsumed() {
        return timeConsumed;
    }

    public Navigation setTimeConsumed(long timeConsumed) {
        this.timeConsumed = timeConsumed;
        return this;
    }

    public Set<NavigationAttribute> getAttributes() {
        return attributes;
    }

    public Navigation setAttributes(Set<NavigationAttribute> attributes) {
        attributes.stream().forEach(att -> att.setNavigation(this));
        this.attributes = attributes;
        return this;
    }

    public AuthenticationSession getSession() {
        return session;
    }

    public Navigation setSession(AuthenticationSession session) {
        this.session = session;
        return this;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }
}
