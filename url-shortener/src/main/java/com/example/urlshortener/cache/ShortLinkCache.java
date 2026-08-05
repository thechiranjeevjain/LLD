package com.example.urlshortener.cache;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import com.example.urlshortener.config.UrlShortenerProperties;
import com.example.urlshortener.domain.ShortLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class ShortLinkCache {

	private static final Logger log = LoggerFactory.getLogger(ShortLinkCache.class);
	private static final String KEY_PREFIX = "short-link:redirect:";

	private final StringRedisTemplate redisTemplate;
	private final UrlShortenerProperties properties;
	private final Clock clock;

	public ShortLinkCache(StringRedisTemplate redisTemplate, UrlShortenerProperties properties, Clock clock) {
		this.redisTemplate = redisTemplate;
		this.properties = properties;
		this.clock = clock;
	}

	public Optional<String> getLongUrl(String code) {
		try {
			return Optional.ofNullable(redisTemplate.opsForValue().get(key(code)));
		}
		catch (DataAccessException ex) {
			log.warn("Redis lookup failed for short code {}: {}", code, ex.getMessage());
			return Optional.empty();
		}
	}

	public void put(ShortLink link) {
		Duration ttl = ttlFor(link.getExpiresAt());
		if (ttl.isZero() || ttl.isNegative()) {
			evict(link.getCode());
			return;
		}

		try {
			redisTemplate.opsForValue().set(key(link.getCode()), link.getLongUrl(), ttl);
		}
		catch (DataAccessException ex) {
			log.warn("Redis write failed for short code {}: {}", link.getCode(), ex.getMessage());
		}
	}

	public void evict(String code) {
		try {
			redisTemplate.delete(key(code));
		}
		catch (DataAccessException ex) {
			log.warn("Redis eviction failed for short code {}: {}", code, ex.getMessage());
		}
	}

	private Duration ttlFor(Instant expiresAt) {
		if (expiresAt == null) {
			return properties.getCacheTtl();
		}
		return Duration.between(Instant.now(clock), expiresAt);
	}

	private static String key(String code) {
		return KEY_PREFIX + code;
	}
}
