package com.mlyauth.security.context;

import com.mlyauth.domain.Person;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.LinkedList;

public class PrimaUser extends User {

    private final Person person;

    public PrimaUser(Person person) {
        super(person.getAuthenticationInfo().getLogin(),
                person.getAuthenticationInfo().getPassword(),
                true,
                true,
                true,
                true,
                new LinkedList<GrantedAuthority>());
        this.person = person;
    }


    public Person getPerson() {
        return person;
    }
}
