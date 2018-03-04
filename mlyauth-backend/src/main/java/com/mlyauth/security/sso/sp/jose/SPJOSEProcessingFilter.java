package com.mlyauth.security.sso.sp.jose;

import com.mlyauth.constants.AuthAspectAttribute;
import com.mlyauth.constants.AuthAspectType;
import com.mlyauth.dao.ApplicationAspectAttributeDAO;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.ApplicationAspectAttribute;
import com.mlyauth.exception.JOSEErrorException;
import com.mlyauth.token.jose.JOSEAccessToken;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

public class SPJOSEProcessingFilter extends AbstractAuthenticationProcessingFilter {

    @Autowired
    private KeyManager keyManager;

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private ApplicationAspectAttributeDAO attributeDAO;

    public static final String FILTER_URL = "/sp/jose/sso";

    public SPJOSEProcessingFilter() {
        this(FILTER_URL);
    }

    protected SPJOSEProcessingFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
        setFilterProcessesUrl(defaultFilterProcessesUrl);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer "))
            throw JOSEErrorException.newInstance();

        final String encodedToken = header.substring(7);
        String issuer = loadIssuer(encodedToken);
        final JOSEAccessToken joseAccessToken = new JOSEAccessToken(encodedToken, keyManager.getDefaultCredential().getPrivateKey(), loadPublicKey(issuer));
        joseAccessToken.decipher();
        JOSEAuthenticationToken token = new JOSEAuthenticationToken(joseAccessToken);
        return getAuthenticationManager().authenticate(token);
    }

    private RSAPublicKey loadPublicKey(String issuer) {
        try {
            final Application app = applicationDAO.findByAppname(issuer);
            final Map<AuthAspectAttribute, ApplicationAspectAttribute> attributes = attributeDAO.findAndIndex(app.getId(), AuthAspectType.IDP_JOSE.name());
            final ApplicationAspectAttribute certificate = attributes.get(AuthAspectAttribute.IDP_JOSE_ENCRYPTION_CERTIFICATE);
            Base64URL decoder = new Base64URL(certificate.getValue());
            ByteArrayInputStream inputStream = new ByteArrayInputStream(decoder.decode());
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            final X509Certificate x509Certificate = (X509Certificate) certFactory.generateCertificate(inputStream);
            return (RSAPublicKey) x509Certificate.getPublicKey();
        } catch (Exception e) {
            throw JOSEErrorException.newInstance(e);
        }
    }

    private String loadIssuer(String encodedToken) {
        try {
            EncryptedJWT tokenHolder = EncryptedJWT.parse(encodedToken);
            tokenHolder.decrypt(new RSADecrypter(keyManager.getDefaultCredential().getPrivateKey()));
            final SignedJWT signedJWT = tokenHolder.getPayload().toSignedJWT();
            return signedJWT.getHeader().getKeyID();
        } catch (Exception e) {
            throw new BadCredentialsException("Couldn't verify the credentials", e);
        }
    }

}
