package com.primasolutions.idp.person.validator;

import com.primasolutions.idp.person.model.PersonBean;

public interface IPersonValidator {

    void validateNew(PersonBean bean);

    void validateUpdate(PersonBean bean);

}
