package com.example.urlshortener.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "short_links")
public class ShortLink {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 32)
	private String code;

	@Column(name = "long_url", nullable = false, length = 2048)
	private String longUrl;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "expires_at")
	private Instant expiresAt;

	@Column(nullable = false)
	private boolean active = true;

	@Column(name = "click_count", nullable = false)
	private long clickCount;

	@Version
	@Column(nullable = false)
	private long version;

	protected ShortLink() {
	}

	public ShortLink(String code, String longUrl, Instant createdAt, Instant expiresAt) {
		this.code = code;
		this.longUrl = longUrl;
		this.createdAt = createdAt;
		this.expiresAt = expiresAt;
	}

	public Long getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getLongUrl() {
		return longUrl;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public boolean isActive() {
		return active;
	}

	public long getClickCount() {
		return clickCount;
	}

	public long getVersion() {
		return version;
	}

	public boolean isExpired(Instant now) {
		return expiresAt != null && !expiresAt.isAfter(now);
	}

	public void deactivate() {
		this.active = false;
	}
}
