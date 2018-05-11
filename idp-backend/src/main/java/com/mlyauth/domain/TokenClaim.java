package com.mlyauth.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "TOKEN_CLAIM")
public class TokenClaim {

    public static final int ID_INIT_VALUE = 9999;
    public static final int ID_INC_STEP = 1;

    @Id
    @Column(name = "ID", nullable = false)
    @TableGenerator(name = "TOKEN_ID", table = "IDS_SEQUENCES", pkColumnName = "SEQUENCENAME",
            pkColumnValue = "TOKEN_ID", valueColumnName = "SEQUENCEVALUE",
            initialValue = ID_INIT_VALUE, allocationSize = ID_INC_STEP)
    @GeneratedValue(generator = "TOKEN_ID", strategy = GenerationType.TABLE)
    private long id;

    @Column(name = "CODE", nullable = false)
    private String code;

    @Column(name = "VALUE", nullable = false)
    private String value;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "TOKEN_ID")
    private Token token;

    public static TokenClaim newInstance() {
        return new TokenClaim();
    }

    public long getId() {
        return id;
    }

    public TokenClaim setId(final long id) {
        this.id = id;
        return this;
    }

    public String getCode() {
        return code;
    }

    public TokenClaim setCode(final String code) {
        this.code = code;
        return this;
    }

    public String getValue() {
        return value;
    }

    public TokenClaim setValue(final String value) {
        this.value = value;
        return this;
    }

    public Token getToken() {
        return token;
    }

    public TokenClaim setToken(final Token token) {
        this.token = token;
        return this;
    }
}
