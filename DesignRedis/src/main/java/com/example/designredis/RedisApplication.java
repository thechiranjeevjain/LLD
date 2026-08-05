package com.example.designredis;

import com.example.designredis.command.CommandProcessor;
import com.example.designredis.store.InMemoryRedisStore;

import java.util.Scanner;

public final class RedisApplication {

    private RedisApplication() {
    }

    public static void main(String[] args) {
        int maxKeys = parseMaxKeys(args);
        CommandProcessor processor = new CommandProcessor(new InMemoryRedisStore(maxKeys));

        System.out.println("DesignRedis started with maxKeys=" + maxKeys + ". Type HELP for commands or EXIT to quit.");
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("redis> ");
                if (!scanner.hasNextLine()) {
                    break;
                }

                String line = scanner.nextLine();
                if ("EXIT".equalsIgnoreCase(line.trim()) || "QUIT".equalsIgnoreCase(line.trim())) {
                    break;
                }

                String output = processor.execute(line);
                if (!output.isBlank()) {
                    System.out.println(output);
                }
            }
        }
    }

    private static int parseMaxKeys(String[] args) {
        if (args.length == 0 || args[0].isBlank()) {
            return 1024;
        }

        try {
            int maxKeys = Integer.parseInt(args[0]);
            if (maxKeys <= 0) {
                throw new IllegalArgumentException("maxKeys must be positive");
            }
            return maxKeys;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("maxKeys must be a positive integer", exception);
        }
    }
}
