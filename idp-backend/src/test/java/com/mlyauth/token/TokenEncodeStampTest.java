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
        final String stamp = "Br5NUEuDdQpyMTs"; //UUID.randomUUID().toString();
        SecurityConfig securityConfig = new SecurityConfig();
        final String encodedStamp = securityConfig.passwordEncoder().encode(stamp);
        final String checksum = DigestUtils.sha256Hex(encodedStamp);
        assertThat(securityConfig.passwordEncoder().matches(stamp, encodedStamp), equalTo(true));
        assertThat(checksum, notNullValue());
    }

}
