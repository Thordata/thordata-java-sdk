package com.thordata.sdk;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class Utils {
  private Utils() {}

  public static String buildUserAgent(String version) {
    return "thordata-java-sdk/" + version + " (java " + System.getProperty("java.version") + "; " + System.getProperty("os.name") + ")";
  }

  public static String getenvOrDefault(String envKey, String provided, String def) {
    String env = System.getenv(envKey);
    if (env != null && !env.isBlank()) return env;
    if (provided != null && !provided.isBlank()) return provided;
    return def;
  }

  public static String formEncode(java.util.Map<String, String> payload) {
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (var e : payload.entrySet()) {
      if (e.getValue() == null) continue;
      if (!first) sb.append("&");
      first = false;
      sb.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8));
      sb.append("=");
      sb.append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
    }
    return sb.toString();
  }
}