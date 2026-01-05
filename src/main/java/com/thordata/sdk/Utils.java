// src/main/java/com/thordata/sdk/Utils.java
package com.thordata.sdk;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.InetSocketAddress;
import java.net.URI;

public final class Utils {
  private Utils() {}

  public static String buildUserAgent(String version) {
    String javaVer = System.getProperty("java.version");
    String osName = System.getProperty("os.name");
    String osArch = System.getProperty("os.arch");
    // Format: thordata-java-sdk/{version} java/{java_ver} ({os_name}/{os_arch})
    return String.format("thordata-java-sdk/%s java/%s (%s/%s)", 
        version, javaVer, osName, osArch);
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

    public static InetSocketAddress parseHttpProxy(String proxyUrl) {
    if (proxyUrl == null || proxyUrl.isBlank()) return null;
    URI u = URI.create(proxyUrl.trim());
    String host = u.getHost();
    int port = u.getPort();
    if (host == null || port <= 0) return null;
    return new InetSocketAddress(host, port);
  }
}