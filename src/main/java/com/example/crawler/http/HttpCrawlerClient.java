package com.example.crawler.http;

import cn.hutool.core.util.RandomUtil;
import lombok.Builder;
import lombok.Getter;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class HttpCrawlerClient implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(HttpCrawlerClient.class);
    private static final List<String> USER_AGENTS = Arrays.asList(
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 Chrome/126 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 Chrome/125 Safari/537.36");

    private final OkHttpClient client;
    private final RequestOptions options;

    @Builder
    public HttpCrawlerClient(Duration connectTimeout, Duration readTimeout, int maxRetries, Duration retryDelay) {
        this.options = RequestOptions.builder().maxRetries(maxRetries).retryDelay(retryDelay).build();
        this.client = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .callTimeout(readTimeout.plus(connectTimeout))
                .build();
    }

    public static HttpCrawlerClient createDefault() {
        return HttpCrawlerClient.builder().connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(20)).maxRetries(2).retryDelay(Duration.ofMillis(800)).build();
    }

    public String get(String url) throws IOException {
        Request request = new Request.Builder().url(url)
                .header("User-Agent", USER_AGENTS.get(RandomUtil.randomInt(USER_AGENTS.size())))
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8").get().build();
        IOException last = null;
        for (int attempt = 0; attempt <= options.maxRetries; attempt++) {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) return response.body().string();
                if (response.code() < 500 && response.code() != 429) throw new IOException("HTTP " + response.code());
                last = new IOException("HTTP " + response.code());
            } catch (IOException e) { last = e; }
            if (attempt < options.maxRetries) {
                long delay = options.retryDelay.toMillis() * (1L << attempt);
                log.debug("Retrying {} after {} ms", url, delay);
                try { TimeUnit.MILLISECONDS.sleep(delay); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); throw new IOException("Retry interrupted", e);
                }
            }
        }
        throw last == null ? new IOException("Request failed") : last;
    }

    public Document getDocument(String url) throws IOException { return Jsoup.parse(get(url), url); }
    @Override public void close() { client.dispatcher().executorService().shutdown(); client.connectionPool().evictAll(); }

    @Getter @Builder
    private static class RequestOptions { @Builder.Default private int maxRetries = 2; @Builder.Default private Duration retryDelay = Duration.ofMillis(800); }
}
