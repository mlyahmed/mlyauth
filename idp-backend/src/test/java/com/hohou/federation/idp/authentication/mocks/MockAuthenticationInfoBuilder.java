package com.hohou.federation.idp.authentication.mocks;

import com.hohou.federation.idp.authentication.AuthenticationInfoBuilder;
import com.hohou.federation.idp.tools.MockReseter;
import com.hohou.federation.idp.tools.ResettableMock;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class MockAuthenticationInfoBuilder extends AuthenticationInfoBuilder implements ResettableMock {

    private static volatile MockAuthenticationInfoBuilder instance;

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
