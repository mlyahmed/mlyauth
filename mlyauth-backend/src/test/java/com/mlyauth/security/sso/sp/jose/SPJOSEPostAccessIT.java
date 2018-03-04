package com.mlyauth.security.sso.sp.jose;

import com.mlyauth.AbstractIntegrationTest;
import com.mlyauth.constants.AuthAspectAttribute;
import com.mlyauth.constants.AuthAspectType;
import com.mlyauth.dao.ApplicationAspectAttributeDAO;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.ApplicationAspectAttribute;
import com.mlyauth.domain.ApplicationAspectAttributeId;
import com.mlyauth.tools.KeysForTests;
import com.nimbusds.jose.util.Base64URL;
import javafx.util.Pair;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;

public class SPJOSEPostAccessIT extends AbstractIntegrationTest {

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private ApplicationAspectAttributeDAO appAspectAttrDAO;

    @Test
    public void when_post_a_true_access_from_a_defined_idp_then_OK() throws CertificateEncodingException {
        final Pair<PrivateKey, X509Certificate> credential = KeysForTests.generateRSACredential();
        Application linkAssu = Application.newInstance()
                .setAppname("LinkASSU")
                .setTitle("Link ASSU")
                .setAspects(new HashSet<>(Arrays.asList(AuthAspectType.IDP_JOSE)));
        linkAssu = applicationDAO.save(linkAssu);

        final ApplicationAspectAttribute linkAssuID = ApplicationAspectAttribute.newInstance()
                .setId(ApplicationAspectAttributeId.newInstance()
                        .setApplicationId(linkAssu.getId())
                        .setAspectCode(AuthAspectType.IDP_JOSE.name())
                        .setAttributeCode(AuthAspectAttribute.IDP_JOSE_ENTITY_ID.getValue()))
                .setValue("LinkAssuDev");

        final ApplicationAspectAttribute linkAssuSSOURL = ApplicationAspectAttribute.newInstance()
                .setId(ApplicationAspectAttributeId.newInstance()
                        .setApplicationId(linkAssu.getId())
                        .setAspectCode(AuthAspectType.IDP_JOSE.name())
                        .setAttributeCode(AuthAspectAttribute.IDP_JOSE_SSO_URL.getValue()))
                .setValue("http://localhost/idp/jose/sso");

        final ApplicationAspectAttribute linkAssuCertificate = ApplicationAspectAttribute.newInstance()
                .setId(ApplicationAspectAttributeId.newInstance()
                        .setApplicationId(linkAssu.getId())
                        .setAspectCode(AuthAspectType.IDP_JOSE.name())
                        .setAttributeCode(AuthAspectAttribute.IDP_JOSE_ENCRYPTION_CERTIFICATE.getValue()))
                .setValue(Base64URL.encode(credential.getValue().getEncoded()).toString());


        appAspectAttrDAO.save(Arrays.asList(linkAssuID, linkAssuSSOURL, linkAssuCertificate));

    }
}
