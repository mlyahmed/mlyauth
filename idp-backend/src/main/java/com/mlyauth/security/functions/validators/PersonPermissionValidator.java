package com.mlyauth.security.functions.validators;

import com.mlyauth.beans.PersonBean;
import com.mlyauth.constants.ProfileCode;
import com.mlyauth.security.functions.IDPPermission;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class PersonPermissionValidator implements IDPPermissionValidator<PersonBean> {

    @Override
    public boolean hasPermission(Authentication authentication, PersonBean person, Object permission) {
        if (authentication == null) return false;
        if (person == null) return true;

        if (permission instanceof IDPPermission && permission == IDPPermission.CREATE) {
            return authentication.getAuthorities()
                    .stream().filter(aut -> ProfileCode.MASTER.name().equals(aut.getAuthority())).count() > 0;
        }

        return true;
    }

    @Override
    public Class<PersonBean> support() {
        return PersonBean.class;
    }
}
