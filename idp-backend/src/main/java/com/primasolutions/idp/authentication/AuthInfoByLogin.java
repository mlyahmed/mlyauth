package com.primasolutions.idp.authentication;

import com.primasolutions.idp.sensitive.EncryptedDomain;
import com.primasolutions.idp.sensitive.TokenizedDomain;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "AUTHENTICATION_INFO_BY_LOGIN")
public class AuthInfoByLogin implements EncryptedDomain, TokenizedDomain {

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
    @Type(type = ENCRYPTED_LONG)
    private long authInfoId;

    @Column(name = "LOGIN", nullable = false)
    @Type(type = TOKENIZED_LOGIN)
    private String login;

    public static AuthInfoByLogin newInstance() {
        return new AuthInfoByLogin();
    }

    public long getId() {
        return id;
    }

    public AuthInfoByLogin setId(final long id) {
        this.id = id;
        return this;
    }

    public long getAuthInfoId() {
        return authInfoId;
    }

    public AuthInfoByLogin setAuthInfoId(final long authInfoId) {
        this.authInfoId = authInfoId;
        return this;
    }

    public String getLogin() {
        return login;
    }

    public AuthInfoByLogin setLogin(final String login) {
        this.login = login;
        return this;
    }
}
