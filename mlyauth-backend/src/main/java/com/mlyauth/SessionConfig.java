package com.mlyauth;

import com.mlyauth.context.IContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

@Configuration
public class SessionConfig {
    private static final Logger logger = LoggerFactory.getLogger(SessionConfig.class);

    @Autowired
    private IContextHolder contextHolder;

    @Bean
    public HttpSessionListener httpSessionListener() {

        return new HttpSessionListener() {

            @Override
            public void sessionCreated(HttpSessionEvent se) {
                logger.info("Session Created with session id + " + se.getSession().getId());
            }

            @Override
            public void sessionDestroyed(HttpSessionEvent se) {
                //TODO fix the NullPointerException Exception we get.
                contextHolder.reset();
            }

        };

    }
}
