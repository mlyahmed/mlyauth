package com.hohou.federation.idp.authentication;

import com.hohou.federation.idp.constants.RoleCode;
import com.hohou.federation.idp.exception.Error;
import com.hohou.federation.idp.exception.IDPException;
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
