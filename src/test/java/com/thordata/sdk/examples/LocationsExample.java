package com.thordata.sdk.examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thordata.sdk.ThordataClient;
import com.thordata.sdk.ThordataConfig;

import java.time.Duration;

public final class LocationsExample {
  public static void main(String[] args) throws Exception {
    String token = Env.get("THORDATA_SCRAPER_TOKEN");
    String pub = Env.get("THORDATA_PUBLIC_TOKEN");
    String key = Env.get("THORDATA_PUBLIC_KEY");

    if (token == null || token.isBlank() || pub == null || pub.isBlank() || key == null || key.isBlank()) {
      System.out.println("Missing THORDATA_SCRAPER_TOKEN / THORDATA_PUBLIC_TOKEN / THORDATA_PUBLIC_KEY.");
      System.exit(1);
    }

    String proxy = Env.get("HTTPS_PROXY");
    if (proxy == null || proxy.isBlank()) proxy = Env.get("HTTP_PROXY");

    ThordataClient client = new ThordataClient(new ThordataConfig(
        token, pub, key,
        Duration.ofSeconds(60),
        null,
        null, null, null, null,
        proxy
    ));

    Object out = client.listCountries(1);
    System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(out));
  }
}