package com.hohou.federation.idp.token;

import com.hohou.federation.idp.constants.TokenScope;
import com.hohou.federation.idp.exception.TokenUnmodifiableExc;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractToken implements IToken {

    protected boolean committed = false;
    protected boolean locked = false;

    protected void checkCommitted() {
        if (committed) throw TokenUnmodifiableExc.newInstance();
    }

    protected void checkLocked() {
        if (locked) throw TokenUnmodifiableExc.newInstance();
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
