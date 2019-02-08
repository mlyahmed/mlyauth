package com.hohou.federation.idp.person.service;

import com.hohou.federation.idp.person.model.PersonBean;

public interface PersonService {

    PersonBean lookupPerson(String externalId);

    void createPerson(PersonBean bean);

    void updatePerson(PersonBean bean);

    void assignApplication(String appname, String personExternalId);

}
