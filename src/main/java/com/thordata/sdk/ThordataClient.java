package com.thordata.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
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

  public ThordataClient(ThordataConfig cfg) {
    if (cfg == null || cfg.scraperToken == null || cfg.scraperToken.isBlank()) {
      throw new IllegalArgumentException("scraperToken is required");
    }
    this.cfg = cfg;
    this.http = HttpClient.newBuilder()
        .connectTimeout(cfg.timeout == null ? Duration.ofSeconds(30) : cfg.timeout)
        .build();

    String s = cfg.scraperApiBaseUrl.replaceAll("/+$", "");
    String u = cfg.universalApiBaseUrl.replaceAll("/+$", "");
    String w = cfg.webScraperApiBaseUrl.replaceAll("/+$", "");

    this.serpUrl = s + "/request";
    this.builderUrl = s + "/builder";
    this.universalUrl = u + "/request";
    this.statusUrl = w + "/tasks-status";
    this.downloadUrl = w + "/tasks-download";
  }

  public Object serpSearch(SerpOptions opt) throws IOException, InterruptedException {
    if (opt == null || opt.query == null || opt.query.isBlank()) {
      throw new IllegalArgumentException("query is required");
    }

    String engine = (opt.engine == null || opt.engine.isBlank()) ? "google" : opt.engine.toLowerCase();
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
}