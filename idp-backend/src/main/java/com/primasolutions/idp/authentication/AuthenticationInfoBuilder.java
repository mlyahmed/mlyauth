package com.primasolutions.idp.authentication;

import com.primasolutions.idp.constants.AuthInfoStatus;
import com.primasolutions.idp.person.PersonBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class AuthenticationInfoBuilder {

    private static final int A_CENTURY = 1000 * 60 * 60 * 24 * 365 * 100;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    public AuthInfo toEntity(final PersonBean bean) {
        return AuthInfo.newInstance()
                .setLogin(bean.getEmail())
                .setPassword(passwordEncoder.encode(buildPassword(bean)))
                .setStatus(AuthInfoStatus.ACTIVE)
                .setEffectiveAt(new Date())
                .setExpireAt(new Date(System.currentTimeMillis() + A_CENTURY));
    }

    private String buildPassword(final PersonBean bean) {
        return bean.getPassword() == null ? UUID.randomUUID().toString() : String.valueOf(bean.getPassword());
    }
}
