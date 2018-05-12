package com.mlyauth.atests.world;

import com.mlyauth.beans.ApplicationBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Scope("cucumber-glue")
public class ApplicationsHolder {

    private Map<String, ApplicationBean> applications = new HashMap<>();

    public void addApplication(final ApplicationBean application) {
        applications.put(application.getAppname(), application);
    }

    public ApplicationBean getApplication(final String appname) {
        return applications.get(appname);
    }

}
