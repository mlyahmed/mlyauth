package com.mlyauth.exception;

public class AuthError {

    private final String code;
    private final String message;

    public AuthError(final String code, final String message) {
        this.code = code;
        this.message = message;
    }

    public static AuthError newInstance(final String code) {
        return new AuthError(code, "");
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
