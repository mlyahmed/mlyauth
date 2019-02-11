package com.hohou.federation.idp.person.validator;

import com.hohou.federation.idp.exception.Error;
import com.hohou.federation.idp.exception.IDPException;
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
            throw IDPException.newInstance().setErrors(asList(Error.newInstance("EMAIL_EMPTY")));

        if (email.length() > MAX_EMAIL_LENGTH)
            throw IDPException.newInstance().setErrors(asList(Error.newInstance("EMAIL_TOO_LONG")));

        if (!EmailValidator.getInstance().isValid(email))
            throw IDPException.newInstance().setErrors(asList(Error.newInstance("EMAIL_INVALID")));
    }
}
