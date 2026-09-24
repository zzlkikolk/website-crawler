package com.tencent;

import com.microsoft.playwright.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
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



    /**
     * 检查是否需要登录
     * @return true/false
     */
    public boolean checkLogin() {

        return false;
    }


    public Map<String,String> login(){


        try( Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(false) // 设置为无头模式
                        .setSlowMo(0) // 设置慢动作，便于观察

        );
        ){
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            page.setDefaultTimeout(10000); // 全局默认超时10秒
            page.navigate(QR_CODE_LOGIN_URL);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
