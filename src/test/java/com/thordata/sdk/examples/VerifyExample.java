package com.thordata.sdk.examples;

import com.thordata.sdk.CommonSettings;
import com.thordata.sdk.ThordataClient;
import com.thordata.sdk.ThordataConfig;
import com.thordata.sdk.VideoTaskOptions;

import java.time.Duration;
import java.util.List;

public class VerifyExample {
  public static void main(String[] args) throws Exception {
    String token = Env.get("THORDATA_SCRAPER_TOKEN");
    String pub = Env.get("THORDATA_PUBLIC_TOKEN");
    String key = Env.get("THORDATA_PUBLIC_KEY");
    
    // Allow running without credentials if we just want to verify compilation/structure
    if (pub == null || key == null) {
      System.out.println("⚠️ Public API credentials missing. Skipping live verification.");
      System.out.println("✅ Java SDK Management Interface compilation verified.");
      return;
    }

    ThordataConfig cfg = new ThordataConfig(
        token != null ? token : "dummy", pub, key,
        Duration.ofSeconds(60), null,
        null, null, null, null, null
    );
    ThordataClient client = new ThordataClient(cfg);

    System.out.println("=== Verifying Management Features ===");

    // 1. Whitelist List (Safe read operation)
    System.out.println("\n--- Whitelist IPs ---");
    try {
      List<String> ips = client.listWhitelistIps(1);
      System.out.println("✅ IP List size: " + ips.size());
    } catch (Exception e) {
      System.out.println("❌ Failed: " + e.getMessage());
    }

    // 2. Video Task (if token present)
    if (token != null) {
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
}