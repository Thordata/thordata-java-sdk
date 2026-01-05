// src/main/java/com/thordata/sdk/ProxyNetwork.java

package com.thordata.sdk;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public final class ProxyNetwork {
  private ProxyNetwork() {}

  public static ProxyResponse proxyGet(String targetUrl, ProxyConfig config, String userAgent, Duration timeout) throws Exception {
    if (config == null) {
      config = ProxyConfig.residentialFromEnv();
    }

    String proxyHost = config.effectiveHost();
    int proxyPort = config.effectivePort();
    String proxyUser = config.noAuth ? null : config.buildGatewayUsername();
    String proxyPass = config.noAuth ? null : config.password;

    OkHttpClient.Builder builder = new OkHttpClient.Builder()
        .connectTimeout(timeout)
        .readTimeout(timeout)
        .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));

    if (!config.noAuth && proxyUser != null && proxyPass != null) {
      String finalUser = proxyUser;
      String finalPass = proxyPass;
      builder.proxyAuthenticator((route, response) -> {
        String credential = Credentials.basic(finalUser, finalPass);
        return response.request().newBuilder()
            .header("Proxy-Authorization", credential)
            .build();
      });
    }

    OkHttpClient client = builder.build();

    Request request = new Request.Builder()
        .url(targetUrl)
        .header("User-Agent", userAgent)
        .build();

    try (Response response = client.newCall(request).execute()) {
      Map<String, String> headers = new HashMap<>();
      response.headers().names().forEach(name -> 
          headers.put(name.toLowerCase(), response.header(name)));
      
      byte[] body = response.body() != null ? response.body().bytes() : new byte[0];
      return new ProxyResponse(response.code(), headers, body);
    }
  }
}