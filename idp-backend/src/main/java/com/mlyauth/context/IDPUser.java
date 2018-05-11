package com.mlyauth.context;

import com.mlyauth.domain.Application;
import com.mlyauth.domain.Person;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Date;
import java.util.Set;

import static com.mlyauth.constants.AuthenticationInfoStatus.ACTIVE;
import static com.mlyauth.constants.AuthenticationInfoStatus.LOCKED;
import static java.util.stream.Collectors.toSet;

public class IDPUser extends User {

    private final IContext context;

    public IDPUser(final IContext context) {
        super(context.getLogin(),
                context.getPassword(),
                context.getAuthenticationInfo().getStatus() == ACTIVE,
                context.getAuthenticationInfo().getExpireAt().after(new Date()),
                true,
                context.getAuthenticationInfo().getStatus() != LOCKED,
                toAuthorities(context)
        );

        this.context = context;
    }

    private static Set<SimpleGrantedAuthority> toAuthorities(final IContext context) {
        return context.getProfiles().stream()
                .map(profile -> new SimpleGrantedAuthority(profile.getCode().name()))
                .collect(toSet());
    }


    public Person getPerson() {
        return context.getPerson();
    }

    public IContext getContext() {
        return context;
    }

    public Application getApplication() {
        return context.getApplication();
    }

    public boolean isAPerson() {
        return context.isAPerson();
    }

    public boolean isAnApplication() {
        return context.isAnApplication();
    }
}
