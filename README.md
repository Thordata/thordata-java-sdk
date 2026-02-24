# Thordata Java SDK

<div align="center">

<img src="https://img.shields.io/badge/Thordata-AI%20Infrastructure-blue?style=for-the-badge" alt="Thordata Logo">

**The Official Java Client for Thordata APIs**

*Enterprise integration & task orchestration for Thordata infrastructure.*

[![Maven Central](https://img.shields.io/maven-central/v/com.thordata/thordata-java-sdk.svg?style=flat-square)](https://search.maven.org/artifact/com.thordata/thordata-java-sdk)
[![License](https://img.shields.io/badge/license-MIT-green?style=flat-square)](LICENSE)

</div>

---

## 📖 Introduction

The Thordata Java SDK is the **official client for enterprise backends, control panels, and task orchestration services** built on top of Thordata.
It focuses on:

- **Management APIs**: usage statistics, proxy users, whitelist IPs, proxy server lists, expiration time, and geo-locations.
- **Web Scraper Tasks lifecycle**: create tasks, poll status, download results, and a high-level `runTask` helper.
- **Core scraping APIs (P1)**: SERP and Universal/Web Unlocker for simple data collection flows.

Behavior (endpoints, parameters, error model) follows the Python SDK and the shared `thordata-sdk-spec` repository.

---

## 📦 Installation

Add this dependency to your `pom.xml`:

```xml
<dependency>
  <groupId>com.thordata</groupId>
  <artifactId>thordata-java-sdk</artifactId>
  <version>1.2.1</version>
</dependency>
```

Requires **Java 11+** (tested on Java 17 in CI).

---

## 🔐 Configuration

1. Copy the example env file and fill in your real credentials:

```bash
cp .env.example .env
```

2. At minimum, set:

- `THORDATA_SCRAPER_TOKEN` – Scraper APIs (SERP, Universal, Tasks builder)
- `THORDATA_PUBLIC_TOKEN` / `THORDATA_PUBLIC_KEY` – Management & locations APIs

3. Optionally configure proxy endpoints and upstream proxy (see `.env.example` for a full reference).

The Java examples use a small helper `Env` class to load `.env` and fall back to real environment variables. In your own project you can either:

- read env vars directly (e.g. `System.getenv("THORDATA_SCRAPER_TOKEN")`), or
- port a similar `.env` loader if you prefer local files during development.

---

## 🚀 Quick Start

### 1. Initialize client from env

```java
import com.thordata.sdk.ThordataClient;
import com.thordata.sdk.ThordataConfig;

ThordataConfig config = ThordataConfig.builder(System.getenv("THORDATA_SCRAPER_TOKEN"))
    .publicToken(System.getenv("THORDATA_PUBLIC_TOKEN"))
    .publicKey(System.getenv("THORDATA_PUBLIC_KEY"))
    .build();

ThordataClient client = new ThordataClient(config);
```

### 2. SERP search (Google)

```java
import com.thordata.sdk.SerpOptions;
import com.thordata.sdk.SerpResponse;

SerpOptions opt = new SerpOptions();
opt.query = "pizza";
opt.engine = "google";
opt.country = "us";
opt.num = 10;

SerpResponse result = client.serpSearch(opt);
System.out.println("Organic results: " + result.organicResults.size());
```

### 3. Universal scrape (Web Unlocker)

```java
import com.thordata.sdk.UniversalOptions;

UniversalOptions opt = new UniversalOptions();
opt.url = "https://httpbin.org/html";
opt.jsRender = false;
opt.outputFormat = "html";

Object out = client.universalScrape(opt);
System.out.println(String.valueOf(out));
```

### 4. Web Scraper task lifecycle helper

```java
import com.thordata.sdk.RunTaskConfig;
import com.thordata.sdk.ScraperTaskOptions;

ScraperTaskOptions task = new ScraperTaskOptions();
task.fileName = "java_example";
task.spiderId = "youtube_video-post_by-url";
task.spiderName = "youtube.com";
task.parameters.put("url", "https://www.youtube.com/@stephcurry/videos");

RunTaskConfig runCfg = new RunTaskConfig(); // defaults: maxWait=10m, polling backoff
String downloadUrl = client.runTask(task, runCfg);
System.out.println("Download URL: " + downloadUrl);
```

See `src/test/java/com/thordata/sdk/examples/` for more complete, runnable examples:

- `SerpExample` – SERP quick start
- `UniversalExample` – Universal/Web Unlocker
- `LocationsExample` – locations & geo metadata
- `VerifyRunTask` – end-to-end task creation + wait + result
- `VerifyExample` – management APIs (whitelist IPs, video tasks)

---

## 🧪 Testing

- **Unit tests (offline)**: run `mvn test` – uses local HTTP servers, no live traffic.
- **Integration checks (live)**: from the repo root, with `.env` filled in:
  - `mvn -Dexec.mainClass="com.thordata.sdk.examples.SerpExample" -Dexec.classpathScope=test exec:java`
  - `mvn -Dexec.mainClass="com.thordata.sdk.examples.UniversalExample" -Dexec.classpathScope=test exec:java`
  - `mvn -Dexec.mainClass="com.thordata.sdk.examples.LocationsExample" -Dexec.classpathScope=test exec:java`
  - `mvn -Dexec.mainClass="com.thordata.sdk.examples.VerifyRunTask" -Dexec.classpathScope=test exec:java`

These examples are designed to be **short, self-contained acceptance tests** for the Java SDK.

---

## 📄 License

MIT License.