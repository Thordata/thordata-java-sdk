package com.thordata.sdk;

import java.util.HashMap;
import java.util.Map;

public final class SerpOptions {
  public String query;
  public String engine = "google";
  public Integer num;
  public Integer start;
  public String country;
  public String language;
  public String searchType;
  public String device;
  public Boolean renderJs;
  public Boolean noCache;
  public String outputFormat = "json";
  public Map<String, String> extra = new HashMap<>();
}