package com.primasolutions.idp.authentication.mocks;

import com.primasolutions.idp.authentication.AuthenticationInfoBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class MockAuthenticationInfoBuilder extends AuthenticationInfoBuilder {

    private static final int STRENGTH = 4;

    private static class LazyHolder {
        static final MockAuthenticationInfoBuilder INSTANCE = new MockAuthenticationInfoBuilder();
    }

    public static MockAuthenticationInfoBuilder getInstance() {
        return LazyHolder.INSTANCE;
    }

    private MockAuthenticationInfoBuilder() {
        passwordEncoder = new BCryptPasswordEncoder(STRENGTH);
    }

}
