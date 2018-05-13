package com.primasolutions.idp.application;

import com.primasolutions.idp.beans.ApplicationBean;

public interface IApplicationService {
    ApplicationBean newApplication(ApplicationBean application);
    ApplicationBean updateApplication(ApplicationBean application);
}
