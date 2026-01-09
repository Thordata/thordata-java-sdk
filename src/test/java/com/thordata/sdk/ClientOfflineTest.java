package com.thordata.sdk;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ClientOfflineTest {

  @Test
  public void universalHtml_offline() throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext("/request", ex -> {
      try {
        String body = readBody(ex.getRequestBody());
        assertTrue(body.contains("type=html"));
        writeJson(ex, 200, "{\"code\":200,\"html\":\"<h1>Hello</h1>\"}");
      } catch (Exception e) { throw new RuntimeException(e); }
    });
    server.start();
    String base = "http://127.0.0.1:" + server.getAddress().getPort();

    ThordataConfig cfg = new ThordataConfig(
        "token", "pub", "key", Duration.ofSeconds(10), null,
        base, base, base, base
    );
    ThordataClient client = new ThordataClient(cfg);

    UniversalOptions opt = new UniversalOptions();
    opt.url = "https://example.com";
    Object out = client.universalScrape(opt);
    assertTrue(String.valueOf(out).contains("Hello"));
    server.stop(0);
  }

  @Test
  public void tasks_offline() throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext("/builder", ex -> writeJson(ex, 200, "{\"code\":200,\"data\":{\"task_id\":\"t1\"}}"));
    server.createContext("/tasks-status", ex -> writeJson(ex, 200, "{\"code\":200,\"data\":[{\"task_id\":\"t1\",\"status\":\"ready\"}]}"));
    server.createContext("/tasks-download", ex -> writeJson(ex, 200, "{\"code\":200,\"data\":{\"download\":\"https://example.com/file.json\"}}"));
    server.start();
    String base = "http://127.0.0.1:" + server.getAddress().getPort();

    ThordataConfig cfg = new ThordataConfig(
        "token", "pub", "key", Duration.ofSeconds(10), null,
        base, base, base, base
    );
    ThordataClient client = new ThordataClient(cfg);

    ScraperTaskOptions opt = new ScraperTaskOptions();
    opt.fileName = "f";
    opt.spiderId = "s1";
    opt.spiderName = "e";
    opt.parameters = Map.of("url", "x");
    
    assertEquals("t1", client.createScraperTask(opt));
    assertEquals("ready", client.getTaskStatus("t1"));
    assertTrue(client.getTaskResult("t1", "json").startsWith("https://"));
    server.stop(0);
  }

  @Test
  public void locations_offline() throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext("/countries", ex -> writeJson(ex, 200, "{\"code\":200,\"data\":[{\"country_code\":\"US\"}]}"));
    server.start();
    String base = "http://127.0.0.1:" + server.getAddress().getPort();

    ThordataConfig cfg = new ThordataConfig(
        "token", "pub", "key", Duration.ofSeconds(10), null,
        base, base, base, base
    );
    ThordataClient client = new ThordataClient(cfg);
    assertNotNull(client.listCountries(1));
    server.stop(0);
  }

  private static String readBody(InputStream in) throws Exception {
    try (in) { return new String(in.readAllBytes(), StandardCharsets.UTF_8); }
  }

  private static void writeJson(com.sun.net.httpserver.HttpExchange ex, int status, String json) {
    try {
        byte[] b = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.sendResponseHeaders(status, b.length);
        ex.getResponseBody().write(b);
        ex.close();
    } catch (Exception e) { e.printStackTrace(); }
  }
}