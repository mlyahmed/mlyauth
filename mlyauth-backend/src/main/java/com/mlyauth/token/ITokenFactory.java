package com.mlyauth.token;

import com.mlyauth.token.saml.SAMLAccessToken;
import org.opensaml.xml.security.credential.Credential;

public interface ITokenFactory {

    SAMLAccessToken createSAMLAccessToken(Credential credential);

}
