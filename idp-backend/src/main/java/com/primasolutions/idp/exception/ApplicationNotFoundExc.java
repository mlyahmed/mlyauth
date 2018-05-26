package com.primasolutions.idp.exception;

import java.util.Arrays;

public final class ApplicationNotFoundExc extends IDPException {

    private ApplicationNotFoundExc() {
        this.setErrors(Arrays.asList(Error.newInstance("APPLICATION_NOT_FOUND")));
    }

    public static ApplicationNotFoundExc newInstance() {
        return new ApplicationNotFoundExc();
    }

}
