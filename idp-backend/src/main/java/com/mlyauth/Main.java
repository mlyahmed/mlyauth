package com.mlyauth;

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

    public  static final String startUpPassphraseProperty = "startup.passphrase";
    private static final String encodedPassphrase = "$2a$13$n.UA0h5.4Shz4awT4EMAT.KDxri//akqZV/NyixLRgsUtbjGmM7he";
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(13);

    public static void main(String[] args) {
        final String passphrase = System.getProperty(startUpPassphraseProperty);
        notNull(passphrase, "Startup passphrase is absent.");
        isTrue(passwordEncoder.matches(passphrase, encodedPassphrase), "Passphrase does not match !");

        SpringApplication.run(Main.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Main.class);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {};
    }


    @Profile("runtime")
    @Bean(name="jasyptStringEncryptor")
    public StandardPBEStringEncryptor jasyptStringEncryptor() {
        Security.addProvider(new BouncyCastleProvider());
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(System.getProperty(startUpPassphraseProperty));
        encryptor.setAlgorithm("PBEWITHSHA256AND128BITAES-CBC-BC");
        encryptor.setProviderName(BouncyCastleProvider.PROVIDER_NAME);
        return encryptor;
    }

}
