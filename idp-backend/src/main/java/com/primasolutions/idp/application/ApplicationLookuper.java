package com.primasolutions.idp.application;

import com.primasolutions.idp.dao.ApplicationDAO;
import com.primasolutions.idp.domain.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ApplicationLookuper {

    @Autowired
    private ApplicationDAO applicationDAO;

    public Application byName(final String appname) {
        return applicationDAO.findByAppname(appname);
    }
}
