package com.mlyauth.security.sso.sp;

import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.Person;
import com.mlyauth.security.context.IContextHolder;
import com.mlyauth.security.context.PrimaUser;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.impl.XSStringImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.stream.Stream;

import static com.mlyauth.beans.AttributeBean.*;

@Service
@Transactional
public class SAMLUserDetailsServiceImpl implements SAMLUserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(SAMLUserDetailsServiceImpl.class);


    @Autowired
    private PersonDAO personDAO;


    @Autowired
    private IContextHolder contextHolder;

    public Object loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {
        //HttpSession request = (HttpSession) RequestContextHolder.getRequestAttributes().resolveReference(RequestAttributes.REFERENCE_SESSION);
        checkCredentials(credential);
        return new PrimaUser(personDAO.findByExternalId(credential.getAttributeAsString(SAML_RESPONSE_CLIENT_ID.getCode())));
    }

    private void checkCredentials(SAMLCredential credential) {
        Assert.notNull(credential, "SAML Credential is null");
        Assert.notEmpty(credential.getAttributes(), "SAML Credential : Attributes List is empty");
        logAttributes(credential);

        Assert.notNull(credential.getAttributeAsString(SAML_RESPONSE_CLIENT_ID.getCode()), "SAML Credential : The clientId attribute is undefined");
        Assert.notNull(credential.getAttributeAsString(SAML_RESPONSE_PROFILE.getCode()), "SAML Credential : The profile code attribute is undefined");

        final Person person = personDAO.findByExternalId(credential.getAttributeAsString(SAML_RESPONSE_CLIENT_ID.getCode()));
        Assert.notNull(person, "SAML Credential : Person Not Found");

        if (credential.getAttributeAsString(SAML_RESPONSE_APP.getCode()) != null) {
            final boolean assigned = person.getApplications().stream().anyMatch(app -> app.getAppname().equals(credential.getAttributeAsString(SAML_RESPONSE_APP.getCode())));
            Assert.isTrue(assigned, "SAML Credential : The application is not assigned to the person");
        }
    }

    private void logAttributes(SAMLCredential samlCredential) {
        samlCredential.getAttributes().stream().forEach(attribute -> {
            final Stream<XMLObject> values = attribute.getAttributeValues().stream();
            values.filter(value -> (value instanceof XSStringImpl)).map(value -> (XSStringImpl) value).forEach(
                    v -> logger.info(String.format("SAML RESPONSE ATTRIBUTE (%s) : (%s)", attribute.getName(), v.getValue())));
        });
    }

}
