package com.mlyauth.token;

import com.mlyauth.SecurityConfig;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class TokenEncodeStampTest {

    @Test
    public void generate_new_stamps() {
        SecurityConfig securityConfig = new SecurityConfig();
        final String stamp = "c810d2fe-5f91-4a41-accc-da88c5028fd3"; //UUID.randomUUID().toString();
        final String password = "n90014d8o621AXc";
        final String encodedPassword = securityConfig.passwordEncoder().encode(password);
        final String encodedStamp = DigestUtils.sha256Hex(stamp);
        final String checksum = DigestUtils.sha256Hex(encodedStamp);
        assertThat(securityConfig.passwordEncoder().matches(password, encodedPassword), equalTo(true));
        assertThat(encodedStamp, notNullValue());
        assertThat(checksum, notNullValue());
    }

}
