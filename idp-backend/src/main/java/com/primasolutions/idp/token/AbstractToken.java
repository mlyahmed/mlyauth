package com.primasolutions.idp.token;

import com.primasolutions.idp.constants.TokenScope;
import com.primasolutions.idp.exception.TokenUnmodifiableException;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractToken implements IToken {

    protected boolean committed = false;
    protected boolean locked = false;

    protected void checkCommitted() {
        if (committed) throw TokenUnmodifiableException.newInstance();
    }

    protected void checkLocked() {
        if (locked) throw TokenUnmodifiableException.newInstance();
    }


    protected void checkUnmodifiable() {
        checkCommitted();
        checkLocked();
    }

    protected String compactScopes(final Set<TokenScope> scopes) {
        return scopes != null ? scopes.stream().map(TokenScope::name).collect(Collectors.joining("|")) : null;
    }

    protected Set<TokenScope> splitScopes(final String scopes) {
        return Arrays.stream(scopes.split("\\|")).map(TokenScope::valueOf).collect(Collectors.toSet());
    }
}
