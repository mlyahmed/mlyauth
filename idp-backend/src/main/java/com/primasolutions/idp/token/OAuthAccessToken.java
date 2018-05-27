package com.primasolutions.idp.token;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OAuthAccessToken {

    @JsonProperty("access_token")
    private final String accessToken;

    @JsonProperty("token_type")
    private final String tokenType;

    @JsonProperty("expires_in")
    private final long expiresIn;

    @JsonProperty("refresh_token")
    private final String refreshToken;

    public OAuthAccessToken(final String accessToken, final long expiresIn) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        tokenType = "Bearer";
        refreshToken = null;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
