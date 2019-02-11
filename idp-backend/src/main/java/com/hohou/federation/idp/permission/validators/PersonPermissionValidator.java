package com.hohou.federation.idp.permission.validators;

import com.hohou.federation.idp.constants.ProfileCode;
import com.hohou.federation.idp.permission.IDPPermission;
import com.hohou.federation.idp.person.model.PersonBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class PersonPermissionValidator implements IDPPermissionValidator<PersonBean> {

    @Override
    public boolean hasPermission(final Authentication auth, final PersonBean person, final Object permission) {
        if (auth == null) return false;
        if (person == null) return true;

        if (permission instanceof IDPPermission && permission == IDPPermission.CREATE) {
            return auth.getAuthorities()
                    .stream().filter(aut -> (isMaster(aut) || isFeeder(aut))).count() > 0;
        }

        return true;
    }

    private boolean isMaster(final GrantedAuthority aut) {
        return ProfileCode.MASTER.name().equals(aut.getAuthority());
    }

    private boolean isFeeder(final GrantedAuthority aut) {
        return ProfileCode.MASTER.name().equals(aut.getAuthority());
    }

    @Override
    public Class<PersonBean> support() {
        return PersonBean.class;
    }
}
