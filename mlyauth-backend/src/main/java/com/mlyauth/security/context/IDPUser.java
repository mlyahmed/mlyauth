package com.mlyauth.security.context;

import com.mlyauth.domain.Person;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.LinkedList;

public class IDPUser extends User {

    private final IContext context;

    public IDPUser(IContext context) {
        super(context.getLogin(),
                context.getPassword(),
                true,
                true,
                true,
                true,
                new LinkedList<GrantedAuthority>());
        this.context = context;
    }


    public Person getPerson() {
        return context.getPerson();
    }

    public IContext getContext() {
        return context;
    }
}
