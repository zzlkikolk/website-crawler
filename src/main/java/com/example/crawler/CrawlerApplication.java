package com.example.crawler;

import com.example.crawler.http.HttpCrawlerClient;
import org.jsoup.nodes.Document;

public final class CrawlerApplication {
    private CrawlerApplication() {}

    public static void main(String[] args) throws Exception {
        String url = args.length == 0 ? "https://example.com" : args[0];
        try (HttpCrawlerClient client = HttpCrawlerClient.createDefault()) {
            Document document = client.getDocument(url);
            System.out.printf("title=%s, links=%d%n", document.title(), document.select("a[href]").size());
        }
    }
}
