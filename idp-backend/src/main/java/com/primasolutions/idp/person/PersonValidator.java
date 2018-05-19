package com.primasolutions.idp.person;

import com.primasolutions.idp.exception.AuthError;
import com.primasolutions.idp.exception.IDPException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Arrays.asList;

@Component
public class PersonValidator implements IPersonValidator {

    @Autowired
    protected PersonLookuper personLookuper;

    @Override
    public void validateNew(final PersonBean bean) {
        thePersonMustNotBeNull(bean);
        theEmailMustBeValid(bean);
        theEmailMustBeNew(bean);

        if (StringUtils.isEmpty(bean.getFirstname()))
            throw IDPException.newInstance().setErrors(asList(AuthError.newInstance("PERSON_FIRSTNAME_IS_EMPTY")));
    }

    private void thePersonMustNotBeNull(final PersonBean bean) {
        if (bean == null)
            throw IDPException.newInstance().setErrors(asList(AuthError.newInstance("PERSON_IS_NULL")));
    }

    private void theEmailMustBeValid(final PersonBean bean) {
        PersonEmailValidator.newInstance().validate(bean.getEmail());
    }

    private void theEmailMustBeNew(final PersonBean bean) {
        if (personLookuper.byEmail(bean.getEmail()) != null)
            throw IDPException.newInstance().setErrors(asList(AuthError.newInstance("PERSON_ALREADY_EXISTS")));
    }
}
