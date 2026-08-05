package com.example.urlshortener.web;

import java.net.URI;

import com.example.urlshortener.service.ShortLinkService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RedirectController {

	private final ShortLinkService service;

	public RedirectController(ShortLinkService service) {
		this.service = service;
	}

	@GetMapping("/{code:[A-Za-z0-9_-]{3,32}}")
	public ResponseEntity<Void> redirect(@PathVariable String code) {
		String longUrl = service.resolveRedirectUrl(code);
		return ResponseEntity.status(HttpStatus.FOUND)
			.header(HttpHeaders.CACHE_CONTROL, "private, no-store")
			.location(URI.create(longUrl))
			.build();
	}
}
