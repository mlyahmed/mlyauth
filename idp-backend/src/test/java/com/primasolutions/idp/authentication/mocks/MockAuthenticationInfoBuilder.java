package com.primasolutions.idp.authentication.mocks;

import com.primasolutions.idp.authentication.AuthenticationInfoBuilder;
import com.primasolutions.idp.tools.MockReseter;
import com.primasolutions.idp.tools.ResettableMock;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class MockAuthenticationInfoBuilder extends AuthenticationInfoBuilder implements ResettableMock {

    private static MockAuthenticationInfoBuilder instance;

    private static final int STRENGTH = 4;

    public static MockAuthenticationInfoBuilder getInstance() {
        if (instance == null) {
            synchronized (MockAuthenticationInfoBuilder.class) {
                if (instance == null)
                    instance = new MockAuthenticationInfoBuilder();
            }
        }
        return instance;
    }

    private MockAuthenticationInfoBuilder() {
        MockReseter.register(this);
        passwordEncoder = new BCryptPasswordEncoder(STRENGTH);
    }

    @Override
    public void reset() {
        instance = null;
    }
}
