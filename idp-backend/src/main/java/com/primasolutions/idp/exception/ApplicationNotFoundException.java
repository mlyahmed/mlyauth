package com.primasolutions.idp.exception;

public class ApplicationNotFoundException extends IDPException {

    public static ApplicationNotFoundException newInstance() {
        return new ApplicationNotFoundException();
    }

}
