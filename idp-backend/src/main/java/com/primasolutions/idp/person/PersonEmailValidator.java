package com.primasolutions.idp.person;

import com.primasolutions.idp.exception.AuthError;
import com.primasolutions.idp.exception.IDPException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

import static java.util.Arrays.asList;

public final class PersonEmailValidator {

    private static final int MAX_EMAIL_LENGTH = 64;

    public static PersonEmailValidator newInstance() {
        return new PersonEmailValidator();
    }

    public void validate(final String email) {
        if (StringUtils.isEmpty(email))
            throw IDPException.newInstance().setErrors(asList(AuthError.newInstance("PERSON_EMAIL_IS_EMPTY")));

        if (email.length() > MAX_EMAIL_LENGTH)
            throw IDPException.newInstance().setErrors(asList(AuthError.newInstance("PERSON_EMAIL_IS_TOO_LONG")));

        if (!EmailValidator.getInstance().isValid(email))
            throw IDPException.newInstance().setErrors(asList(AuthError.newInstance("PERSON_EMAIL_IS_INVALID")));
    }
}
