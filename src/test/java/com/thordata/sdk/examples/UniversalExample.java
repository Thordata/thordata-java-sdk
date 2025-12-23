package com.thordata.sdk.examples;

import com.thordata.sdk.ThordataClient;
import com.thordata.sdk.ThordataConfig;
import com.thordata.sdk.UniversalOptions;
import com.thordata.sdk.examples.Env;

import java.time.Duration;

public final class UniversalExample {
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

    UniversalOptions opt = new UniversalOptions();
    opt.url = "https://httpbin.org/html";
    opt.jsRender = false;
    opt.outputFormat = "html";

    Object out = client.universalScrape(opt);
    String s = String.valueOf(out);
    System.out.println("Preview: " + (s.length() > 300 ? s.substring(0, 300) + "..." : s));
  }
}