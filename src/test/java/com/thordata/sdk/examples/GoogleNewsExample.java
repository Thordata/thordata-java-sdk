package com.thordata.sdk.examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thordata.sdk.SerpOptions;
import com.thordata.sdk.ThordataClient;
import com.thordata.sdk.ThordataConfig;

import java.time.Duration;

public final class GoogleNewsExample {
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
        null, null, null, null,
        proxy
    ));

    SerpOptions opt = new SerpOptions();
    opt.query = "AI regulation";
    opt.engine = "google_news";
    opt.country = "us";
    opt.outputFormat = "json";
    opt.extra.put("so", "1");

    Object out = client.serpSearch(opt);
    System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(out));
  }
}