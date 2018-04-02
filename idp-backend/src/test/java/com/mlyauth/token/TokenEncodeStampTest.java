package com.mlyauth.token;

import com.mlyauth.SecurityConfig;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class TokenEncodeStampTest {

    @Test
    public void generate_new_stamps() {
        SecurityConfig securityConfig = new SecurityConfig();
        final String stamp = UUID.randomUUID().toString();
        final String password = "))E-i9? \"8<\\)/`It~\"4+*,@#9.*\\Y>/.>z75v.b[lkKg{\"\\8]240* 5_5G<c4Ub";
        final String encodedPassword = securityConfig.passwordEncoder().encode(password);
        final String hashedPassword = DigestUtils.sha256Hex(encodedPassword);
        final String hashedStamp = DigestUtils.sha256Hex(stamp);
        final String checksum = DigestUtils.sha256Hex(hashedStamp);
        assertThat(securityConfig.passwordEncoder().matches(password, encodedPassword), equalTo(true));
        assertThat(hashedPassword, notNullValue());
        assertThat(hashedStamp, notNullValue());
        assertThat(checksum, notNullValue());
    }

}
