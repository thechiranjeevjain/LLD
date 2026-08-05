package com.example.urlshortener.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
public class Base62CodeGenerator {

	private static final char[] ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

	private final SecureRandom random = new SecureRandom();

	public String generate(int length) {
		char[] code = new char[length];
		for (int index = 0; index < length; index++) {
			code[index] = ALPHABET[random.nextInt(ALPHABET.length)];
		}
		return new String(code);
	}
}
