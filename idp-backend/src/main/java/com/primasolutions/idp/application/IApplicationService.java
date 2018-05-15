package com.primasolutions.idp.application;

public interface IApplicationService {
    ApplicationBean newApplication(ApplicationBean application);
    ApplicationBean updateApplication(ApplicationBean application);
}
