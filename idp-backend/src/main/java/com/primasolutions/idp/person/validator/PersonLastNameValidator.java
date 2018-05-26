package com.primasolutions.idp.person.validator;

import com.primasolutions.idp.exception.Error;
import com.primasolutions.idp.exception.IDPException;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

import static java.util.Arrays.asList;

public class PersonLastNameValidator {

    private static final int MAX_LENGTH = 100;

    public static PersonLastNameValidator newInstance() {
        return new PersonLastNameValidator();
    }

    public void validate(final String lastName) {
        if (StringUtils.isEmpty(lastName))
            throw IDPException.newInstance().setErrors(asList(Error.newInstance("LAST_NAME_EMPTY")));

        if (lastName.length() > MAX_LENGTH)
            throw IDPException.newInstance().setErrors(Arrays.asList(Error.newInstance("LAST_NAME_TOO_LONG")));
    }

}
