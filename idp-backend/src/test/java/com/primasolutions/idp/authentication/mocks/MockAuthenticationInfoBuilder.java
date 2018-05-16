package com.primasolutions.idp.authentication.mocks;

import com.primasolutions.idp.authentication.AuthenticationInfoBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class MockAuthenticationInfoBuilder extends AuthenticationInfoBuilder {

    public static final int STRENGTH = 4;

    public MockAuthenticationInfoBuilder() {
        passwordEncoder = new BCryptPasswordEncoder(STRENGTH);
    }
}
