package com.mlyauth.domain;

import javax.persistence.*;

@Entity
@Table(name = "TOKEN_CLAIM")
public class TokenClaim {

    @Id
    @Column(name = "ID", nullable = false)
    @TableGenerator(name = "TOKEN_ID", table = "IDS_SEQUENCES", pkColumnName = "SEQUENCENAME", pkColumnValue = "TOKEN_ID", valueColumnName = "SEQUENCEVALUE", initialValue = 9999, allocationSize = 1)
    @GeneratedValue(generator = "TOKEN_ID", strategy = GenerationType.TABLE)
    private long id;

    @Column(name = "CODE", nullable = false)
    private String code;

    @Column(name = "VALUE", nullable = false)
    private String value;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "TOKEN_ID")
    private Token token;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }
}
