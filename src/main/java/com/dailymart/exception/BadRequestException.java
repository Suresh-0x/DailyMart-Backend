package com.dailymart.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}
