package com.hohou.federation.idp.person.validator;

import com.hohou.federation.idp.exception.Error;
import com.hohou.federation.idp.exception.IDPException;
import org.apache.commons.validator.routines.DateValidator;

import static com.hohou.federation.idp.person.mapper.PersonMapperImpl.DATE_FORMAT;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class PersonBirthDateValidator {

    public static PersonBirthDateValidator newInstance() {
        return new PersonBirthDateValidator();
    }

    public void validate(final String birthDate) {
        if (isNotEmpty(birthDate) && DateValidator.getInstance().validate(birthDate, DATE_FORMAT) == null)
            throw IDPException.newInstance().setErrors(asList(Error.newInstance("BIRTH_DATE_BAD_FORMAT")));
    }

}
