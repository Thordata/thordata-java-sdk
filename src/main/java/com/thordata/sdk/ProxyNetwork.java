package com.thordata.sdk;

import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

public final class ProxyNetwork {
  private ProxyNetwork() {}

  public static ProxyResponse proxyGet(String targetUrl, ProxyConfig proxy, String userAgent, Duration timeout) throws Exception {
    if (proxy == null) {
      // default: residential from env (you can extend to datacenter/mobile priority like Go)
      proxy = ProxyConfig.residentialFromEnv();
    }

    String proxyHost = proxy.effectiveHost();
    int proxyPort = proxy.effectivePort();
    String proxyProtocol = proxy.effectiveProtocol();

    URI uri = URI.create(targetUrl);
    String scheme = uri.getScheme() == null ? "https" : uri.getScheme().toLowerCase(Locale.ROOT);
    String host = uri.getHost();
    int port = uri.getPort() > 0 ? uri.getPort() : (scheme.equals("https") ? 443 : 80);

    if (host == null || host.isBlank()) throw new IllegalArgumentException("Invalid target URL host");

    // Connect to proxy (TLS-to-proxy if protocol=https)
    Socket proxySocket;
    if (proxyProtocol.equals("https")) {
      proxySocket = SSLSocketFactory.getDefault().createSocket(proxyHost, proxyPort);
    } else {
      proxySocket = new Socket(proxyHost, proxyPort);
    }
    int timeoutMs = (int) (timeout == null ? 30_000 : timeout.toMillis());
    proxySocket.setSoTimeout(timeoutMs);

    // CONNECT
    String authHeader = null;
    if (!proxy.noAuth) {
      if (proxy.username == null || proxy.username.isBlank() || proxy.password == null || proxy.password.isBlank()) {
        throw new IllegalArgumentException("Proxy username/password required (or enable THORDATA_PROXY_WHITELIST)");
      }
      String login = proxy.buildGatewayUsername() + ":" + proxy.password;
      String basic = Base64.getEncoder().encodeToString(login.getBytes(StandardCharsets.UTF_8));
      authHeader = "Basic " + basic;
    }

    OutputStream out = proxySocket.getOutputStream();
    InputStream in = proxySocket.getInputStream();

    StringBuilder connect = new StringBuilder();
    connect.append("CONNECT ").append(host).append(":").append(port).append(" HTTP/1.1\r\n");
    connect.append("Host: ").append(host).append(":").append(port).append("\r\n");
    if (authHeader != null) connect.append("Proxy-Authorization: ").append(authHeader).append("\r\n");
    connect.append("Proxy-Connection: Keep-Alive\r\n");
    connect.append("Connection: Keep-Alive\r\n");
    connect.append("\r\n");

    out.write(connect.toString().getBytes(StandardCharsets.UTF_8));
    out.flush();

    // Read CONNECT response headers
    String statusLine = readLine(in);
    if (statusLine == null) throw new IOException("Empty CONNECT response");
    int status = parseStatus(statusLine);
    Map<String, String> connectHeaders = readHeaders(in);
    if (status != 200) {
      throw new IOException("Proxy CONNECT failed: " + statusLine + " headers=" + connectHeaders);
    }

    // Now tunnel established. If target is https, start TLS to target over the tunnel.
    InputStream reqIn = in;
    OutputStream reqOut = out;

    Socket tunnelSocket = proxySocket;
    if (scheme.equals("https")) {
      SSLSocketFactory f = (SSLSocketFactory) SSLSocketFactory.getDefault();
      SSLSocket ssl = (SSLSocket) f.createSocket(proxySocket, host, port, true);

      SSLParameters p = ssl.getSSLParameters();
      p.setEndpointIdentificationAlgorithm("HTTPS");
      p.setServerNames(List.of(new SNIHostName(host)));
      ssl.setSSLParameters(p);

      ssl.startHandshake();
      tunnelSocket = ssl;
      reqIn = ssl.getInputStream();
      reqOut = ssl.getOutputStream();
    }

    // Build request
    String path = uri.getRawPath();
    if (path == null || path.isBlank()) path = "/";
    if (uri.getRawQuery() != null && !uri.getRawQuery().isBlank()) path = path + "?" + uri.getRawQuery();

    String ua = (userAgent == null || userAgent.isBlank()) ? "thordata-java-sdk" : userAgent;

    StringBuilder req = new StringBuilder();
    req.append("GET ").append(path).append(" HTTP/1.1\r\n");
    req.append("Host: ").append(host).append("\r\n");
    req.append("User-Agent: ").append(ua).append("\r\n");
    req.append("Accept: */*\r\n");
    req.append("Accept-Encoding: identity\r\n");
    req.append("Connection: close\r\n");
    req.append("\r\n");

    reqOut.write(req.toString().getBytes(StandardCharsets.UTF_8));
    reqOut.flush();

    // Parse response
    String respStatus = readLine(reqIn);
    if (respStatus == null) throw new IOException("Empty response after CONNECT");
    int respCode = parseStatus(respStatus);
    Map<String, String> headers = readHeaders(reqIn);
    byte[] body = readBody(reqIn, headers);

    try { tunnelSocket.close(); } catch (Exception ignored) {}

    return new ProxyResponse(respCode, headers, body);
  }

  private static String readLine(InputStream in) throws IOException {
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    int b;
    boolean gotCR = false;
    while ((b = in.read()) != -1) {
      if (gotCR && b == '\n') break;
      if (b == '\r') { gotCR = true; continue; }
      gotCR = false;
      buf.write(b);
    }
    if (buf.size() == 0 && b == -1) return null;
    return buf.toString(StandardCharsets.UTF_8);
  }

  private static Map<String, String> readHeaders(InputStream in) throws IOException {
    Map<String, String> h = new LinkedHashMap<>();
    while (true) {
      String line = readLine(in);
      if (line == null) break;
      if (line.isEmpty()) break;
      int idx = line.indexOf(':');
      if (idx > 0) {
        String k = line.substring(0, idx).trim();
        String v = line.substring(idx + 1).trim();
        h.put(k.toLowerCase(Locale.ROOT), v);
      }
    }
    return h;
  }

  private static int parseStatus(String statusLine) {
    // HTTP/1.1 200 Connection established
    String[] parts = statusLine.split(" ");
    if (parts.length >= 2) {
      try { return Integer.parseInt(parts[1].trim()); } catch (Exception ignored) {}
    }
    return 0;
  }

  private static byte[] readBody(InputStream in, Map<String, String> headers) throws IOException {
    String te = headers.getOrDefault("transfer-encoding", "");
    String cl = headers.getOrDefault("content-length", "");

    if (te.toLowerCase(Locale.ROOT).contains("chunked")) {
      return readChunked(in);
    }

    if (!cl.isBlank()) {
      try {
        int n = Integer.parseInt(cl.trim());
        return in.readNBytes(n);
      } catch (Exception ignored) {}
    }

    // fallback: read until EOF
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    byte[] tmp = new byte[8192];
    int r;
    while ((r = in.read(tmp)) != -1) buf.write(tmp, 0, r);
    return buf.toByteArray();
  }

  private static byte[] readChunked(InputStream in) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    while (true) {
      String line = readLine(in);
      if (line == null) break;
      String sizeStr = line.split(";", 2)[0].trim();
      int size = Integer.parseInt(sizeStr, 16);
      if (size == 0) {
        // trailing headers
        readHeaders(in);
        break;
      }
      byte[] chunk = in.readNBytes(size);
      out.write(chunk);
      // trailing CRLF
      readLine(in);
    }
    return out.toByteArray();
  }
}