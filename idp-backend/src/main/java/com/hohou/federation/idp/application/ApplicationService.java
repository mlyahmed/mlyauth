package com.hohou.federation.idp.application;

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
    private ApplicationTypeDAO applicationTypeDAO;

    @Override
    public ApplicationBean newApplication(final ApplicationBean application) {
        Application app = new Application();
        BeanUtils.copyProperties(application, app);
        app.setType(applicationTypeDAO.findById(application.getType()).orElse(null));
        app = applicationDAO.save(app);
        application.setId(app.getId());
        return application;
    }

    @Override
    public ApplicationBean updateApplication(final ApplicationBean application) {
        Application app = applicationDAO.findByAppname(application.getAppname());
        BeanUtils.copyProperties(application, app);
        applicationDAO.save(app);
        return application;
    }

}
