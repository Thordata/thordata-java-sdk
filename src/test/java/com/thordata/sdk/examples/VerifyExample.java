package com.thordata.sdk.examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thordata.sdk.CommonSettings;
import com.thordata.sdk.ThordataClient;
import com.thordata.sdk.ThordataConfig;
import com.thordata.sdk.VideoTaskOptions;

import java.time.Duration;

public class VerifyExample {
  public static void main(String[] args) throws Exception {
    String token = Env.get("THORDATA_SCRAPER_TOKEN");
    String pub = Env.get("THORDATA_PUBLIC_TOKEN");
    String key = Env.get("THORDATA_PUBLIC_KEY");
    
    if (token == null) {
      System.err.println("THORDATA_SCRAPER_TOKEN required");
      System.exit(1);
    }

    ThordataConfig cfg = new ThordataConfig(
        token, pub, key,
        Duration.ofSeconds(60), null,
        null, null, null, null, null
    );
    ThordataClient client = new ThordataClient(cfg);

    System.out.println("=== Verifying Features ===");

    // 1. Video Task
    System.out.println("\n--- Video Task ---");
    try {
      VideoTaskOptions opt = new VideoTaskOptions();
      opt.fileName = "test_video";
      opt.spiderId = "youtube_video_by-url";
      opt.spiderName = "youtube.com";
      opt.parameters.put("url", "https://www.youtube.com/watch?v=dQw4w9WgXcQ");
      opt.commonSettings = new CommonSettings();
      opt.commonSettings.resolution = "720p";
      opt.commonSettings.isSubtitles = "false";
      
      String taskId = client.createVideoTask(opt);
      System.out.println("✅ Created: " + taskId);
    } catch (Exception e) {
      System.out.println("❌ Failed: " + e.getMessage());
    }

  }
}