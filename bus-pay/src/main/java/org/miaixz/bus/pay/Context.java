/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.pay;

import com.alipay.api.AlipayClient;
import lombok.Builder;
import lombok.*;
import org.miaixz.bus.core.xyz.StringKit;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 上下文配置
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Context implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final ThreadLocal<String> TL = new ThreadLocal<>();
    private static final Map<String, Context> CFG_MAP = new ConcurrentHashMap<>();
    private static final String DEFAULT_CFG_KEY = "_default_key_";

    /**
     * 应用编号
     */
    public String appId;
    /**
     * 应用编号
     */
    public String appKey;
    /**
     * 应用编号
     */
    public String appSecret;

    /**
     * 商户账号
     */
    private String mchId;
    /**
     * 服务商应用编号
     */
    private String slAppId;
    /**
     * 服务商商户号
     */
    private String slMchId;
    /**
     * 同 apiKey 后续版本会舍弃
     */
    private String partnerKey;
    /**
     * 商户平台「API安全」中的 API 密钥
     */
    private String apiKey;
    /**
     * 商户平台「API安全」中的 APIv3 密钥
     */
    private String apiKey3;
    /**
     * 应用域名，回调中会使用此参数
     */
    private String domain;
    /**
     * API 证书中的 p12
     */
    private String certPath;


    /*********************unionpay*****************************/
    /**
     * API 证书中的 key.pem
     */
    private String keyPemPath;
    /**
     * API 证书中的 cert.pem
     */
    private String certPemPath;
    /**
     * 其他附加参数
     */
    private Object exParams;

    /****************************paypal**********************************/
    /**
     * 连锁商户号
     */
    private String groupMchId;
    /**
     * 授权交易机构代码
     */
    private String agentMchId;
    /**
     * 商户平台网关
     */
    private String serverUrl;
    /**
     * 应用编号
     */
    private String clientId;
    /**
     * 应用密钥
     */
    private String secret;
    /**
     * 是否是沙箱环境
     */
    private boolean sandBox;


    /****************************jdpay**********************************/

    private String rsaPrivateKey;
    private String rsaPublicKey;
    private String desKey;
    /****************************jdpay**********************************/

    private String privateKey;
    private String aliPayPublicKey;
    private String serviceUrl;
    private String charset;
    private String signType;
    private String format;
    private boolean certModel;
    private String appCertPath;
    private String appCertContent;
    private String aliPayCertPath;
    private String aliPayCertContent;
    private String aliPayRootCertPath;
    private String aliPayRootCertContent;
    private AlipayClient alipayClient;

    /**
     * 添加微信支付配置，每个appId只需添加一次，相同appId将被覆盖
     *
     * @param context 微信支付配置
     * @return {WxPayApiConfig} 微信支付配置
     */
    public static Context putApiConfig(Context context) {
        if (CFG_MAP.size() == 0) {
            CFG_MAP.put(DEFAULT_CFG_KEY, context);
        }
        return CFG_MAP.put(getAppId(), context);
    }

    public static Context setThreadLocalJdPayApiConfig(Context context) {
        if (StringKit.isNotEmpty(getAppId())) {
            setThreadLocalAppId(getAppId());
        }
        return putApiConfig(context);
    }

    public static Context removeApiConfig(Context context) {
        return removeApiConfig(getAppId());
    }

    public static Context removeApiConfig(String appId) {
        return CFG_MAP.remove(appId);
    }

    public static void setThreadLocalAppId(String appId) {
        if (StringKit.isEmpty(appId)) {
            appId = CFG_MAP.get(DEFAULT_CFG_KEY).getAppId();
        }
        TL.set(appId);
    }

    public static void removeThreadLocalAppId() {
        TL.remove();
    }

    public static String getAppId() {
        String appId = TL.get();
        if (StringKit.isEmpty(appId)) {
            appId = CFG_MAP.get(DEFAULT_CFG_KEY).getAppId();
        }
        return appId;
    }

    public static Context getJdPayApiConfig() {
        String appId = getAppId();
        return getApiConfig(appId);
    }

    public static Context getApiConfig(String appId) {
        Context context = CFG_MAP.get(appId);
        if (context == null) {
            throw new IllegalStateException("需事先调用 Context 将 appId 对应的配置对象存入，才可以使用其他的系列方法");
        }
        return context;
    }

}
