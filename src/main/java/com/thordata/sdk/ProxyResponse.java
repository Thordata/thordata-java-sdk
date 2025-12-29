package com.thordata.sdk;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class ProxyResponse {
  public final int statusCode;
  public final Map<String, String> headers;
  public final byte[] body;

  public ProxyResponse(int statusCode, Map<String, String> headers, byte[] body) {
    this.statusCode = statusCode;
    this.headers = headers;
    this.body = body;
  }

  public String bodyText() {
    return new String(body, StandardCharsets.UTF_8);
  }
}