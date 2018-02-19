package com.mlyauth.security.context;

import com.mlyauth.domain.Person;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Date;
import java.util.LinkedList;

import static com.mlyauth.constants.AuthenticationInfoStatus.ACTIVE;
import static com.mlyauth.constants.AuthenticationInfoStatus.LOCKED;

public class IDPUser extends User {

    private final IContext context;

    public IDPUser(IContext context) {
        super(context.getLogin(),
                context.getPassword(),
                context.getAuthenticationInfo().getStatus() == ACTIVE,
                context.getAuthenticationInfo().getExpireAt().after(new Date()),
                true,
                context.getAuthenticationInfo().getStatus() != LOCKED,
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
