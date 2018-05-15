package com.primasolutions.idp.authentication.sp.saml;

import com.google.common.base.Stopwatch;
import com.primasolutions.idp.constants.Direction;
import com.primasolutions.idp.constants.TokenPurpose;
import com.primasolutions.idp.constants.TokenStatus;
import com.primasolutions.idp.context.IContext;
import com.primasolutions.idp.exception.IDPSAMLErrorException;
import com.primasolutions.idp.navigation.Navigation;
import com.primasolutions.idp.navigation.NavigationAttribute;
import com.primasolutions.idp.navigation.NavigationDAO;
import com.primasolutions.idp.token.Token;
import com.primasolutions.idp.token.TokenDAO;
import com.primasolutions.idp.token.TokenMapper;
import com.primasolutions.idp.token.saml.SAMLAccessToken;
import com.primasolutions.idp.token.saml.SAMLHelper;
import com.primasolutions.idp.token.saml.SAMLTokenFactory;
import org.apache.commons.codec.digest.DigestUtils;
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
    private SAMLTokenFactory tokenFactory;

    @Autowired
    private SAMLHelper samlHelper;

    @Override
    public Authentication attemptAuthentication(final HttpServletRequest req, final HttpServletResponse res)
            throws AuthenticationException {
        final Stopwatch started = Stopwatch.createStarted();
        try {

            if (!"POST".equals(req.getMethod())) {
                res.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource Not Found");
                return null;
            } else {
                final Authentication authentication = super.attemptAuthentication(req, res);
                traceNavigation(req, res, started);
                return authentication;
            }

        } catch (Exception e) {
            logger.error("Incoming SAML message is invalid", e);
            throw new AuthenticationServiceException("Incoming SAML message is invalid", e);
        }
    }

    private void traceNavigation(final HttpServletRequest req, final HttpServletResponse res,
                                 final Stopwatch started) {
        String serialized = req.getParameter("SAMLResponse");
        final SAMLAccessToken access = loadAccess(serialized, req, res);
        final Token token = saveToken(access, req);
        final Navigation navigation = buildNavigation(access, token);
        navigation.setAttributes(buildAttributes(serialized));
        navigation.setTimeConsumed(started.elapsed(TimeUnit.MILLISECONDS));
        navigationDAO.save(navigation);
    }

    private SAMLAccessToken loadAccess(final String serialized, final HttpServletRequest req,
                                       final HttpServletResponse res) {
        SAMLMessageContext messageContext = retrieveMessageContext(req, res);
        final SAMLAccessToken access = tokenFactory.createAccessToken(serialized, buildCredential(messageContext));
        access.decipher();
        return access;
    }

    private SAMLMessageContext retrieveMessageContext(final HttpServletRequest req, final HttpServletResponse res) {
        try {
            SAMLMessageContext messageContext = contextProvider.getLocalAndPeerEntity(req, res);
            processor.retrieveMessage(messageContext);
            return messageContext;
        } catch (Exception e) {
            throw IDPSAMLErrorException.newInstance(e);
        }
    }

    private Token saveToken(final SAMLAccessToken access, final HttpServletRequest req) {
        Token token = tokenMapper.toToken(access);
        token.setPurpose(TokenPurpose.NAVIGATION).setSession(context.getAuthenticationSession());
        token.setStatus(TokenStatus.CHECKED);
        token.setChecksum(DigestUtils.sha256Hex(req.getParameter("SAMLResponse")));
        return tokenDAO.save(token);
    }

    private Navigation buildNavigation(final SAMLAccessToken access, final Token token) {
        return Navigation.newInstance()
                .setCreatedAt(new Date())
                .setDirection(Direction.INBOUND)
                .setTargetURL(access.getTargetURL())
                .setToken(token)
                .setSession(this.context.getAuthenticationSession());
    }

    private HashSet<NavigationAttribute> buildAttributes(final String encodedMessage) {
        return new HashSet<>(asList(NavigationAttribute.newInstance()
                .setCode("SAMLResponse")
                .setAlias("SAMLResponse")
                .setValue(encodedMessage)));
    }

    private BasicX509Credential buildCredential(final SAMLMessageContext context) {
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
