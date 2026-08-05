# FIX Gateway LLD

A runnable Java 17 project that models a simplified FIX order gateway. It covers the core responsibilities expected in a low-level design interview:

- FIX message parsing and serialization with body length and checksum validation.
- Session management with sender/target validation and sequence number tracking.
- Logon, heartbeat, session reject, and new-order-single flows.
- Order translation from FIX tags into domain objects.
- Risk checks for duplicate client order IDs, max quantity, and max notional.
- Routing to an in-memory exchange adapter and returning execution reports.

This is not a production FIX engine. It intentionally keeps persistence, network sockets, resend handling, TLS, repeating groups, and market-data support outside the implementation so the project stays focused on design clarity.

## Run

```powershell
mvn test
mvn exec:java
```

The demo prints outbound FIX responses in a pipe-delimited form:

```text
8=FIX.4.4|9=...|35=A|...
8=FIX.4.4|9=...|35=8|...|39=0|...
8=FIX.4.4|9=...|35=8|...|39=8|58=Duplicate ClOrdID...
```

## Supported inbound messages

| MsgType | Name | Behavior |
| --- | --- | --- |
| `A` | Logon | Marks the client session as logged on and returns logon acknowledgement. |
| `0` | Heartbeat | Returns a heartbeat after the session is logged on. |
| `D` | NewOrderSingle | Validates, risk-checks, routes to the exchange adapter, and returns an execution report. |
| other | Unsupported | Returns a session reject (`35=3`). |

## Example NewOrderSingle

```text
8=FIX.4.4|9=105|35=D|34=2|49=CLIENT1|56=FIX-GW|52=20260805-12:00:00.000|11=ORD-1|55=AAPL|54=1|38=100|40=2|44=175.25|59=0|10=230|
```

Tag highlights:

| Tag | Meaning |
| --- | --- |
| `11` | Client order ID |
| `55` | Symbol |
| `54` | Side: `1` buy, `2` sell |
| `38` | Quantity |
| `40` | Order type: `1` market, `2` limit |
| `44` | Price, required for limit orders |
| `59` | Time in force |

## Project structure

```text
src/main/java/com/chiranjeev/lld/fixgateway
  fix/       FIX wire model, parser, serializer, checksums, session sequencing
  domain/    Order and execution-report domain model
  risk/      Risk engine and individual risk rules
  routing/   Exchange routing abstraction and in-memory adapter
  gateway/   Gateway orchestration and FIX/domain mapping
```

See [docs/LLD.md](docs/LLD.md) for design details and sequence diagrams.
