package com.thordata.sdk.examples;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thordata.sdk.RunTaskConfig;
import com.thordata.sdk.ScraperTaskOptions;
import com.thordata.sdk.ThordataClient;
import com.thordata.sdk.ThordataConfig;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class VerifyRunTask {
    public static void main(String[] args) throws Exception {
        // 1. Load Env
        String token = Env.get("THORDATA_SCRAPER_TOKEN");
        String pub = Env.get("THORDATA_PUBLIC_TOKEN");
        String key = Env.get("THORDATA_PUBLIC_KEY");
        
        String spiderId = Env.get("THORDATA_TASK_SPIDER_ID");
        String spiderName = Env.get("THORDATA_TASK_SPIDER_NAME");
        String paramsJson = Env.get("THORDATA_TASK_PARAMETERS_JSON");

        if (token == null || spiderId == null) {
            System.err.println("❌ Missing required .env variables.");
            System.exit(1);
        }

        // 2. Parse Parameters
        ObjectMapper om = new ObjectMapper();
        Map<String, Object> params;
        
        // Handle Array vs Object input
        String jsonTrimmed = paramsJson.trim();
        if (jsonTrimmed.startsWith("[")) {
            List<Map<String, Object>> list = om.readValue(jsonTrimmed, new TypeReference<>() {});
            params = list.get(0);
        } else {
            params = om.readValue(jsonTrimmed, new TypeReference<>() {});
        }

        // 3. Init Client (Fix: Use Builder Pattern to ensure default URLs are loaded)
        ThordataConfig config = ThordataConfig.builder(token)
            .publicToken(pub)
            .publicKey(key)
            .timeout(Duration.ofSeconds(60))
            .build();

        ThordataClient client = new ThordataClient(config);

        System.out.println("--- Testing Java runTask [" + spiderName + "] ---");

        try {
            ScraperTaskOptions opts = new ScraperTaskOptions();
            opts.fileName = "java_test_" + System.currentTimeMillis();
            opts.spiderId = spiderId;
            opts.spiderName = spiderName;
            opts.parameters = params;
            opts.includeErrors = true;

            // Run with custom config
            RunTaskConfig runConfig = new RunTaskConfig(
                Duration.ofMinutes(10),
                Duration.ofSeconds(3),
                Duration.ofSeconds(10)
            );

            // 4. Run Task
            String url = client.runTask(opts, runConfig);
            System.out.println("✅ Success! Download URL: " + url);
        } catch (Exception e) {
            System.err.println("❌ Failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}