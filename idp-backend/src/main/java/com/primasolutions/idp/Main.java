package com.primasolutions.idp;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.Security;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

@SpringBootApplication
@EnableEncryptableProperties
public class Main extends SpringBootServletInitializer {

    public  static final String START_UP_PASSPHRASE_PROPERTY = "startup.passphrase";
    private static final String ENCODED_PASSPHRASE = "$2a$13$n.UA0h5.4Shz4awT4EMAT.KDxri//akqZV/NyixLRgsUtbjGmM7he";
    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder(13);

    public static void main(final String[] args) {
        final String passphrase = System.getProperty(START_UP_PASSPHRASE_PROPERTY);
        notNull(passphrase, "Startup passphrase is absent.");
        isTrue(PASSWORD_ENCODER.matches(passphrase, ENCODED_PASSPHRASE), "Passphrase does not match !");

        SpringApplication.run(Main.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder application) {
        return application.sources(Main.class);
    }

    @Bean
    public CommandLineRunner commandLineRunner(final ApplicationContext ctx) {
        return args -> { };
    }


    @Profile("runtime")
    @Bean(name = "jasyptStringEncryptor")
    public StandardPBEStringEncryptor jasyptStringEncryptor() {
        Security.addProvider(new BouncyCastleProvider());
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(System.getProperty(START_UP_PASSPHRASE_PROPERTY));
        encryptor.setAlgorithm("PBEWITHSHA256AND128BITAES-CBC-BC");
        encryptor.setProviderName(BouncyCastleProvider.PROVIDER_NAME);

        return encryptor;
    }

}
