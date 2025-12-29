package com.thordata.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SpecParityTest {
  @Test
  public void specProxyPorts() throws Exception {
    Path p = Path.of(System.getenv().getOrDefault("THORDATA_SDK_SPEC_PATH", "sdk-spec/v1.json"));
    if (!Files.exists(p)) {
      return; // skip locally if submodule not initialized
    }

    ObjectMapper om = new ObjectMapper();
    Map<?, ?> spec = om.readValue(Files.readString(p), Map.class);

    Map<?, ?> proxy = (Map<?, ?>) spec.get("proxy");
    Map<?, ?> products = (Map<?, ?>) proxy.get("products");

    assertEquals(9999, ((Number)((Map<?, ?>)products.get("residential")).get("port")).intValue());
    assertEquals(5555, ((Number)((Map<?, ?>)products.get("mobile")).get("port")).intValue());
    assertEquals(7777, ((Number)((Map<?, ?>)products.get("datacenter")).get("port")).intValue());
    assertEquals(6666, ((Number)((Map<?, ?>)products.get("isp")).get("port")).intValue());
  }

  @Test
  public void specSerpMapping() throws Exception {
    Path p = Path.of(System.getenv().getOrDefault("THORDATA_SDK_SPEC_PATH", "sdk-spec/v1.json"));
    if (!Files.exists(p)) {
      return;
    }

    ObjectMapper om = new ObjectMapper();
    Map<?, ?> spec = om.readValue(Files.readString(p), Map.class);

    Map<?, ?> serp = (Map<?, ?>) spec.get("serp");
    Map<?, ?> mappings = (Map<?, ?>) serp.get("mappings");
    Map<?, ?> tbm = (Map<?, ?>) mappings.get("searchTypeToTbm");

    assertEquals("nws", tbm.get("news"));
  }
  @Test
  public void specShouldNotContainPublicApiNewOrSignApiKey() throws Exception {
    Path p = Path.of(System.getenv().getOrDefault("THORDATA_SDK_SPEC_PATH", "sdk-spec/v1.json"));
    if (!Files.exists(p)) return;

    ObjectMapper om = new ObjectMapper();
    Map<?, ?> spec = om.readValue(Files.readString(p), Map.class);

    assertFalse(spec.containsKey("publicApiNew"));

    Map<?, ?> auth = (Map<?, ?>) spec.get("auth");
    if (auth != null && auth.containsKey("credentials")) {
      Map<?, ?> creds = (Map<?, ?>) auth.get("credentials");
      assertFalse(creds.containsKey("sign"));
      assertFalse(creds.containsKey("apiKey"));
    }
  }
}