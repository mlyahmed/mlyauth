package com.hohou.federation.idp.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ApplicationLookuperImpl implements ApplicationLookuper {

    @Autowired
    protected ApplicationDAO applicationDAO;

    @Override
    public Application byName(final String appname) {
        return applicationDAO.findByAppname(appname);
    }
}
