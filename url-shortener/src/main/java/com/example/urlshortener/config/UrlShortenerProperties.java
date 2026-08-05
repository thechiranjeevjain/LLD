package com.example.urlshortener.config;

import java.time.Duration;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.shortener")
public class UrlShortenerProperties {

	private static final String DEFAULT_BASE_URL = "http://localhost:8080";

	@NotBlank
	private String baseUrl = DEFAULT_BASE_URL;

	@Min(6)
	@Max(16)
	private int generatedCodeLength = 8;

	@Min(1)
	@Max(20)
	private int codeGenerationAttempts = 10;

	@NotNull
	private Duration cacheTtl = Duration.ofHours(24);

	public String getBaseUrl() {
		return trimTrailingSlash(baseUrl);
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public int getGeneratedCodeLength() {
		return generatedCodeLength;
	}

	public void setGeneratedCodeLength(int generatedCodeLength) {
		this.generatedCodeLength = generatedCodeLength;
	}

	public int getCodeGenerationAttempts() {
		return codeGenerationAttempts;
	}

	public void setCodeGenerationAttempts(int codeGenerationAttempts) {
		this.codeGenerationAttempts = codeGenerationAttempts;
	}

	public Duration getCacheTtl() {
		return cacheTtl;
	}

	public void setCacheTtl(Duration cacheTtl) {
		this.cacheTtl = cacheTtl;
	}

	private static String trimTrailingSlash(String value) {
		if (value == null || value.length() <= 1 || !value.endsWith("/")) {
			return value;
		}
		return value.substring(0, value.length() - 1);
	}
}
