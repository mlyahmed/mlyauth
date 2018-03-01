package com.mlyauth.services.domain;

import com.mlyauth.beans.PersonBean;

public interface IPersonService {
    PersonBean createPerson(PersonBean bean);

    PersonBean updatePerson(PersonBean bean);
}
