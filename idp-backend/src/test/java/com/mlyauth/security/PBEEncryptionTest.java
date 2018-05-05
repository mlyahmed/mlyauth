package com.mlyauth.security;

import com.mlyauth.AbstractIntegrationTest;
import org.hamcrest.Matchers;
import org.jasypt.encryption.StringEncryptor;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PBEEncryptionTest extends AbstractIntegrationTest {

    @Autowired
    private StringEncryptor jasyptStringEncryptor;

    @Test
    public void values_are_encrypted(){
        final String encrypt = jasyptStringEncryptor.encrypt("root");
        Assert.assertThat(encrypt, Matchers.notNullValue());
    }

    @Test
    public void values_are_decrypted(){
        final String encrypt = jasyptStringEncryptor.encrypt("root");
        final String row = jasyptStringEncryptor.decrypt(encrypt);
        Assert.assertThat(row, Matchers.equalTo("root"));
    }
}
