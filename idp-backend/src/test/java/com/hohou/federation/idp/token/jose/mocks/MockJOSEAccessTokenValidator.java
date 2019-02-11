package com.hohou.federation.idp.token.jose.mocks;

import com.hohou.federation.idp.token.jose.JOSEAccessToken;
import com.hohou.federation.idp.token.jose.JOSEAccessTokenValidator;

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
