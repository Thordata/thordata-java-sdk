package com.thordata.sdk.examples;

import com.thordata.sdk.ProxyConfig;
import com.thordata.sdk.ProxyResponse;
import com.thordata.sdk.ThordataClient;
import com.thordata.sdk.ThordataConfig;
import java.time.Duration;

public class SmokeProxy {
    // 辅助方法：优先读 System Property，其次读 Env
    private static String getEnv(String key) {
        String v = System.getProperty(key);
        if (v != null && !v.isEmpty()) return v;
        return System.getenv(key);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Java Proxy Smoke Test ===");
        
        String token = getEnv("THORDATA_SCRAPER_TOKEN");
        // 初始化 Client
        ThordataConfig cfg = new ThordataConfig(token, null, null, Duration.ofSeconds(60), null, null, null, null, null, null);
        ThordataClient client = new ThordataClient(cfg);

        // 手动构建 ProxyConfig，绕过 residentialFromEnv 的限制
        String user = getEnv("THORDATA_RESIDENTIAL_USERNAME");
        String pass = getEnv("THORDATA_RESIDENTIAL_PASSWORD");
        String host = getEnv("THORDATA_PROXY_HOST");
        
        if (user == null || pass == null || host == null) {
             System.err.println("❌ Proxy creds missing!");
             return;
        }

        ProxyConfig proxy = new ProxyConfig();
        proxy.product = com.thordata.sdk.ProxyProduct.RESIDENTIAL;
        proxy.username = user;
        proxy.password = pass;
        proxy.host = host;
        proxy.port = 9999;
        proxy.country = "us";
        proxy.protocol = "https"; // 强制 HTTPS
        
        System.out.println("Testing HTTPS Proxy: " + proxy.effectiveHost());

        try {
            ProxyResponse resp = client.proxyGet("https://ipinfo.thordata.com", proxy);
            System.out.println("Status: " + resp.statusCode);
            System.out.println("Body: " + resp.bodyText());
            
            if (resp.statusCode == 200) {
                System.out.println("✅ HTTPS Proxy SUCCESS!");
            } else {
                System.out.println("❌ Failed with status " + resp.statusCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Exception: " + e.getMessage());
        }
        
        // 我们不测 SOCKS5，因为环境不支持 Java 的 SOCKS5 Auth 比较麻烦，且环境已证实不通
        System.exit(0);
    }
}