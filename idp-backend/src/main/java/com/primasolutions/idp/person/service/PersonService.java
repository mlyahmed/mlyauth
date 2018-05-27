package com.primasolutions.idp.person.service;

import com.primasolutions.idp.person.model.PersonBean;

public interface PersonService {

    PersonBean lookupPerson(String externalId);

    void createPerson(PersonBean bean);

    void updatePerson(PersonBean bean);

    void assignApplication(String appname, String personExternalId);

}
