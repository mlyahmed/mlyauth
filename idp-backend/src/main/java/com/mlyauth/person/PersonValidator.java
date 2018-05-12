package com.mlyauth.person;

import com.mlyauth.beans.PersonBean;
import com.mlyauth.exception.AuthError;
import com.mlyauth.exception.IDPException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Arrays.asList;

@Component
public class PersonValidator implements IPersonValidator {

    @Autowired
    private PersonLookuper personLookuper;

    @Override
    public void validateNewPerson(final PersonBean bean) {
        if (bean == null)
            throw IDPException.newInstance().setErrors(asList(AuthError.newInstance("PERSON_IS_NULL")));

        if (personLookuper.byEmail(bean.getEmail()) != null)
            throw IDPException.newInstance().setErrors(asList(AuthError.newInstance("PERSON_ALREADY_EXISTS")));

        if (bean.getEmail() == null)
            throw IDPException.newInstance().setErrors(asList(AuthError.newInstance("EMAIL_INVALID")));
    }
}
