package com.thordata.sdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class UniversalOptions {
  public String url;
  public boolean jsRender = false;
  public String outputFormat = "html"; // "html" or "png"
  public String country;
  public String blockResources;
  public String cleanContent;
  public Integer wait;
  public String waitFor;
  public List<Map<String, String>> headers = new ArrayList<>();
  public List<Map<String, String>> cookies = new ArrayList<>();
  public Map<String, String> extra = new HashMap<>();
}