package com.primasolutions.idp.person;

import com.primasolutions.idp.beans.PersonBean;

public interface IPersonService {
    PersonBean createPerson(PersonBean bean);
    PersonBean updatePerson(PersonBean bean);
    void assignApplication(String appname, String personExternalId);

}
