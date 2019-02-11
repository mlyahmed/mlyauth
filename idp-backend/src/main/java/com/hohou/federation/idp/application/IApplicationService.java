package com.hohou.federation.idp.application;

public interface IApplicationService {
    ApplicationBean newApplication(ApplicationBean application);
    ApplicationBean updateApplication(ApplicationBean application);
}
