package com.mlyauth.security.token;

import com.mlyauth.constants.*;
import com.mlyauth.exception.TokenAlreadyCommitedException;
import com.mlyauth.exception.TokenNotCipheredException;
import com.mlyauth.security.sso.SAMLHelper;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.opensaml.saml2.core.*;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.security.credential.Credential;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.mlyauth.constants.TokenStatus.CYPHERED;
import static com.mlyauth.constants.TokenStatus.FORGED;
import static com.mlyauth.constants.TokenVerdict.FAIL;
import static com.mlyauth.constants.TokenVerdict.SUCCESS;
import static org.opensaml.saml2.core.StatusCode.AUTHN_FAILED_URI;
import static org.opensaml.saml2.core.StatusCode.SUCCESS_URI;
import static org.opensaml.xml.util.Base64.encodeBytes;
import static org.springframework.util.Assert.notNull;

public class SAMLResponseToken implements IDPToken<Response> {

    public static final String BP_ATTR = "bp";
    public static final String SCOPES_ATTR = "scopes";
    public static final String DELEGATOR_ATTR = "delegator";
    public static final String DELEGATE_ATTR = "delegate";
    public static final String STATE_ATTR = "state";

    private Credential credential;
    private Response response;
    private Assertion assertion;
    private Subject subject;
    private Audience audience;
    private TokenStatus status;
    private boolean committed;

    @Autowired
    private SAMLHelper samlHelper = new SAMLHelper();

    public SAMLResponseToken(final Credential credential) {
        notNull(credential, "The credential argument is mandatory !");
        notNull(credential.getPrivateKey(), "The private key argument is mandatory !");
        notNull(credential.getPublicKey(), "The public key argument is mandatory !");
        this.credential = credential;
        init();
    }

    private void init() {
        initAssertion();
        initAttributes();
        initSubject();
        initAudience();
        initResponse();
        status = TokenStatus.FRESH;
    }

    private void initAssertion() {
        assertion = samlHelper.buildSAMLObject(Assertion.class);
        final AuthnStatement authnStatement = samlHelper.buildSAMLObject(AuthnStatement.class);
        final AuthnContext authnContext = samlHelper.buildSAMLObject(AuthnContext.class);
        AuthnContextClassRef authnContextClassRef = samlHelper.buildSAMLObject(AuthnContextClassRef.class);
        authnContextClassRef.setAuthnContextClassRef(AuthnContext.PASSWORD_AUTHN_CTX);
        authnContext.setAuthnContextClassRef(authnContextClassRef);
        authnStatement.setAuthnContext(authnContext);
        authnStatement.setAuthnInstant(DateTime.now());
        assertion.getAuthnStatements().add(authnStatement);
        assertion.setIssuer(samlHelper.buildSAMLObject(Issuer.class));
        assertion.setIssueInstant(DateTime.now());
    }

    private void initAttributes() {
        AttributeStatement attributeStatement = samlHelper.buildSAMLObject(AttributeStatement.class);
        final List<Attribute> assertionAttributes = attributeStatement.getAttributes();
        final Attribute state = samlHelper.buildStringAttribute(STATE_ATTR, null);
        final Attribute scopes = samlHelper.buildStringAttribute(SCOPES_ATTR, null);
        final Attribute bp = samlHelper.buildStringAttribute(BP_ATTR, null);
        final Attribute delegator = samlHelper.buildStringAttribute(DELEGATOR_ATTR, null);
        final Attribute delegate = samlHelper.buildStringAttribute(DELEGATE_ATTR, null);
        assertionAttributes.addAll(Arrays.asList(state, scopes, bp, delegator, delegate));
        assertion.getAttributeStatements().add(attributeStatement);
    }

    private void initAudience() {
        audience = samlHelper.buildSAMLObject(Audience.class);
        final Conditions conditions = samlHelper.buildSAMLObject(Conditions.class);
        AudienceRestriction audienceRestriction = samlHelper.buildSAMLObject(AudienceRestriction.class);
        audienceRestriction.getAudiences().add(audience);
        conditions.getAudienceRestrictions().add(audienceRestriction);
        conditions.setNotOnOrAfter(DateTime.now().plusMinutes(2));
        assertion.setConditions(conditions);
    }

