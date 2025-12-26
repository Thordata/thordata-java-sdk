# thordata-java-sdk

Official Java SDK for Thordata APIs.

## Installation

Add to `pom.xml`:

```xml
<dependency>
  <groupId>com.thordata</groupId>
  <artifactId>thordata-java-sdk</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Quick Start

```java
import com.thordata.sdk.ThordataClient;
import com.thordata.sdk.ThordataConfig;
import com.thordata.sdk.SerpOptions;

public class Main {
    public static void main(String[] args) throws Exception {
        ThordataConfig cfg = new ThordataConfig(
            System.getenv("THORDATA_SCRAPER_TOKEN"),
            System.getenv("THORDATA_PUBLIC_TOKEN"),
            System.getenv("THORDATA_PUBLIC_KEY")
        );
        ThordataClient client = new ThordataClient(cfg);

        SerpOptions opt = new SerpOptions();
        opt.query = "java sdk";
        opt.engine = "google";
        
        Object result = client.serpSearch(opt);
        System.out.println(result);
    }
}
```

## Features

### Web Scraper API

```java
// Create Video Task
VideoTaskOptions taskOpt = new VideoTaskOptions();
taskOpt.fileName = "video";
taskOpt.spiderId = "youtube_video_by-url";
taskOpt.spiderName = "youtube.com";
taskOpt.parameters.put("url", "...");
taskOpt.commonSettings = new CommonSettings();
taskOpt.commonSettings.resolution = "1080p";

String taskId = client.createVideoTask(taskOpt);

// Get Result
String url = client.getTaskResult(taskId, "json");
```

### Account Management

```java
// Usage
Object stats = client.getUsageStatistics("2024-01-01", "2024-01-31");

// Proxy Users
Object users = client.listProxyUsers(1); // 1=Residential

// Whitelist
client.addWhitelistIp("1.2.3.4", 1);
```

### Public API NEW

```java
Map<String, Object> balance = client.getResidentialBalance();
List<Object> ispRegions = client.getIspRegions();
```