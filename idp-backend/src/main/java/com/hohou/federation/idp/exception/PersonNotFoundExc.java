package com.hohou.federation.idp.exception;

import java.util.Arrays;

public final class PersonNotFoundExc extends NotFoundExc {

    private PersonNotFoundExc() {
        this.setErrors(Arrays.asList(Error.newInstance("PERSON_NOT_FOUND")));
    }

    public static PersonNotFoundExc newInstance() {
        return new PersonNotFoundExc();
    }

}
