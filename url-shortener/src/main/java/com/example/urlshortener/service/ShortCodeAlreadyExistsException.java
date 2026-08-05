package com.example.urlshortener.service;

public class ShortCodeAlreadyExistsException extends RuntimeException {

	public ShortCodeAlreadyExistsException(String code) {
		super("Short code already exists: " + code);
	}
}
