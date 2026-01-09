package com.thordata.sdk.examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thordata.sdk.SerpOptions;
import com.thordata.sdk.SerpResponse;
import com.thordata.sdk.ThordataClient;
import com.thordata.sdk.ThordataConfig;

import java.time.Duration;

public final class SerpExample {
  public static void main(String[] args) throws Exception {
    String token = Env.get("THORDATA_SCRAPER_TOKEN");
    if (token == null || token.isBlank()) {
      System.out.println("Missing THORDATA_SCRAPER_TOKEN. Set env var and re-run.");
      System.exit(1);
    }

    String proxy = Env.get("HTTPS_PROXY");
    if (proxy == null || proxy.isBlank()) proxy = Env.get("HTTP_PROXY");

    // Use Builder Pattern
    ThordataConfig config = ThordataConfig.builder(token)
        .publicToken(Env.get("THORDATA_PUBLIC_TOKEN"))
        .publicKey(Env.get("THORDATA_PUBLIC_KEY"))
        .timeout(Duration.ofSeconds(60))
        .httpProxy(proxy)
        .build();

    ThordataClient client = new ThordataClient(config);

    SerpOptions opt = new SerpOptions();
    opt.query = "pizza";
    opt.engine = "google";
    opt.country = "us";
    opt.outputFormat = "json";

    try {
        // Now returns SerpResponse
        SerpResponse out = client.serpSearch(opt);
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(out));
    } catch (Exception e) {
        System.err.println("API error: " + e.getMessage());
        e.printStackTrace();
    }
  }
}