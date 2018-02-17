package com.mlyauth.exception;

public class ApplicationNotFound extends IDPException {

    public static ApplicationNotFound newInstance() {
        return new ApplicationNotFound();
    }

}
