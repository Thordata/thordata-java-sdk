package com.thordata.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ThordataClient {
  private final ThordataConfig cfg;
  private final HttpClient apiClient; 
  private final ObjectMapper om = new ObjectMapper();

  // API Endpoints
  private final String serpUrl;
  private final String universalUrl;
  private final String builderUrl;
  private final String statusUrl;
  private final String downloadUrl;
  private final String locationsBaseUrl;
  private final String videoBuilderUrl;
  private final String usageStatsUrl;
  private final String proxyUsersUrl;
  private final String whitelistUrl;
  private final String proxyListUrl;
  private final String proxyExpirationUrl;
  private final String taskListUrl;

  private static final Map<String, String> TBM_MAP = Map.of(
      "images", "isch",
      "shopping", "shop",
      "news", "nws",
      "videos", "vid",
      "isch", "isch",
      "shop", "shop",
      "nws", "nws",
      "vid", "vid"
  );

  private static String normalizeEngine(String engine) {
    if (engine == null) return "google";
    String e = engine.trim().toLowerCase();
    return switch (e) {
      case "google_search" -> "google";
      case "bing_search" -> "bing";
      case "yandex_search" -> "yandex";
      case "duckduckgo_search" -> "duckduckgo";
      default -> e;
    };
  }

  public ThordataClient(ThordataConfig cfg) {
    if (cfg == null) {
      throw new IllegalArgumentException("Config cannot be null");
    }
    this.cfg = cfg;
    
    // Initialize API Client
    HttpClient.Builder b = HttpClient.newBuilder()
        .connectTimeout(cfg.timeout == null ? Duration.ofSeconds(30) : cfg.timeout)
        .followRedirects(HttpClient.Redirect.NORMAL);

    if (cfg.httpProxyUrl != null && !cfg.httpProxyUrl.isBlank()) {
        InetSocketAddress proxy = Utils.parseHttpProxy(cfg.httpProxyUrl);
        if (proxy != null) {
            b.proxy(ProxySelector.of(proxy));
        }
    }
    this.apiClient = b.build();

    // Setup URLs
    String s = normalizeUrl(cfg.scraperApiBaseUrl);
    String u = normalizeUrl(cfg.universalApiBaseUrl);
    String w = normalizeUrl(cfg.webScraperApiBaseUrl);
    String l = normalizeUrl(cfg.locationsBaseUrl);

    this.serpUrl = s + "/request";
    this.builderUrl = s + "/builder";
    this.universalUrl = u + "/request";
    this.statusUrl = w + "/tasks-status";
    this.downloadUrl = w + "/tasks-download";
    this.locationsBaseUrl = l;
    this.videoBuilderUrl = s + "/video_builder";

    String apiBase = l.replace("/locations", "");
    this.usageStatsUrl = apiBase + "/account/usage-statistics";
    this.proxyUsersUrl = apiBase + "/proxy-users";
    this.whitelistUrl = "https://api.thordata.com/api/whitelisted-ips";
    this.proxyListUrl = "https://openapi.thordata.com/api/proxy/proxy-list";
    this.proxyExpirationUrl = apiBase + "/proxy/expiration-time";
    this.taskListUrl = w + "/tasks-list";
  }

  private String normalizeUrl(String url) {
      if (url == null) return "";
      return url.replaceAll("/+$", "");
  }

  // ==========================================================
  // Management APIs
  // ==========================================================

  public void updateProxyUser(String username, Integer trafficLimit, Boolean status, int proxyType) throws Exception {
    requirePublicCreds();
    Map<String, String> payload = new HashMap<>();
    payload.put("username", username);
    payload.put("proxy_type", String.valueOf(proxyType));
    if (trafficLimit != null) payload.put("traffic_limit", String.valueOf(trafficLimit));
    if (status != null) payload.put("status", status ? "true" : "false");

    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(proxyUsersUrl + "/update-user"))
        .timeout(cfg.timeout)
        .header("token", cfg.publicToken)
        .header("key", cfg.publicKey)
        .header("Content-Type", "application/x-www-form-urlencoded")
        .header("User-Agent", cfg.userAgent)
        .POST(HttpRequest.BodyPublishers.ofString(Utils.formEncode(payload)))
        .build();

    HttpResponse<String> res = apiClient.send(req, HttpResponse.BodyHandlers.ofString());
    checkApiResponse("Update proxy user failed", res);
  }

  public void deleteProxyUser(String username, int proxyType) throws Exception {
    requirePublicCreds();
    Map<String, String> payload = new HashMap<>();
    payload.put("username", username);
    payload.put("proxy_type", String.valueOf(proxyType));

    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(proxyUsersUrl + "/delete-user"))
        .timeout(cfg.timeout)
        .header("token", cfg.publicToken)
        .header("key", cfg.publicKey)
        .header("Content-Type", "application/x-www-form-urlencoded")
        .header("User-Agent", cfg.userAgent)
        .POST(HttpRequest.BodyPublishers.ofString(Utils.formEncode(payload)))
        .build();

    HttpResponse<String> res = apiClient.send(req, HttpResponse.BodyHandlers.ofString());
    checkApiResponse("Delete proxy user failed", res);
  }

  public void deleteWhitelistIp(String ip, int proxyType) throws Exception {
    requirePublicCreds();
    Map<String, String> payload = new HashMap<>();
    payload.put("ip", ip);
    payload.put("proxy_type", String.valueOf(proxyType));

    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(whitelistUrl + "/delete-ip"))
        .timeout(cfg.timeout)
        .header("token", cfg.publicToken)
        .header("key", cfg.publicKey)
        .header("Content-Type", "application/x-www-form-urlencoded")
        .header("User-Agent", cfg.userAgent)
        .POST(HttpRequest.BodyPublishers.ofString(Utils.formEncode(payload)))
        .build();

    HttpResponse<String> res = apiClient.send(req, HttpResponse.BodyHandlers.ofString());
    checkApiResponse("Delete whitelist IP failed", res);
  }

  @SuppressWarnings("unchecked")
  public List<String> listWhitelistIps(int proxyType) throws Exception {
    requirePublicCreds();
    String qs = "token=" + URLEncoder.encode(cfg.publicToken, StandardCharsets.UTF_8) + 
                "&key=" + URLEncoder.encode(cfg.publicKey, StandardCharsets.UTF_8) + 
                "&proxy_type=" + proxyType;
                
    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(whitelistUrl + "/ip-list?" + qs))
        .timeout(cfg.timeout)
        .header("User-Agent", cfg.userAgent)
        .GET()
        .build();

    HttpResponse<String> res = apiClient.send(req, HttpResponse.BodyHandlers.ofString());
    Object parsed = safeParseJson(res.body());
    
    if (parsed instanceof Map<?, ?> m) {
        Integer apiCode = m.containsKey("code") ? toInt(m.get("code")) : null;
        if (apiCode != null && apiCode != 200) {
            throw raiseForCode("List whitelist IPs failed", m, res.statusCode());
        }
        Object data = m.get("data");
        if (data instanceof List<?>) {
            return (List<String>) data;
        }
    }
    return List.of();
  }

  private void checkApiResponse(String errorMsg, HttpResponse<String> res) {
      Object parsed = safeParseJson(res.body());
      if (parsed instanceof Map<?, ?> m) {
          Integer apiCode = m.containsKey("code") ? toInt(m.get("code")) : null;
          if (apiCode != null && apiCode != 200) {
              throw raiseForCode(errorMsg, m, res.statusCode());
          }
      } else if (res.statusCode() >= 400) {
          throw new ThordataErrors.ThordataApiException(errorMsg, 0, res.statusCode(), res.body());
      }
  }

  // ==========================================================
  // Public API Methods (Existing)
  // ==========================================================

  public Object getUsageStatistics(String fromDate, String toDate) throws Exception {
    requirePublicCreds();
    String qs = "token=" + cfg.publicToken + "&key=" + cfg.publicKey + 
                "&from_date=" + fromDate + "&to_date=" + toDate;
                
    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(usageStatsUrl + "?" + qs))
        .timeout(cfg.timeout)
        .header("User-Agent", cfg.userAgent)
        .GET()
        .build();
        
    HttpResponse<String> res = apiClient.send(req, HttpResponse.BodyHandlers.ofString());
    Object parsed = safeParseJson(res.body());
    if (parsed instanceof Map<?, ?> m) {
        Integer apiCode = m.containsKey("code") ? toInt(m.get("code")) : null;
        if (apiCode != null && apiCode != 200) {
            throw raiseForCode("Usage statistics failed", m, res.statusCode());
        }
        if (m.containsKey("data")) {
            return m.get("data");
        }
    }
    return parsed;
  }

  public Object listProxyUsers(int proxyType) throws Exception {
    requirePublicCreds();
    String qs = "token=" + cfg.publicToken + "&key=" + cfg.publicKey + "&proxy_type=" + proxyType;
    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(proxyUsersUrl + "/user-list?" + qs))
        .timeout(cfg.timeout)
        .header("User-Agent", cfg.userAgent)
        .GET()
        .build();

    HttpResponse<String> res = apiClient.send(req, HttpResponse.BodyHandlers.ofString());
    Object parsed = safeParseJson(res.body());
    if (parsed instanceof Map<?, ?> m) {
        Integer apiCode = m.containsKey("code") ? toInt(m.get("code")) : null;
        if (apiCode != null && apiCode != 200) {
            throw raiseForCode("List proxy users failed", m, res.statusCode());
        }
        if (m.containsKey("data")) return m.get("data");
    }
    return parsed;
  }

  public Object createProxyUser(String username, String password, int trafficLimit, boolean status, int proxyType) throws Exception {
    requirePublicCreds();
    Map<String, String> payload = new HashMap<>();
    payload.put("username", username);
    payload.put("password", password);
    payload.put("traffic_limit", String.valueOf(trafficLimit));
    payload.put("status", status ? "true" : "false");
    payload.put("proxy_type", String.valueOf(proxyType));

    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(proxyUsersUrl + "/create-user"))
        .timeout(cfg.timeout)
        .header("token", cfg.publicToken)
        .header("key", cfg.publicKey)
        .header("Content-Type", "application/x-www-form-urlencoded")
        .header("User-Agent", cfg.userAgent)
        .POST(HttpRequest.BodyPublishers.ofString(Utils.formEncode(payload)))
        .build();

    HttpResponse<String> res = apiClient.send(req, HttpResponse.BodyHandlers.ofString());
    Object parsed = safeParseJson(res.body());
    if (parsed instanceof Map<?, ?> m) {
        Integer apiCode = m.containsKey("code") ? toInt(m.get("code")) : null;
        if (apiCode != null && apiCode != 200) {
            throw raiseForCode("Create proxy user failed", m, res.statusCode());
        }
        if (m.containsKey("data")) return m.get("data");
    }
    return parsed;
  }

  public Object addWhitelistIp(String ip, int proxyType, boolean status) throws Exception {
    requirePublicCreds();
    Map<String, String> payload = new HashMap<>();
    payload.put("ip", ip);
    payload.put("proxy_type", String.valueOf(proxyType));
    payload.put("status", status ? "true" : "false");

    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(whitelistUrl + "/add-ip"))
        .timeout(cfg.timeout)
        .header("token", cfg.publicToken)
        .header("key", cfg.publicKey)
        .header("Content-Type", "application/x-www-form-urlencoded")
        .header("User-Agent", cfg.userAgent)
        .POST(HttpRequest.BodyPublishers.ofString(Utils.formEncode(payload)))
        .build();

    HttpResponse<String> res = apiClient.send(req, HttpResponse.BodyHandlers.ofString());
    Object parsed = safeParseJson(res.body());
    if (parsed instanceof Map<?, ?> m) {
        Integer apiCode = m.containsKey("code") ? toInt(m.get("code")) : null;
        if (apiCode != null && apiCode != 200) {
            throw raiseForCode("Add whitelist IP failed", m, res.statusCode());
        }
        return m.get("data");
    }
    return parsed;
  }

  public Object listProxyServers(int proxyType) throws Exception {
    requirePublicCreds();
    String qs = "token=" + cfg.publicToken + "&key=" + cfg.publicKey + "&proxy_type=" + proxyType;
    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(proxyListUrl + "?" + qs))
        .timeout(cfg.timeout)
        .header("User-Agent", cfg.userAgent)
        .GET()
        .build();

    HttpResponse<String> res = apiClient.send(req, HttpResponse.BodyHandlers.ofString());
    Object parsed = safeParseJson(res.body());
    if (parsed instanceof Map<?, ?> m) {
        Integer apiCode = m.containsKey("code") ? toInt(m.get("code")) : null;
        if (apiCode != null && apiCode != 200) {
            throw raiseForCode("List proxy servers failed", m, res.statusCode());
        }
        if (m.containsKey("data")) return m.get("data");
        if (m.containsKey("list")) return m.get("list");
    }
    return parsed;
  }

  public Object getProxyExpiration(String ips, int proxyType) throws Exception {
    requirePublicCreds();
    String qs = "token=" + cfg.publicToken + "&key=" + cfg.publicKey + 
                "&proxy_type=" + proxyType + "&ips=" + ips;
    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(proxyExpirationUrl + "?" + qs))
        .timeout(cfg.timeout)
        .header("User-Agent", cfg.userAgent)
        .GET()
        .build();

    HttpResponse<String> res = apiClient.send(req, HttpResponse.BodyHandlers.ofString());
    Object parsed = safeParseJson(res.body());
    if (parsed instanceof Map<?, ?> m) {
        Integer apiCode = m.containsKey("code") ? toInt(m.get("code")) : null;
        if (apiCode != null && apiCode != 200) {
            throw raiseForCode("Get proxy expiration failed", m, res.statusCode());
        }
        if (m.containsKey("data")) return m.get("data");
    }
    return parsed;
  }

  // ==========================================================
  // SERP API
  // ==========================================================

  public SerpResponse serpSearch(SerpOptions opt) throws Exception {
    if (cfg.scraperToken == null || cfg.scraperToken.isBlank()) {
        throw new IllegalArgumentException("scraperToken is required for SERP API");
    }    
    if (opt == null || opt.query == null || opt.query.isBlank()) {
      throw new IllegalArgumentException("query is required");
    }

    String engine = (opt.engine == null || opt.engine.isBlank()) ? "google" : normalizeEngine(opt.engine);
    
    Map<String, String> payload = new HashMap<>();
    payload.put("engine", engine);
    payload.put("json", "1");

    if (engine.equals("yandex")) payload.put("text", opt.query);
    else payload.put("q", opt.query);

    if (opt.num != null) payload.put("num", String.valueOf(opt.num));
    if (opt.start != null) payload.put("start", String.valueOf(opt.start));
    if (opt.country != null) payload.put("gl", opt.country.toLowerCase());
    if (opt.language != null) payload.put("hl", opt.language.toLowerCase());

    if (opt.searchType != null && !opt.searchType.isBlank()) {
      String st = opt.searchType.toLowerCase();
      payload.put("tbm", TBM_MAP.getOrDefault(st, st));
    }

    if (opt.device != null) payload.put("device", opt.device.toLowerCase());
    if (opt.renderJs != null) payload.put("render_js", opt.renderJs ? "True" : "False");
    if (opt.noCache != null) payload.put("no_cache", opt.noCache ? "True" : "False");

    if (opt.extra != null) payload.putAll(opt.extra);

    String body = Utils.formEncode(payload);

    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(serpUrl))
        .timeout(cfg.timeout)
        .header("token", cfg.scraperToken)
        .header("Authorization", "Bearer " + cfg.scraperToken)
        .header("Content-Type", "application/x-www-form-urlencoded")
        .header("User-Agent", cfg.userAgent)
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();

    HttpResponse<String> res = apiClient.send(req, HttpResponse.BodyHandlers.ofString());

    try {
        SerpResponse response = om.readValue(res.body(), SerpResponse.class);
        
        if (response.code != 0 && response.code != 200) {
            throw new ThordataErrors.ThordataApiException(
                "SERP API Error: " + (response.status != null ? response.status : "Unknown"), 
                response.code, res.statusCode(), response
            );
        }
        return response;
    } catch (Exception e) {
        if (res.statusCode() >= 400) {
             throw new ThordataErrors.ThordataApiException("Request failed: " + res.body(), 0, res.statusCode(), res.body());
        }
        throw e;
    }
  }

  // ==========================================================
  // Universal API
  // ==========================================================

  public Object universalScrape(UniversalOptions opt) throws Exception {
    if (cfg.scraperToken == null || cfg.scraperToken.isBlank()) {
        throw new IllegalArgumentException("scraperToken is required for Universal API");
    }
    if (opt == null || opt.url == null || opt.url.isBlank()) {
      throw new IllegalArgumentException("url is required");
    }
    String format = (opt.outputFormat == null || opt.outputFormat.isBlank()) ? "html" : opt.outputFormat.toLowerCase();
    if (!format.equals("html") && !format.equals("png")) {
      throw new IllegalArgumentException("invalid outputFormat; supported: \"html\", \"png\"");
    }

    Map<String, String> payload = new HashMap<>();
    payload.put("url", opt.url);
    payload.put("js_render", opt.jsRender ? "True" : "False");
    payload.put("type", format);

    if (opt.country != null) payload.put("country", opt.country.toLowerCase());
    if (opt.blockResources != null) payload.put("block_resources", opt.blockResources);
    if (opt.cleanContent != null) payload.put("clean_content", opt.cleanContent);
    if (opt.wait != null) payload.put("wait", String.valueOf(opt.wait));
    if (opt.waitFor != null) payload.put("wait_for", opt.waitFor);

    if (opt.headers != null && !opt.headers.isEmpty()) {
      payload.put("headers", om.writeValueAsString(opt.headers));
    }
    if (opt.cookies != null && !opt.cookies.isEmpty()) {
      payload.put("cookies", om.writeValueAsString(opt.cookies));
    }
    if (opt.extra != null) payload.putAll(opt.extra);

    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(universalUrl))
        .timeout(cfg.timeout)
        .header("token", cfg.scraperToken)
        .header("Authorization", "Bearer " + cfg.scraperToken)
        .header("Content-Type", "application/x-www-form-urlencoded")
        .header("User-Agent", cfg.userAgent)
        .POST(HttpRequest.BodyPublishers.ofString(Utils.formEncode(payload)))
        .build();

    if (format.equals("png")) {
      HttpResponse<byte[]> res = apiClient.send(req, HttpResponse.BodyHandlers.ofByteArray());
      byte[] raw = res.body();

      Object parsed = safeParseJsonBytes(raw);
      if (parsed instanceof Map<?, ?> m) {
        Integer apiCode = m.containsKey("code") ? toInt(m.get("code")) : null;
        if (apiCode != null && apiCode != 200) {
          throw raiseForCode("Universal API error", m, res.statusCode());
        }
        if (m.containsKey("png")) {
          String b64 = String.valueOf(m.get("png"));
          return decodeBase64MaybeDataUri(b64);
        }
      }
      return raw;
    }

    HttpResponse<String> res = apiClient.send(req, HttpResponse.BodyHandlers.ofString());
    Object parsed = safeParseJson(res.body());

    if (parsed instanceof Map<?, ?> m && m.containsKey("code")) {
      Integer apiCode = toInt(m.get("code"));
      if (apiCode != null && apiCode != 200) {
        throw raiseForCode("Universal API error", m, res.statusCode());
      }
      if (m.containsKey("html")) {
        return String.valueOf(m.get("html"));
      }
    }
    return parsed;
  }

  // ==========================================================
  // Web Scraper API
  // ==========================================================

  public String createScraperTask(ScraperTaskOptions opt) throws Exception {
    if (cfg.scraperToken == null || cfg.scraperToken.isBlank()) {
        throw new IllegalArgumentException("scraperToken is required for Task Builder");
    }
    if (opt == null || opt.fileName == null || opt.spiderId == null || opt.spiderName == null) {
      throw new IllegalArgumentException("fileName, spiderId, spiderName are required");
    }
    if (opt.parameters == null) {
      throw new IllegalArgumentException("parameters is required");
    }

    Map<String, String> payload = new HashMap<>();
    payload.put("file_name", opt.fileName);
    payload.put("spider_id", opt.spiderId);
    payload.put("spider_name", opt.spiderName);
    payload.put("spider_parameters", om.writeValueAsString(List.of(opt.parameters)));
    payload.put("spider_errors", opt.includeErrors ? "true" : "false");

    if (opt.universalParams != null) {
      payload.put("spider_universal", om.writeValueAsString(opt.universalParams));
    }

    HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
        .uri(URI.create(builderUrl))
        .timeout(cfg.timeout)
        .header("Authorization", "Bearer " + cfg.scraperToken)
        .header("Content-Type", "application/x-www-form-urlencoded")
        .header("User-Agent", cfg.userAgent);

    if (cfg.publicToken != null) reqBuilder.header("token", cfg.publicToken);
    if (cfg.publicKey != null) reqBuilder.header("key", cfg.publicKey);

    HttpRequest req = reqBuilder.POST(HttpRequest.BodyPublishers.ofString(Utils.formEncode(payload))).build();

    HttpResponse<String> res = apiClient.send(req, HttpResponse.BodyHandlers.ofString());
    Object parsed = safeParseJson(res.body());

    if (!(parsed instanceof Map<?, ?> m)) {
      throw new ThordataErrors.ThordataApiException("Invalid response from builder API", null, res.statusCode(), parsed);
    }
    Integer apiCode = m.containsKey("code") ? toInt(m.get("code")) : null;
    if (apiCode != null && apiCode != 200) {
      throw raiseForCode("Task creation failed", m, res.statusCode());
    }

    Object dataObj = m.get("data");
    if (dataObj instanceof Map<?, ?> dm && dm.containsKey("task_id")) {
      return String.valueOf(dm.get("task_id"));
    }
    throw new ThordataErrors.ThordataApiException("task_id missing in response", apiCode, res.statusCode(), parsed);
  }

  public String getTaskStatus(String taskId) throws Exception {
    requirePublicCreds();
    if (taskId == null || taskId.isBlank()) throw new IllegalArgumentException("taskId is required");

    Map<String, String> payload = Map.of("tasks_ids", taskId);

    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(statusUrl))
        .timeout(cfg.timeout)
        .header("token", cfg.publicToken)
        .header("key", cfg.publicKey)
        .header("Content-Type", "application/x-www-form-urlencoded")
        .header("User-Agent", cfg.userAgent)
        .POST(HttpRequest.BodyPublishers.ofString(Utils.formEncode(payload)))
        .build();

    HttpResponse<String> res = apiClient.send(req, HttpResponse.BodyHandlers.ofString());
    Object parsed = safeParseJson(res.body());

    if (parsed instanceof Map<?, ?> m) {
      Integer apiCode = m.containsKey("code") ? toInt(m.get("code")) : null;
      if (apiCode != null && apiCode != 200) {
        throw raiseForCode("Task status failed", m, res.statusCode());
      }
      Object data = m.get("data");
      if (data instanceof List<?> list) {
        for (Object it : list) {
          if (it instanceof Map<?, ?> item) {
            if (taskId.equals(String.valueOf(item.get("task_id")))) {
              Object st = item.get("status");
              return st == null ? "unknown" : String.valueOf(st);
            }
          }
        }
      }
    }
    return "unknown";
  }

  public String getTaskResult(String taskId, String fileType) throws Exception {
    requirePublicCreds();
    if (taskId == null || taskId.isBlank()) throw new IllegalArgumentException("taskId is required");
    if (fileType == null || fileType.isBlank()) fileType = "json";

    Map<String, String> payload = Map.of("tasks_id", taskId, "type", fileType);

    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(downloadUrl))
        .timeout(cfg.timeout)
        .header("token", cfg.publicToken)
        .header("key", cfg.publicKey)
        .header("Content-Type", "application/x-www-form-urlencoded")
        .header("User-Agent", cfg.userAgent)
        .POST(HttpRequest.BodyPublishers.ofString(Utils.formEncode(payload)))
        .build();

    HttpResponse<String> res = apiClient.send(req, HttpResponse.BodyHandlers.ofString());
    Object parsed = safeParseJson(res.body());

    if (parsed instanceof Map<?, ?> m) {
      Integer apiCode = m.containsKey("code") ? toInt(m.get("code")) : null;
      if (apiCode != null && apiCode == 200 && m.get("data") instanceof Map<?, ?> dm) {
        Object dl = dm.get("download");
        if (dl != null) return String.valueOf(dl);
      }
      throw raiseForCode("Get task result failed", m, res.statusCode());
    }

    throw new ThordataErrors.ThordataApiException("Invalid response from download API", null, res.statusCode(), parsed);
  }

  public String createVideoTask(VideoTaskOptions opt) throws Exception {
    if (cfg.scraperToken == null || cfg.scraperToken.isBlank()) {
        throw new IllegalArgumentException("scraperToken is required for Video Task Builder");
    }
    if (opt == null || opt.fileName == null || opt.spiderId == null || opt.spiderName == null) {
      throw new IllegalArgumentException("fileName, spiderId, spiderName are required");
    }
    if (opt.parameters == null) {
      throw new IllegalArgumentException("parameters is required");
    }

    Map<String, String> payload = new HashMap<>();
    payload.put("file_name", opt.fileName);
    payload.put("spider_id", opt.spiderId);
    payload.put("spider_name", opt.spiderName);
    payload.put("spider_parameters", om.writeValueAsString(List.of(opt.parameters)));
    payload.put("spider_errors", opt.includeErrors ? "true" : "false");

    if (opt.commonSettings != null) {
      payload.put("common_settings", om.writeValueAsString(opt.commonSettings));
    }

    HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
        .uri(URI.create(videoBuilderUrl))
        .timeout(cfg.timeout)
        .header("Authorization", "Bearer " + cfg.scraperToken)
        .header("Content-Type", "application/x-www-form-urlencoded")
        .header("User-Agent", cfg.userAgent);

    if (cfg.publicToken != null) reqBuilder.header("token", cfg.publicToken);
    if (cfg.publicKey != null) reqBuilder.header("key", cfg.publicKey);

    HttpRequest req = reqBuilder.POST(HttpRequest.BodyPublishers.ofString(Utils.formEncode(payload))).build();

    HttpResponse<String> res = apiClient.send(req, HttpResponse.BodyHandlers.ofString());
    Object parsed = safeParseJson(res.body());

    if (!(parsed instanceof Map<?, ?> m)) {
      throw new ThordataErrors.ThordataApiException("Invalid response", null, res.statusCode(), parsed);
    }
    Integer apiCode = m.containsKey("code") ? toInt(m.get("code")) : null;
    if (apiCode != null && apiCode != 200) {
      throw raiseForCode("Video task creation failed", m, res.statusCode());
    }

    Object dataObj = m.get("data");
    if (dataObj instanceof Map<?, ?> dm && dm.containsKey("task_id")) {
      return String.valueOf(dm.get("task_id"));
    }
    throw new ThordataErrors.ThordataApiException("task_id missing", apiCode, res.statusCode(), parsed);
  }

  public Map<String, Object> listTasks(int page, int size) throws Exception {
    requirePublicCreds();
    Map<String, String> payload = new HashMap<>();
    payload.put("page", String.valueOf(page));
    payload.put("size", String.valueOf(size));

    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(taskListUrl))
        .timeout(cfg.timeout)
        .header("token", cfg.publicToken)
        .header("key", cfg.publicKey)
        .header("Content-Type", "application/x-www-form-urlencoded")
        .header("User-Agent", cfg.userAgent)
        .POST(HttpRequest.BodyPublishers.ofString(Utils.formEncode(payload)))
        .build();

    HttpResponse<String> res = apiClient.send(req, HttpResponse.BodyHandlers.ofString());
    Object parsed = safeParseJson(res.body());
    
    if (parsed instanceof Map<?, ?> m) {
        Integer apiCode = m.containsKey("code") ? toInt(m.get("code")) : null;
        if (apiCode != null && apiCode != 200) {
            throw raiseForCode("List tasks failed", m, res.statusCode());
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) m.get("data");
        return data;
    }
    return new HashMap<>();
  }

  // ==========================================================
  // Locations API
  // ==========================================================

  public Object listCountries(int proxyType) throws Exception {
    return getLocations("countries", Map.of("proxy_type", String.valueOf(proxyType)));
  }

  public Object listStates(String countryCode, int proxyType) throws Exception {
    return getLocations("states", Map.of(
        "proxy_type", String.valueOf(proxyType),
        "country_code", countryCode.toUpperCase()
    ));
  }

  public Object listCities(String countryCode, String stateCode, int proxyType) throws Exception {
    Map<String, String> params = new HashMap<>();
    params.put("proxy_type", String.valueOf(proxyType));
    params.put("country_code", countryCode.toUpperCase());
    if (stateCode != null && !stateCode.isBlank()) params.put("state_code", stateCode.toLowerCase());
    return getLocations("cities", params);
  }

  public Object listAsns(String countryCode, int proxyType) throws Exception {
    return getLocations("asn", Map.of(
        "proxy_type", String.valueOf(proxyType),
        "country_code", countryCode.toUpperCase()
    ));
  }

  private Object getLocations(String endpoint, Map<String, String> params) throws Exception {
    requirePublicCreds();

    StringBuilder qs = new StringBuilder();
    qs.append("token=").append(java.net.URLEncoder.encode(cfg.publicToken, java.nio.charset.StandardCharsets.UTF_8));
    qs.append("&key=").append(java.net.URLEncoder.encode(cfg.publicKey, java.nio.charset.StandardCharsets.UTF_8));
    for (var e : params.entrySet()) {
      qs.append("&").append(java.net.URLEncoder.encode(e.getKey(), java.nio.charset.StandardCharsets.UTF_8));
      qs.append("=").append(java.net.URLEncoder.encode(e.getValue(), java.nio.charset.StandardCharsets.UTF_8));
    }

    String url = locationsBaseUrl + "/" + endpoint + "?" + qs;

    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .timeout(cfg.timeout)
        .header("User-Agent", cfg.userAgent)
        .GET()
        .build();

    HttpResponse<String> res = apiClient.send(req, HttpResponse.BodyHandlers.ofString());
    Object parsed = safeParseJson(res.body());

    if (parsed instanceof Map<?, ?> m) {
      Integer apiCode = m.containsKey("code") ? toInt(m.get("code")) : null;
      if (apiCode != null && apiCode != 200) {
        throw raiseForCode("Locations API error", m, res.statusCode());
      }
      if (m.containsKey("data")) return m.get("data");
      return List.of();
    }

    return parsed;
  }

  public String runTask(ScraperTaskOptions taskOpt, RunTaskConfig runConfig) throws Exception {
      if (runConfig == null) runConfig = new RunTaskConfig();
      
      // 1. Create Task
      String taskId = createScraperTask(taskOpt);
      
      // 2. Poll Status
      long startTime = System.currentTimeMillis();
      long maxWaitMs = runConfig.maxWait.toMillis();
      long currentPollMs = runConfig.initialPollInterval.toMillis();
      long maxPollMs = runConfig.maxPollInterval.toMillis();
      
      while (System.currentTimeMillis() - startTime < maxWaitMs) {
          String status = getTaskStatus(taskId);
          String lower = status.toLowerCase();
          
          if (lower.equals("ready") || lower.equals("success") || lower.equals("finished")) {
              return getTaskResult(taskId, "json");
          }
          
          if (lower.equals("failed") || lower.equals("error") || lower.equals("cancelled")) {
              throw new ThordataErrors.ThordataApiException("Task failed with status: " + status, null, 200, null);
          }
          
          try { Thread.sleep(currentPollMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw e; }
          
          currentPollMs = Math.min((long)(currentPollMs * 1.5), maxPollMs);
      }
      
      throw new java.util.concurrent.TimeoutException("Task " + taskId + " timed out after " + runConfig.maxWait);
  }

  // ==========================================================
  // Helpers
  // ==========================================================

  private void requirePublicCreds() {
    if (cfg.publicToken == null || cfg.publicToken.isBlank() || cfg.publicKey == null || cfg.publicKey.isBlank()) {
      throw new IllegalArgumentException("publicToken and publicKey are required for this operation");
    }
  }

  private Object safeParseJson(String data) {
    try {
      Object obj = om.readValue(data, Object.class);
      if (obj instanceof String s) {
        try {
          return om.readValue(s.trim(), Object.class);
        } catch (Exception ignored) {
          return obj;
        }
      }
      return obj;
    } catch (Exception e) {
      return data;
    }
  }

  private Object safeParseJsonBytes(byte[] data) {
    try {
      Object obj = om.readValue(data, Object.class);
      if (obj instanceof String s) {
        try {
          return om.readValue(s.trim(), Object.class);
        } catch (Exception ignored) {
          return obj;
        }
      }
      return obj;
    } catch (Exception e) {
      return data;
    }
  }

  private static Integer toInt(Object o) {
    if (o == null) return null;
    if (o instanceof Integer i) return i;
    if (o instanceof Number n) return n.intValue();
    return null;
  }

  private RuntimeException raiseForCode(String message, Map<?, ?> payload, int httpStatus) {
    Integer apiCode = payload.containsKey("code") ? toInt(payload.get("code")) : null;

    int effective;
    if (apiCode != null && apiCode != 200) effective = apiCode;
    else if (httpStatus != 200) effective = httpStatus;
    else effective = apiCode == null ? httpStatus : apiCode;

    String errMsg = message;
    if (payload.containsKey("msg")) errMsg = String.valueOf(payload.get("msg"));
    else if (payload.containsKey("message")) errMsg = String.valueOf(payload.get("message"));

    if (effective == 300) return new ThordataErrors.ThordataNotCollectedException(errMsg, apiCode, httpStatus, payload);
    if (effective == 401 || effective == 403) return new ThordataErrors.ThordataAuthException(errMsg, apiCode, httpStatus, payload);
    if (effective == 402 || effective == 429) return new ThordataErrors.ThordataRateLimitException(errMsg, apiCode, httpStatus, payload);
    if (effective >= 500 && effective < 600) return new ThordataErrors.ThordataServerException(errMsg, apiCode, httpStatus, payload);
    if (effective == 400 || effective == 422) return new ThordataErrors.ThordataValidationException(errMsg, apiCode, httpStatus, payload);

    return new ThordataErrors.ThordataApiException(errMsg, apiCode, httpStatus, payload);
  }

  private static byte[] decodeBase64MaybeDataUri(String s) {
    if (s == null) return new byte[0];
    String v = s.trim();
    int idx = v.indexOf(",");
    if (idx >= 0) v = v.substring(idx + 1);
    v = v.replace("\n", "").replace("\r", "").replace(" ", "");
    int mod = v.length() % 4;
    if (mod == 2) v = v + "==";
    if (mod == 3) v = v + "=";
    return Base64.getDecoder().decode(v);
  }
}