package com.mlyauth.security.saml;

import com.mlyauth.security.PrimaUser;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.impl.XSStringImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class SAMLUserDetailsServiceImpl implements SAMLUserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(SAMLUserDetailsServiceImpl.class);


    public Object loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {
        logAttributes(credential);
        String userID = credential.getNameID().getValue();
        logger.info(userID + " is logged in");
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        authorities.add(authority);
        return new PrimaUser(userID, "<abc123>", true, true, true, true, authorities);
    }

    private void logAttributes(SAMLCredential samlCredential) {
        samlCredential.getAttributes().stream().forEach(attribute -> {
            final Stream<XMLObject> values = attribute.getAttributeValues().stream();
            values.filter(value -> (value instanceof XSStringImpl)).map(value -> (XSStringImpl) value).forEach(
                    v -> logger.info(String.format("SAML RESPONSE ATTRIBUTE (%s) : (%s)", attribute.getName(), v.getValue())));
        });
    }

}
