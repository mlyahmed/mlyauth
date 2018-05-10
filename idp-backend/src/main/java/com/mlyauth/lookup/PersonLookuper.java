package com.mlyauth.lookup;

import com.mlyauth.dao.PersonByEmailDAO;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.Person;
import com.mlyauth.domain.PersonByEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;

@Component
public class PersonLookuper {

    @Autowired
    private PersonDAO personDAO;

    @Autowired
    private PersonByEmailDAO personByEmailDAO;

    public Person byEmail(String email){
        final Set<PersonByEmail> byEmail = personByEmailDAO.findByEmail(email);
        return isEmpty(byEmail) ? null : filterByEmail(byEmail, email);
    }

    private Person filterByEmail(Set<PersonByEmail> index, String email){
        return index.stream().map(entry -> personDAO.findByExternalId(entry.getPersonId()))
                .filter(person -> person != null)
                .filter(person -> person.getEmail().equals(email))
                .findFirst().orElse(null);
    }

}
