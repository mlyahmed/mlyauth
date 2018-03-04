package com.mlyauth.token;

import org.opensaml.xml.security.credential.Credential;

public interface ITokenFactory {

    IDPToken createFreshSAMLResponseToken(Credential credential);

}
