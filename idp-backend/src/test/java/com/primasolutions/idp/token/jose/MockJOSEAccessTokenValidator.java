package com.primasolutions.idp.token.jose;

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
