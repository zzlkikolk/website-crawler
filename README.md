# crawler-starter

Java 11 爬虫项目脚手架，默认集成：

- OkHttp：连接池、超时、HTTP 请求与重试
- Jsoup：HTML 解析、CSS Selector、链接提取
- Hutool：随机数、JSON 等常用工具
- Lombok：减少样板代码
- SLF4J：日志门面（默认 simple 实现）

## 运行

```bash
/Users/zhangzhilin/App/apache-maven-3.9.0/bin/mvn clean test
/Users/zhangzhilin/App/apache-maven-3.9.0/bin/mvn exec:java \
  -Dexec.mainClass=com.example.crawler.CrawlerApplication \
  -Dexec.args="https://example.com"
```

## 扩展建议

将具体业务拆成 `Spider`/`Parser`/`Pipeline` 三层；列表页分页和详情页抓取可使用线程池，但必须增加限速、robots.txt/站点条款检查、持久化去重和失败重试队列。需要 JSON API 时可使用 Hutool JSON；需要浏览器渲染时再按需引入 Playwright。

## QQ 农场扫码登录

`QQNongchang` 使用 Playwright 1.24.0 打开登录页，自动尝试点击“触屏版”，等待扫码后读取浏览器上下文 Cookie，再请求 `NC_INDEX_URL`：

```bash
/Users/zhangzhilin/App/apache-maven-3.9.0/bin/mvn exec:java \\
  -Dexec.mainClass=com.tencent.QQNongchangLoginApplication
```

登录状态会保存在 `target/qq-nongchang-login`，请勿提交该目录；其中包含登录凭据，应妥善保护。
