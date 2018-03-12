package com.mlyauth.token.saml;

import com.mlyauth.constants.*;
import com.mlyauth.exception.TokenNotCipheredException;
import com.mlyauth.token.AbstractToken;
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

import static com.mlyauth.constants.TokenStatus.*;
import static com.mlyauth.constants.TokenVerdict.FAIL;
import static com.mlyauth.constants.TokenVerdict.SUCCESS;
import static com.mlyauth.token.IDPClaims.*;
import static org.opensaml.saml2.core.StatusCode.AUTHN_FAILED_URI;
import static org.opensaml.saml2.core.StatusCode.SUCCESS_URI;
import static org.opensaml.xml.util.Base64.encodeBytes;
import static org.springframework.util.Assert.notNull;

public class SAMLAccessToken extends AbstractToken {

    private Credential credential;
    private Response response;
    private Assertion assertion;
    private Subject subject;
    private Audience audience;
    private TokenStatus status;

    @Autowired
    private SAMLHelper samlHelper = new SAMLHelper();

    public SAMLAccessToken(final Credential credential) {
        notNull(credential, "The credential argument is mandatory !");
        notNull(credential.getPrivateKey(), "The private key argument is mandatory !");
        notNull(credential.getPublicKey(), "The public key argument is mandatory !");
        this.credential = credential;
        init();
    }

    public SAMLAccessToken(String serialized, Credential credential) {
        this.credential = credential;
        response = (Response) samlHelper.decode(serialized);
        status = TokenStatus.CYPHERED;
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
        final Attribute state = samlHelper.buildStringAttribute(STATE.getValue(), null);
        final Attribute scopes = samlHelper.buildStringAttribute(SCOPES.getValue(), null);
        final Attribute bp = samlHelper.buildStringAttribute(BP.getValue(), null);
        final Attribute delegator = samlHelper.buildStringAttribute(DELEGATOR.getValue(), null);
        final Attribute delegate = samlHelper.buildStringAttribute(DELEGATE.getValue(), null);
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
    public String getStamp() {
        return response.getID();
    }

    @Override
    public void setStamp(String stamp) {
        checkCommitted();
        response.setID(stamp);
        assertion.setID(stamp);
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
        if (StringUtils.isBlank(getAttributeValue(SCOPES.getValue()))) return Collections.emptySet();
        return splitScopes(Objects.requireNonNull(getAttributeValue(SCOPES.getValue())));
    }

    @Override
    public void setScopes(Set<TokenScope> scopes) {
        checkCommitted();
        setAttributeValue(SCOPES.getValue(), compactScopes(scopes));
        status = FORGED;
    }

    @Override
    public String getBP() {
        return getAttributeValue(BP.getValue());
    }

    @Override
    public void setBP(String bp) {
        checkCommitted();
        setAttributeValue(BP.getValue(), bp);
        status = FORGED;
    }

    @Override
    public String getState() {
        return getAttributeValue(STATE.getValue());
    }

    @Override
    public void setState(String state) {
        checkCommitted();
        setAttributeValue(STATE.getValue(), state);
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
        return getAttributeValue(DELEGATOR.getValue());
    }

    @Override
    public void setDelegator(String delegatorID) {
        checkCommitted();
        setAttributeValue(DELEGATOR.getValue(), delegatorID);
        status = FORGED;
    }

    @Override
    public String getDelegate() {
        return getAttributeValue(DELEGATE.getValue());
    }

    @Override
    public void setDelegate(String delegateURI) {
        checkCommitted();
        setAttributeValue(DELEGATE.getValue(), delegateURI);
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
        if (StringUtils.isBlank(getAttributeValue(claimURI))) newAttribute(claimURI);
        setAttributeValue(claimURI, value);
    }

    private void newAttribute(String claimURI) {
        final Attribute claim = samlHelper.buildStringAttribute(claimURI, null);
        claim.setName(claimURI);
        assertion.getAttributeStatements().get(0).getAttributes().add(claim);
    }

    @Override
    public String getClaim(String claimURI) {
        return getAttributeValue(claimURI);
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
        assertion = samlHelper.decryptAssertion(response.getEncryptedAssertions().get(0), credential);
        subject = assertion.getSubject();
        audience = assertion.getConditions().getAudienceRestrictions().get(0).getAudiences().get(0);
        status = DECIPHERED;
    }

    @Override
    public String serialize() {
        if (status != CYPHERED) throw TokenNotCipheredException.newInstance();
        return encodeBytes(samlHelper.toString(response).getBytes());
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
