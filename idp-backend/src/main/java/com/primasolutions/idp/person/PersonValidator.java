package com.primasolutions.idp.person;

import com.primasolutions.idp.exception.AuthError;
import com.primasolutions.idp.exception.IDPException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Arrays.asList;

@Component
public class PersonValidator implements IPersonValidator {

    private static final int MAX_EMAIL_LENGTH = 64;

    @Autowired
    protected PersonLookuper personLookuper;

    @Override
    public void validateNew(final PersonBean bean) {
        if (bean == null)
            throw IDPException.newInstance().setErrors(asList(AuthError.newInstance("PERSON_IS_NULL")));

        if (StringUtils.isEmpty(bean.getEmail()))
            throw IDPException.newInstance().setErrors(asList(AuthError.newInstance("PERSON_EMAIL_IS_EMPTY")));

        if (bean.getEmail().length() > MAX_EMAIL_LENGTH)
            throw IDPException.newInstance().setErrors(asList(AuthError.newInstance("PERSON_EMAIL_IS_TOO_LONG")));

        if (!EmailValidator.getInstance().isValid(bean.getEmail()))
            throw IDPException.newInstance().setErrors(asList(AuthError.newInstance("PERSON_EMAIL_IS_INVALID")));

        if (personLookuper.byEmail(bean.getEmail()) != null)
            throw IDPException.newInstance().setErrors(asList(AuthError.newInstance("PERSON_ALREADY_EXISTS")));
    }
}
