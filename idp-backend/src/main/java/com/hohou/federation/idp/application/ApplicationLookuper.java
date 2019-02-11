package com.hohou.federation.idp.application;

import com.hohou.federation.idp.exception.ApplicationNotFoundExc;

public interface ApplicationLookuper {

    default Application byNameOrError(final String appname) {
        final Application application = byName(appname);
        if (application == null) throw ApplicationNotFoundExc.newInstance();
        return application;
    }

    Application byName(String appname);


}
