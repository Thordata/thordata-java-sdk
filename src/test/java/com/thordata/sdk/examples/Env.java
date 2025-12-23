package com.thordata.sdk.examples;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class Env {
  private static final Map<String, String> DOTENV = loadDotEnv();

  private Env() {}

  public static String get(String key) {
    String v = DOTENV.get(key);
    if (v != null && !v.isBlank()) return v;

    v = System.getenv(key);
    if (v != null && !v.isBlank()) return v;

    return null;
  }

  public static String getOrDefault(String key, String def) {
    String v = get(key);
    return (v == null || v.isBlank()) ? def : v;
  }

  private static Map<String, String> loadDotEnv() {
    Map<String, String> out = new HashMap<>();
    Path p = Path.of(".env");
    if (!Files.exists(p)) return out;

    try {
      for (String line : Files.readAllLines(p, StandardCharsets.UTF_8)) {
        String s = line.trim();
        if (s.isEmpty() || s.startsWith("#")) continue;

        int idx = s.indexOf('=');
        if (idx <= 0) continue;

        String k = s.substring(0, idx).trim();
        String v = s.substring(idx + 1).trim();

        // Strip optional quotes
        if ((v.startsWith("\"") && v.endsWith("\"")) || (v.startsWith("'") && v.endsWith("'"))) {
          v = v.substring(1, v.length() - 1);
        }

        out.put(k, v);
      }
      return out;
    } catch (Exception e) {
      return out;
    }
  }
}