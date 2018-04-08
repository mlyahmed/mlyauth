package com.mlyauth.application;

import com.mlyauth.beans.ApplicationBean;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.domain.Application;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ApplicationService implements IApplicationService {

    @Autowired
    private ApplicationDAO applicationDAO;


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

}
