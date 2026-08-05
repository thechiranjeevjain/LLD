package com.example.urlshortener.web.dto;

import java.time.Instant;

import com.example.urlshortener.domain.ShortLink;

public record ShortLinkResponse(
		String code,
		String shortUrl,
		String longUrl,
		Instant createdAt,
		Instant expiresAt,
		boolean active,
		long clickCount) {

	public static ShortLinkResponse from(ShortLink link, String shortUrl) {
		return from(link, shortUrl, link.isActive());
	}

	public static ShortLinkResponse from(ShortLink link, String shortUrl, boolean active) {
		return new ShortLinkResponse(
			link.getCode(),
			shortUrl,
			link.getLongUrl(),
			link.getCreatedAt(),
			link.getExpiresAt(),
			active,
			link.getClickCount());
	}
}
