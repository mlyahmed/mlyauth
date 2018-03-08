package com.mlyauth.sso.sp.jose;

import com.mlyauth.constants.AspectAttribute;
import com.mlyauth.dao.ApplicationAspectAttributeDAO;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.ApplicationAspectAttribute;
import com.mlyauth.exception.JOSEErrorException;
import com.mlyauth.key.CredentialManager;
import com.mlyauth.token.jose.JOSEAccessToken;
import com.mlyauth.token.jose.JOSEHelper;
import com.nimbusds.jose.util.Base64URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.util.Map;

import static com.mlyauth.constants.AspectType.IDP_JOSE;
import static com.mlyauth.constants.AttributeType.CERTIFICATE;

public class SPJOSEProcessingFilter extends AbstractAuthenticationProcessingFilter {

    @Autowired
    private CredentialManager credentialManager;

    @Autowired
    private JOSEHelper joseHelper;

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

        return getAuthenticationManager().authenticate(new JOSEAuthenticationToken(reconstituteAccessToken(header)));
    }

    private JOSEAccessToken reconstituteAccessToken(String header) {
        final JOSEAccessToken joseAccessToken = new JOSEAccessToken(getToken(header),
                credentialManager.getLocalPrivateKey(),
                loadPublicKey(joseHelper.loadIssuer(getToken(header), credentialManager.getLocalPrivateKey())));
        joseAccessToken.decipher();
        return joseAccessToken;
    }

    private String getToken(String header) {
        return header.substring(7);
    }

    private PublicKey loadPublicKey(String issuer) {
        try {
            final Application app = applicationDAO.findByAppname(issuer);
            final Map<AspectAttribute, ApplicationAspectAttribute> attributes = attributeDAO.findAndIndex(app.getId(), IDP_JOSE.name());
            final ApplicationAspectAttribute certificate = attributes.get(AspectAttribute.get(IDP_JOSE, CERTIFICATE));
            ByteArrayInputStream inputStream = new ByteArrayInputStream(new Base64URL(certificate.getValue()).decode());
            return CertificateFactory.getInstance("X.509").generateCertificate(inputStream).getPublicKey();
        } catch (Exception e) {
            throw JOSEErrorException.newInstance(e);
        }
    }


}
