# 流程

## 1.请求首页

> GET https://mcapp.z.qq.com/nc/cgi-bin/wap_farm_index?sid=c&g_ut=3

如果跳转到`https://ctc.appimg.qq.com/happyfarm/wap_farm_index_begin.html`,那说明需要登录

## 2.登录
> GET https://ctc.appimg.qq.com/happyfarm/wap_farm_index_begin.html

```html
<body>
	<h1 class="hid">QQ农场HD版</h1>
	<div class="mainbg">
		<div class="header">
			<div class="header_box">
				<p class="logo_title"></p>
				<p class="header_title"></p>
				<p class="header_title"></p>
				<p class="header_title"></p>
			</div>
			
		</div>
		<a href="http://farm.qzone.qq.com/cgi-bin/cgi_app_download?appid=353&type=13" class="btn_open">打开QQ农场HD版</a>
		<a href="https://appimg.qq.com/happyfarm/qc_login/release/loginjump_qc.html?type=login&callBackUrl=http%3A%2F%2Fmcapp.z.qq.com%2Fnc%2Fcgi-bin%2Fwap_farm_index%3Fg_ut%3D3%26low_login%3D0" class="btn_continue">继续访问触屏版</a>
		<!-- <a href="https://ui.ptlogin2.qq.com/cgi-bin/login?style=9&appid=1600000084&daid=0&s_url=http%3A%2F%2Fmcapp.z.qq.com%2Fnc%2Fcgi-bin%2Fwap_farm_index%3Fg_ut%3D3&low_login=0" class="btn_continue">继续访问触屏版</a> -->
	</div>

</body>
```
在body中找到`继续访问触屏版`链接，请求登录授权

## 3.登录授权

> GET https://appimg.qq.com/happyfarm/qc_login/release/loginjump_qc.html?type=login&callBackUrl=http%3A%2F%2Fmcapp.z.qq.com%2Fnc%2Fcgi-bin%2Fwap_farm_index%3Fg_ut%3D3%26low_login%3D0

使用playwright请求登录授权，因登录授权需要用户扫码

## 4.等待授权成功

请求`3.登录授权`后，用户扫码授权，此时`network`中会出现以下url
> GET https://xui.ptlogin2.qq.com/ssl/ptqrlogin

响应数据有以下几种（包括但非全部）
```html
ptuiCB('66', '0', '', '0', '二维码未失效。', '')
```

```html
ptuiCB('67', '0', '', '0', '二维码认证中。', '')
```

```html
ptuiCB('0', '0', 'https://ssl.ptlogin2.graph.qq.com/check_sig?', '0', '登录成功！', 'xxx', '')
```
需要判断登录成功
