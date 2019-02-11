package com.hohou.federation.idp.authentication.sp.saml;

import com.hohou.federation.idp.application.Application;

public interface ISPSAMLAspectValidator {
    void validate(Application application);
}
