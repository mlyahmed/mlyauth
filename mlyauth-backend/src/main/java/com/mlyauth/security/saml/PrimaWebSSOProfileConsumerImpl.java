package com.mlyauth.security.saml;

import org.opensaml.common.SAMLException;
import org.opensaml.common.SAMLObject;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.validation.ValidationException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.websso.WebSSOProfileConsumerImpl;

public class PrimaWebSSOProfileConsumerImpl extends WebSSOProfileConsumerImpl {

    public SAMLCredential processAuthenticationResponse(SAMLMessageContext context) throws SAMLException, org.opensaml.xml.security.SecurityException, ValidationException, DecryptionException {
        verify(context);
        this.setIncludeAllAttributes(true);
        this.setResponseSkew(60); // 10 mn
        this.setMaxAuthenticationAge(72000); // 20 hours
        this.setMaxAssertionTime(3000); // 50 mn
        return super.processAuthenticationResponse(context);
    }

    private void verify(SAMLMessageContext context) throws SAMLException {
        SAMLObject message = context.getInboundSAMLMessage();
        checkInBoundMessageIsResponse(message);
        checkResponseIsSigned((Response) message);
        checkResponseAssertionsAreEncrypted((Response) message);
    }

    private void checkInBoundMessageIsResponse(SAMLObject message) throws SAMLException {
        if (!(message instanceof Response))
            throw new SAMLException("Message is not of a Response object type");
    }

    private void checkResponseIsSigned(Response response) throws SAMLException {
        if (response.getSignature() == null)
            throw new SAMLException("The Response must be signed");
    }

    private void checkResponseAssertionsAreEncrypted(Response response) throws SAMLException {
        if (response.getEncryptedAssertions().isEmpty())
            throw new SAMLException("The Response Assertions must be encrypted");
    }

}
