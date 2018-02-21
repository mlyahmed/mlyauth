package com.mlyauth.security.functions.validators;

import com.mlyauth.beans.PersonBean;
import com.mlyauth.constants.ProfileCode;
import com.mlyauth.security.context.IContext;
import com.mlyauth.security.functions.IDPPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class PersonPermissionValidator implements IDPPermissionValidator<PersonBean> {

    @Autowired
    private IContext context;

    @Override
    public boolean hasPermission(Authentication authentication, PersonBean target, Object permission) {
        if (permission instanceof IDPPermission && permission == IDPPermission.CREATE) {
            return context.getProfiles().stream().filter(profile -> profile.getCode() == ProfileCode.MASTER).count() > 0;
        }

        return true;
    }

    @Override
    public Class<PersonBean> support() {
        return PersonBean.class;
    }
}
