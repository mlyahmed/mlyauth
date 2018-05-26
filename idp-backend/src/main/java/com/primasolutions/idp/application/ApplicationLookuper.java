package com.primasolutions.idp.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ApplicationLookuper {

    @Autowired
    protected ApplicationDAO applicationDAO;

    public Application byName(final String appname) {
        return applicationDAO.findByAppname(appname);
    }
}
