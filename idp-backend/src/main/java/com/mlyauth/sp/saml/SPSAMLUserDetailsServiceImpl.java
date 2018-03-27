package com.mlyauth.sp.saml;

import com.mlyauth.context.IContext;
import com.mlyauth.context.IContextHolder;
import com.mlyauth.context.IDPUser;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.Person;
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

import static com.mlyauth.token.Claims.*;
import static org.springframework.util.Assert.notEmpty;
import static org.springframework.util.Assert.notNull;

@Service
@Transactional
public class SPSAMLUserDetailsServiceImpl implements SAMLUserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(SPSAMLUserDetailsServiceImpl.class);


    @Autowired
    private PersonDAO personDAO;


    @Autowired
    private IContextHolder contextHolder;

    public Object loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {
        checkCredentials(credential);
        final Person person = personDAO.findByExternalId(credential.getAttributeAsString(CLIENT_ID.getValue()));
        final IContext context = contextHolder.newPersonContext(person);
        credential.getAttributes().forEach(attr -> context.putAttribute(attr.getName(), credential.getAttributeAsString(attr.getName())));
        return new IDPUser(context);
    }

    private void checkCredentials(SAMLCredential credential) {
        notNull(credential, "SAML Credential is null");
        notEmpty(credential.getAttributes(), "SAML Credential : Attributes List is empty");
        logAttributes(credential);

        notNull(credential.getAttributeAsString(CLIENT_ID.getValue()), "SAML Credential : The clientId attribute is undefined");
        notNull(credential.getAttributeAsString(CLIENT_PROFILE.getValue()), "SAML Credential : The profile code attribute is undefined");

        final Person person = personDAO.findByExternalId(credential.getAttributeAsString(CLIENT_ID.getValue()));
        notNull(person, "SAML Credential : Person Not Found");

        if (credential.getAttributeAsString(APPLICATION.getValue()) != null) {
            final boolean assigned = person.getApplications().stream().anyMatch(app -> app.getAppname().equals(credential.getAttributeAsString(APPLICATION.getValue())));
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
