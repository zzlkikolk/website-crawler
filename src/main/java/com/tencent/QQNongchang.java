package com.tencent;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
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
            log.info("登录成功，cookie：{}", login_cookie);

        }
    }


    /**
     * 检查是否需要登录
     * @return true/false
     */
    public CheckLoginContext checkLogin() {

        String index_url = "https://mcapp.z.qq.com/nc/cgi-bin/wap_farm_index?sid=c&g_ut=3";
        CheckLoginContext context  = new CheckLoginContext();
        context.setLogin(false);
        try {
            Connection.Response index= Jsoup.connect(index_url)
                    .header("user-agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/151.0.0.0 Safari/537.36")
                    .execute();

            Document document = index.parse();
            Element direct = document.select("body > div > a.btn_continue").first();
            if(direct==null){
                context.setLogin(true);
                Map<String,String> cookies =  index.cookies();
                context.setCookies(cookies);
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
}
