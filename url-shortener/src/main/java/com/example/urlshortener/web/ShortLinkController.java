package com.example.urlshortener.web;

import java.net.URI;

import com.example.urlshortener.service.ShortLinkService;
import com.example.urlshortener.web.dto.CreateShortLinkRequest;
import com.example.urlshortener.web.dto.ShortLinkResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/links")
public class ShortLinkController {

	private final ShortLinkService service;

	public ShortLinkController(ShortLinkService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<ShortLinkResponse> create(@Valid @RequestBody CreateShortLinkRequest request) {
		ShortLinkResponse response = service.create(request);
		return ResponseEntity.created(URI.create(response.shortUrl())).body(response);
	}

	@GetMapping("/{code}")
	public ShortLinkResponse get(@PathVariable String code) {
		return service.get(code);
	}

	@DeleteMapping("/{code}")
	public ResponseEntity<Void> deactivate(@PathVariable String code) {
		service.deactivate(code);
		return ResponseEntity.noContent().build();
	}
}
