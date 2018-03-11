package com.mlyauth.domain;

import com.mlyauth.constants.TokenNorm;
import com.mlyauth.constants.TokenType;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "TOKEN")
public class Token {

    @Id
    @Column(name = "ID", nullable = false)
    @TableGenerator(name = "TOKEN_ID", table = "IDS_SEQUENCES", pkColumnName = "SEQUENCENAME", pkColumnValue = "TOKEN_ID", valueColumnName = "SEQUENCEVALUE", initialValue = 9999, allocationSize = 1)
    @GeneratedValue(generator = "TOKEN_ID", strategy = GenerationType.TABLE)
    private long id;


    @Column(name = "STAMP", nullable = false, unique = true)
    private String stamp;

    @Column(name = "TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private TokenType type;

    @Column(name = "NORM", nullable = false)
    @Enumerated(EnumType.STRING)
    private TokenNorm norm;

    @Column(name = "ISSUANCE_TIME", nullable = false)
    private Date issuanceTime;

    @Column(name = "EFFECTIVE_TIME", nullable = false)
    private Date effectiveTime;

    @Column(name = "EXPIRY_TIME", nullable = false)
    private Date expiryTime;

    @OneToMany(mappedBy = "token", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<TokenClaim> claims;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "APPLICATION_ID")
    private Application application;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStamp() {
        return stamp;
    }

    public void setStamp(String stamp) {
        this.stamp = stamp;
    }

    public TokenType getType() {
        return type;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    public TokenNorm getNorm() {
        return norm;
    }

    public void setNorm(TokenNorm norm) {
        this.norm = norm;
    }

    public Date getIssuanceTime() {
        return issuanceTime;
    }

    public void setIssuanceTime(Date issuanceTime) {
        this.issuanceTime = issuanceTime;
    }

    public Date getEffectiveTime() {
        return effectiveTime;
    }

    public void setEffectiveTime(Date effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    public Date getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(Date expiryTime) {
        this.expiryTime = expiryTime;
    }

    public Set<TokenClaim> getClaims() {
        return claims;
    }

    public void setClaims(Set<TokenClaim> claims) {
        this.claims = claims;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }
}
