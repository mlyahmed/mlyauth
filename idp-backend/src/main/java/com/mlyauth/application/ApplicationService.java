package com.mlyauth.application;

import com.mlyauth.beans.ApplicationBean;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.Person;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ApplicationService implements IApplicationService {

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private PersonDAO personDAO;

    @Override
    public ApplicationBean newApplication(final ApplicationBean application){
        Application app = new Application();
        BeanUtils.copyProperties(application, app);
        app = applicationDAO.save(app);
        application.setId(app.getId());
        return application;
    }

    @Override
    public ApplicationBean updateApplication(ApplicationBean application) {
        Application app = applicationDAO.findByAppname(application.getAppname());
        BeanUtils.copyProperties(application, app);
        applicationDAO.save(app);
        return application;
    }

    @Override
    public void assignApplication(String appname, String personExternalId) {
        final Person person = personDAO.findByExternalId(personExternalId);
        final Application application = applicationDAO.findByAppname(appname);
        person.getApplications().add(application);
        personDAO.saveAndFlush(person);
    }

}
