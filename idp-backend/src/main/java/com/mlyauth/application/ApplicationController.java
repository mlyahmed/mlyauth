package com.mlyauth.application;

import com.mlyauth.beans.ApplicationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/domain/application")
public class ApplicationController {

    @Autowired
    private IApplicationService applicationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody ApplicationBean newApplication(@RequestBody ApplicationBean application) {
        return applicationService.newApplication(application);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public @ResponseBody ApplicationBean updateApplication(@RequestBody ApplicationBean application) {
        return applicationService.updateApplication(application);
    }

}
