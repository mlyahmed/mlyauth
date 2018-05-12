package com.mlyauth.application;

import com.mlyauth.beans.ApplicationBean;

public interface IApplicationService {
    ApplicationBean newApplication(ApplicationBean application);
    ApplicationBean updateApplication(ApplicationBean application);
}
