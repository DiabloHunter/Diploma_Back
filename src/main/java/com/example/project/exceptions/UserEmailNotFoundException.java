package com.example.project.exceptions;

import org.springframework.security.core.AuthenticationException;

public class UserEmailNotFoundException extends AuthenticationException {

    public UserEmailNotFoundException(String msg) {
        super(msg);
    }

    public UserEmailNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
}