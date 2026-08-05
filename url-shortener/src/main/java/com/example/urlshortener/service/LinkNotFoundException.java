package com.example.urlshortener.service;

public class LinkNotFoundException extends RuntimeException {

	public LinkNotFoundException(String code) {
		super("Short link not found: " + code);
	}
}
