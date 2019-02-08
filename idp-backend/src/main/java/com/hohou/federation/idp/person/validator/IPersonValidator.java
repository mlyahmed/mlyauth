package com.hohou.federation.idp.person.validator;

import com.hohou.federation.idp.person.model.PersonBean;

public interface IPersonValidator {

    void validateNew(PersonBean bean);

    void validateUpdate(PersonBean bean);

}
