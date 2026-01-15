package com.thordata.sdk;

import java.util.Locale;

public final class ProxyConfig {
  public ProxyProduct product = ProxyProduct.RESIDENTIAL;

  // account username/password (the part after td-customer-)
  public String username;
  public String password;

  // endpoint overrides (Dashboard shard host recommended)
  public String protocol; // http / https (IMPORTANT: often https required)
  public String host;
  public Integer port;

  public boolean noAuth = false;

  // options
  public String country;
  public String city;
  public String sessionId;
  public Integer sessionDurationMinutes;

  public ProxyConfig country(String c) { this.country = c == null ? null : c.toLowerCase(Locale.ROOT); return this; }
  public ProxyConfig city(String c) { this.city = c == null ? null : c.toLowerCase(Locale.ROOT).replace(" ", "_"); return this; }
  public ProxyConfig session(String s) { this.sessionId = s; return this; }
  public ProxyConfig sticky(int minutes) { this.sessionDurationMinutes = minutes; return this; }

  public String buildGatewayUsername() {
    String base = "td-customer-" + this.username;
    StringBuilder sb = new StringBuilder(base);

    if (country != null && !country.isBlank()) sb.append("-country-").append(country);
    if (city != null && !city.isBlank()) sb.append("-city-").append(city);

    if (sessionId != null && !sessionId.isBlank()) sb.append("-sessid-").append(sessionId);
    if (sessionDurationMinutes != null && sessionDurationMinutes > 0) {
      sb.append("-sesstime-").append(sessionDurationMinutes);
    }
    return sb.toString();
  }

public String effectiveProtocol() {
    // 1. Manual override
    if (this.protocol != null && !this.protocol.isBlank()) return this.protocol.trim().toLowerCase(Locale.ROOT);
    
    // 2. Global Env
    String p = System.getenv("THORDATA_PROXY_PROTOCOL");
    return (p == null || p.isBlank()) ? "https" : p.trim().toLowerCase(Locale.ROOT);
  }

  public String effectiveHost() {
    // 1. Manual override
    if (this.host != null && !this.host.isBlank()) return this.host.trim();

    // 2. Product Env
    String per = System.getenv("THORDATA_" + product.name() + "_PROXY_HOST");
    if (per != null && !per.isBlank()) return per.trim();

    // 3. Global Env
    String global = System.getenv("THORDATA_PROXY_HOST");
    if (global != null && !global.isBlank()) return global.trim();

    // 4. Default Fallback
    return switch (product) {
      case RESIDENTIAL -> "t.pr.thordata.net";
      case DATACENTER -> "dc.pr.thordata.net";
      case MOBILE -> "m.pr.thordata.net";
      case ISP -> "isp.pr.thordata.net";
    };
  }

  public int effectivePort() {
    // 1. Manual override
    if (this.port != null && this.port > 0) return this.port;

    // 2. Product Env
    String per = System.getenv("THORDATA_" + product.name() + "_PROXY_PORT");
    if (per != null && !per.isBlank()) {
        try { return Integer.parseInt(per.trim()); } catch (Exception ignored) {}
    }

    // 3. Global Env
    String global = System.getenv("THORDATA_PROXY_PORT");
    if (global != null && !global.isBlank()) {
        try { return Integer.parseInt(global.trim()); } catch (Exception ignored) {}
    }

    // 4. Default Fallback
    return switch (product) {
      case RESIDENTIAL -> 9999;
      case DATACENTER -> 7777;
      case MOBILE -> 5555;
      case ISP -> 6666;
    };
  }

  public static ProxyConfig residentialFromEnv() {
    ProxyConfig p = new ProxyConfig();
    p.product = ProxyProduct.RESIDENTIAL;
    p.username = System.getenv("THORDATA_RESIDENTIAL_USERNAME");
    p.password = System.getenv("THORDATA_RESIDENTIAL_PASSWORD");
    return p;
  }

  public static ProxyConfig datacenterFromEnv() {
    ProxyConfig p = new ProxyConfig();
    p.product = ProxyProduct.DATACENTER;
    p.username = System.getenv("THORDATA_DATACENTER_USERNAME");
    p.password = System.getenv("THORDATA_DATACENTER_PASSWORD");
    return p;
  }

  public static ProxyConfig mobileFromEnv() {
    ProxyConfig p = new ProxyConfig();
    p.product = ProxyProduct.MOBILE;
    p.username = System.getenv("THORDATA_MOBILE_USERNAME");
    p.password = System.getenv("THORDATA_MOBILE_PASSWORD");
    return p;
  }
}