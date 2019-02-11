package com.hohou.federation.idp;

import com.hohou.federation.idp.tools.ConstantsForTests;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;

import java.security.Security;

@Configuration
@Profile("test")
public class TestConfig {

    private static final int CACHE_PORT = 6379;
    private static final MariaDBContainer DB_CONTAINER = new MariaDBContainer("mariadb:10.3.6");
    private static final GenericContainer CACHE_CONTAINER = new GenericContainer("redis:3.0.6").withExposedPorts(CACHE_PORT);


    static {
        DB_CONTAINER.start();
        CACHE_CONTAINER.start();
    }

    @Bean(destroyMethod = "stop")
    public MariaDBContainer databaseContainer() {
        return DB_CONTAINER;
    }

    @Bean
    public GenericContainer cacheContainer() {
        return CACHE_CONTAINER;
    }



    @Bean(name = "jasyptStringEncryptor")
    public StandardPBEStringEncryptor jasyptStringEncryptor() {
        Security.addProvider(new BouncyCastleProvider());
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(ConstantsForTests.TEST_START_UP_PASSPHRASE);
        encryptor.setAlgorithm("PBEWITHSHA256AND128BITAES-CBC-BC");
        encryptor.setProviderName(BouncyCastleProvider.PROVIDER_NAME);
        return encryptor;
    }


    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext appConf) {
            TestPropertyValues.of(
                    dbDriver(),
                    dbUrl(),
                    dbUsername(),
                    dbPassword(),
                    cacheHost(),
                    cachePort()
            ).applyTo(appConf.getEnvironment());
        }

        @NotNull
        private String dbPassword() {
            return "spring.datasource.password=" + DB_CONTAINER.getPassword();
        }

        @NotNull
        private String dbUsername() {
            return "spring.datasource.username=" + DB_CONTAINER.getUsername();
        }

        @NotNull
        private String dbUrl() {
            return "spring.datasource.url=" + DB_CONTAINER.getJdbcUrl();
        }

        private String dbDriver() {
            return "spring.datasource.driver-class-name=" + DB_CONTAINER.getDriverClassName();
        }


        private String cacheHost() {
            return "spring.redis.host=" + CACHE_CONTAINER.getContainerIpAddress();
        }

        private String cachePort() {
            return "spring.redis.port=" + CACHE_CONTAINER.getMappedPort(CACHE_PORT);
        }

    }



}
