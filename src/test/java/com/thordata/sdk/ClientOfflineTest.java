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
        assertTrue(body.contains("js_render=True"));
        assertEquals("Bearer token", ex.getRequestHeaders().getFirst("Authorization"));

        writeJson(ex, 200, "{\"code\":200,\"html\":\"<h1>Hello</h1>\"}");
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    server.start();
    String base = "http://127.0.0.1:" + server.getAddress().getPort();

    ThordataClient client = new ThordataClient(new ThordataConfig(
        "token", "pub", "key",
        Duration.ofSeconds(10), null,
        base, base, base, base
    ));

    UniversalOptions opt = new UniversalOptions();
    opt.url = "https://example.com";
    opt.jsRender = true;
    opt.outputFormat = "html";

    Object out = client.universalScrape(opt);
    assertTrue(String.valueOf(out).contains("Hello"));

    server.stop(0);
  }

  @Test
  public void tasks_offline() throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

    server.createContext("/builder", ex -> {
      try {
        assertEquals("Bearer token", ex.getRequestHeaders().getFirst("Authorization"));
        writeJson(ex, 200, "{\"code\":200,\"data\":{\"task_id\":\"t1\"}}");
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    server.createContext("/tasks-status", ex -> {
      try {
        assertEquals("pub", ex.getRequestHeaders().getFirst("token"));
        assertEquals("key", ex.getRequestHeaders().getFirst("key"));
        writeJson(ex, 200, "{\"code\":200,\"data\":[{\"task_id\":\"t1\",\"status\":\"ready\"}]}");
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    server.createContext("/tasks-download", ex -> {
      try {
        writeJson(ex, 200, "{\"code\":200,\"data\":{\"download\":\"https://example.com/file.json\"}}");
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    server.start();
    String base = "http://127.0.0.1:" + server.getAddress().getPort();

    ThordataClient client = new ThordataClient(new ThordataConfig(
        "token", "pub", "key",
        Duration.ofSeconds(10), null,
        base, base, base, base
    ));

    ScraperTaskOptions opt = new ScraperTaskOptions();
    opt.fileName = "f";
    opt.spiderId = "s1";
    opt.spiderName = "example.com";
    opt.parameters = Map.of("url", "https://example.com");
    opt.includeErrors = true;

    String taskId = client.createScraperTask(opt);
    assertEquals("t1", taskId);

    String st = client.getTaskStatus("t1");
    assertEquals("ready", st);

    String dl = client.getTaskResult("t1", "json");
    assertTrue(dl.startsWith("https://"));

    server.stop(0);
  }

  @Test
  public void locations_offline() throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

    server.createContext("/countries", ex -> {
      try {
        String q = ex.getRequestURI().getQuery();
        assertNotNull(q);
        assertTrue(q.contains("token=pub"));
        assertTrue(q.contains("key=key"));
        assertTrue(q.contains("proxy_type=1"));

        writeJson(ex, 200, "{\"code\":200,\"data\":[{\"country_code\":\"US\"}]}");
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    server.start();
    String base = "http://127.0.0.1:" + server.getAddress().getPort();

    ThordataClient client = new ThordataClient(new ThordataConfig(
        "token", "pub", "key",
        Duration.ofSeconds(10), null,
        base, base, base, base
    ));

    Object out = client.listCountries(1);
    assertNotNull(out);

    server.stop(0);
  }

  private static String readBody(InputStream in) throws Exception {
    try (in) {
      byte[] b = in.readAllBytes();
      return new String(b, StandardCharsets.UTF_8);
    }
  }

  private static void writeJson(com.sun.net.httpserver.HttpExchange ex, int status, String json) throws Exception {
    byte[] b = json.getBytes(StandardCharsets.UTF_8);
    ex.getResponseHeaders().add("Content-Type", "application/json");
    ex.sendResponseHeaders(status, b.length);
    ex.getResponseBody().write(b);
    ex.close();
  }
}