package com.primasolutions.idp.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationInfoSaver {

    @Autowired
    protected AuthInfoDAO authInfoDAO;

    @Autowired
    protected AuthInfoByLoginDAO authInfoByLoginDAO;

    public void create(final AuthInfo auth) {
        final AuthInfo authInfo = authInfoDAO.saveAndFlush(auth);
        authInfoByLoginDAO.saveAndFlush(AuthInfoByLogin.newInstance()
                .setAuthInfoId(authInfo.getId())
                .setLogin(authInfo.getLogin()));
    }
}
