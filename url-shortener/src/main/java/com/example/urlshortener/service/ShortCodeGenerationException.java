package com.example.urlshortener.service;

public class ShortCodeGenerationException extends RuntimeException {

	public ShortCodeGenerationException() {
		super("Unable to allocate a unique short code");
	}
}
