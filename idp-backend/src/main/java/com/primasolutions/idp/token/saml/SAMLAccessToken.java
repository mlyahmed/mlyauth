package com.primasolutions.idp.token.saml;

import com.primasolutions.idp.constants.TokenNorm;
import com.primasolutions.idp.constants.TokenProcessingStatus;
import com.primasolutions.idp.constants.TokenRefreshMode;
import com.primasolutions.idp.constants.TokenScope;
import com.primasolutions.idp.constants.TokenType;
import com.primasolutions.idp.constants.TokenValidationMode;
import com.primasolutions.idp.constants.TokenVerdict;
import com.primasolutions.idp.exception.TokenNotCipheredException;
import com.primasolutions.idp.token.AbstractToken;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.NameIDType;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.security.credential.Credential;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.primasolutions.idp.constants.TokenVerdict.SUCCESS;
import static com.primasolutions.idp.token.Claims.BP;
import static com.primasolutions.idp.token.Claims.DELEGATE;
import static com.primasolutions.idp.token.Claims.DELEGATOR;
import static com.primasolutions.idp.token.Claims.SCOPES;
import static com.primasolutions.idp.token.Claims.STATE;
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
    private TokenProcessingStatus status;

    @Autowired
    private SAMLHelper samlHelper = new SAMLHelper();

    public SAMLAccessToken(final Credential credential) {
        notNull(credential, "The credential argument is mandatory !");
        notNull(credential.getPrivateKey(), "The private key argument is mandatory !");
        notNull(credential.getPublicKey(), "The public key argument is mandatory !");
        this.credential = credential;
        init();
    }

    public SAMLAccessToken(final String serialized, final Credential credential) {
        notNull(serialized, "The serialized token argument is mandatory !");
        notNull(credential, "The credential argument is mandatory !");
        notNull(credential.getPrivateKey(), "The private key argument is mandatory !");
        notNull(credential.getPublicKey(), "The public key argument is mandatory !");
        this.credential = credential;
        response = (Response) samlHelper.decode(serialized);
        status = TokenProcessingStatus.CYPHERED;
        locked = true;
    }

    private void init() {
        initAssertion();
        initAttributes();
        initSubject();
        initAudience();
        initResponse();
        status = TokenProcessingStatus.FRESH;
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
        final SubjectConfirmationData confirmationData = samlHelper.buildSAMLObject(SubjectConfirmationData.class);
        confirmationData.setNotOnOrAfter(DateTime.now().plusMinutes(2));
        subjectConfirmation.setMethod(SubjectConfirmation.METHOD_BEARER);
        subjectConfirmation.setSubjectConfirmationData(confirmationData);
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
    public TokenRefreshMode getRefreshMode() {
        return null;
    }

    @Override
    public void setRefreshMode(final TokenRefreshMode mode) {

    }

    @Override
    public TokenValidationMode getValidationMode() {
        return null;
    }

    @Override
    public void setValidationMode(final TokenValidationMode mode) {

    }

    @Override
    public String getStamp() {
        return response.getID();
    }

    @Override
    public void setStamp(final String stamp) {
        checkUnmodifiable();
        response.setID(stamp);
        assertion.setID(stamp);
        status = TokenProcessingStatus.FORGED;
    }

    @Override
    public String getSubject() {
        return assertion.getSubject().getNameID().getValue();
    }

    @Override
    public void setSubject(final String value) {
        checkUnmodifiable();
        subject.getNameID().setValue(value);
        status = TokenProcessingStatus.FORGED;
    }

    @Override
    public Set<TokenScope> getScopes() {
        if (StringUtils.isBlank(getAttributeValue(SCOPES.getValue()))) return Collections.emptySet();
        return splitScopes(Objects.requireNonNull(getAttributeValue(SCOPES.getValue())));
    }

    @Override
    public void setScopes(final  Set<TokenScope> scopes) {
        checkUnmodifiable();
        setAttributeValue(SCOPES.getValue(), compactScopes(scopes));
        status = TokenProcessingStatus.FORGED;
    }

    @Override
    public String getBP() {
        return getAttributeValue(BP.getValue());
    }

    @Override
    public void setBP(final String bp) {
        checkUnmodifiable();
        setAttributeValue(BP.getValue(), bp);
        status = TokenProcessingStatus.FORGED;
    }

    @Override
    public String getState() {
        return getAttributeValue(STATE.getValue());
    }

    @Override
    public void setState(final String state) {
        checkUnmodifiable();
        setAttributeValue(STATE.getValue(), state);
        status = TokenProcessingStatus.FORGED;
    }

    @Override
    public String getIssuer() {
        return response.getIssuer() != null ? response.getIssuer().getValue() : null;
    }

    @Override
    public void setIssuer(final String issuerURI) {
        checkUnmodifiable();
        response.getIssuer().setValue(issuerURI);
        assertion.getIssuer().setValue(issuerURI);
        status = TokenProcessingStatus.FORGED;
    }

    @Override
    public String getAudience() {
        return audience.getAudienceURI();
    }

    @Override
    public void setAudience(final String audienceURI) {
        checkUnmodifiable();
        audience.setAudienceURI(audienceURI);
        subject.getSubjectConfirmations().get(0).getSubjectConfirmationData().setInResponseTo(audienceURI);
        status = TokenProcessingStatus.FORGED;
    }

    @Override
    public String getTargetURL() {
        return response.getDestination();
    }

    @Override
    public void setTargetURL(final String url) {
        checkUnmodifiable();
        response.setDestination(url);
        subject.getSubjectConfirmations().get(0).getSubjectConfirmationData().setRecipient(url);
        status = TokenProcessingStatus.FORGED;
    }

    @Override
    public String getDelegator() {
        return getAttributeValue(DELEGATOR.getValue());
    }

    @Override
    public void setDelegator(final String delegatorID) {
        checkUnmodifiable();
        setAttributeValue(DELEGATOR.getValue(), delegatorID);
        status = TokenProcessingStatus.FORGED;
    }

    @Override
    public String getDelegate() {
        return getAttributeValue(DELEGATE.getValue());
    }

    @Override
    public void setDelegate(final String delegateURI) {
        checkUnmodifiable();
        setAttributeValue(DELEGATE.getValue(), delegateURI);
        status = TokenProcessingStatus.FORGED;
    }

    @Override
    public TokenVerdict getVerdict() {
        if (StringUtils.isBlank(response.getStatus().getStatusCode().getValue())) return null;
        return SUCCESS_URI.equalsIgnoreCase(response.getStatus().getStatusCode().getValue())
                ? SUCCESS : TokenVerdict.FAIL;
    }

    @Override
    public void setVerdict(final TokenVerdict verdict) {
        checkUnmodifiable();
        response.getStatus().getStatusCode().setValue((SUCCESS == verdict) ? SUCCESS_URI : AUTHN_FAILED_URI);
        status = TokenProcessingStatus.FORGED;
    }

    @Override
    public LocalDateTime getExpiryTime() {
        final Date date = assertion.getConditions().getNotOnOrAfter().toDateTime(DateTimeZone.getDefault()).toDate();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @Override
    public LocalDateTime getEffectiveTime() {
        final Date date = assertion.getAuthnStatements().get(0)
                .getAuthnInstant().toDateTime(DateTimeZone.getDefault()).toDate();
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
    public TokenProcessingStatus getStatus() {
        return status;
    }

    @Override
    public void setClaim(final String claimURI, final String value) {
        checkCommitted();
        if (StringUtils.isBlank(getAttributeValue(claimURI))) newAttribute(claimURI);
        setAttributeValue(claimURI, value);
    }

    private void newAttribute(final String claimURI) {
        final Attribute claim = samlHelper.buildStringAttribute(claimURI, null);
        claim.setName(claimURI);
        assertion.getAttributeStatements().get(0).getAttributes().add(claim);
    }

    @Override
    public String getClaim(final String claimURI) {
        return getAttributeValue(claimURI);
    }

    @Override
    public void cypher() {
        response.getAssertions().clear();
        response.getEncryptedAssertions().add(samlHelper.encryptAssertion(assertion, credential));
        samlHelper.signObject(response, credential);
        status = TokenProcessingStatus.CYPHERED;
        committed = true;
    }

    @Override
    public void decipher() {
        checkCommitted();
        samlHelper.validateSignature(response, credential);
        assertion = samlHelper.decryptAssertion(response.getEncryptedAssertions().get(0), credential);
        subject = assertion.getSubject();
        audience = assertion.getConditions().getAudienceRestrictions().get(0).getAudiences().get(0);
        status = TokenProcessingStatus.DECIPHERED;
    }

    @Override
    public String serialize() {
        if (status != TokenProcessingStatus.CYPHERED) throw TokenNotCipheredException.newInstance();
        return encodeBytes(samlHelper.toString(response).getBytes());
    }

    private void setAttributeValue(final String attributeName, final String attributeValue) {
        final Attribute actual = assertion.getAttributeStatements().get(0)
                .getAttributes().stream().filter(attr -> attributeName.equals(attr.getName())).findFirst().get();
        ((XSString) actual.getAttributeValues().get(0)).setValue(attributeValue);
    }

    private String getAttributeValue(final String attributeName) {
        final Attribute actual = assertion.getAttributeStatements().get(0)
                .getAttributes().stream().filter(attr -> attributeName.equals(attr.getName()))
                .findFirst().orElse(null);
        return actual != null ? ((XSString) actual.getAttributeValues().get(0)).getValue() : null;
    }
}
