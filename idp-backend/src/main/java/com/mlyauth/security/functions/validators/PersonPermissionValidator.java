package com.mlyauth.security.functions.validators;

import com.mlyauth.beans.PersonBean;
import com.mlyauth.constants.ProfileCode;
import com.mlyauth.security.functions.IDPPermission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class PersonPermissionValidator implements IDPPermissionValidator<PersonBean> {

    @Override
    public boolean hasPermission(Authentication authentication, PersonBean person, Object permission) {
        if (authentication == null) return false;
        if (person == null) return true;

        if (permission instanceof IDPPermission && permission == IDPPermission.CREATE) {
            return authentication.getAuthorities()
                    .stream().filter(aut -> (isMaster(aut) || isFeeder(aut))).count() > 0;
        }

        return true;
    }

    private boolean isMaster(GrantedAuthority aut) {
        return ProfileCode.MASTER.name().equals(aut.getAuthority());
    }

    private boolean isFeeder(GrantedAuthority aut) {
        return ProfileCode.MASTER.name().equals(aut.getAuthority());
    }

    @Override
    public Class<PersonBean> support() {
        return PersonBean.class;
    }
}
