package com.primasolutions.idp.exception;

public class Error {

    private final String code;
    private final String message;

    public Error(final String code, final String message) {
        this.code = code;
        this.message = message;
    }

    public static Error newInstance(final String code) {
        return new Error(code, "");
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
