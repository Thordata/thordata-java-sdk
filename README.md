# Thordata Java SDK

<div align="center">

**Official Java Client for Thordata APIs**

*Proxy Network • SERP API • Web Unlocker • Web Scraper API*

[![Maven Central](https://img.shields.io/maven-central/v/com.thordata/thordata-java-sdk.svg)](https://search.maven.org/artifact/com.thordata/thordata-java-sdk)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

</div>

---

## 📦 Installation

Add to `pom.xml`:

```xml
<dependency>
  <groupId>com.thordata</groupId>
  <artifactId>thordata-java-sdk</artifactId>
  <version>1.0.1</version>
</dependency>
```

## 🔐 Configuration

Set environment variables or pass `ThordataConfig` object.

```bash
export THORDATA_SCRAPER_TOKEN="your_token"
export THORDATA_PUBLIC_TOKEN="public_token"
export THORDATA_PUBLIC_KEY="public_key"
```

## 🚀 Quick Start

```java
import com.thordata.sdk.*;

public class Main {
    public static void main(String[] args) throws Exception {
        // Load from env
        ThordataConfig cfg = new ThordataConfig(
            System.getenv("THORDATA_SCRAPER_TOKEN"), 
            null, null
        );
        ThordataClient client = new ThordataClient(cfg);

        // SERP Search
        SerpOptions opt = new SerpOptions();
        opt.query = "java sdk";
        opt.engine = "google";
        
        Object result = client.serpSearch(opt);
        System.out.println(result);
    }
}
```

## 📚 Core Features

### 🌐 Proxy Network

Uses `OkHttp` for high-performance tunneling.

```java
// Create Proxy Config
ProxyConfig proxy = ProxyConfig.residentialFromEnv()
    .country("us")
    .city("new_york")
    .sticky(10); // 10 min session

// Make Request
ProxyResponse resp = client.proxyGet("https://httpbin.org/ip", proxy);
System.out.println(resp.bodyText());
```

### 🔍 SERP API

```java
SerpOptions opt = new SerpOptions();
opt.query = "AI trends";
opt.engine = "google_news";
opt.num = 20;
opt.country = "us";

Object result = client.serpSearch(opt);
```

### 🔓 Universal Scraping API

```java
UniversalOptions opt = new UniversalOptions();
opt.url = "https://example.com/spa";
opt.jsRender = true;
opt.waitFor = ".content";
opt.outputFormat = "html";

Object result = client.universalScrape(opt);
```

### 🕷️ Web Scraper API (Tasks)

```java
// 1. Create Task
ScraperTaskOptions taskOpt = new ScraperTaskOptions();
taskOpt.fileName = "my_task";
taskOpt.spiderId = "universal";
taskOpt.spiderName = "universal";
taskOpt.parameters.put("url", "https://example.com");

String taskId = client.createScraperTask(taskOpt);

// 2. Check Status
String status = client.getTaskStatus(taskId);

// 3. Get Result
if ("ready".equals(status)) {
    String url = client.getTaskResult(taskId, "json");
    System.out.println(url);
}
```

### 📹 Video/Audio Tasks

```java
VideoTaskOptions vidOpt = new VideoTaskOptions();
vidOpt.fileName = "video";
vidOpt.spiderId = "youtube_video_by-url";
vidOpt.spiderName = "youtube.com";
vidOpt.parameters.put("url", "https://...");
vidOpt.commonSettings = new CommonSettings();
vidOpt.commonSettings.resolution = "1080p";

String vidId = client.createVideoTask(vidOpt);
```

### 📊 Account Management

```java
// Usage Stats
Object stats = client.getUsageStatistics("2024-01-01", "2024-01-31");

// Whitelist IP
client.addWhitelistIp("1.2.3.4", 1);
```

## 📄 License

MIT License