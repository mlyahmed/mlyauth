package com.mlyauth.sso.sp.jose;

import org.junit.Test;

public class SPJOSEUserDetailsServiceImplTest {

    @Test(expected = IllegalArgumentException.class)
    public void when_the_token_is_null_then_error() {
        SPJOSEUserDetailsServiceImpl service = new SPJOSEUserDetailsServiceImpl();
        service.loadUserByJOSE(null);
    }

}