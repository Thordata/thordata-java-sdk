package com.thordata.sdk;

import java.util.HashMap;
import java.util.Map;

public final class ScraperTaskOptions {
  public String fileName;
  public String spiderId;
  public String spiderName;
  public Map<String, Object> parameters = new HashMap<>();
  public Map<String, Object> universalParams;
  public boolean includeErrors = true;
}