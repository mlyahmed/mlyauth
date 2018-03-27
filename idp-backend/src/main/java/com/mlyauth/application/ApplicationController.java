package com.mlyauth.application;

import com.mlyauth.beans.ApplicationBean;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.domain.Application;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/domain/application")
public class ApplicationController {

    @Autowired
    private ApplicationDAO applicationDAO;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody ApplicationBean newApplication(@RequestBody ApplicationBean application) {
        Application app = new Application();
        BeanUtils.copyProperties(application, app);
        app = applicationDAO.save(app);
        application.setId(app.getId());
        return application;
    }

    @PutMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public @ResponseBody ApplicationBean updateApplication(@RequestBody ApplicationBean application) {
        Application app = applicationDAO.findByAppname(application.getAppname());
        BeanUtils.copyProperties(application, app);
        applicationDAO.save(app);
        return application;
    }

}
