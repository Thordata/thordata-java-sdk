# Thordata Java SDK

<div align="center">

<img src="https://img.shields.io/badge/Thordata-AI%20Infrastructure-blue?style=for-the-badge" alt="Thordata Logo">

**The Official Java Client for Thordata APIs**

*Native implementation for maximum compatibility and performance.*

[![Maven Central](https://img.shields.io/maven-central/v/com.thordata/thordata-java-sdk.svg?style=flat-square)](https://search.maven.org/artifact/com.thordata/thordata-java-sdk)
[![License](https://img.shields.io/badge/license-MIT-green?style=flat-square)](LICENSE)

</div>

---

## 📖 Introduction

The Thordata Java SDK provides a robust integration with Thordata's infrastructure. It features a **custom socket-level implementation** for proxy tunneling, ensuring 100% compatibility with Thordata's secure gateway authentication (TLS-in-TLS) where standard libraries often fail.

**Key Features:**
*   **🛡️ Rock-Solid Proxying:** Custom socket implementation supports Preemptive Authentication and SSL Tunneling perfectly.
*   **⚡ Connection Pooling:** Internal `HttpClient` cache for high-throughput scenarios.
*   **☕ Pure Java:** Minimal dependencies, leveraging `java.net.http` (Java 11+).
*   **🧩 Lazy Validation:** Flexible initialization for different use cases.

---

## 📦 Installation

Add this to your `pom.xml`:

```xml
<dependency>
  <groupId>com.thordata</groupId>
  <artifactId>thordata-java-sdk</artifactId>
  <version>1.1.0</version>
</dependency>
```

---

## 🚀 Quick Start

### 1. Initialization

```java
import com.thordata.sdk.*;

// Auto-loads tokens from environment variables
ThordataConfig cfg = new ThordataConfig(
    System.getenv("THORDATA_SCRAPER_TOKEN"), 
    null, null
);
ThordataClient client = new ThordataClient(cfg);
```

### 2. Proxy Network (The Robust Way)

```java
// Create Proxy Config (Residential, US, Sticky)
ProxyConfig proxy = ProxyConfig.residentialFromEnv()
    .country("us")
    .city("new_york")
    .sticky(10); // 10 min session

// This uses the custom socket implementation for max compatibility
ProxyResponse resp = client.proxyGet("https://httpbin.org/ip", proxy);

System.out.println("Status: " + resp.statusCode);
System.out.println("Body: " + resp.bodyText());
```

### 3. SERP Search

```java
SerpOptions opt = new SerpOptions();
opt.query = "Java threading";
opt.engine = "google";
opt.num = 10;

// Returns strongly-typed response object
SerpResponse result = client.serpSearch(opt);

System.out.println("Result count: " + result.organicResults.size());
```

---

## ⚙️ Advanced Usage

### Universal Scrape (Web Unlocker)

```java
UniversalOptions opt = new UniversalOptions();
opt.url = "https://example.com/protected";
opt.jsRender = true;
opt.waitFor = ".content-loaded";

Object result = client.universalScrape(opt);
```

### Web Scraper Tasks

```java
// 1. Create Task
ScraperTaskOptions taskOpt = new ScraperTaskOptions();
taskOpt.fileName = "job_01";
taskOpt.spiderId = "universal";
taskOpt.spiderName = "universal";
taskOpt.parameters.put("url", "https://example.com");

String taskId = client.createScraperTask(taskOpt);

// 2. Poll Status & Get Result
while (true) {
    String status = client.getTaskStatus(taskId);
    if ("ready".equalsIgnoreCase(status)) {
        String url = client.getTaskResult(taskId, "json");
        System.out.println("Data URL: " + url);
        break;
    }
    Thread.sleep(5000);
}
```

---

## 📄 License

MIT License.