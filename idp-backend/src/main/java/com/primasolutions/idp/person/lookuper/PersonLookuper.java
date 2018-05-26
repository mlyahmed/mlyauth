package com.primasolutions.idp.person.lookuper;

import com.primasolutions.idp.exception.PersonNotFoundExc;
import com.primasolutions.idp.person.model.Person;

public interface PersonLookuper {

    default Person byExternalIdOrError(final String externalId) {
        final Person person = byExternalId(externalId);
        if (person == null) throw PersonNotFoundExc.newInstance();
        return person;
    }

    Person byEmail(String email);

    Person byExternalId(String externalId);
}
