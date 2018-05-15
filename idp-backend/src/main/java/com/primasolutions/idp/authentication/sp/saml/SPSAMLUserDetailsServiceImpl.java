package com.primasolutions.idp.authentication.sp.saml;

import com.primasolutions.idp.context.IContext;
import com.primasolutions.idp.context.IContextHolder;
import com.primasolutions.idp.context.IDPUser;
import com.primasolutions.idp.person.Person;
import com.primasolutions.idp.person.PersonDAO;
import com.primasolutions.idp.token.Claims;
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

import static org.springframework.util.Assert.notEmpty;
import static org.springframework.util.Assert.notNull;

@Service
@Transactional
public class SPSAMLUserDetailsServiceImpl implements SAMLUserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SPSAMLUserDetailsServiceImpl.class);


    @Autowired
    private PersonDAO personDAO;


    @Autowired
    private IContextHolder contextHolder;

    public Object loadUserBySAML(final SAMLCredential cred) throws UsernameNotFoundException {
        checkCredentials(cred);
        final Person person = personDAO.findByExternalId(cred.getAttributeAsString(Claims.CLIENT_ID.getValue()));
        final IContext ctx = contextHolder.newPersonContext(person);
        cred.getAttributes().forEach(att -> ctx.putAttribute(att.getName(), cred.getAttributeAsString(att.getName())));
        return new IDPUser(ctx);
    }

    private void checkCredentials(final SAMLCredential credential) {
        notNull(credential, "SAML Credential is null");
        notEmpty(credential.getAttributes(), "SAML Credential : Attributes List is empty");
        logAttributes(credential);

        notNull(credential.getAttributeAsString(Claims.CLIENT_ID.getValue()), "The clientId attribute is missing");
        notNull(credential.getAttributeAsString(Claims.CLIENT_PROFILE.getValue()), "The profile attribute is missing");

        final Person person = personDAO.findByExternalId(credential.getAttributeAsString(Claims.CLIENT_ID.getValue()));
        notNull(person, "SAML Credential : Person Not Found");

        if (credential.getAttributeAsString(Claims.APPLICATION.getValue()) != null) {
            final boolean assigned = person.getApplications().stream().anyMatch(app -> app.getAppname()
                    .equals(credential.getAttributeAsString(Claims.APPLICATION.getValue())));
            Assert.isTrue(assigned, "SAML Credential : The application is not assigned to the person");
        }
    }

    private void logAttributes(final SAMLCredential samlCredential) {
        samlCredential.getAttributes().stream().forEach(attribute -> {
            final Stream<XMLObject> values = attribute.getAttributeValues().stream();
            values.filter(value -> (value instanceof XSStringImpl)).map(value -> (XSStringImpl) value).forEach(
                    v -> LOGGER.info(String.format("SAML ATTRIBUTE (%s) : (%s)", attribute.getName(), v.getValue())));
        });
    }

}
