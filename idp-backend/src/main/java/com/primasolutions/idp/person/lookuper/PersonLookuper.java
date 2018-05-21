package com.primasolutions.idp.person.lookuper;

import com.primasolutions.idp.person.model.Person;

public interface PersonLookuper {
    Person byEmail(String email);

    Person byExternalId(String externalId);
}
