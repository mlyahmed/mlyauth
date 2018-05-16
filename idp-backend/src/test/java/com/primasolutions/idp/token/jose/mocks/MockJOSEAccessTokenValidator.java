package com.primasolutions.idp.token.jose.mocks;

import com.primasolutions.idp.token.jose.JOSEAccessToken;
import com.primasolutions.idp.token.jose.JOSEAccessTokenValidator;

public class MockJOSEAccessTokenValidator extends JOSEAccessTokenValidator {

    private boolean byPasse;

    public MockJOSEAccessTokenValidator(final boolean byPasse) {
        this.byPasse = byPasse;
    }


    public boolean validate(final JOSEAccessToken access) {
        if (byPasse) return true;
        else return super.validate(access);
    }
}
