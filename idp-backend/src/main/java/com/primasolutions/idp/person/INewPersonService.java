package com.primasolutions.idp.person;

public interface INewPersonService {
    PersonBean createPerson(PersonBean bean);
    PersonBean updatePerson(PersonBean bean);
    void assignApplication(String appname, String personExternalId);

}
