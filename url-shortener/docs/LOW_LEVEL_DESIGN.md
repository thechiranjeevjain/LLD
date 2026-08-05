# Low-Level Design: URL Shortener

## Interview Scope

Design a URL shortener with REST APIs, custom aliases, generated Base62 codes, redirect caching, expiry, soft delete, and database-backed uniqueness.

Functional requirements:

- Create a short link for a valid HTTP or HTTPS URL.
- Support optional custom alias.
- Support optional expiry time.
- Redirect by short code.
- Fetch link metadata.
- Deactivate a short link.
- Count redirects on a best-effort basis.

Non-functional requirements:

- MySQL is the source of truth.
- Redis is a cache-aside optimization for redirect reads.
- Generated code collisions should retry safely.
- Custom aliases should be unique.
- Redis failures should not make the source-of-truth API unavailable.

## Core Class Diagram

```mermaid
classDiagram
    class ShortLinkController {
        -ShortLinkService service
        +create(CreateShortLinkRequest) ResponseEntity~ShortLinkResponse~
        +get(String) ShortLinkResponse
        +deactivate(String) ResponseEntity~Void~
    }

    class RedirectController {
        -ShortLinkService service
        +redirect(String) ResponseEntity~Void~
    }

    class ShortLinkService {
        -ShortLinkRepository repository
        -ShortLinkCache cache
        -Base62CodeGenerator codeGenerator
        -UrlShortenerProperties properties
        -Clock clock
        +create(String, String, Instant) ShortLink
        +getByCode(String) ShortLink
        +resolveRedirect(String) URI
        +deactivate(String) void
    }

    class ShortLinkRepository {
        <<interface>>
        +findByCode(String) Optional~ShortLink~
        +existsByCode(String) boolean
        +incrementClickCount(String) int
    }

    class ShortLinkCache {
        -StringRedisTemplate redisTemplate
        -UrlShortenerProperties properties
        -Clock clock
        +get(String) Optional~String~
        +put(ShortLink) void
        +evict(String) void
    }

    class Base62CodeGenerator {
        -SecureRandom random
        +generate(int) String
    }

    class ShortLink
    class UrlShortenerProperties
    class CreateShortLinkRequest
    class ShortLinkResponse
    class ErrorResponse
    class ApiExceptionHandler

    ShortLinkController --> ShortLinkService
    RedirectController --> ShortLinkService
    ShortLinkService --> ShortLinkRepository
    ShortLinkService --> ShortLinkCache
    ShortLinkService --> Base62CodeGenerator
    ShortLinkService --> UrlShortenerProperties
    ShortLinkRepository --> ShortLink
    ShortLinkCache --> ShortLink
    ShortLinkController --> CreateShortLinkRequest
    ShortLinkController --> ShortLinkResponse
    ApiExceptionHandler --> ErrorResponse
```

## Main Responsibilities

| Component | Responsibility |
| --- | --- |
| `ShortLinkController` | Admin-style create, read, and deactivate REST endpoints. |
| `RedirectController` | Public `/{code}` redirect endpoint. |
| `ShortLinkService` | Validates input, allocates codes, resolves redirects, handles expiry, updates cache, and increments clicks. |
| `ShortLinkRepository` | JPA persistence for source-of-truth MySQL rows. |
| `ShortLinkCache` | Redis cache-aside wrapper with graceful failure behavior. |
| `Base62CodeGenerator` | Generates random URL-safe short codes. |
| `ApiExceptionHandler` | Maps domain exceptions to HTTP responses. |

## Create Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant API as ShortLinkController
    participant S as ShortLinkService
    participant G as Base62CodeGenerator
    participant DB as ShortLinkRepository

    C->>API: POST /api/v1/links
    API->>S: create(longUrl, alias, expiresAt)
    S->>S: validate URL, alias, and expiry
    alt custom alias
        S->>DB: existsByCode(alias)
    else generated code
        S->>G: generate(length)
        S->>DB: existsByCode(code)
    end
    S->>DB: save(ShortLink)
    DB-->>S: persisted link
    S-->>API: ShortLink
    API-->>C: 201 ShortLinkResponse
```

## Redirect Flow

```mermaid
sequenceDiagram
    participant C as Browser
    participant R as RedirectController
    participant S as ShortLinkService
    participant Cache as ShortLinkCache
    participant DB as ShortLinkRepository

    C->>R: GET /{code}
    R->>S: resolveRedirect(code)
    S->>Cache: get(code)
    alt cache hit
        Cache-->>S: longUrl
        S->>DB: incrementClickCount(code)
    else cache miss
        S->>DB: findByCode(code)
        S->>S: validate active and not expired
        S->>Cache: put(link)
        S->>DB: incrementClickCount(code)
    end
    S-->>R: URI
    R-->>C: 302 Location
```

## Database Model

`short_links` is the source-of-truth table.

| Column | Purpose |
| --- | --- |
| `id` | Internal primary key. |
| `code` | Public short code with a unique index. |
| `long_url` | Original URL. |
| `created_at` | Creation timestamp. |
| `expires_at` | Optional expiry. |
| `active` | Soft-delete flag. |
| `click_count` | Best-effort redirect counter. |
| `version` | Optimistic locking support. |

## Design Patterns

| Pattern | Where | Why it matters in interview discussion |
| --- | --- | --- |
| Layered Architecture | Controllers, service, repository, cache | Separates API, business rules, persistence, and caching. |
| Repository | `ShortLinkRepository` | Hides JPA and SQL details from business logic. |
| Cache-Aside | `ShortLinkCache` plus `ShortLinkService.resolveRedirect` | Redis accelerates hot redirects while MySQL remains authoritative. |
| Strategy-like Generator | `Base62CodeGenerator` | Code allocation can be replaced by counter, Snowflake, hash, or range allocator. |
| DTO | Request and response records | Keeps REST payloads separate from the JPA entity. |
| Exception Mapper | `ApiExceptionHandler` | Centralizes domain-to-HTTP error translation. |

## Consistency and Failure Handling

Important invariants:

- `code` is unique in MySQL.
- Only active, non-expired links redirect.
- Deactivation evicts Redis.
- Expiring links are cached only until their expiry time.
- Redis failures are treated as misses; MySQL remains the source of truth.

Tradeoffs:

- Random code generation is simple but must retry on collision.
- Updating click count synchronously is easy but can become a write bottleneck.
- Redis hit with MySQL unavailable can still redirect, but click counting may be skipped.
- Cleanup of expired links is a background operational concern.

## Extension Points

- Add a `CodeGenerator` interface if multiple code allocation strategies are needed.
- Move click events to Kafka or Pub/Sub for asynchronous analytics.
- Add user ownership and access control.
- Add custom domain support.
- Add sharding by code hash when one MySQL primary becomes limiting.

## Interview Talking Points

- The redirect path is read-heavy and latency-sensitive, so Redis cache-aside is justified.
- The database unique constraint is the final guard for alias and generated-code collision correctness.
- Generated codes should not encode the long URL if privacy and enumeration resistance matter.
- Click counting is best-effort in this implementation; strict analytics needs an event pipeline.
