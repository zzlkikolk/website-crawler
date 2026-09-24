package com.tencent;

/**
 * @author zhangzhilin
 * @version 2026
 * @date 2026/09/24 16:46
 */
public class QQNongchang {

    private static final String NC_INDEX_URL ="https://mcapp.z.qq.com/nc/cgi-bin/wap_farm_index?sid=c&g_ut=3";


    private static final String NC_LOGIN_URL = "https://ctc.appimg.qq.com/happyfarm/wap_farm_index_begin.html";

    /**
     * 检查是否需要登录
     * @return true/false
     */
    public boolean checkLogin() {

        return false;
    }
}
