package com.example.urlshortener.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import com.example.urlshortener.cache.ShortLinkCache;
import com.example.urlshortener.config.UrlShortenerProperties;
import com.example.urlshortener.domain.ShortLink;
import com.example.urlshortener.repository.ShortLinkRepository;
import com.example.urlshortener.web.dto.CreateShortLinkRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class ShortLinkServiceTest {

	private static final Instant NOW = Instant.parse("2026-08-05T12:00:00Z");

	@Mock
	private ShortLinkRepository repository;

	@Mock
	private ShortLinkCache cache;

	@Mock
	private Base62CodeGenerator codeGenerator;

	private ShortLinkService service;

	@BeforeEach
	void setUp() {
		UrlShortenerProperties properties = new UrlShortenerProperties();
		properties.setBaseUrl("http://sho.rt");
		properties.setGeneratedCodeLength(8);
		properties.setCacheTtl(Duration.ofHours(1));
		service = new ShortLinkService(repository, cache, codeGenerator, properties, Clock.fixed(NOW, ZoneOffset.UTC));
	}

	@Test
	void createsShortLinkWithCustomAlias() {
		CreateShortLinkRequest request = new CreateShortLinkRequest("https://spring.io/projects/spring-boot", "spring", null);
		when(repository.existsByCode("spring")).thenReturn(false);
		when(repository.saveAndFlush(any(ShortLink.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var response = service.create(request);

		assertThat(response.code()).isEqualTo("spring");
		assertThat(response.shortUrl()).isEqualTo("http://sho.rt/spring");
		assertThat(response.longUrl()).isEqualTo("https://spring.io/projects/spring-boot");
		assertThat(response.active()).isTrue();
		verify(cache).put(any(ShortLink.class));
	}

	@Test
	void rejectsDuplicateCustomAlias() {
		CreateShortLinkRequest request = new CreateShortLinkRequest("https://example.com", "taken", null);
		when(repository.existsByCode("taken")).thenReturn(true);

		assertThatThrownBy(() -> service.create(request))
			.isInstanceOf(ShortCodeAlreadyExistsException.class);
	}

	@Test
	void retriesGeneratedCodeWhenInsertCollides() {
		CreateShortLinkRequest request = new CreateShortLinkRequest("https://example.com", null, null);
		when(codeGenerator.generate(8)).thenReturn("abc12345", "xyz78901");
		when(repository.existsByCode("abc12345")).thenReturn(false);
		when(repository.existsByCode("xyz78901")).thenReturn(false);
		when(repository.saveAndFlush(any(ShortLink.class)))
			.thenThrow(new DataIntegrityViolationException("duplicate code"))
			.thenAnswer(invocation -> invocation.getArgument(0));

		var response = service.create(request);

		assertThat(response.code()).isEqualTo("xyz78901");
		verify(cache).put(any(ShortLink.class));
	}

	@Test
	void resolvesCacheHitEvenIfClickCountingFails() {
		when(cache.getLongUrl("abc123")).thenReturn(Optional.of("https://example.com/docs"));
		doThrow(new DataAccessResourceFailureException("db unavailable")).when(repository).incrementClickCount("abc123");

		String longUrl = service.resolveRedirectUrl("abc123");

		assertThat(longUrl).isEqualTo("https://example.com/docs");
	}

	@Test
	void expiresDatabaseHitAndEvictsCache() {
		ShortLink expired = new ShortLink("old123", "https://example.com/old", NOW.minusSeconds(60), NOW.minusSeconds(1));
		when(cache.getLongUrl("old123")).thenReturn(Optional.empty());
		when(repository.findByCodeAndActiveTrue("old123")).thenReturn(Optional.of(expired));

		assertThatThrownBy(() -> service.resolveRedirectUrl("old123"))
			.isInstanceOf(LinkExpiredException.class);

		verify(repository).deactivateByCode("old123");
		verify(cache).evict("old123");
	}

	@Test
	void rejectsNonHttpUrl() {
		CreateShortLinkRequest request = new CreateShortLinkRequest("ftp://example.com/file", null, null);

		assertThatThrownBy(() -> service.create(request))
			.isInstanceOf(InvalidUrlException.class);
	}
}
