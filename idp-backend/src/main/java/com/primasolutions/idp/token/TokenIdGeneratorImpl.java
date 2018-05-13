package com.primasolutions.idp.token;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TokenIdGeneratorImpl implements TokenIdGenerator {
    @Override
    public String generateId() {
        return UUID.randomUUID().toString();
    }
}
