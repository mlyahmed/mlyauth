package com.primasolutions.idp;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.MariaDBContainer;

import java.security.Security;

import static com.primasolutions.idp.tools.ConstantsForTests.TEST_START_UP_PASSPHRASE;

@Configuration
@Profile("test")
public class TestConfig {

    private static final MariaDBContainer DB_CONTAINER = new MariaDBContainer("mariadb:10.3.6");
    static {
        DB_CONTAINER.start();
    }

    static MariaDBContainer dbContainer() {
        return DB_CONTAINER;
    }

    @Bean(destroyMethod = "stop")
    public MariaDBContainer databaseContainer() {
        return dbContainer();
    }


    @Bean(name = "jasyptStringEncryptor")
    public StandardPBEStringEncryptor jasyptStringEncryptor() {
        Security.addProvider(new BouncyCastleProvider());
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(TEST_START_UP_PASSPHRASE);
        encryptor.setAlgorithm("PBEWITHSHA256AND128BITAES-CBC-BC");
        encryptor.setProviderName(BouncyCastleProvider.PROVIDER_NAME);
        return encryptor;
    }


    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext appConf) {
            TestPropertyValues.of(driver(), url(), username(), password()).applyTo(appConf.getEnvironment());
        }

        @NotNull
        private String password() {
            return "spring.datasource.password=" + dbContainer().getPassword();
        }

        @NotNull
        private String username() {
            return "spring.datasource.username=" + dbContainer().getUsername();
        }

        @NotNull
        private String url() {
            return "spring.datasource.url=" + dbContainer().getJdbcUrl();
        }

        private String driver() {
            return "spring.datasource.driver-class-name=" + dbContainer().getDriverClassName();
        }

    }



}
