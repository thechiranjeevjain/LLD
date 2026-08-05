# URL Shortener

Spring Boot URL shortener using MySQL as the source of truth and Redis as a cache-aside redirect cache.

For interview prep, see [docs/LOW_LEVEL_DESIGN.md](docs/LOW_LEVEL_DESIGN.md) for the class diagram, create/redirect sequences, design patterns, cache strategy, and scaling tradeoffs.

## Run Locally

```bash
docker compose up -d
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
docker compose up -d
.\mvnw.cmd spring-boot:run
```

Docker-free demo profile:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=demo
```

The `demo` profile uses in-memory H2 for persistence. Redis is optional in that mode; cache misses fall back to the database.

Create a link:

```bash
curl -X POST http://localhost:8080/api/v1/links \
  -H "Content-Type: application/json" \
  -d '{"longUrl":"https://spring.io/projects/spring-boot","customAlias":"spring"}'
```

Redirect:

```bash
curl -i http://localhost:8080/spring
```

## API Design

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/v1/links` | Create a short link. Body: `longUrl`, optional `customAlias`, optional `expiresAt`. |
| `GET` | `/api/v1/links/{code}` | Fetch metadata: target URL, creation time, expiry, active flag, click count. |
| `DELETE` | `/api/v1/links/{code}` | Soft-delete a short link by marking it inactive and evicting Redis. |
| `GET` | `/{code}` | Redirect to the long URL with HTTP `302 Found`. |

Important status codes:

| Code | Meaning |
| --- | --- |
| `201` | Short link created. |
| `302` | Redirect found. |
| `400` | Invalid URL, invalid alias, or past expiry. |
| `404` | Code does not exist. |
| `409` | Custom alias already exists. |
| `410` | Link exists but has expired. |
| `503` | MySQL is unavailable or code allocation failed. |

## DB Design

`short_links` is the source-of-truth table.

| Column | Purpose |
| --- | --- |
| `id` | Internal surrogate key. |
| `code` | Public short code, unique indexed. |
| `long_url` | Original HTTP or HTTPS URL. |
| `created_at` | Creation timestamp in UTC. |
| `expires_at` | Optional expiration timestamp. |
| `active` | Soft-delete flag. |
| `click_count` | Best-effort redirect counter. |
| `version` | Optimistic-locking version column. |

Indexes:

| Index | Why |
| --- | --- |
| `uk_short_links_code` | Single-row lookup for redirect and alias uniqueness. |
| `idx_short_links_active_expiry` | Efficient expiry cleanup job support. |
| `idx_short_links_created_at` | Operational reporting and pagination support. |

## Cache Strategy

Redis stores `short-link:redirect:{code} -> longUrl`.

This is cache-aside:

1. Redirect first checks Redis.
2. On hit, return the long URL and increment `click_count` best-effort.
3. On miss, read MySQL, validate active and expiry, write Redis, then redirect.
4. On delete or expiry, evict Redis.

TTL behavior:

| Link type | Redis TTL |
| --- | --- |
| Expiring link | `expiresAt - now` so Redis naturally expires with the link. |
| Non-expiring link | Configured `app.shortener.cache-ttl`, default `24h`, so stale values are bounded. |

Redis failures are logged and treated as cache misses; MySQL remains the source of truth.

## Scaling Strategy

App layer:

| Concern | Strategy |
| --- | --- |
| Horizontal scaling | Run many stateless Spring Boot instances behind a load balancer. |
| Hot redirects | Redis absorbs repeated reads for popular codes. |
| Code space | Base62 length 8 gives about `62^8` combinations. Increase length before collisions become material. |
| Write conflicts | Unique DB constraint on `code`; generated codes retry on collision. |
| Analytics load | Move click events to Kafka/Pub/Sub and aggregate asynchronously instead of updating MySQL per redirect. |

Storage layer:

| Concern | Strategy |
| --- | --- |
| MySQL read scaling | Add replicas for metadata/admin reads. Redirect path stays mostly Redis-backed. |
| MySQL write scaling | Partition or shard by code hash when write volume exceeds one primary. |
| Redis scaling | Use Redis Cluster and key hash distribution. |
| Expiry cleanup | Scheduled job scans `active=true AND expires_at < now`, deactivates rows, and evicts keys. |

## Failure Handling

| Failure | Behavior |
| --- | --- |
| Redis down | Redirects fall back to MySQL. Creates still persist in MySQL. |
| Redis write fails | Request succeeds after MySQL commit; next redirect repopulates cache. |
| MySQL down and Redis hit | Redirect can still succeed; click count update is skipped. |
| MySQL down and Redis miss | API returns `503` because source of truth is unavailable. |
| Duplicate custom alias | Returns `409`; DB unique constraint is the final guard. |
| Expired link | Returns `410`, deactivates row, evicts Redis. |
| Invalid URL | Returns `400`; only absolute HTTP and HTTPS URLs are accepted. |

## LLD Notes

Main classes:

| Class | Responsibility |
| --- | --- |
| `ShortLinkController` | CRUD-style REST API. |
| `RedirectController` | Public redirect endpoint. |
| `ShortLinkService` | Business rules: validation, code allocation, expiry, cache-aside resolution. |
| `ShortLinkCache` | Redis access with graceful fallback. |
| `ShortLinkRepository` | MySQL persistence and targeted update queries. |
| `Base62CodeGenerator` | Random URL-safe code generation. |
