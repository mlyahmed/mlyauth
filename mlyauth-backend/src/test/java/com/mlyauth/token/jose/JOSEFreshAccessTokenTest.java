package com.mlyauth.token.jose;

import com.mlyauth.constants.*;
import com.mlyauth.exception.TokenNotCipheredException;
import com.mlyauth.exception.TokenUnmodifiableException;
import com.mlyauth.token.IDPClaims;
import com.mlyauth.tools.KeysForTests;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import javafx.util.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mlyauth.constants.TokenScope.*;
import static com.mlyauth.constants.TokenVerdict.FAIL;
import static com.mlyauth.constants.TokenVerdict.SUCCESS;
import static com.mlyauth.token.IDPClaims.*;
import static com.mlyauth.tools.RandomForTests.randomString;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(DataProviderRunner.class)
public class JOSEFreshAccessTokenTest {

    private JOSEAccessToken token;
    private Pair<PrivateKey, RSAPublicKey> cypherCred;
    private Pair<PrivateKey, RSAPublicKey> decipherCred;

    @Before
    public void setup() {
        final Pair<PrivateKey, X509Certificate> peerCred = KeysForTests.generateRSACredential();
        final Pair<PrivateKey, X509Certificate> localCred = KeysForTests.generateRSACredential();
        cypherCred = new Pair<>(localCred.getKey(), (RSAPublicKey) peerCred.getValue().getPublicKey());
        decipherCred = new Pair<>(peerCred.getKey(), (RSAPublicKey) localCred.getValue().getPublicKey());
        token = new JOSEAccessToken(cypherCred.getKey(), cypherCred.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_create_a_fresh_token_and_private_key_is_null_then_error() {
        final Pair<PrivateKey, X509Certificate> credential = KeysForTests.generateRSACredential();
        new JOSEAccessToken(null, (RSAPublicKey) credential.getValue().getPublicKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_create_a_fresh_token_and_public_key_is_null_then_error() {
        final Pair<PrivateKey, X509Certificate> credential = KeysForTests.generateRSACredential();
        new JOSEAccessToken(credential.getKey(), null);
    }

    @Test
    public void when_create_fresh_response_then_token_claims_must_be_fresh() {
        assertThat(token.getId(), nullValue());
        assertThat(token.getSubject(), nullValue());
        assertThat(token.getScopes(), empty());
        assertThat(token.getBP(), nullValue());
        assertThat(token.getState(), nullValue());
        assertThat(token.getIssuer(), nullValue());
        assertThat(token.getAudience(), nullValue());
        assertThat(token.getDelegator(), nullValue());
        assertThat(token.getDelegate(), nullValue());
        assertThat(token.getVerdict(), nullValue());
        assertThat(token.getNorm(), equalTo(TokenNorm.JWT));
        assertThat(token.getType(), equalTo(TokenType.ACCESS));
        assertThat(token.getStatus(), equalTo(TokenStatus.FRESH));
    }

    @Test
    public void when_create_a_fresh_token_and_set_Id_then_must_be_set() {
        String id = randomString();
        token.setId(id);
        assertThat(token.getId(), equalTo(id));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_id_must_be_committed() throws Exception {
        final String id = randomString();
        token.setId(id);
        token.cypher();
        JWEObject loadedToken = JWEObject.parse(token.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getJWTID(), equalTo(id));
    }

    @Test
    public void when_create_a_fresh_token_and_set_subject_then_must_be_set() {
        String subject = randomString();
        token.setSubject(subject);
        assertThat(token.getSubject(), equalTo(subject));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_subject_must_be_committed() throws Exception {
        String subject = randomString();
        token.setSubject(subject);
        token.cypher();
        JWEObject loadedToken = JWEObject.parse(token.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getSubject(), equalTo(subject));
    }

    @DataProvider
    public static Object[][] scopes() {
        // @formatter:off
        return new Object[][]{
                {PROPOSAL.name(), POLICY.name(), CLAIM.name()},
                {PROPOSAL.name(), POLICY.name(), PERSON.name()},
                {POLICY.name(), CLAIM.name(), PERSON.name(), PROPOSAL.name()},
                {PROPOSAL.name()},
                {PERSON.name()},
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("scopes")
    public void when_create_a_fresh_token_and_set_scopes_then_they_must_be_set(String... scopesArrays) {
        final Set<TokenScope> scopes = Arrays.stream(scopesArrays).map(TokenScope::valueOf).collect(toSet());
        token.setScopes(scopes);
        assertThat(token.getScopes(), equalTo(scopes));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    @UseDataProvider("scopes")
    public void when_serialize_cyphered_token_then_the_scopes_must_be_committed(String... scopesArrays) throws Exception {
        final Set<TokenScope> scopes = Arrays.stream(scopesArrays).map(TokenScope::valueOf).collect(toSet());
        token.setScopes(scopes);
        token.cypher();
        JWEObject loadedToken = JWEObject.parse(token.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(SCOPES.getValue()),
                equalTo(scopes.stream().map(TokenScope::name).collect(Collectors.joining("|"))));
    }

    @Test
    public void when_create_a_fresh_token_and_set_BP_then_must_be_set() {
        String bp = randomString();
        token.setBP(bp);
        assertThat(token.getBP(), equalTo(bp));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_BP_must_be_committed() throws Exception {
        String bp = randomString();
        token.setBP(bp);
        token.cypher();
        JWEObject loadedToken = JWEObject.parse(token.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(BP.getValue()), equalTo(bp));
    }

    @Test
    public void when_create_a_fresh_token_and_set_State_then_must_be_set() {
        String state = randomString();
        token.setState(state);
        assertThat(token.getState(), equalTo(state));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_State_must_be_committed() throws Exception {
        String state = randomString();
        token.setState(state);
        token.cypher();
        JWEObject loadedToken = JWEObject.parse(token.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(STATE.getValue()), equalTo(state));
    }

    @Test
    public void when_create_a_fresh_token_and_set_Issuer_then_must_be_set() {
        String issuer = randomString();
        token.setIssuer(issuer);
        assertThat(token.getIssuer(), equalTo(issuer));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_Issuer_must_be_committed() throws Exception {
        String issuer = randomString();
        token.setIssuer(issuer);
        token.cypher();
        JWEObject loadedToken = JWEObject.parse(token.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getIssuer(), equalTo(issuer));
        assertThat(signedJWT.getHeader().getCustomParam(IDPClaims.ISSUER.getValue()), equalTo(issuer));
    }

    @Test
    public void when_create_a_fresh_token_and_set_Audience_then_must_be_set() {
        String audienceURI = randomString();
        token.setAudience(audienceURI);
        assertThat(token.getAudience(), equalTo(audienceURI));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_Audience_must_be_committed() throws Exception {
        String audienceURI = randomString();
        token.setAudience(audienceURI);
        token.cypher();
        JWEObject loadedToken = JWEObject.parse(token.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getAudience().get(0), equalTo(audienceURI));
    }

    @Test
    public void when_create_a_fresh_token_and_set_Target_URL_then_must_be_set() {
        String targetURL = randomString();
        token.setTargetURL(targetURL);
        assertThat(token.getTargetURL(), equalTo(targetURL));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_Target_URL_must_be_committed() throws Exception {
        String targetURL = randomString();
        token.setTargetURL(targetURL);
        token.cypher();
        JWEObject loadedToken = JWEObject.parse(token.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(TARGET_URL.getValue()), equalTo(targetURL));
    }

    @Test
    public void when_create_a_fresh_token_and_set_Delegator_then_must_be_set() {
        String delegator = randomString();
        token.setDelegator(delegator);
        assertThat(token.getDelegator(), equalTo(delegator));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_Delegator_must_be_committed() throws Exception {
        String delegator = randomString();
        token.setDelegator(delegator);
        token.cypher();
        JWEObject loadedToken = JWEObject.parse(token.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(DELEGATOR.getValue()), equalTo(delegator));
    }

    @Test
    public void when_create_a_fresh_token_and_set_Delegate_then_must_be_set() {
        String delegate = randomString();
        token.setDelegate(delegate);
        assertThat(token.getDelegate(), equalTo(delegate));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_Delegate_must_be_committed() throws Exception {
        String delegate = randomString();
        token.setDelegate(delegate);
        token.cypher();
        JWEObject loadedToken = JWEObject.parse(token.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(DELEGATE.getValue()), equalTo(delegate));
    }

    @Test
    public void when_create_a_fresh_token_and_set_Verdict_then_must_be_set() {
        token.setVerdict(SUCCESS);
        assertThat(token.getVerdict(), equalTo(SUCCESS));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @DataProvider
    public static Object[][] claims() {
        // @formatter:off
        return new String[][]{
                {"prestationId", "FCJHDJHDJHD545454d54d"},
                {"action", "DFDFD"},
                {"codeRole", "GS"},
                {"referenceRole", "54sd54s5d4s54d5s4d"},
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("claims")
    public void when_set_other_claim_then_it_must_be_set(String... claimPair) {
        token.setClaim(claimPair[0], claimPair[1]);
        assertThat(token.getClaim(claimPair[0]), equalTo(claimPair[1]));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    @UseDataProvider("claims")
    public void when_serialize_cyphered_token_then_the_other_claims_must_be_committed(String... claimPair) throws Exception {
        token.setClaim(claimPair[0], claimPair[1]);
        token.cypher();
        JWEObject loadedToken = JWEObject.parse(token.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(claimPair[0]), equalTo(claimPair[1]));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_Verdict_must_be_committed() throws Exception {
        token.setVerdict(FAIL);
        token.cypher();
        JWEObject loadedToken = JWEObject.parse(token.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(VERDICT.getValue()), equalTo(FAIL.name()));
    }

    @Test
    public void when_create_a_fresh_token_then_it_expires_in_3_minutes() {
        assertThat(token.getExpiryTime(), notNullValue());
        assertThat(token.getExpiryTime().isBefore(LocalDateTime.now().plusMinutes(3)), equalTo(true));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_expiry_time_must_be_committed() throws Exception {
        token.cypher();
        Instant instant = LocalDateTime.now().plusMinutes(3).atZone(ZoneId.systemDefault()).toInstant();
        JWEObject loadedToken = JWEObject.parse(token.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getExpirationTime().before(Date.from(instant)), equalTo(true));
    }

    @Test
    public void when_create_a_fresh_token_then_it_is_effective_now() {
        assertThat(token.getEffectiveTime(), notNullValue());
        assertThat(token.getEffectiveTime().isAfter(LocalDateTime.now().minusSeconds(2)), equalTo(true));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_effective_time_must_be_committed() throws Exception {
        token.cypher();
        Instant instant = LocalDateTime.now().minusSeconds(2).atZone(ZoneId.systemDefault()).toInstant();
        JWEObject loadedToken = JWEObject.parse(token.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getNotBeforeTime().after(Date.from(instant)), equalTo(true));
    }

    @Test
    public void when_create_a_fresh_token_then_it_is_issued_now() {
        assertThat(token.getIssuanceTime(), notNullValue());
        assertThat(token.getIssuanceTime().isAfter(LocalDateTime.now().minusSeconds(2)), equalTo(true));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_issuance_time_must_be_committed() throws Exception {
        token.cypher();
        Instant instant = LocalDateTime.now().minusSeconds(2).atZone(ZoneId.systemDefault()).toInstant();
        JWEObject loadedToken = JWEObject.parse(token.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getIssueTime().after(Date.from(instant)), equalTo(true));
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_id_and_already_ciphered_then_error() {
        token.cypher();
        token.setId(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_subject_and_already_ciphered_then_error() {
        token.cypher();
        token.setSubject(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_scopes_and_already_ciphered_then_error() {
        token.cypher();
        token.setScopes(Collections.emptySet());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_BP_and_already_ciphered_then_error() {
        token.cypher();
        token.setBP(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_State_and_already_ciphered_then_error() {
        token.cypher();
        token.setState(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_Issuer_and_already_ciphered_then_error() {
        token.cypher();
        token.setIssuer(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_Audience_and_already_ciphered_then_error() {
        token.cypher();
        token.setAudience(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_Target_URL_and_already_ciphered_then_error() {
        token.cypher();
        token.setTargetURL(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_Delegator_and_already_ciphered_then_error() {
        token.cypher();
        token.setDelegator(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_Delegate_and_already_ciphered_then_error() {
        token.cypher();
        token.setDelegate(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_Verdict_and_already_ciphered_then_error() {
        token.cypher();
        token.setVerdict(TokenVerdict.SUCCESS);
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_Claim_and_already_ciphered_then_error() {
        token.cypher();
        token.setClaim(randomString(), randomString());
    }

    @Test
    public void when_cypher_a_fresh_token_then_it_must_be_signed_and_encrypted() throws Exception {
        token.cypher();
        JWEObject loadedToken = JWEObject.parse(token.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT, notNullValue());
        assertTrue(signedJWT.verify(new RSASSAVerifier(decipherCred.getValue())));
    }

    @Test
    public void when_cypher_a_fresh_token_then_set_it_as_cyphered() {
        token.cypher();
        assertThat(token.getStatus(), equalTo(TokenStatus.CYPHERED));
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_cypher_a_fresh_token_and_decypher_then_error() {
        token.cypher();
        token.decipher();
    }

    @Test(expected = TokenNotCipheredException.class)
    public void when_serialize_a_non_cyphered_token_then_error() {
        token.serialize();
    }
}
