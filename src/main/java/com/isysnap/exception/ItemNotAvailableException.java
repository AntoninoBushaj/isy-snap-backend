package com.isysnap.exception;

public class ItemNotAvailableException extends RuntimeException {
    public ItemNotAvailableException(String itemName) {
        super("Item not available: " + itemName);
    }
}