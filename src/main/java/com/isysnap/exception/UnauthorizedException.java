package com.isysnap.exception;

/**
 * Exception thrown when a user tries to access a resource they are not authorized to access
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