    private void initSubject() {
        subject = samlHelper.buildSAMLObject(Subject.class);
        subject.setNameID(newSubjectNameID());
        final SubjectConfirmation subjectConfirmation = samlHelper.buildSAMLObject(SubjectConfirmation.class);
        final SubjectConfirmationData subjectConfirmationData = samlHelper.buildSAMLObject(SubjectConfirmationData.class);
        subjectConfirmationData.setNotOnOrAfter(DateTime.now().plusMinutes(2));
        subjectConfirmation.setMethod(SubjectConfirmation.METHOD_BEARER);
        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);
        subject.getSubjectConfirmations().add(subjectConfirmation);
        assertion.setSubject(subject);
    }

    private NameID newSubjectNameID() {
        NameID nameID = samlHelper.buildSAMLObject(NameID.class);
        nameID.setFormat(NameIDType.TRANSIENT);
        return nameID;
    }

    private void initResponse() {
        response = samlHelper.buildSAMLObject(Response.class);
        response.setIssuer(samlHelper.buildSAMLObject(Issuer.class));
        Status responseStatus = samlHelper.buildSAMLObject(Status.class);
        responseStatus.setStatusCode(samlHelper.buildSAMLObject(StatusCode.class));
        response.setStatus(responseStatus);
        response.setIssueInstant(DateTime.now());
        response.getAssertions().add(assertion);
    }

    @Override
    public String getId() {
        return response.getID();
    }

    @Override
    public void setId(String id) {
        checkCommitted();
        response.setID(id);
        assertion.setID(id);
        status = FORGED;
    }

    @Override
    public String getSubject() {
        return assertion.getSubject().getNameID().getValue();
    }

    @Override
    public void setSubject(String value) {
        checkCommitted();
        subject.getNameID().setValue(value);
        status = FORGED;
    }

    @Override
    public Set<TokenScope> getScopes() {
        if (StringUtils.isBlank(getAttributeValue(SCOPES_ATTR))) return Collections.emptySet();
        return Arrays.stream(getAttributeValue(SCOPES_ATTR).split("\\|"))
                .map(v -> TokenScope.valueOf(v)).collect(Collectors.toSet());
    }

    @Override
    public void setScopes(Set<TokenScope> scopes) {
        checkCommitted();
        setAttributeValue(SCOPES_ATTR, scopes.stream().map(TokenScope::name).collect(Collectors.joining("|")));
        status = FORGED;
    }

    @Override
    public String getBP() {
        return getAttributeValue(BP_ATTR);
    }

    @Override
    public void setBP(String bp) {
        checkCommitted();
        setAttributeValue(BP_ATTR, bp);
        status = FORGED;
    }

    @Override
    public String getState() {
        return getAttributeValue(STATE_ATTR);
    }

    @Override
    public void setState(String state) {
        checkCommitted();
        setAttributeValue(STATE_ATTR, state);
        status = FORGED;
    }

    @Override
    public String getIssuer() {
        return response.getIssuer() != null ? response.getIssuer().getValue() : null;
    }

    @Override
    public void setIssuer(String issuerURI) {
        checkCommitted();
        response.getIssuer().setValue(issuerURI);
        assertion.getIssuer().setValue(issuerURI);
        status = FORGED;
    }

    @Override
    public String getAudience() {
        return audience.getAudienceURI();
    }

    @Override
    public void setAudience(String audienceURI) {
        checkCommitted();
        audience.setAudienceURI(audienceURI);
        subject.getSubjectConfirmations().get(0).getSubjectConfirmationData().setInResponseTo(audienceURI);
        status = FORGED;
    }

    @Override
    public String getTargetURL() {
        return response.getDestination();
    }

    @Override
    public void setTargetURL(String url) {
        checkCommitted();
        response.setDestination(url);
        subject.getSubjectConfirmations().get(0).getSubjectConfirmationData().setRecipient(url);
        status = FORGED;
    }

    @Override
    public String getDelegator() {
        return getAttributeValue(DELEGATOR_ATTR);
    }

    @Override
    public void setDelegator(String delegatorID) {
        checkCommitted();
        setAttributeValue(DELEGATOR_ATTR, delegatorID);
        status = FORGED;
    }

    @Override
    public String getDelegate() {
        return getAttributeValue(DELEGATE_ATTR);
    }

    @Override
    public void setDelegate(String delegateURI) {
        checkCommitted();
        setAttributeValue(DELEGATE_ATTR, delegateURI);
        status = FORGED;
    }

    @Override
    public TokenVerdict getVerdict() {
        if (StringUtils.isBlank(response.getStatus().getStatusCode().getValue())) return null;
        return SUCCESS_URI.equalsIgnoreCase(response.getStatus().getStatusCode().getValue()) ? SUCCESS : FAIL;
    }

    @Override
    public void setVerdict(TokenVerdict verdict) {
        checkCommitted();
        response.getStatus().getStatusCode().setValue((SUCCESS == verdict) ? SUCCESS_URI : AUTHN_FAILED_URI);
        status = FORGED;
    }

    @Override
    public LocalDateTime getExpiryTime() {
        final Date date = assertion.getConditions().getNotOnOrAfter().toDateTime(DateTimeZone.getDefault()).toDate();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @Override
    public LocalDateTime getEffectiveTime() {
        final Date date = assertion.getAuthnStatements().get(0).getAuthnInstant().toDateTime(DateTimeZone.getDefault()).toDate();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @Override
    public LocalDateTime getIssuanceTime() {
        final Date date = response.getIssueInstant().toDateTime(DateTimeZone.getDefault()).toDate();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @Override
    public TokenNorm getNorm() {
        return TokenNorm.SAML;
    }

    @Override
    public TokenType getType() {
        return TokenType.ACCESS;
    }

    @Override
    public TokenStatus getStatus() {
        return status;
    }

    @Override
    public void setClaim(String claimURI, String value) {
        checkCommitted();
        if (StringUtils.isBlank(getAttributeValue(claimURI)))
            newAttribute(claimURI);
        setAttributeValue(claimURI, value);
    }

    private void newAttribute(String claimURI) {
        final Attribute claim = samlHelper.buildStringAttribute(STATE_ATTR, null);
        claim.setName(claimURI);
        assertion.getAttributeStatements().get(0).getAttributes().add(claim);
    }

    @Override
    public String getClaim(String claimURI) {
        return getAttributeValue(claimURI);
    }

    @Override
    public Response getNative() {
        return response;
    }

    @Override
    public void cypher() {
        response.getAssertions().clear();
        response.getEncryptedAssertions().add(samlHelper.encryptAssertion(assertion, credential));
        samlHelper.signObject(response, credential);
        status = CYPHERED;
        committed = true;
    }

    @Override
    public void decipher() {
        checkCommitted();
    }

    @Override
    public String serialize() {
        if (status != CYPHERED) throw TokenNotCipheredException.newInstance();
        return encodeBytes(samlHelper.toString(response).getBytes());
    }

    private void checkCommitted() {
        if (committed) throw TokenAlreadyCommitedException.newInstance();
    }

    private void setAttributeValue(String attributeName, String attributeValue) {
        final Attribute actual = assertion.getAttributeStatements().get(0).getAttributes().stream().filter(attr -> attributeName.equals(attr.getName())).findFirst().get();
        ((XSString) actual.getAttributeValues().get(0)).setValue(attributeValue);
    }

    private String getAttributeValue(String attributeName) {
        final Attribute actual = assertion.getAttributeStatements().get(0).getAttributes().stream().filter(attr -> attributeName.equals(attr.getName())).findFirst().orElse(null);
        return actual != null ? ((XSString) actual.getAttributeValues().get(0)).getValue() : null;
    }
}
