package com.primasolutions.idp;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.security.Security;

import static com.primasolutions.idp.tools.ConstantsForTests.TEST_START_UP_PASSPHRASE;

@Configuration
@Profile("test")
public class TestConfig {

    @Bean(name = "jasyptStringEncryptor")
    public StandardPBEStringEncryptor jasyptStringEncryptor() {
        Security.addProvider(new BouncyCastleProvider());
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(TEST_START_UP_PASSPHRASE);
        encryptor.setAlgorithm("PBEWITHSHA256AND128BITAES-CBC-BC");
        encryptor.setProviderName(BouncyCastleProvider.PROVIDER_NAME);
        return encryptor;
    }

}
