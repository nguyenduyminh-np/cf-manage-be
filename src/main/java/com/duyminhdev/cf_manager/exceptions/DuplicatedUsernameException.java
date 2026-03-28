package com.duyminhdev.cf_manager.exceptions;

public class DuplicatedUsernameException extends RuntimeException {
    public DuplicatedUsernameException(String username) {
        super("Username '" + username + "' already exists");
    }
}
