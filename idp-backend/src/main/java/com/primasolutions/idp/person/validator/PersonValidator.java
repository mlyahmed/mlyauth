package com.primasolutions.idp.person.validator;

import com.primasolutions.idp.authentication.RoleValidator;
import com.primasolutions.idp.exception.AuthError;
import com.primasolutions.idp.exception.IDPException;
import com.primasolutions.idp.person.PersonBean;
import com.primasolutions.idp.person.PersonLookuper;
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
        theFirstNameMustBeValid(bean);
        theLastNameMustBeValid(bean);
        theExternalIdMustBeValid(bean);
        theExternalIdMustBeNew(bean);
        theBirthDateMustBeValid(bean);
        theRoleMustBeValid(bean);
    }

    private void thePersonMustNotBeNull(final PersonBean bean) {
        if (bean == null)
            throw IDPException.newInstance().setErrors(asList(AuthError.newInstance("PERSON_NULL")));
    }

    private void theEmailMustBeValid(final PersonBean bean) {
        PersonEmailValidator.newInstance().validate(bean.getEmail());
    }

    private void theEmailMustBeNew(final PersonBean bean) {
        if (personLookuper.byEmail(bean.getEmail()) != null)
            throw IDPException.newInstance().setErrors(asList(AuthError.newInstance("EMAIL_ALREADY_EXISTS")));
    }

    private void theFirstNameMustBeValid(final PersonBean bean) {
        PersonFirstNameValidator.newInstance().validate(bean.getFirstname());
    }

    private void theLastNameMustBeValid(final PersonBean bean) {
        PersonLastNameValidator.newInstance().validate(bean.getLastname());
    }

    private void theExternalIdMustBeValid(final PersonBean bean) {
        PersonExternalIdValidator.newInstance().validate(bean.getExternalId());
    }

    private void theExternalIdMustBeNew(final PersonBean bean) {
        if (personLookuper.byExternalId(bean.getExternalId()) != null)
            throw IDPException.newInstance().setErrors(asList(AuthError.newInstance("EXTERNAL_ID_ALREADY_EXISTS")));
    }

    private void theBirthDateMustBeValid(final PersonBean bean) {
        PersonBirthDateValidator.newInstance().validate(bean.getBirthdate());
    }

    private void theRoleMustBeValid(final PersonBean bean) {
        RoleValidator.newInstance().validate(bean.getRole());
    }

}
