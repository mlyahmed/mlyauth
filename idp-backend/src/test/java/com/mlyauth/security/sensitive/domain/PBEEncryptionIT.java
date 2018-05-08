package com.mlyauth.security.sensitive.domain;

import com.mlyauth.AbstractIntegrationTest;
import org.hamcrest.Matchers;
import org.jasypt.encryption.StringEncryptor;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PBEEncryptionIT extends AbstractIntegrationTest {

    @Autowired
    private StringEncryptor jasyptStringEncryptor;

    @Test
    public void values_are_encrypted(){
        final String encrypt = jasyptStringEncryptor.encrypt("Ahmed");
        Assert.assertThat(encrypt, Matchers.notNullValue());
    }

    @Test
    public void values_are_decrypted(){
        final String encrypt = jasyptStringEncryptor.encrypt("root");
        final String row = jasyptStringEncryptor.decrypt(encrypt);
        Assert.assertThat(row, Matchers.equalTo("root"));
    }
}
