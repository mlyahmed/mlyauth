package com.primasolutions.idp.authentication;

import com.primasolutions.idp.constants.RoleCode;
import com.primasolutions.idp.exception.Error;
import com.primasolutions.idp.exception.IDPException;
import org.apache.commons.lang3.StringUtils;

import static java.util.Arrays.asList;

public class RoleValidator {

    public static RoleValidator newInstance() {
        return new RoleValidator();
    }

    public void validate(final String role) {
        if (StringUtils.isEmpty(role))
            throw IDPException.newInstance().setErrors(asList(Error.newInstance("ROLE_EMPTY")));

        if (RoleCode.create(role) == null)
            throw IDPException.newInstance().setErrors(asList(Error.newInstance("ROLE_INVALID")));
    }

}
