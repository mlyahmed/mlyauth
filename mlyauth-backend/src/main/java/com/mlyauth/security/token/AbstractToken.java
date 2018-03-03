package com.mlyauth.security.token;

import com.mlyauth.constants.TokenScope;
import com.mlyauth.exception.TokenAlreadyCommittedException;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractToken implements IDPToken {

    protected boolean committed = false;

    protected void checkCommitted() {
        if (committed) throw TokenAlreadyCommittedException.newInstance();
    }

    protected String compactScopes(Set<TokenScope> scopes) {
        return scopes.stream().map(TokenScope::name).collect(Collectors.joining("|"));
    }

    protected Set<TokenScope> splitScopes(String scopes) {
        return Arrays.stream(scopes.split("\\|")).map(TokenScope::valueOf).collect(Collectors.toSet());
    }
}
