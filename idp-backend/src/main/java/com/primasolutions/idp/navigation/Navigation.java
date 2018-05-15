package com.primasolutions.idp.navigation;

import com.primasolutions.idp.constants.Direction;
import com.primasolutions.idp.domain.AuthenticationSession;
import com.primasolutions.idp.token.Token;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "NAVIGATION")
public class Navigation {

    public static final int ID_INIT_VALUE = 9999;
    public static final int ID_INC_STEP = 1;

    @Id
    @Column(name = "ID", nullable = false)
    @TableGenerator(name = "NAVIGATION_ID", table = "IDS_SEQUENCES", pkColumnName = "SEQUENCENAME",
            pkColumnValue = "NAVIGATION_ID", valueColumnName = "SEQUENCEVALUE",
            initialValue = ID_INIT_VALUE, allocationSize = ID_INC_STEP)
    @GeneratedValue(generator = "NAVIGATION_ID", strategy = GenerationType.TABLE)
    private long id;

    @Column(name = "TARGET_URL", nullable = false)
    private String targetURL;

    @Column(name = "CREATED_AT", nullable = false)
    private Date createdAt;

    @Column(name = "DIRECTION", nullable = false)
    @Enumerated(EnumType.STRING)
    private Direction direction;

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

    public void setId(final long id) {
        this.id = id;
    }

    public String getTargetURL() {
        return targetURL;
    }

    public Navigation setTargetURL(final String targetURL) {
        this.targetURL = targetURL;
        return this;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Navigation setCreatedAt(final Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Direction getDirection() {
        return direction;
    }

    public Navigation setDirection(final Direction direction) {
        this.direction = direction;
        return this;
    }

    public long getTimeConsumed() {
        return timeConsumed;
    }

    public Navigation setTimeConsumed(final long timeConsumed) {
        this.timeConsumed = timeConsumed;
        return this;
    }

    public Set<NavigationAttribute> getAttributes() {
        return attributes;
    }

    public Navigation setAttributes(final Set<NavigationAttribute> attributes) {
        attributes.stream().forEach(att -> att.setNavigation(this));
        this.attributes = attributes;
        return this;
    }

    @Transient
    public NavigationAttribute getAttribute(final String code) {
        return this.getAttributes().stream().filter(att -> att.getCode().equals(code)).findFirst().orElse(null);
    }

    public AuthenticationSession getSession() {
        return session;
    }

    public Navigation setSession(final AuthenticationSession session) {
        this.session = session;
        return this;
    }

    public Token getToken() {
        return token;
    }

    public Navigation setToken(final Token token) {
        this.token = token;
        return this;
    }
}
