package com.isysnap.exception;

public class MenuItemNotFoundException extends RuntimeException {
    public MenuItemNotFoundException(String itemId) {
        super("Menu item not found: " + itemId);
    }
}