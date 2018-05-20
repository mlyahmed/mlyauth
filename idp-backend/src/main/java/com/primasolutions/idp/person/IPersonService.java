package com.primasolutions.idp.person;

public interface IPersonService {

    PersonBean lookupPerson(String externalId);

    PersonBean createPerson(PersonBean bean);

    PersonBean updatePerson(PersonBean bean);

    void assignApplication(String appname, String personExternalId);

}
