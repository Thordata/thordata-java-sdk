package com.thordata.sdk.examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thordata.sdk.SerpOptions;
import com.thordata.sdk.ThordataClient;
import com.thordata.sdk.ThordataConfig;
import com.thordata.sdk.examples.Env;

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

    ThordataClient client = new ThordataClient(new ThordataConfig(
        token,
        Env.get("THORDATA_PUBLIC_TOKEN"),
        Env.get("THORDATA_PUBLIC_KEY"),
        Duration.ofSeconds(60),
        null,
        null, null, null, 
        proxy
    ));

    SerpOptions opt = new SerpOptions();
    opt.query = "pizza";
    opt.engine = "google";
    opt.country = "us";
    opt.outputFormat = "json";

    try {
    Object out = client.serpSearch(opt);
    System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(out));
  } catch (com.thordata.sdk.ThordataErrors.ThordataApiException e) {
    System.err.println("API error:");
    System.err.println("  message: " + e.getMessage());
    System.err.println("  httpStatus: " + e.httpStatus);
    System.err.println("  apiCode: " + e.apiCode);
    System.err.println("  payload: " + String.valueOf(e.payload));
    throw e;
  }
  }
}