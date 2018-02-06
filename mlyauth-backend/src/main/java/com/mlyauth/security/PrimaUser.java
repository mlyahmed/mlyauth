package com.mlyauth.security;

import com.mlyauth.domain.Person;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.LinkedList;

public class PrimaUser extends User {

    private final Person person;

    public PrimaUser(String username, String password, boolean enabled,
                     boolean accountNonExpired, boolean credentialsNonExpired,
                     boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        person = null;
    }

    public PrimaUser(Person person) {
        super(person.getUsername(), person.getPassword(), true, true, true, true, new LinkedList<GrantedAuthority>());
        this.person = person;
    }


    public Person getPerson() {
        return person;
    }
}
