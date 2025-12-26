package com.thordata.sdk;

import java.time.Duration;

public final class ThordataConfig {
  public final String scraperToken;
  public final String publicToken;
  public final String publicKey;
  
  public final String sign;
  public final String apiKey;

  public final Duration timeout;
  public final String userAgent;

  public final String httpProxyUrl;
  public final String scraperApiBaseUrl;
  public final String universalApiBaseUrl;
  public final String webScraperApiBaseUrl;
  public final String locationsBaseUrl;
  public final String gatewayBaseUrl;

  public ThordataConfig(
      String scraperToken,
      String publicToken,
      String publicKey,
      String sign,
      String apiKey,
      Duration timeout,
      String userAgent,
      String scraperApiBaseUrl,
      String universalApiBaseUrl,
      String webScraperApiBaseUrl,
      String locationsBaseUrl,
      String httpProxyUrl
  ) {
    this.scraperToken = scraperToken;
    this.publicToken = publicToken;
    this.publicKey = publicKey;

    String envSign = System.getenv("THORDATA_SIGN");
    this.sign = (sign != null && !sign.isBlank()) ? sign : (envSign != null && !envSign.isBlank() ? envSign : publicToken);
    
    String envKey = System.getenv("THORDATA_API_KEY");
    this.apiKey = (apiKey != null && !apiKey.isBlank()) ? apiKey : (envKey != null && !envKey.isBlank() ? envKey : publicKey);

    this.timeout = timeout == null ? Duration.ofSeconds(30) : timeout;
    this.userAgent = userAgent == null || userAgent.isBlank() ? Utils.buildUserAgent("0.1.0") : userAgent;

    this.scraperApiBaseUrl = Utils.getenvOrDefault("THORDATA_SCRAPERAPI_BASE_URL", scraperApiBaseUrl, "https://scraperapi.thordata.com");
    this.universalApiBaseUrl = Utils.getenvOrDefault("THORDATA_UNIVERSALAPI_BASE_URL", universalApiBaseUrl, "https://universalapi.thordata.com");

    this.webScraperApiBaseUrl = Utils.getenvOrDefault("THORDATA_WEB_SCRAPER_API_BASE_URL", webScraperApiBaseUrl, "https://openapi.thordata.com/api/web-scraper-api");
    this.locationsBaseUrl = Utils.getenvOrDefault("THORDATA_LOCATIONS_BASE_URL", locationsBaseUrl, "https://openapi.thordata.com/api/locations");

    // API NEW
    this.gatewayBaseUrl = Utils.getenvOrDefault("THORDATA_GATEWAY_BASE_URL", null, "https://api.thordata.com/api/gateway");
    this.httpProxyUrl = httpProxyUrl;
  }

  public ThordataConfig(
      String scraperToken,
      String publicToken,
      String publicKey,
      Duration timeout,
      String userAgent,
      String scraperApiBaseUrl,
      String universalApiBaseUrl,
      String webScraperApiBaseUrl,
      String locationsBaseUrl,
      String httpProxyUrl
  ) {
      this(scraperToken, publicToken, publicKey, null, null, timeout, userAgent,
          scraperApiBaseUrl, universalApiBaseUrl, webScraperApiBaseUrl, locationsBaseUrl, httpProxyUrl);
  }

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
      this(scraperToken, publicToken, publicKey, null, null, timeout, userAgent,
          scraperApiBaseUrl, universalApiBaseUrl, webScraperApiBaseUrl, locationsBaseUrl, null);
  }
}