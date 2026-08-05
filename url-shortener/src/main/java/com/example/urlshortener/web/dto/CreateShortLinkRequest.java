package com.example.urlshortener.web.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateShortLinkRequest(
		@NotBlank
		@Size(max = 2048)
		String longUrl,

		@Pattern(regexp = "^[A-Za-z0-9_-]{3,32}$", message = "customAlias must be 3-32 URL-safe characters")
		String customAlias,

		Instant expiresAt) {

	public boolean hasCustomAlias() {
		return customAlias != null && !customAlias.isBlank();
	}
}
