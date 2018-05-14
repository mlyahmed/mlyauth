package com.primasolutions.idp.person;

import com.primasolutions.idp.beans.PersonBean;
import com.primasolutions.idp.exception.AuthError;
import com.primasolutions.idp.exception.IDPException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static java.util.Arrays.asList;

@Component
public class PersonValidator implements IPersonValidator {

    @Autowired
    private PersonLookuper personLookuper;

    @Override
    public void validateNew(final PersonBean bean) {
        if (bean == null)
            throw IDPException.newInstance().setErrors(Arrays.asList(AuthError.newInstance("PERSON_IS_NULL")));

        if (personLookuper.byEmail(bean.getEmail()) != null)
            throw IDPException.newInstance().setErrors(asList(AuthError.newInstance("PERSON_ALREADY_EXISTS")));

        if (bean.getEmail() == null)
            throw IDPException.newInstance().setErrors(asList(AuthError.newInstance("EMAIL_INVALID")));
    }
}
