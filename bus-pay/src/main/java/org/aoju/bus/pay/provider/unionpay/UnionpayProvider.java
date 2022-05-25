package org.aoju.bus.pay.provider.unionpay;

import org.aoju.bus.pay.metric.HttpKit;
import org.aoju.bus.pay.metric.WxPayKit;

import java.util.Map;

/**
 * 云闪付接口
 */
public class UnionpayProvider {

    public static String authUrl = "https://qr.95516.com/qrcGtwWeb-web/api/userAuth?version=1.0.0&redirectUrl=%s";

    public static String execution(String url, Map<String, String> params) {
        return HttpKit.post(url, WxPayKit.toXml(params));
    }

    /**
     * 获取用户授权 API
     *
     * @param url 回调地址，可以自定义参数 https://pay.javen.com/callback?sdk=ijpay
     * @return 银联重定向 Url
     */
    public static String buildAuthUrl(String url) {
        return String.format(authUrl, url);
    }

}
