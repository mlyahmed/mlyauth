package com.hohou.federation.idp.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AuthenticationInfoLookuper {

    @Autowired
    protected AuthInfoDAO authInfoDAO;

    @Autowired
    protected AuthInfoByLoginDAO authInfoByLoginDAO;

    public AuthInfo byLogin(final String login) {
        final Set<AuthInfoByLogin> byLogin = authInfoByLoginDAO.findByLogin(login);
        return authInfoDAO.findById(byLogin.iterator().next().getAuthInfoId()).orElse(null);
    }

}
