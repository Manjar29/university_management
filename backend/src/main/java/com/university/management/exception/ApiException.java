package com.university.management.exception;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}
