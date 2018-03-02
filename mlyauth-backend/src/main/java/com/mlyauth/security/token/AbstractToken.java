package com.mlyauth.security.token;

import com.mlyauth.exception.TokenAlreadyCommitedException;

public abstract class AbstractToken implements IDPToken {

    protected boolean committed = false;

    protected void checkCommitted() {
        if (committed) throw TokenAlreadyCommitedException.newInstance();
    }
}
