package com.thordata.sdk;

import java.time.Duration;

public final class ThordataConfig {
  public final String scraperToken;
  public final String publicToken;
  public final String publicKey;

  public final Duration timeout;
  public final String userAgent;

  public final String scraperApiBaseUrl;
  public final String universalApiBaseUrl;
  public final String webScraperApiBaseUrl;
  public final String locationsBaseUrl;

  public ThordataConfig(
      String scraperToken,
      String publicToken,
      String publicKey,
      Duration timeout,
      String userAgent,
      String scraperApiBaseUrl,
      String universalApiBaseUrl,
      String webScraperApiBaseUrl,
      String locationsBaseUrl
  ) {
    this.scraperToken = scraperToken;
    this.publicToken = publicToken;
    this.publicKey = publicKey;
    this.timeout = timeout == null ? Duration.ofSeconds(30) : timeout;
    this.userAgent = userAgent == null || userAgent.isBlank() ? Utils.buildUserAgent("0.0.0") : userAgent;

    this.scraperApiBaseUrl = Utils.getenvOrDefault("THORDATA_SCRAPERAPI_BASE_URL", scraperApiBaseUrl, "https://scraperapi.thordata.com");
    this.universalApiBaseUrl = Utils.getenvOrDefault("THORDATA_UNIVERSALAPI_BASE_URL", universalApiBaseUrl, "https://universalapi.thordata.com");
    this.webScraperApiBaseUrl = Utils.getenvOrDefault("THORDATA_WEB_SCRAPER_API_BASE_URL", webScraperApiBaseUrl, "https://api.thordata.com/api/web-scraper-api");
    this.locationsBaseUrl = Utils.getenvOrDefault("THORDATA_LOCATIONS_BASE_URL", locationsBaseUrl, "https://api.thordata.com/api/locations");
  }
}