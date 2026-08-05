package com.example.designredis.command;

import com.example.designredis.store.RedisException;
import com.example.designredis.store.RedisStore;
import com.example.designredis.store.RedisType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class CommandProcessor {
    private static final String NIL = "(nil)";

    private final RedisStore store;

    public CommandProcessor(RedisStore store) {
        this.store = store;
    }

    public String execute(String line) {
        List<String> tokens;
        try {
            tokens = tokenize(line);
        } catch (IllegalArgumentException exception) {
            return error(exception.getMessage());
        }

        if (tokens.isEmpty()) {
            return "";
        }

        String command = tokens.get(0).toUpperCase(Locale.ROOT);
        try {
            return switch (command) {
                case "SET" -> set(tokens);
                case "GET" -> get(tokens);
                case "DEL" -> del(tokens);
                case "EXPIRE" -> expire(tokens);
                case "TTL" -> ttl(tokens);
                case "INCR" -> incr(tokens);
                case "TYPE" -> type(tokens);
                case "KEYS" -> keys(tokens);
                case "LPUSH" -> lpush(tokens);
                case "RPOP" -> rpop(tokens);
                case "LRANGE" -> lrange(tokens);
                case "HSET" -> hset(tokens);
                case "HGET" -> hget(tokens);
                case "HGETALL" -> hgetall(tokens);
                case "HELP" -> help(tokens);
                case "EXIT", "QUIT" -> "Use Ctrl+C or close the shell to exit this processor.";
                default -> error("unknown command '" + tokens.get(0) + "'");
            };
        } catch (IllegalArgumentException | RedisException exception) {
            return error(exception.getMessage());
        }
    }

    private String set(List<String> tokens) {
        if (tokens.size() != 3 && tokens.size() != 5) {
            return error("usage: SET key value [EX seconds]");
        }

        if (tokens.size() == 3) {
            store.set(tokens.get(1), tokens.get(2));
            return "OK";
        }

        if (!"EX".equalsIgnoreCase(tokens.get(3))) {
            return error("usage: SET key value [EX seconds]");
        }

        store.set(tokens.get(1), tokens.get(2), Duration.ofSeconds(parseLong(tokens.get(4), "seconds")));
        return "OK";
    }

    private String get(List<String> tokens) {
        requireArity(tokens, 2, "GET key");
        return store.get(tokens.get(1)).orElse(NIL);
    }

    private String del(List<String> tokens) {
        if (tokens.size() < 2) {
            return error("usage: DEL key [key ...]");
        }
        List<String> keys = tokens.subList(1, tokens.size());
        return integer(store.delete(keys.toArray(String[]::new)));
    }

    private String expire(List<String> tokens) {
        requireArity(tokens, 3, "EXPIRE key seconds");
        boolean changed = store.expire(tokens.get(1), Duration.ofSeconds(parseLong(tokens.get(2), "seconds")));
        return integer(changed ? 1 : 0);
    }

    private String ttl(List<String> tokens) {
        requireArity(tokens, 2, "TTL key");
        return integer(store.ttlSeconds(tokens.get(1)));
    }

    private String incr(List<String> tokens) {
        requireArity(tokens, 2, "INCR key");
        return integer(store.increment(tokens.get(1)));
    }

    private String type(List<String> tokens) {
        requireArity(tokens, 2, "TYPE key");
        return store.type(tokens.get(1)).map(RedisType::wireName).orElse("none");
    }

    private String keys(List<String> tokens) {
        requireArity(tokens, 1, "KEYS");
        Set<String> keys = store.keys();
        if (keys.isEmpty()) {
            return "(empty)";
        }
        return numbered(new ArrayList<>(keys));
    }

    private String lpush(List<String> tokens) {
        if (tokens.size() < 3) {
            return error("usage: LPUSH key value [value ...]");
        }
        return integer(store.lpush(tokens.get(1), tokens.subList(2, tokens.size()).toArray(String[]::new)));
    }

    private String rpop(List<String> tokens) {
        requireArity(tokens, 2, "RPOP key");
        return store.rpop(tokens.get(1)).orElse(NIL);
    }

    private String lrange(List<String> tokens) {
        requireArity(tokens, 4, "LRANGE key start stop");
        List<String> values = store.lrange(
                tokens.get(1),
                parseInt(tokens.get(2), "start"),
                parseInt(tokens.get(3), "stop")
        );
        if (values.isEmpty()) {
            return "(empty)";
        }
        return numbered(values);
    }

    private String hset(List<String> tokens) {
        requireArity(tokens, 4, "HSET key field value");
        return integer(store.hset(tokens.get(1), tokens.get(2), tokens.get(3)));
    }

    private String hget(List<String> tokens) {
        requireArity(tokens, 3, "HGET key field");
        return store.hget(tokens.get(1), tokens.get(2)).orElse(NIL);
    }

    private String hgetall(List<String> tokens) {
        requireArity(tokens, 2, "HGETALL key");
        Map<String, String> values = store.hgetall(tokens.get(1));
        if (values.isEmpty()) {
            return "(empty)";
        }

        List<String> lines = new ArrayList<>();
        values.forEach((field, value) -> {
            lines.add(field);
            lines.add(value);
        });
        return numbered(lines);
    }

    private String help(List<String> tokens) {
        requireArity(tokens, 1, "HELP");
        return """
                SET key value [EX seconds]
                GET key
                DEL key [key ...]
                EXPIRE key seconds
                TTL key
                INCR key
                TYPE key
                KEYS
                LPUSH key value [value ...]
                RPOP key
                LRANGE key start stop
                HSET key field value
                HGET key field
                HGETALL key
                EXIT
                """.stripTrailing();
    }

    private static void requireArity(List<String> tokens, int expected, String usage) {
        if (tokens.size() != expected) {
            throw new IllegalArgumentException("usage: " + usage);
        }
    }

    private static int parseInt(String value, String name) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(name + " must be an integer", exception);
        }
    }

    private static long parseLong(String value, String name) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(name + " must be an integer", exception);
        }
    }

    private static String numbered(List<String> values) {
        List<String> lines = new ArrayList<>();
        for (int index = 0; index < values.size(); index++) {
            lines.add((index + 1) + ") " + values.get(index));
        }
        return String.join(System.lineSeparator(), lines);
    }

    private static String integer(long value) {
        return "(integer) " + value;
    }

    private static String error(String message) {
        return "(error) " + message;
    }

    static List<String> tokenize(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuote = false;
        char quote = 0;
        boolean escaping = false;
        boolean tokenStarted = false;

        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (escaping) {
                current.append(character);
                escaping = false;
                tokenStarted = true;
                continue;
            }

            if (character == '\\') {
                escaping = true;
                tokenStarted = true;
                continue;
            }

            if (inQuote) {
                if (character == quote) {
                    inQuote = false;
                } else {
                    current.append(character);
                }
                tokenStarted = true;
                continue;
            }

            if (character == '"' || character == '\'') {
                inQuote = true;
                quote = character;
                tokenStarted = true;
                continue;
            }

            if (Character.isWhitespace(character)) {
                if (tokenStarted) {
                    tokens.add(current.toString());
                    current.setLength(0);
                    tokenStarted = false;
                }
                continue;
            }

            current.append(character);
            tokenStarted = true;
        }

        if (escaping) {
            current.append('\\');
            tokenStarted = true;
        }
        if (inQuote) {
            throw new IllegalArgumentException("unterminated quoted string");
        }
        if (tokenStarted) {
            tokens.add(current.toString());
        }
        return tokens;
    }
}
