package com.tencent;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangzhilin
 * @version 2026
 * @date 2026/09/24 16:46
 */
@Slf4j
public class QQNongchang {

    private static final String NC_INDEX_URL ="https://mcapp.z.qq.com/nc/cgi-bin/wap_farm_index?sid=c&g_ut=3";

    private static final String NC_INDEX_DOMAIN = "https://mcapp.z.qq.com";

    private static final Path COOKIE_FILE = Paths.get(".cookies", QQNongchang.class.getSimpleName() + ".json");


    // 登录页面
    private static final String NC_LOGIN_URL = "https://ctc.appimg.qq.com/happyfarm/wap_farm_index_begin.html";

    //扫码登录页
    private static final String QR_CODE_LOGIN_URL = "https://appimg.qq.com/happyfarm/qc_login/release/loginjump_qc.html?type=login&callBackUrl=http://mcapp.z.qq.com/nc/cgi-bin/wap_farm_index?g_ut=3&low_login=0";

    public static void main(String[] args) {

        QQNongchang qqNongchang = new QQNongchang();
        CheckLoginContext context = qqNongchang.checkLogin();
        if(!context.login){
            if(context.getLoginUrl()==null){
                throw new RuntimeException("未登录，未找到登录链接");
            }

            Map<String,String> login_cookie = qqNongchang.login(context.loginUrl);
            if(login_cookie == null){
                throw new RuntimeException("扫码登录失败");
            }
            context.setCookies(login_cookie);
            qqNongchang.saveCookies(login_cookie);
            log.info("登录成功，cookie：{}", login_cookie);
        }

        qqNongchang.getAll(context.getCookies());
    }



    /**
     * 一键收获
     */
    public void getAll(Map<String,String> cookies){

        try {
            Connection.Response index = Jsoup.connect(NC_INDEX_URL)
                    .header("user-agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/151.0.0.0 Safari/537.36")
                    .cookies(cookies)
                    .execute();
            Document document = index.parse();
            Element harvest = document.selectFirst("a[href*=wap_farm_harvest]:contains(收获)");
            if(harvest==null){
                log.warn("无可收获");
                return;
            }
            String url = resolveUrl(harvest.attr("href"));


            log.info("开始收获，url:{}",url);
            Connection.Response response = Jsoup.connect(url)
                    .header("user-agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/151.0.0.0 Safari/537.36")
                    .cookies(cookies)
                    .execute();
            log.info("收获完成，response:{}",response.body());
        } catch (IOException e) {
            log.error("请求异常",e);
        }


        /*
             <a href="http://mcapp.z.qq.com/nc/cgi-bin/wap_farm_harvest?sid=&amp;B_UID=0&amp;place=0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25&amp;g_ut=3&amp;time=-2147483648">收获</a>
         */
    }

    /**
     * 将页面中的相对链接转换为绝对链接。
     */
    private String resolveUrl(String url) {
        URI target = URI.create(url);
        if (target.isAbsolute()) {
            return url;
        }
        return URI.create(NC_INDEX_URL).resolve(target).toString();
    }

    public void fish(){
        /*
        <a href="http://mcapp.z.qq.com/nc/cgi-bin/wap_farm_fish_index?sid=c&amp;g_ut=3">我的池塘</a>
        <a href="./wap_farm_fish_harvest?sid=c&amp;g_ut=3&amp;index=0,1,2,3,4,5&amp;flag=2&amp;time=-2147483648">捞鱼</a>
        <a href="./wap_fish_seed_list?sid=c&amp;g_ut=3&amp;pnum=6&amp;flag=1&amp;time=-2147483648">养殖</a>
         */
    }

    /**
     * 检查是否需要登录
     * @return true/false
     */
    public CheckLoginContext checkLogin() {

        CheckLoginContext context  = new CheckLoginContext();
        context.setLogin(false);
        Map<String, String> cookies = loadCookies();
        context.setCookies(cookies);
        try {
            Connection.Response index= Jsoup.connect(NC_INDEX_URL)
                    .header("user-agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/151.0.0.0 Safari/537.36")
                    .cookies(cookies)
                    .execute();

            Document document = index.parse();
            Element direct = document.select("body > div > a.btn_continue").first();
            if(direct==null){
                context.setLogin(true);
                cookies.putAll(index.cookies());
                context.setCookies(cookies);
                saveCookies(cookies);
                return context;
            }

            //未登录找到登录链接
            if("继续访问触屏版".equals(direct.text())){
                context.setLoginUrl(direct.attr("href"));
                return context;
            }

        } catch (IOException e) {
            log.error("checkLogin error",e);
        }
        return context;
    }

    private Map<String, String> loadCookies() {
        if (!Files.isRegularFile(COOKIE_FILE)) {
            return new HashMap<>();
        }
        try {
            CookieStore cookieStore = JSONUtil.toBean(
                    Files.readString(COOKIE_FILE, StandardCharsets.UTF_8), CookieStore.class);
            if (!QQNongchang.class.getName().equals(cookieStore.getClassName())
                    || cookieStore.getCookies() == null) {
                log.warn("Cookie文件类名不匹配或内容为空：{}", COOKIE_FILE);
                return new HashMap<>();
            }
            return new HashMap<>(cookieStore.getCookies());
        } catch (Exception e) {
            log.warn("读取Cookie文件失败：{}", COOKIE_FILE, e);
            return new HashMap<>();
        }
    }

    private void saveCookies(Map<String, String> cookies) {
        try {
            Files.createDirectories(COOKIE_FILE.getParent());
            CookieStore cookieStore = new CookieStore(QQNongchang.class.getName(), new HashMap<>(cookies));
            Files.writeString(COOKIE_FILE, JSONUtil.toJsonPrettyStr(cookieStore), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("保存Cookie文件失败：{}", COOKIE_FILE, e);
        }
    }


    /**
     * 打开登录链接
     * @param loginUrl 登录链接
     * @return cookie信息
     */

    // 扫码登录超时时间（毫秒）
    private static final long LOGIN_TIMEOUT = 120_000L;


    public Map<String,String> login(String loginUrl){


        try( Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(false) // 设置为无头模式
                        .setSlowMo(0) // 设置慢动作，便于观察

        );
        ){
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            page.setDefaultTimeout(30000); // 全局默认超时30秒

            page.navigate(loginUrl);
            log.info("已打开登录页面，请使用QQ扫码授权");

            // 扫码授权成功后会经过 check_sig 跳转回农场首页，直接等待跳转完成
            page.waitForURL(url -> url.contains("mcapp.z.qq.com"),
                    new Page.WaitForURLOptions().setTimeout(LOGIN_TIMEOUT));
            page.waitForLoadState();
            log.info("扫码登录成功，已跳转回农场首页：{}", page.url());

            // 提取浏览器中的cookie
            Map<String,String> cookies = new HashMap<>();
            for (Cookie cookie : context.cookies()) {
                cookies.put(cookie.name, cookie.value);
            }
            return cookies;
        }catch (Exception e){
            log.error("打开登录链接失败",e);
        }
        return null;
    }

    @Data
    public static class CheckLoginContext{
        private Boolean login;

        private String loginUrl;

        private Map<String,String> cookies;
    }

    @Data
    @NoArgsConstructor
    public static class CookieStore {
        private String className;
        private Map<String, String> cookies;

        public CookieStore(String className, Map<String, String> cookies) {
            this.className = className;
            this.cookies = cookies;
        }
    }
}
