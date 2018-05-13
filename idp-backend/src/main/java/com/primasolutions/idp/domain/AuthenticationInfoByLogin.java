package com.primasolutions.idp.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "AUTHENTICATION_INFO_BY_LOGIN")
public class AuthenticationInfoByLogin {

    public static final int ID_INIT_VALUE = 9999;
    public static final int ID_INC_STEP = 1;


    @Id
    @Column(name = "ID", nullable = false)
    @TableGenerator(name = "AUTHENTICATION_INFO_BY_LOGIN_ID", table = "IDS_SEQUENCES", pkColumnName = "SEQUENCENAME",
            valueColumnName = "SEQUENCEVALUE", pkColumnValue = "AUTHENTICATION_INFO_BY_LOGIN_ID",
            initialValue = ID_INIT_VALUE, allocationSize = ID_INC_STEP)
    @GeneratedValue(generator = "AUTHENTICATION_INFO_BY_LOGIN_ID", strategy = GenerationType.TABLE)
    private long id;


    @Column(name = "AUTHENTICATION_INFO_ID", nullable = false)
    private long authInfoId;

    @Column(name = "LOGIN", nullable = false)
    private String login;

    public static AuthenticationInfoByLogin newInstance() {
        return new AuthenticationInfoByLogin();
    }

    public long getId() {
        return id;
    }

    public AuthenticationInfoByLogin setId(final long id) {
        this.id = id;
        return this;
    }

    public long getAuthInfoId() {
        return authInfoId;
    }

    public AuthenticationInfoByLogin setAuthInfoId(final long authInfoId) {
        this.authInfoId = authInfoId;
        return this;
    }

    public String getLogin() {
        return login;
    }

    public AuthenticationInfoByLogin setLogin(final String login) {
        this.login = login;
        return this;
    }
}
