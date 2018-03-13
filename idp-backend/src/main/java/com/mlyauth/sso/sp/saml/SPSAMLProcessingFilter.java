package com.mlyauth.sso.sp.saml;

import com.google.common.base.Stopwatch;
import com.mlyauth.constants.TokenPurpose;
import com.mlyauth.context.IContext;
import com.mlyauth.dao.NavigationDAO;
import com.mlyauth.dao.TokenDAO;
import com.mlyauth.domain.Navigation;
import com.mlyauth.domain.NavigationAttribute;
import com.mlyauth.domain.Token;
import com.mlyauth.exception.IDPSAMLErrorException;
import com.mlyauth.token.ITokenFactory;
import com.mlyauth.token.TokenMapper;
import com.mlyauth.token.saml.SAMLAccessToken;
import com.mlyauth.token.saml.SAMLHelper;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.saml.SAMLProcessingFilter;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.metadata.MetadataManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import static com.mlyauth.constants.Direction.INBOUND;
import static java.util.Arrays.asList;

public class SPSAMLProcessingFilter extends SAMLProcessingFilter {

    @Autowired
    private IContext context;

    @Autowired
    private MetadataManager metadataManager;

    @Autowired
    private KeyManager keyManager;

    @Autowired
    private TokenDAO tokenDAO;

    @Autowired
    private NavigationDAO navigationDAO;

    @Autowired
    private TokenMapper tokenMapper;

    @Autowired
    private ITokenFactory tokenFactory;

    @Autowired
    private SAMLHelper samlHelper;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        final Stopwatch started = Stopwatch.createStarted();
        try {

            if (!"POST".equals(request.getMethod())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource Not Found");
                return null;
            } else {
                final Authentication authentication = super.attemptAuthentication(request, response);
                traceNavigation(request, response, started);
                return authentication;
            }

        } catch (Exception e) {
            logger.error("Incoming SAML message is invalid", e);
            throw new AuthenticationServiceException("Incoming SAML message is invalid", e);
        }
    }

    private void traceNavigation(HttpServletRequest request, HttpServletResponse response, Stopwatch started) {
        String serialized = request.getParameter("SAMLResponse");
        final SAMLAccessToken access = loadAccess(serialized, request, response);
        final Token token = saveToken(access);
        final Navigation navigation = buildNavigation(access, token);
        navigation.setAttributes(buildAttributes(serialized));
        navigation.setTimeConsumed(started.elapsed(TimeUnit.MILLISECONDS));
        navigationDAO.save(navigation);
    }

    private SAMLAccessToken loadAccess(String serialized, HttpServletRequest request, HttpServletResponse response) {
        SAMLMessageContext messageContext = retrieveMessageContext(request, response);
        final SAMLAccessToken access = tokenFactory.createSAMLAccessToken(serialized, buildCredential(messageContext));
        access.decipher();
        return access;
    }

    private SAMLMessageContext retrieveMessageContext(HttpServletRequest request, HttpServletResponse response) {
        try {
            SAMLMessageContext messageContext = contextProvider.getLocalAndPeerEntity(request, response);
            processor.retrieveMessage(messageContext);
            return messageContext;
        } catch (Exception e) {
            throw IDPSAMLErrorException.newInstance(e);
        }
    }

    private Token saveToken(SAMLAccessToken access) {
        Token token = tokenMapper.toToken(access);
        token.setPurpose(TokenPurpose.NAVIGATION).setSession(context.getAuthenticationSession());
        return tokenDAO.save(token);
    }

    private Navigation buildNavigation(SAMLAccessToken access, Token token) {
        return Navigation.newInstance()
                .setCreatedAt(new Date())
                .setDirection(INBOUND)
                .setTargetURL(access.getTargetURL())
                .setToken(token)
                .setSession(this.context.getAuthenticationSession());
    }

    private HashSet<NavigationAttribute> buildAttributes(String encodedMessage) {
        return new HashSet<>(asList(NavigationAttribute.newInstance()
                .setCode("SAMLResponse")
                .setAlias("SAMLResponse")
                .setValue(encodedMessage)));
    }

    private BasicX509Credential buildCredential(SAMLMessageContext context) {
        try {

            final EntityDescriptor idpDescriptor = metadataManager.getEntityDescriptor(context.getPeerEntityId());
            final KeyInfoCredentialResolver keyInfoResolver = context.getLocalTrustEngine().getKeyInfoResolver();
            final Credential peerCredential = samlHelper.getSigningCredential(idpDescriptor, keyInfoResolver);
            BasicX509Credential decipherCred = new BasicX509Credential();
            decipherCred.setPrivateKey(keyManager.getDefaultCredential().getPrivateKey());
            decipherCred.setEntityCertificate(((BasicX509Credential) peerCredential).getEntityCertificate());

            return decipherCred;
        } catch (Exception e) {
            throw IDPSAMLErrorException.newInstance(e);
        }
    }


}
