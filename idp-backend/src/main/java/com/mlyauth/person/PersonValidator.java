package com.mlyauth.person;

import com.mlyauth.beans.PersonBean;
import com.mlyauth.exception.AuthError;
import com.mlyauth.exception.IDPException;
import com.mlyauth.lookup.PersonLookuper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class PersonValidator implements IPersonValidator {

    @Autowired
    private PersonLookuper personLookuper;

    @Override
    public void validateNewPerson(PersonBean bean) {
        if (bean == null)
            throw IDPException.newInstance().setErrors(Arrays.asList(AuthError.newInstance("PERSON_IS_NULL")));

        if (personLookuper.byEmail(bean.getEmail()) != null)
            throw IDPException.newInstance().setErrors(Arrays.asList(AuthError.newInstance("PERSON_ALREADY_EXISTS")));

        if (bean.getEmail() == null)
            throw IDPException.newInstance().setErrors(Arrays.asList(AuthError.newInstance("EMAIL_INVALID")));
    }
}
