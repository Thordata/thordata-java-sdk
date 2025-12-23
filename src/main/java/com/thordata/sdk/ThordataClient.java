package com.thordata.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.InetSocketAddress;
import java.net.ProxySelector;

import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ThordataClient {
  private final ThordataConfig cfg;
  private final HttpClient http;
  private final ObjectMapper om = new ObjectMapper();

  private final String serpUrl;
  private final String universalUrl;
  private final String builderUrl;
  private final String statusUrl;
  private final String downloadUrl;
  private final String locationsBaseUrl;

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
    if (cfg == null || cfg.scraperToken == null || cfg.scraperToken.isBlank()) {
      throw new IllegalArgumentException("scraperToken is required");
    }
    this.cfg = cfg;
    HttpClient.Builder b = HttpClient.newBuilder()
        .connectTimeout(cfg.timeout == null ? Duration.ofSeconds(30) : cfg.timeout);

    InetSocketAddress proxy = Utils.parseHttpProxy(cfg.httpProxyUrl);
    if (proxy != null) {
      b.proxy(ProxySelector.of(proxy));
    }
    this.http = b.build();

    String s = cfg.scraperApiBaseUrl.replaceAll("/+$", "");
    String u = cfg.universalApiBaseUrl.replaceAll("/+$", "");
    String w = cfg.webScraperApiBaseUrl.replaceAll("/+$", "");
    String l = cfg.locationsBaseUrl.replaceAll("/+$", "");

    this.serpUrl = s + "/request";
    this.builderUrl = s + "/builder";
    this.universalUrl = u + "/request";
    this.statusUrl = w + "/tasks-status";
    this.downloadUrl = w + "/tasks-download";
    this.locationsBaseUrl = l;
  }

  // --------------------------
  // 1) SERP API
  // --------------------------

  public Object serpSearch(SerpOptions opt) throws Exception {
    if (opt == null || opt.query == null || opt.query.isBlank()) {
      throw new IllegalArgumentException("query is required");
    }

    String engine = (opt.engine == null || opt.engine.isBlank()) ? "google" : normalizeEngine(opt.engine);
    String out = (opt.outputFormat == null || opt.outputFormat.isBlank()) ? "json" : opt.outputFormat.toLowerCase();

    Map<String, String> payload = new HashMap<>();
    payload.put("engine", engine);
    payload.put("json", out.equals("html") ? "0" : "1");

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
        .header("Authorization", "Bearer " + cfg.scraperToken)
        .header("Content-Type", "application/x-www-form-urlencoded")
        .header("User-Agent", cfg.userAgent)
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();

    HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

    if (out.equals("html")) {
      Map<String, Object> html = new HashMap<>();
      html.put("html", res.body());
      return html;
    }

    Object parsed = safeParseJson(res.body());
    if (parsed instanceof Map<?, ?> m && m.containsKey("code")) {
      Integer apiCode = toInt(m.get("code"));
      if (apiCode != null && apiCode != 200) {
        throw raiseForCode("SERP API error", m, res.statusCode());
      }
    }

    if (res.statusCode() < 200 || res.statusCode() >= 300) {
      if (parsed instanceof Map<?, ?> m) {
        throw raiseForCode("SERP HTTP error", m, res.statusCode());
      }
      throw new ThordataErrors.ThordataApiException("SERP request failed", null, res.statusCode(), parsed);
    }

    return parsed;
  }

  // --------------------------
  // 2) Universal API
  // --------------------------

  public Object universalScrape(UniversalOptions opt) throws Exception {
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
        .header("Authorization", "Bearer " + cfg.scraperToken)
        .header("Content-Type", "application/x-www-form-urlencoded")
        .header("User-Agent", cfg.userAgent)
        .POST(HttpRequest.BodyPublishers.ofString(Utils.formEncode(payload)))
        .build();

    if (format.equals("png")) {
      HttpResponse<byte[]> res = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
      byte[] raw = res.body();

      // Try JSON first (some backends may return base64 png in JSON)
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

    HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
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

    return parsed instanceof String ? parsed : om.writeValueAsString(parsed);
  }

  // --------------------------
  // 3) Web Scraper API (Tasks)
  // --------------------------

  public String createScraperTask(ScraperTaskOptions opt) throws Exception {
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

    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(builderUrl))
        .timeout(cfg.timeout)
        .header("Authorization", "Bearer " + cfg.scraperToken)
        .header("Content-Type", "application/x-www-form-urlencoded")
        .header("User-Agent", cfg.userAgent)
        .POST(HttpRequest.BodyPublishers.ofString(Utils.formEncode(payload)))
        .build();

    HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
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

    HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
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

    HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
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

  // --------------------------
  // 4) Locations API
  // --------------------------

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

    HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
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

  private void requirePublicCreds() {
    if (cfg.publicToken == null || cfg.publicToken.isBlank() || cfg.publicKey == null || cfg.publicKey.isBlank()) {
      throw new IllegalArgumentException("publicToken and publicKey are required for this operation");
    }
  }

  // --------------------------
  // Helpers
  // --------------------------

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

  // Precedence: payload code (when != 200) > HTTP status (when != 200)
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