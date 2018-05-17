package com.primasolutions.idp.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AuthenticationInfoLookuper {

    @Autowired
    protected AuthenticationInfoDAO authInfoDAO;

    @Autowired
    protected AuthenticationInfoByLoginDAO authInfoByLoginDAO;

    public AuthenticationInfo byLogin(final String login) {
        final Set<AuthenticationInfoByLogin> byLogin = authInfoByLoginDAO.findByLogin(login);
        return authInfoDAO.findOne(byLogin.iterator().next().getAuthInfoId());
    }

}
