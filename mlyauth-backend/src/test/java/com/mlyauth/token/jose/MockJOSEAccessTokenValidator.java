package com.mlyauth.token.jose;

public class MockJOSEAccessTokenValidator extends JOSEAccessTokenValidator {

    private boolean byPasse;

    public MockJOSEAccessTokenValidator(boolean byPasse) {
        this.byPasse = byPasse;
    }


    public boolean validate(JOSEAccessToken access) {
        if (byPasse) return true;
        else return super.validate(access);
    }
}
