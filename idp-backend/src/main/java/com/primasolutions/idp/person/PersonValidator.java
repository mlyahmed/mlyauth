package com.primasolutions.idp.person;

import com.primasolutions.idp.exception.AuthError;
import com.primasolutions.idp.exception.IDPException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Arrays.asList;

@Component
public class PersonValidator implements IPersonValidator {

    @Autowired
    protected PersonLookuper personLookuper;

    @Override
    public void validateNew(final PersonBean bean) {
        if (bean == null)
            throw IDPException.newInstance().setErrors(asList(AuthError.newInstance("PERSON_IS_NULL")));

        if (bean.getEmail() == null)
            throw IDPException.newInstance().setErrors(asList(AuthError.newInstance("EMAIL_INVALID")));

        if (personLookuper.byEmail(bean.getEmail()) != null)
            throw IDPException.newInstance().setErrors(asList(AuthError.newInstance("PERSON_ALREADY_EXISTS")));
    }
}
