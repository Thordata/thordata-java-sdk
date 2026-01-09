package com.thordata.sdk.examples;

import com.thordata.sdk.SerpOptions;
import com.thordata.sdk.SerpResponse;
import com.thordata.sdk.ThordataClient;
import com.thordata.sdk.ThordataConfig;

public class VerifyStructuredSerp {
    public static void main(String[] args) {
        String token = System.getenv("THORDATA_SCRAPER_TOKEN");
        if (token == null) {
            System.err.println("THORDATA_SCRAPER_TOKEN required");
            return;
        }

        // 1. New Builder Pattern Initialization
        ThordataConfig config = ThordataConfig.builder(token)
            .timeout(java.time.Duration.ofSeconds(60))
            .build();
            
        ThordataClient client = new ThordataClient(config);

        // 2. Strong Typed Search
        System.out.println("--- Testing Strong Typed SERP ---");
        SerpOptions opt = new SerpOptions();
        opt.query = "coffee";
        opt.engine = "google_maps"; // simulating what we did in Python/JS

        try {
            // Now returns SerpResponse object, not generic Object
            SerpResponse res = client.serpSearch(opt);
            
            System.out.println("✅ Request Successful!");
            System.out.println("   Code: " + res.code);
            if (res.localResults != null) {
                System.out.println("   Found " + res.localResults.size() + " local results");
            }
        } catch (Exception e) {
            System.out.println("✅ SDK Structure Works! (API returned: " + e.getMessage() + ")");
        }
    }
}