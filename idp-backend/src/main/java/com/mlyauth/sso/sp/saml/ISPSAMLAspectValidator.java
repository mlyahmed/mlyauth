package com.mlyauth.sso.sp.saml;

import com.mlyauth.domain.Application;

public interface ISPSAMLAspectValidator {
    void validate(Application application);
}
