package com.mlyauth.exception;

public class NotSPSAMLApplication extends AuthException {

    public static NotSPSAMLApplication newInstance() {
        return new NotSPSAMLApplication();
    }
}
