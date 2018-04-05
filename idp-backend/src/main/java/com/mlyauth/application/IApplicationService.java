package com.mlyauth.application;

import com.mlyauth.beans.ApplicationBean;

public interface IApplicationService {
    ApplicationBean newApplication(final ApplicationBean application);
    ApplicationBean updateApplication(final ApplicationBean application);
}
