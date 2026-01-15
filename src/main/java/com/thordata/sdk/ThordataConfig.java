package com.thordata.sdk;

import java.time.Duration;

public final class ThordataConfig {
  public final String scraperToken;
  public final String publicToken;
  public final String publicKey;

  public final Duration timeout;
  public final String userAgent;
  public final String httpProxyUrl;

  public final String scraperApiBaseUrl;
  public final String universalApiBaseUrl;
  public final String webScraperApiBaseUrl;
  public final String locationsBaseUrl;

  private ThordataConfig(Builder builder) {
    this.scraperToken = builder.scraperToken;
    this.publicToken = builder.publicToken;
    this.publicKey = builder.publicKey;
    this.timeout = builder.timeout;
    this.userAgent = builder.userAgent;
    this.httpProxyUrl = builder.httpProxyUrl;
    
    this.scraperApiBaseUrl = Utils.getenvOrDefault("THORDATA_SCRAPERAPI_BASE_URL", builder.scraperApiBaseUrl, "https://scraperapi.thordata.com");
    this.universalApiBaseUrl = Utils.getenvOrDefault("THORDATA_UNIVERSALAPI_BASE_URL", builder.universalApiBaseUrl, "https://universalapi.thordata.com");
    this.webScraperApiBaseUrl = Utils.getenvOrDefault("THORDATA_WEB_SCRAPER_API_BASE_URL", builder.webScraperApiBaseUrl, "https://openapi.thordata.com/api/web-scraper-api");
    this.locationsBaseUrl = Utils.getenvOrDefault("THORDATA_LOCATIONS_BASE_URL", builder.locationsBaseUrl, "https://openapi.thordata.com/api/locations");
  }

  // Deprecated constructor for backward compatibility (updated to match what tests expect + proxy)
  public ThordataConfig(
      String scraperToken, String publicToken, String publicKey,
      Duration timeout, String userAgent,
      String scraperApiBaseUrl, String universalApiBaseUrl,
      String webScraperApiBaseUrl, String locationsBaseUrl,
      String httpProxyUrl
  ) {
      this.scraperToken = scraperToken;
      this.publicToken = publicToken;
      this.publicKey = publicKey;
      this.timeout = timeout == null ? Duration.ofSeconds(30) : timeout;
      this.userAgent = (userAgent == null || userAgent.isBlank()) ? Utils.buildUserAgent("1.2.0") : userAgent;
      this.scraperApiBaseUrl = scraperApiBaseUrl;
      this.universalApiBaseUrl = universalApiBaseUrl;
      this.webScraperApiBaseUrl = webScraperApiBaseUrl;
      this.locationsBaseUrl = locationsBaseUrl;
      this.httpProxyUrl = httpProxyUrl;
  }
  
  // Another overload for tests that don't pass proxy
  public ThordataConfig(
      String scraperToken, String publicToken, String publicKey,
      Duration timeout, String userAgent,
      String scraperApiBaseUrl, String universalApiBaseUrl,
      String webScraperApiBaseUrl, String locationsBaseUrl
  ) {
      this(scraperToken, publicToken, publicKey, timeout, userAgent, scraperApiBaseUrl, universalApiBaseUrl, webScraperApiBaseUrl, locationsBaseUrl, null);
  }

  public static Builder builder(String scraperToken) {
    return new Builder(scraperToken);
  }

  public static class Builder {
    private final String scraperToken;
    private String publicToken;
    private String publicKey;
    private Duration timeout = Duration.ofSeconds(30);
    private String userAgent = Utils.buildUserAgent("1.2.0");
    private String httpProxyUrl;
    private String scraperApiBaseUrl;
    private String universalApiBaseUrl;
    private String webScraperApiBaseUrl;
    private String locationsBaseUrl;

    public Builder(String scraperToken) {
      this.scraperToken = scraperToken; // Allow null here
    }

    public Builder publicToken(String t) { this.publicToken = t; return this; }
    public Builder publicKey(String k) { this.publicKey = k; return this; }
    public Builder timeout(Duration d) { this.timeout = d; return this; }
    public Builder userAgent(String ua) { this.userAgent = ua; return this; }
    public Builder httpProxy(String url) { this.httpProxyUrl = url; return this; }

    public ThordataConfig build() {
      return new ThordataConfig(this);
    }
  }
}