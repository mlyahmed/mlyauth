package com.mlyauth.context;

import org.apache.catalina.SessionIdGenerator;
import org.apache.catalina.util.StandardSessionIdGenerator;
import org.springframework.stereotype.Component;

@Component
public class ContextIdGenerator implements IContextIdGenerator {

    private SessionIdGenerator idGenerator = new StandardSessionIdGenerator();

    @Override
    public String generateId() {
        return idGenerator.generateSessionId();
    }

}
