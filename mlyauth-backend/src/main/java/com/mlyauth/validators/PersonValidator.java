package com.mlyauth.validators;

import com.mlyauth.beans.PersonBean;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.exception.AuthError;
import com.mlyauth.exception.AuthException;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;

@Named
public class PersonValidator implements IPersonValidator {

    @Inject
    private PersonDAO personDAO;

    @Override
    public void validateNewPerson(PersonBean bean) {
        if (bean == null)
            throw AuthException.newInstance().setErrors(Arrays.asList(AuthError.newInstance("PERSON_IS_NULL")));

        if (personDAO.findByEmail(bean.getEmail()) != null)
            throw AuthException.newInstance().setErrors(Arrays.asList(AuthError.newInstance("PERSON_ALREADY_EXISTS")));

        if (bean.getEmail() == null)
            throw AuthException.newInstance().setErrors(Arrays.asList(AuthError.newInstance("EMAIL_INVALID")));
    }
}
