package com.mlyauth.conf;

import com.mlyauth.security.context.IContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

@Configuration
@EnableTransactionManagement
public class AppConf implements TransactionManagementConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(AppConf.class);

    @Autowired
    private EntityManagerFactory emf;

    @Autowired
    private IContextHolder contextHolder;

    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return transactionManager();
    }


    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }

    @Bean
    public HttpSessionListener httpSessionListener() {

        return new HttpSessionListener() {

            @Override
            public void sessionCreated(HttpSessionEvent se) {
                logger.info("Session Created with session id + " + se.getSession().getId());
            }

            @Override
            public void sessionDestroyed(HttpSessionEvent se) {
                contextHolder.reset();
            }

        };

    }

}
