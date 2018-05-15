package com.primasolutions.idp.token;

import com.primasolutions.idp.application.Application;
import com.primasolutions.idp.authentication.AuthenticationSession;
import com.primasolutions.idp.constants.TokenNorm;
import com.primasolutions.idp.constants.TokenPurpose;
import com.primasolutions.idp.constants.TokenRefreshMode;
import com.primasolutions.idp.constants.TokenStatus;
import com.primasolutions.idp.constants.TokenType;
import com.primasolutions.idp.constants.TokenValidationMode;

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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "TOKEN")
public class Token {

    public static final int ID_INIT_VALUE = 9999;
    public static final int ID_INC_STEP = 1;

    @Id
    @Column(name = "ID", nullable = false)
    @TableGenerator(name = "TOKEN_ID", table = "IDS_SEQUENCES", pkColumnName = "SEQUENCENAME",
            pkColumnValue = "TOKEN_ID", valueColumnName = "SEQUENCEVALUE",
            initialValue = ID_INIT_VALUE, allocationSize = ID_INC_STEP)
    @GeneratedValue(generator = "TOKEN_ID", strategy = GenerationType.TABLE)
    private long id;

    @Column(name = "VALIDATION_MODE", nullable = false)
    @Enumerated(EnumType.STRING)
    private TokenValidationMode validationMode;

    @Column(name = "REFRESH_MODE", nullable = false)
    @Enumerated(EnumType.STRING)
    private TokenRefreshMode refreshMode;

    @Column(name = "STAMP", nullable = false, unique = true)
    private String stamp;

    @Column(name = "CHECKSUM", nullable = false, unique = true)
    private String checksum;

    @Column(name = "TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private TokenType type;

    @Column(name = "NORM", nullable = false)
    @Enumerated(EnumType.STRING)
    private TokenNorm norm;

    @Column(name = "PURPOSE", nullable = false)
    @Enumerated(EnumType.STRING)
    private TokenPurpose purpose;

    @Column(name = "ISSUANCE_TIME", nullable = false)
    private Date issuanceTime;

    @Column(name = "EFFECTIVE_TIME", nullable = false)
    private Date effectiveTime;

    @Column(name = "EXPIRY_TIME", nullable = false)
    private Date expiryTime;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private TokenStatus status;

    @OneToMany(mappedBy = "token", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<TokenClaim> claims;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "APPLICATION_ID")
    private Application application;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "AUTHENTICATION_SESSION_ID", nullable = false, updatable = false)
    private AuthenticationSession session;

    public static Token newInstance() {
        return new Token();
    }

    public long getId() {
        return id;
    }

    public Token setId(final long id) {
        this.id = id;
        return this;
    }

    public TokenValidationMode getValidationMode() {
        return validationMode;
    }

    public Token setValidationMode(final TokenValidationMode validationMode) {
        this.validationMode = validationMode;
        return this;
    }

    public TokenRefreshMode getRefreshMode() {
        return refreshMode;
    }

    public Token setRefreshMode(final TokenRefreshMode refreshMode) {
        this.refreshMode = refreshMode;
        return this;
    }

    public String getStamp() {
        return stamp;
    }

    public Token setStamp(final String stamp) {
        this.stamp = stamp;
        return this;
    }

    public String getChecksum() {
        return checksum;
    }

    public Token setChecksum(final String checksum) {
        this.checksum = checksum;
        return this;
    }

    public TokenType getType() {
        return type;
    }

    public Token setType(final TokenType type) {
        this.type = type;
        return this;
    }

    public TokenNorm getNorm() {
        return norm;
    }

    public Token setNorm(final TokenNorm norm) {
        this.norm = norm;
        return this;
    }

    public TokenPurpose getPurpose() {
        return purpose;
    }

    public Token setPurpose(final TokenPurpose purpose) {
        this.purpose = purpose;
        return this;
    }

    public Date getIssuanceTime() {
        return issuanceTime;
    }

    public Token setIssuanceTime(final Date issuanceTime) {
        this.issuanceTime = issuanceTime;
        return this;
    }

    public Date getEffectiveTime() {
        return effectiveTime;
    }

    public Token setEffectiveTime(final Date effectiveTime) {
        this.effectiveTime = effectiveTime;
        return this;
    }

    public Date getExpiryTime() {
        return expiryTime;
    }

    public Token setExpiryTime(final Date expiryTime) {
        this.expiryTime = expiryTime;
        return this;
    }

    public TokenStatus getStatus() {
        return status;
    }

    public Token setStatus(final TokenStatus status) {
        this.status = status;
        return this;
    }

    public Set<TokenClaim> getClaims() {
        return claims;
    }

    @Transient
    public Map<String, TokenClaim> getClaimsMap() {
        return this.claims.stream().collect(Collectors.toMap(c -> c.getCode(), c -> c));
    }

    public Token setClaims(final Set<TokenClaim> claims) {
        claims.stream().forEach(claim -> claim.setToken(this));
        this.claims = claims;
        return this;
    }

    public Application getApplication() {
        return application;
    }

    public Token setApplication(final Application application) {
        this.application = application;
        return this;
    }

    public AuthenticationSession getSession() {
        return session;
    }

    public Token setSession(final AuthenticationSession session) {
        this.session = session;
        return this;
    }
}
