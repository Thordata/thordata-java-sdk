package com.thordata.sdk.examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thordata.sdk.ScraperTaskOptions;
import com.thordata.sdk.ThordataClient;
import com.thordata.sdk.ThordataConfig;


import java.time.Duration;
import java.util.Map;

public final class TasksExample {
  public static void main(String[] args) throws Exception {
    String token = Env.get("THORDATA_SCRAPER_TOKEN");
    String pub = Env.get("THORDATA_PUBLIC_TOKEN");
    String key = Env.get("THORDATA_PUBLIC_KEY");

    String spiderId = Env.get("THORDATA_SPIDER_ID");
    String spiderName = Env.get("THORDATA_SPIDER_NAME");
    String paramsJson = Env.get("THORDATA_TASK_PARAMETERS_JSON");
    String fileName = Env.get("THORDATA_TASK_FILE_NAME");

    if (token == null || token.isBlank() || pub == null || pub.isBlank() || key == null || key.isBlank()) {
      System.out.println("Missing THORDATA_SCRAPER_TOKEN / THORDATA_PUBLIC_TOKEN / THORDATA_PUBLIC_KEY.");
      System.exit(1);
    }

    if (spiderId == null || spiderId.isBlank() || spiderName == null || spiderName.isBlank() || paramsJson == null || paramsJson.isBlank()) {
      System.out.println("Skipping tasks example.");
      System.out.println("Set THORDATA_SPIDER_ID, THORDATA_SPIDER_NAME, THORDATA_TASK_PARAMETERS_JSON to run.");
      return;
    }

    ObjectMapper om = new ObjectMapper();
    @SuppressWarnings("unchecked")
    Map<String, Object> params = om.readValue(paramsJson, Map.class);

    String proxy = Env.get("HTTPS_PROXY");
    if (proxy == null || proxy.isBlank()) proxy = Env.get("HTTP_PROXY");

    ThordataClient client = new ThordataClient(new ThordataConfig(
        token, pub, key,
        Duration.ofSeconds(60),
        null,
        null, null, null, null,
        proxy
    ));

    ScraperTaskOptions opt = new ScraperTaskOptions();
    opt.fileName = fileName;
    opt.spiderId = spiderId;
    opt.spiderName = spiderName;
    opt.parameters = params;
    opt.includeErrors = true;

    String taskId = client.createScraperTask(opt);
    System.out.println("Task created: " + taskId);

    String status = client.getTaskStatus(taskId);
    System.out.println("Status: " + status);

    if ("ready".equalsIgnoreCase(status) || "success".equalsIgnoreCase(status) || "finished".equalsIgnoreCase(status)) {
      String dl = client.getTaskResult(taskId, "json");
      System.out.println("Download: " + dl);
    }
  }
}