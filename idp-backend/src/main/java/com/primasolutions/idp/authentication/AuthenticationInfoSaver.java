package com.primasolutions.idp.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationInfoSaver {

    @Autowired
    protected AuthenticationInfoDAO authenticationInfoDAO;

    @Autowired
    protected AuthenticationInfoByLoginDAO authInfoByLoginDAO;

    public void create(final AuthenticationInfo auth) {
        final AuthenticationInfo authInfo = authenticationInfoDAO.saveAndFlush(auth);
        authInfoByLoginDAO.saveAndFlush(AuthenticationInfoByLogin.newInstance()
                .setAuthInfoId(authInfo.getId())
                .setLogin(authInfo.getLogin()));
    }
}