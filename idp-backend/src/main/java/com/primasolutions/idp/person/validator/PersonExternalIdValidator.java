package com.primasolutions.idp.person.validator;

import com.primasolutions.idp.exception.AuthError;
import com.primasolutions.idp.exception.IDPException;
import org.apache.commons.lang3.StringUtils;

import static java.util.Arrays.asList;

public class PersonExternalIdValidator {


    private static final int MAX_LENGTH = 100;

    public static PersonExternalIdValidator newInstance() {
        return new PersonExternalIdValidator();
    }

    public void validate(final String externalId) {
        if (StringUtils.isEmpty(externalId))
            throw IDPException.newInstance().setErrors(asList(AuthError.newInstance("EXTERNAL_ID_EMPTY")));

        if (externalId.length() > MAX_LENGTH)
            throw IDPException.newInstance().setErrors(asList(AuthError.newInstance("EXTERNAL_ID_TOO_LONG")));
    }

}
