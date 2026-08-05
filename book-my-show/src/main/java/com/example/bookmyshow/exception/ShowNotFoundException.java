package com.example.bookmyshow.exception;

public class ShowNotFoundException extends RuntimeException {

    public ShowNotFoundException(String showId) {
        super("show not found: " + showId);
    }
}
