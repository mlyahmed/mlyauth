package com.mlyauth.exception;

public class AuthError {

    private final String code;
    private final String message;

    public AuthError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
