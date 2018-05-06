package com.mlyauth;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.hibernate4.encryptor.HibernatePBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import javax.persistence.EntityManagerFactory;
import java.security.Security;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableTransactionManagement
public class DataConfig implements TransactionManagementConfigurer {

    private final String encodedPassphrase = "$2a$13$n.UA0h5.4Shz4awT4EMAT.KDxri//akqZV/NyixLRgsUtbjGmM7he";
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(13);

    @Autowired
    private EntityManagerFactory emf;


    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return transactionManager();
    }

    @Bean(name="jasyptStringEncryptor")
    @Profile("runtime")
    public StandardPBEStringEncryptor jasyptStringEncryptor() {
        final String passphrase = System.getProperty("startup.passphrase");
        notNull(passphrase, "Startup passphrase is absent.");
        isTrue(passwordEncoder.matches(passphrase, encodedPassphrase), "Passphrase does not much !");

        Security.addProvider(new BouncyCastleProvider());
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(passphrase);
        encryptor.setAlgorithm("PBEWITHSHA256AND128BITAES-CBC-BC");
        encryptor.setProviderName(BouncyCastleProvider.PROVIDER_NAME);
        return encryptor;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }

    @Bean
    public HibernatePBEStringEncryptor hibernateStringEncryptor(StandardPBEStringEncryptor stringEncryptor){
        HibernatePBEStringEncryptor encryptor = new HibernatePBEStringEncryptor();
        encryptor.setEncryptor(stringEncryptor);
        encryptor.setRegisteredName("hibernateStringEncryptor");
        return encryptor;
    }

}
