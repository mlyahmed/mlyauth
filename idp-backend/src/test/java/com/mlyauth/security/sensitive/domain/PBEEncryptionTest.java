package com.mlyauth.security.sensitive.domain;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hamcrest.Matchers;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.Security;

import static com.mlyauth.tools.RandomForTests.randomString;
import static org.junit.Assert.assertThat;

@RunWith(DataProviderRunner.class)
public class PBEEncryptionTest {

    @DataProvider
    public static Object[] sensitives() {
        // @formatter:off
        return new String[]{
                "<f8yXTDJ$y2QPjE!L)#gc_uKVVuMMzP=",
                randomString(),
                randomString(),
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("sensitives")
    public void when_encrypt_a_value_then_encrypted_value_must_be_returned(String sensitive){
        Security.addProvider(new BouncyCastleProvider());
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword("cM7g+:S*DY7m>c.D3{8jHtr6tH%^L~3t");
        encryptor.setAlgorithm("PBEWITHSHA256AND128BITAES-CBC-BC");
        encryptor.setProviderName(BouncyCastleProvider.PROVIDER_NAME);
        final String encrypted = encryptor.encrypt(sensitive);
        assertThat(encrypted, Matchers.notNullValue());
        assertThat(encryptor.decrypt(encrypted), Matchers.equalTo(sensitive));
    }
}
