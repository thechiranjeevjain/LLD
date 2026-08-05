package com.example.designredis.command;

import com.example.designredis.store.InMemoryRedisStore;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandProcessorTest {

    @Test
    void supportsQuotedValues() {
        CommandProcessor processor = new CommandProcessor(new InMemoryRedisStore(10));

        assertEquals("OK", processor.execute("SET greeting \"hello world\""));
        assertEquals("hello world", processor.execute("GET greeting"));
        assertEquals("OK", processor.execute("SET empty \"\""));
        assertEquals("", processor.execute("GET empty"));
    }

    @Test
    void reportsCommandErrorsWithoutThrowing() {
        CommandProcessor processor = new CommandProcessor(new InMemoryRedisStore(10));

        assertEquals("(error) usage: GET key", processor.execute("GET"));
        assertEquals("(error) unknown command 'NOPE'", processor.execute("NOPE"));
        assertEquals("(error) unterminated quoted string", processor.execute("SET a \"b"));
    }

    @Test
    void tokenizesEscapesAndQuotes() {
        assertEquals(
                List.of("SET", "path", "C:\\temp folder"),
                CommandProcessor.tokenize("SET path \"C:\\\\temp folder\"")
        );
    }

    @Test
    void formatsListAndHashCommands() {
        CommandProcessor processor = new CommandProcessor(new InMemoryRedisStore(10));

        assertEquals("(integer) 2", processor.execute("LPUSH queue first second"));
        assertEquals("1) second" + System.lineSeparator() + "2) first", processor.execute("LRANGE queue 0 -1"));
        assertEquals("first", processor.execute("RPOP queue"));

        assertEquals("(integer) 1", processor.execute("HSET user:1 name Alice"));
        assertEquals("Alice", processor.execute("HGET user:1 name"));
        assertEquals("1) name" + System.lineSeparator() + "2) Alice", processor.execute("HGETALL user:1"));
    }
}
