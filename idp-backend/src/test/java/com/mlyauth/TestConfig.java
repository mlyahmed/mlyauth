package com.mlyauth;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.security.Security;

import static org.springframework.util.Assert.notNull;

@Configuration
@Profile("test")
public class TestConfig {

    @Bean(name="jasyptStringEncryptor")
    public StandardPBEStringEncryptor jasyptStringEncryptor() {
        final String passphrase = System.getProperty("startup.passphrase");
        notNull(passphrase, "Startup passphrase is absent.");
        Security.addProvider(new BouncyCastleProvider());
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(passphrase);
        encryptor.setAlgorithm("PBEWITHSHA256AND128BITAES-CBC-BC");
        encryptor.setProviderName(BouncyCastleProvider.PROVIDER_NAME);
        return encryptor;
    }

}
