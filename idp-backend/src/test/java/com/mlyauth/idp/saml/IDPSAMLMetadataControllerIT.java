package com.mlyauth.idp.saml;

import com.mlyauth.AbstractIntegrationTest;
import com.mlyauth.token.saml.SAMLHelper;
import org.exparity.hamcrest.date.LocalDateMatchers;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml2.metadata.impl.EntityDescriptorImpl;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.signature.X509Certificate;
import org.opensaml.xml.signature.X509Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.saml.SAMLBootstrap;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.opensaml.common.xml.SAMLConstants.SAML20P_NS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

public class IDPSAMLMetadataControllerIT extends AbstractIntegrationTest {

    @Value("${idp.saml.entityId}")
    private String idpEntityId;

    @Autowired
    private SAMLHelper samlHelper;

    @Autowired
    private Filter samlFilter;

    @Autowired
    private Filter metadataGeneratorFilter;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private SAMLBootstrap samlBootstrap;

    private MockMvc mockMvc;
    private MvcResult result;
    private List<KeyDescriptor> keyDescriptors;
    private KeyDescriptor signingKey;
    private X509Certificate x509Certificate;
    private EntityDescriptorImpl metadata;


    @Before
    public void setup() {
        samlBootstrap.postProcessBeanFactory(null);
        this.mockMvc = webAppContextSetup(this.wac).addFilters(metadataGeneratorFilter, samlFilter).build();
    }

    @Test
    public void when_request_IDP_metadata_then_returned() throws Exception {
        when_get_IDP_metadata();
        final String metadata = result.getResponse().getContentAsString();
        assertThat(metadata, notNullValue());
    }

    @Test
    public void the_IDP_entity_id_must_be_defined() throws Exception {
        when_get_IDP_metadata();
        EntityDescriptorImpl metadata = samlHelper.toMetadata(result.getResponse().getContentAsString());
        assertThat(metadata, notNullValue());
        assertThat(metadata.getEntityID(), equalTo(idpEntityId));
        assertThat(metadata.getID(), equalTo(idpEntityId));
    }

    @Test
    public void the_IDP_metadata_must_have_idp_description() throws Exception {
        when_get_IDP_metadata();
        EntityDescriptorImpl metadata = samlHelper.toMetadata(result.getResponse().getContentAsString());
        final IDPSSODescriptor idpssoDescriptor = metadata.getIDPSSODescriptor(SAML20P_NS);
        assertThat(idpssoDescriptor, notNullValue());
    }


    @Test
    public void the_IDP_metadata_must_have_a_key_to_verify_responses_signatures() throws Exception {
        when_get_IDP_metadata();
        metadata = samlHelper.toMetadata(result.getResponse().getContentAsString());
        final IDPSSODescriptor idpssoDescriptor = metadata.getIDPSSODescriptor(SAML20P_NS);
        then_there_is_one_signing_key(idpssoDescriptor);
        and_then_there_is_one_signing_certificate();
        and_then_the_certificate_still_valid();
    }

    private void then_there_is_one_signing_key(IDPSSODescriptor idpssoDescriptor) {
        keyDescriptors = idpssoDescriptor.getKeyDescriptors();
        assertThat(keyDescriptors, notNullValue());
        Stream<KeyDescriptor> signingKeys = keyDescriptors.stream().filter(key -> UsageType.SIGNING.equals(key.getUse()));
        assertThat(signingKeys.count(), equalTo(1l));

        signingKeys = keyDescriptors.stream().filter(key -> UsageType.SIGNING.equals(key.getUse()));
        signingKey = signingKeys.findFirst().get();
        assertThat(signingKey, notNullValue());
        assertThat(signingKey.getKeyInfo(), notNullValue());
    }

    private void and_then_there_is_one_signing_certificate() {
        List<X509Data> certificateDatas = signingKey.getKeyInfo().getX509Datas();
        assertThat(certificateDatas, Matchers.hasSize(1));

        final X509Data x509Data = certificateDatas.stream().findFirst().get();
        final List<X509Certificate> certificates = x509Data.getX509Certificates();
        assertThat(certificates, Matchers.hasSize(1));
        assertThat(certificates.stream().findFirst().orElseGet(null), notNullValue());
        x509Certificate = certificates.stream().findFirst().get();
    }

    private void and_then_the_certificate_still_valid() throws CertificateException {
        final byte[] decode = org.opensaml.xml.util.Base64.decode(x509Certificate.getValue());
        ByteArrayInputStream inputStream = new ByteArrayInputStream(decode);
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        java.security.cert.X509Certificate cert = (java.security.cert.X509Certificate) certFactory.generateCertificate(inputStream);
        final Date notAfter = cert.getNotAfter();
        final LocalDateTime expiryDate = Instant.ofEpochMilli(notAfter.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
        final LocalDate limiteDate = LocalDate.now().plus(50l, ChronoUnit.DAYS);
        Assert.assertThat(limiteDate, LocalDateMatchers.before(expiryDate.toLocalDate()));
    }

    private void when_get_IDP_metadata() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/idp/saml/metadata"));
        result = resultActions.andExpect(status().isOk())
                .andExpect(content().contentType("application/xml;charset=UTF-8"))
                .andReturn();
    }

}
