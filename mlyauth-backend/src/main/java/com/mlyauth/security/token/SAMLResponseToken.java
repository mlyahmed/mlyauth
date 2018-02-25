package com.mlyauth.security.token;

import com.mlyauth.constants.*;
import com.mlyauth.exception.IDPSAMLErrorException;
import com.mlyauth.security.sso.SAMLHelper;
import org.apache.commons.lang.StringUtils;
import org.opensaml.saml2.core.*;
import org.opensaml.xml.util.XMLObjectHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

public class SAMLResponseToken implements IIDPToken<Response> {

    public static final String SCOPES_ATTR = "scopes";
    private final Response response;
    private final Assertion assertion;
    private final Subject subject;
    private final HashMap<String, String> attributes;
    private TokenStatus status;
    @Autowired
    private SAMLHelper samlHelper = new SAMLHelper();

    public SAMLResponseToken() {
        response = samlHelper.buildSAMLObject(Response.class);
        assertion = samlHelper.buildSAMLObject(Assertion.class);
        subject = samlHelper.buildSAMLObject(Subject.class);

        attributes = new HashMap<>();
        subject.setNameID(newSubjectNameID());
        assertion.setSubject(subject);
        status = TokenStatus.CREATED;
    }

    @Override
    public String getId() {
        return response.getID();
    }

    @Override
    public void setId(String id) {
        response.setID(id);
        status = TokenStatus.FORGED;
    }

    @Override
    public String getSubject() {
        return assertion.getSubject().getNameID().getValue();
    }

    @Override
    public void setSubject(String subject) {
        assertion.getSubject().getNameID().setValue(subject);
        status = TokenStatus.FORGED;
    }

    @Override
    public Set<TokenScope> getScopes() {
        if (StringUtils.isBlank(attributes.get(SCOPES_ATTR))) return Collections.emptySet();
        return Arrays.stream(attributes.get(SCOPES_ATTR).split("\\|"))
                .map(v -> TokenScope.valueOf(v)).collect(Collectors.toSet());
    }

    @Override
    public void setScopes(Set<TokenScope> scopes) {
        attributes.put(SCOPES_ATTR, scopes.stream().map(TokenScope::name).collect(Collectors.joining("|")));
        status = TokenStatus.FORGED;
    }

    @Override
    public String getBP() {
        return null;
    }

    @Override
    public void setBP() {

    }

    @Override
    public String getState() {
        return null;
    }

    @Override
    public void setState(String state) {

    }

    @Override
    public String getIssuer() {
        return null;
    }

    @Override
    public void setIssuer(String issuerURI) {

    }

    @Override
    public String getAudience() {
        return null;
    }

    @Override
    public void setAudience(String audienceURI) {

    }

    @Override
    public String getDelegator() {
        return null;
    }

    @Override
    public void setDelegator(String delegatorID) {

    }

    @Override
    public String getDelegate() {
        return null;
    }

    @Override
    public void setDelegate(String delegateURI) {

    }

    @Override
    public TokenVerdict getVerdict() {
        return null;
    }

    @Override
    public void setVerdict(TokenVerdict verdict) {

    }

    @Override
    public LocalDateTime getExpiryTime() {
        return null;
    }

    @Override
    public LocalDateTime getEffectiveTime() {
        return null;
    }

    @Override
    public LocalDateTime getIssuanceTime() {
        return null;
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
    public Response getNative() {
        try {
            return XMLObjectHelper.cloneXMLObject(response);
        } catch (Exception e) {
            throw IDPSAMLErrorException.newInstance(e);
        }
    }

    @Override
    public void cypher() {

    }

    @Override
    public void decipher() {

    }

    @Override
    public String serialize() {
        return null;
    }

    private NameID newSubjectNameID() {
        NameID nameID = samlHelper.buildSAMLObject(NameID.class);
        nameID.setFormat(NameIDType.TRANSIENT);
        return nameID;
    }
}
