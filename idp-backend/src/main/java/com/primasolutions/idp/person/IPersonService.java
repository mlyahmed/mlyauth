package com.primasolutions.idp.person;

import com.primasolutions.idp.person.model.PersonBean;

public interface IPersonService {

    PersonBean lookupPerson(String externalId);

    PersonBean createPerson(PersonBean bean);

    PersonBean updatePerson(PersonBean bean);

    void assignApplication(String appname, String personExternalId);

}
