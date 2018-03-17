package com.mlyauth.token;

import com.mlyauth.SecurityConfig;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;

public class TokenEncodeStampTest {

    @Test
    public void generate_new_stamps() {
        final String stamp = UUID.randomUUID().toString();
        SecurityConfig securityConfig = new SecurityConfig();
        final String encodedStamp = securityConfig.passwordEncoder().encode(stamp);
        Assert.assertThat(securityConfig.passwordEncoder().matches(stamp, encodedStamp), equalTo(true));
    }

}
