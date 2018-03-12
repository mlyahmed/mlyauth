package com.mlyauth.person;

import com.mlyauth.beans.PersonBean;

public interface IPersonService {
    PersonBean createPerson(PersonBean bean);

    PersonBean updatePerson(PersonBean bean);
}
