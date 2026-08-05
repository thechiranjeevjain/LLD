package com.example.urlshortener.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.regex.Pattern;

import com.example.urlshortener.cache.ShortLinkCache;
import com.example.urlshortener.config.UrlShortenerProperties;
import com.example.urlshortener.domain.ShortLink;
import com.example.urlshortener.repository.ShortLinkRepository;
import com.example.urlshortener.web.dto.CreateShortLinkRequest;
import com.example.urlshortener.web.dto.ShortLinkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShortLinkService {

	public static final Pattern CODE_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{3,32}$");

	private static final Logger log = LoggerFactory.getLogger(ShortLinkService.class);

	private final ShortLinkRepository repository;
	private final ShortLinkCache cache;
	private final Base62CodeGenerator codeGenerator;
	private final UrlShortenerProperties properties;
	private final Clock clock;

	public ShortLinkService(
			ShortLinkRepository repository,
			ShortLinkCache cache,
			Base62CodeGenerator codeGenerator,
			UrlShortenerProperties properties,
			Clock clock) {
		this.repository = repository;
		this.cache = cache;
		this.codeGenerator = codeGenerator;
		this.properties = properties;
		this.clock = clock;
	}

	public ShortLinkResponse create(CreateShortLinkRequest request) {
		String normalizedUrl = normalizeUrl(request.longUrl());
		Instant now = Instant.now(clock);
		if (request.expiresAt() != null && !request.expiresAt().isAfter(now)) {
			throw new InvalidUrlException("expiresAt must be in the future");
		}

		if (request.hasCustomAlias()) {
			return createCustomLink(request.customAlias(), normalizedUrl, now, request.expiresAt());
		}

		return createGeneratedLink(normalizedUrl, now, request.expiresAt());
	}

	public String resolveRedirectUrl(String code) {
		validateCode(code);

		return cache.getLongUrl(code)
			.map(longUrl -> {
				recordClickBestEffort(code);
				return longUrl;
			})
			.orElseGet(() -> resolveFromDatabase(code));
	}

	@Transactional(readOnly = true)
	public ShortLinkResponse get(String code) {
		validateCode(code);

		ShortLink link = repository.findByCode(code)
			.orElseThrow(() -> new LinkNotFoundException(code));
		return ShortLinkResponse.from(link, buildShortUrl(link.getCode()), link.isActive() && !link.isExpired(Instant.now(clock)));
	}

	@Transactional
	public void deactivate(String code) {
		validateCode(code);

		int updated = repository.deactivateByCode(code);
		if (updated == 0) {
			throw new LinkNotFoundException(code);
		}
		cache.evict(code);
	}

	private String resolveFromDatabase(String code) {
		ShortLink link = repository.findByCodeAndActiveTrue(code)
			.orElseThrow(() -> new LinkNotFoundException(code));

		if (link.isExpired(Instant.now(clock))) {
			repository.deactivateByCode(code);
			cache.evict(code);
			throw new LinkExpiredException(code);
		}

		cache.put(link);
		recordClickBestEffort(code);
		return link.getLongUrl();
	}

	private void recordClickBestEffort(String code) {
		try {
			repository.incrementClickCount(code);
		}
		catch (DataAccessException ex) {
			log.warn("Click count update failed for short code {}: {}", code, ex.getMessage());
		}
	}

	private ShortLinkResponse createCustomLink(String code, String longUrl, Instant createdAt, Instant expiresAt) {
		if (repository.existsByCode(code)) {
			throw new ShortCodeAlreadyExistsException(code);
		}

		try {
			ShortLink link = repository.saveAndFlush(new ShortLink(code, longUrl, createdAt, expiresAt));
			cache.put(link);
			return ShortLinkResponse.from(link, buildShortUrl(code));
		}
		catch (DataIntegrityViolationException ex) {
			throw new ShortCodeAlreadyExistsException(code);
		}
	}

	private ShortLinkResponse createGeneratedLink(String longUrl, Instant createdAt, Instant expiresAt) {
		for (int attempt = 0; attempt < properties.getCodeGenerationAttempts(); attempt++) {
			String code = codeGenerator.generate(properties.getGeneratedCodeLength());
			if (repository.existsByCode(code)) {
				continue;
			}

			try {
				ShortLink link = repository.saveAndFlush(new ShortLink(code, longUrl, createdAt, expiresAt));
				cache.put(link);
				return ShortLinkResponse.from(link, buildShortUrl(code));
			}
			catch (DataIntegrityViolationException ex) {
				log.info("Generated short code collided during insert, retrying: {}", code);
			}
		}
		throw new ShortCodeGenerationException();
	}

	private String buildShortUrl(String code) {
		return properties.getBaseUrl() + "/" + code;
	}

	private static void validateCode(String code) {
		if (code == null || !CODE_PATTERN.matcher(code).matches()) {
			throw new LinkNotFoundException(String.valueOf(code));
		}
	}

	private static String normalizeUrl(String rawUrl) {
		try {
			URI uri = new URI(rawUrl).normalize();
			String scheme = uri.getScheme();
			if (scheme == null || uri.getHost() == null) {
				throw new InvalidUrlException("longUrl must be an absolute HTTP or HTTPS URL");
			}
			String normalizedScheme = scheme.toLowerCase(Locale.ROOT);
			if (!normalizedScheme.equals("http") && !normalizedScheme.equals("https")) {
				throw new InvalidUrlException("longUrl must use HTTP or HTTPS");
			}
			return uri.toASCIIString();
		}
		catch (URISyntaxException ex) {
			throw new InvalidUrlException("longUrl is not a valid URL");
		}
	}
}
