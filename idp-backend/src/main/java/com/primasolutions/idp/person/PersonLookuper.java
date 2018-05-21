package com.primasolutions.idp.person;

import com.primasolutions.idp.person.model.Person;
import com.primasolutions.idp.person.model.PersonByEmail;
import com.primasolutions.idp.person.model.PersonByEmailDAO;
import com.primasolutions.idp.person.model.PersonDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;

@Component
public class PersonLookuper {

    @Autowired
    protected PersonDAO personDAO;

    @Autowired
    protected PersonByEmailDAO personByEmailDAO;

    public Person byEmail(final String email) {
        final Set<PersonByEmail> byEmail = personByEmailDAO.findByEmail(email);
        return isEmpty(byEmail) ? null : filterByEmail(byEmail, email);
    }


    public Person byExternalId(final String externalId) {
        return personDAO.findByExternalId(externalId);
    }

    private Person filterByEmail(final Set<PersonByEmail> index, final String email) {
        return index.stream().map(entry -> personDAO.findByExternalId(entry.getPersonId()))
                .filter(Objects::nonNull)
                .filter(person -> person.getEmail().equals(email))
                .findFirst().orElse(null);
    }

}
