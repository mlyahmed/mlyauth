package com.mlyauth.lookup;

import com.mlyauth.dao.PersonByEmailDAO;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.Person;
import com.mlyauth.domain.PersonByEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PersonLookup {

    @Autowired
    private PersonDAO personDAO;

    @Autowired
    private PersonByEmailDAO personByEmailDAO;

    public Person byEmail(String email){
        final PersonByEmail byEmail = personByEmailDAO.findByEmail(email);
        return personDAO.findByExternalId(byEmail.getPersonId());
    }

}
