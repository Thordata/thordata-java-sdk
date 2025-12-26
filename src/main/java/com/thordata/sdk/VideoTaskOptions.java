package com.thordata.sdk;

import java.util.HashMap;
import java.util.Map;

public final class VideoTaskOptions {
  public String fileName;
  public String spiderId;
  public String spiderName;
  public Map<String, Object> parameters = new HashMap<>();
  public CommonSettings commonSettings;
  public boolean includeErrors = true;
}