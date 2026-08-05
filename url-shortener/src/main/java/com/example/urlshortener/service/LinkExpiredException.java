package com.example.urlshortener.service;

public class LinkExpiredException extends RuntimeException {

	public LinkExpiredException(String code) {
		super("Short link has expired: " + code);
	}
}
