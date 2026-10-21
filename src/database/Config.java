package database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Lightweight .env file reader.
 * Reads KEY=VALUE or KEY="VALUE" pairs from a .env file
 * located in the current working directory (project root).
 */
public class Config {
    private static final Map<String, String> env = new HashMap<>();
    private static boolean loaded = false;

    static {
        // Try project root first, then parent directories
        File envFile = findEnvFile();
        if (envFile != null && envFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(envFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        // Strip surrounding quotes
                        if (value.startsWith("\"") && value.endsWith("\"")) {
                            value = value.substring(1, value.length() - 1);
                        }
                        env.put(key, value);
                    }
                }
                loaded = true;
            } catch (Exception e) {
                System.err.println("[Config] Failed to read .env file: " + e.getMessage());
            }
        } else {
            System.err.println("[Config] .env file not found. Please create .env from .env.example in the project root.");
        }
    }

    private static File findEnvFile() {
        // Check current directory first
        File f = new File(".env");
        if (f.exists()) return f;
        // Check parent (in case running from src/ or bin/)
        f = new File("../.env");
        if (f.exists()) return f;
        return new File(".env"); // Return default path even if missing (error handled above)
    }

    public static String get(String key) {
        return env.get(key);
    }

    public static boolean isLoaded() {
        return loaded;
    }
}
