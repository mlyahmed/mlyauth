package com.primasolutions.idp.person.validator;

import com.primasolutions.idp.exception.AuthError;
import com.primasolutions.idp.exception.IDPException;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class PersonFirstNameValidator {

    private static final int MAX_LENGTH = 100;

    public static PersonFirstNameValidator newInstance() {
        return new PersonFirstNameValidator();
    }

    public void validate(final String firstName) {
        if (StringUtils.isEmpty(firstName))
            throw IDPException.newInstance().setErrors(Arrays.asList(AuthError.newInstance("FIRST_NAME_EMPTY")));

        if (firstName.length() > MAX_LENGTH)
            throw IDPException.newInstance().setErrors(Arrays.asList(AuthError.newInstance("FIRST_NAME_TOO_LONG")));
    }

}
