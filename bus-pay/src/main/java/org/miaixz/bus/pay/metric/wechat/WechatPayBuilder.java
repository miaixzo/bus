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
package org.miaixz.bus.pay.metric.wechat;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.pay.Builder;
import org.miaixz.bus.pay.magic.Message;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WechatPayBuilder {

    private static final String OS = System.getProperty("os.name") + "/" + System.getProperty("os.version");
    private static final String VERSION = System.getProperty("java.version");

    private static final String FIELD_SIGN = "sign";
    private static final String FIELD_SIGN_TYPE = "sign_type";
    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    public static Map<String, String> getBaseHeaders(String authorization) {
        String userAgent = String.format(
                "Wechatpay-Http/%s (%s) Java/%s",
                WechatPayBuilder.class.getPackage().getImplementationVersion(),
                OS,
                VERSION == null ? "Unknown" : VERSION);

        Map<String, String> headers = new HashMap<>(5);
        headers.put("Accept", MediaType.APPLICATION_JSON);
        headers.put("Authorization", authorization);
        headers.put("User-Agent", userAgent);
        return headers;
    }

    public static Map<String, String> getHeaders(String authorization, String serialNumber) {
        Map<String, String> headers = getBaseHeaders(authorization);
        headers.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        if (StringKit.isNotEmpty(serialNumber)) {
            headers.put("Wechatpay-Serial", serialNumber);
        }
        return headers;
    }

    public static Map<String, String> getUploadHeaders(String authorization, String serialNumber) {
        Map<String, String> headers = getBaseHeaders(authorization);
        headers.put(HTTP.CONTENT_TYPE, "multipart/form-data;boundary=\"boundary\"");
        if (StringKit.isNotEmpty(serialNumber)) {
            headers.put("Wechatpay-Serial", serialNumber);
        }
        return headers;
    }

    /**
     * 构建返回参数
     *
     * @param response {@link Message}
     * @return {@link Map}
     */
    public static Map<String, Object> buildResMap(Message response) {
        if (response == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>(6);
        String timestamp = response.getHeader("Wechatpay-Timestamp");
        String nonceStr = response.getHeader("Wechatpay-Nonce");
        String serialNo = response.getHeader("Wechatpay-Serial");
        String signature = response.getHeader("Wechatpay-Signature");
        String body = response.getBody();
        int status = response.getStatus();
        map.put("timestamp", timestamp);
        map.put("nonceStr", nonceStr);
        map.put("serialNumber", serialNo);
        map.put("signature", signature);
        map.put("body", body);
        map.put("status", status);
        return map;
    }

    /**
     * 支付异步通知时校验 sign
     *
     * @param params     参数
     * @param partnerKey 支付密钥
     * @return {boolean}
     */
    public static boolean verifyNotify(Map<String, String> params, String partnerKey) {
        String sign = params.get(FIELD_SIGN);
        String localSign = createSign(params, partnerKey, Algorithm.MD5);
        return sign.equals(localSign);
    }

    /**
     * 支付异步通知时校验 sign
     *
     * @param params     参数
     * @param partnerKey 支付密钥
     * @param algorithm  签名类型
     * @param signKey    签名字符
     * @return {boolean}
     */
    public static boolean verifyNotify(Map<String, String> params, String partnerKey, Algorithm algorithm, String signKey) {
        if (StringKit.isEmpty(signKey)) {
            signKey = FIELD_SIGN;
        }
        String sign = params.get(signKey);
        String localSign = createSign(params, partnerKey, algorithm, signKey);
        return sign.equals(localSign);
    }

    /**
     * 支付异步通知时校验 sign
     *
     * @param params     参数
     * @param partnerKey 支付密钥
     * @param signKey    签名字符
     * @return {boolean}
     */
    public static boolean verifyNotify(Map<String, String> params, String partnerKey, String signKey) {
        return verifyNotify(params, partnerKey, Algorithm.MD5, signKey);
    }

    /**
     * 支付异步通知时校验 sign
     *
     * @param params     参数
     * @param partnerKey 支付密钥
     * @param algorithm  {@link Algorithm}
     * @return {@link Boolean} 验证签名结果
     */
    public static boolean verifyNotify(Map<String, String> params, String partnerKey, Algorithm algorithm) {
        return verifyNotify(params, partnerKey, algorithm, null);
    }

    /**
     * 生成签名
     *
     * @param params     需要签名的参数
     * @param partnerKey 密钥
     * @param algorithm  签名类型
     * @return 签名后的数据
     */
    public static String createSign(Map<String, String> params, String partnerKey, Algorithm algorithm) {
        return createSign(params, partnerKey, algorithm, null);
    }


    /**
     * 生成签名
     *
     * @param params     需要签名的参数
     * @param partnerKey 密钥
     * @param algorithm  签名类型
     * @param signKey    签名字符
     * @return 签名后的数据
     */
    public static String createSign(Map<String, String> params, String partnerKey, Algorithm algorithm, String signKey) {
        if (algorithm == null) {
            algorithm = Algorithm.MD5;
        }
        if (StringKit.isEmpty(signKey)) {
            signKey = FIELD_SIGN;
        }
        // 生成签名前先去除sign
        params.remove(signKey);
        String tempStr = Builder.createLinkString(params);
        String stringSignTemp = tempStr + "&key=" + partnerKey;
        if (algorithm == Algorithm.MD5) {
            return org.miaixz.bus.crypto.Builder.md5(stringSignTemp).toUpperCase();
        } else {
            return org.miaixz.bus.crypto.Builder.hmac(Algorithm.HMACSHA256, partnerKey.toUpperCase()).digestHex(stringSignTemp);
        }
    }

    /**
     * 生成签名
     *
     * @param params 需要签名的参数
     * @param secret 企业微信支付应用secret
     * @return 签名后的数据
     */
    public static String createSign(Map<String, String> params, String secret) {
        // 生成签名前先去除sign
        params.remove(FIELD_SIGN);
        String tempStr = Builder.createLinkString(params);
        String stringSignTemp = tempStr + "&secret=" + secret;
        return org.miaixz.bus.crypto.Builder.md5(stringSignTemp).toUpperCase();
    }

    /**
     * 构建签名
     *
     * @param params     需要签名的参数
     * @param partnerKey 密钥
     * @param algorithm  签名类型
     * @return 签名后的 Map
     */
    public static Map<String, String> buildSign(Map<String, String> params, String partnerKey, Algorithm algorithm) {
        return buildSign(params, partnerKey, algorithm, true);
    }

    /**
     * 构建签名
     *
     * @param params       需要签名的参数
     * @param partnerKey   密钥
     * @param algorithm    签名类型
     * @param haveSignType 签名是否包含 sign_type 字段
     * @return 签名后的 Map
     */
    public static Map<String, String> buildSign(Map<String, String> params, String partnerKey, Algorithm algorithm, boolean haveSignType) {
        return buildSign(params, partnerKey, algorithm, null, null, haveSignType);
    }

    /**
     * 构建签名
     *
     * @param params       需要签名的参数
     * @param partnerKey   密钥
     * @param algorithm    签名类型
     * @param signKey      签名字符串
     * @param signTypeKey  签名类型字符串
     * @param haveSignType 签名是否包含签名类型字符串
     * @return 签名后的 Map
     */
    public static Map<String, String> buildSign(Map<String, String> params, String partnerKey, Algorithm algorithm, String signKey, String signTypeKey, boolean haveSignType) {
        if (StringKit.isEmpty(signKey)) {
            signKey = FIELD_SIGN;
        }
        if (haveSignType) {
            if (StringKit.isEmpty(signTypeKey)) {
                signTypeKey = FIELD_SIGN_TYPE;
            }
            params.put(signTypeKey, algorithm.getValue());
        }
        String sign = createSign(params, partnerKey, algorithm);
        params.put(signKey, sign);
        return params;
    }

    public static StringBuffer forEachMap(Map<String, String> params, String prefix, String suffix) {
        return Builder.forEachMap(params, prefix, suffix);
    }

    /**
     * <p>生成二维码链接</p>
     * <p>原生支付接口模式一(扫码模式一)</p>
     *
     * @param sign      签名
     * @param appId     公众账号ID
     * @param mchId     商户号
     * @param productId 商品ID
     * @param timeStamp 时间戳
     * @param nonceStr  随机字符串
     * @return {String}
     */
    public static String bizPayUrl(String sign, String appId, String mchId, String productId, String timeStamp, String nonceStr) {
        String rules = "weixin://wxpay/bizpayurl?sign=Temp&appid=Temp&mch_id=Temp&product_id=Temp&time_stamp=Temp&nonce_str=Temp";
        return replace(rules, "Temp", sign, appId, mchId, productId, timeStamp, nonceStr);
    }

    /**
     * <p>生成二维码链接</p>
     * <p>原生支付接口模式一(扫码模式一)</p>
     *
     * @param partnerKey 密钥
     * @param appId      公众账号ID
     * @param mchId      商户号
     * @param productId  商品ID
     * @param timeStamp  时间戳
     * @param nonceStr   随机字符串
     * @param algorithm  签名类型
     * @return {String}
     */
    public static String bizPayUrl(String partnerKey, String appId, String mchId, String productId, String timeStamp, String nonceStr, Algorithm algorithm) {
        HashMap<String, String> map = new HashMap<>(5);
        map.put("appid", appId);
        map.put("mch_id", mchId);
        map.put("time_stamp", StringKit.isEmpty(timeStamp) ? Long.toString(System.currentTimeMillis() / 1000) : timeStamp);
        map.put("nonce_str", StringKit.isEmpty(nonceStr) ? String.valueOf(DateKit.current()) : nonceStr);
        map.put("product_id", productId);
        return bizPayUrl(createSign(map, partnerKey, algorithm), appId, mchId, productId, timeStamp, nonceStr);
    }

    /**
     * <p>生成二维码链接</p>
     * <p>原生支付接口模式一(扫码模式一)</p>
     *
     * @param partnerKey 密钥
     * @param appId      公众账号ID
     * @param mchId      商户号
     * @param productId  商品ID
     * @return {String}
     */
    public static String bizPayUrl(String partnerKey, String appId, String mchId, String productId) {
        String timeStamp = Long.toString(System.currentTimeMillis() / 1000);
        String nonceStr = String.valueOf(DateKit.current());
        HashMap<String, String> map = new HashMap<>(5);
        map.put("appid", appId);
        map.put("mch_id", mchId);
        map.put("time_stamp", timeStamp);
        map.put("nonce_str", nonceStr);
        map.put("product_id", productId);
        return bizPayUrl(createSign(map, partnerKey, null), appId, mchId, productId, timeStamp, nonceStr);
    }


    /**
     * 替换url中的参数
     *
     * @param str   原始字符串
     * @param regex 表达式
     * @param args  替换字符串
     * @return {String}
     */
    public static String replace(String str, String regex, String... args) {
        for (String arg : args) {
            str = str.replaceFirst(regex, arg);
        }
        return str;
    }

    /**
     * 判断接口返回的 code
     *
     * @param codeValue code 值
     * @return 是否是 SUCCESS
     */
    public static boolean codeIsOk(String codeValue) {
        return StringKit.isNotEmpty(codeValue) && "SUCCESS".equals(codeValue);
    }

    /**
     * <p>公众号支付-预付订单再次签名</p>
     * <p>注意此处签名方式需与统一下单的签名类型一致</p>
     *
     * @param prepayId   预付订单号
     * @param appId      应用编号
     * @param partnerKey API Key
     * @param algorithm  签名方式
     * @return 再次签名后的 Map
     */
    public static Map<String, String> prepayIdCreateSign(String prepayId, String appId, String partnerKey, Algorithm algorithm) {
        Map<String, String> packageParams = new HashMap<>(6);
        packageParams.put("appId", appId);
        packageParams.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        packageParams.put("nonceStr", String.valueOf(System.currentTimeMillis()));
        packageParams.put("package", "prepay_id=" + prepayId);
        if (algorithm == null) {
            algorithm = Algorithm.MD5;
        }
        packageParams.put("algorithm", algorithm.getValue());
        String packageSign = WechatPayBuilder.createSign(packageParams, partnerKey, algorithm);
        packageParams.put("paySign", packageSign);
        return packageParams;
    }

    /**
     * JS 调起支付签名
     *
     * @param appId    应用编号
     * @param prepayId 预付订单号
     * @param keyPath  key.pem 证书路径
     * @return 唤起支付需要的参数
     * @throws Exception 错误信息
     */
    public static Map<String, String> jsApiCreateSign(String appId, String prepayId, String keyPath) throws Exception {
        return jsApiCreateSign(appId, prepayId, Builder.getPrivateKey(keyPath, AuthType.RSA.getCode()));
    }

    /**
     * JS 调起支付签名
     *
     * @param appId      应用编号
     * @param prepayId   预付订单号
     * @param privateKey 商户私钥
     * @return 唤起支付需要的参数
     * @throws Exception 错误信息
     */
    public static Map<String, String> jsApiCreateSign(String appId, String prepayId, PrivateKey privateKey) throws Exception {
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = String.valueOf(System.currentTimeMillis());
        String packageStr = "prepay_id=" + prepayId;
        Map<String, String> packageParams = new HashMap<>(6);
        packageParams.put("appId", appId);
        packageParams.put("timeStamp", timeStamp);
        packageParams.put("nonceStr", nonceStr);
        packageParams.put("package", packageStr);
        packageParams.put("algorithm", Algorithm.RSA.toString());
        ArrayList<String> list = new ArrayList<>();
        list.add(appId);
        list.add(timeStamp);
        list.add(nonceStr);
        list.add(packageStr);
        String packageSign = Builder.createSign(
                Builder.buildSignMessage(list),
                privateKey
        );
        packageParams.put("paySign", packageSign);
        return packageParams;
    }

    /**
     * <p>APP 支付-预付订单再次签名</p>
     * <p>注意此处签名方式需与统一下单的签名类型一致</p>
     *
     * @param appId      应用编号
     * @param partnerId  商户号
     * @param prepayId   预付订单号
     * @param partnerKey API Key
     * @param algorithm  签名方式
     * @return 再次签名后的 Map
     */
    public static Map<String, String> appPrepayIdCreateSign(String appId, String partnerId, String prepayId, String partnerKey, Algorithm algorithm) {
        Map<String, String> packageParams = new HashMap<>(8);
        packageParams.put("appid", appId);
        packageParams.put("partnerid", partnerId);
        packageParams.put("prepayid", prepayId);
        packageParams.put("package", "Sign=WXPay");
        packageParams.put("noncestr", String.valueOf(System.currentTimeMillis()));
        packageParams.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        if (algorithm == null) {
            algorithm = Algorithm.MD5;
        }
        String packageSign = createSign(packageParams, partnerKey, algorithm);
        packageParams.put("sign", packageSign);
        return packageParams;
    }

    /**
     * App 调起支付签名
     *
     * @param appId     应用编号
     * @param partnerId 商户编号
     * @param prepayId  预付订单号
     * @param keyPath   key.pem 证书路径
     * @return 唤起支付需要的参数
     * @throws Exception 错误信息
     */
    public static Map<String, String> appCreateSign(String appId, String partnerId, String prepayId, String keyPath) throws Exception {
        return appCreateSign(appId, partnerId, prepayId, Builder.getPrivateKey(keyPath, AuthType.RSA.getCode()));
    }

    /**
     * App 调起支付签名
     *
     * @param appId      应用编号
     * @param partnerId  商户编号
     * @param prepayId   预付订单号
     * @param privateKey 商户私钥
     * @return 唤起支付需要的参数
     * @throws Exception 错误信息
     */
    public static Map<String, String> appCreateSign(String appId, String partnerId, String prepayId, PrivateKey privateKey) throws Exception {
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = String.valueOf(System.currentTimeMillis());
        Map<String, String> packageParams = new HashMap<>(8);
        packageParams.put("appid", appId);
        packageParams.put("partnerid", partnerId);
        packageParams.put("prepayid", prepayId);
        packageParams.put("package", "Sign=WXPay");
        packageParams.put("timestamp", timeStamp);
        packageParams.put("noncestr", nonceStr);
        packageParams.put("algorithm", Algorithm.RSA.toString());
        ArrayList<String> list = new ArrayList<>();
        list.add(appId);
        list.add(timeStamp);
        list.add(nonceStr);
        list.add(prepayId);
        String packageSign = Builder.createSign(
                Builder.buildSignMessage(list),
                privateKey
        );
        packageParams.put("sign", packageSign);
        return packageParams;
    }

    /**
     * <p>小程序-预付订单再次签名</p>
     * <p>注意此处签名方式需与统一下单的签名类型一致</p>
     *
     * @param appId      应用编号
     * @param prepayId   预付订单号
     * @param partnerKey API Key
     * @param algorithm  签名方式
     * @return 再次签名后的 Map
     */
    public static Map<String, String> miniAppPrepayIdCreateSign(String appId, String prepayId, String partnerKey, Algorithm algorithm) {
        Map<String, String> packageParams = new HashMap<>(6);
        packageParams.put("appId", appId);
        packageParams.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        packageParams.put("nonceStr", String.valueOf(System.currentTimeMillis()));
        packageParams.put("package", "prepay_id=" + prepayId);
        if (algorithm == null) {
            algorithm = Algorithm.MD5;
        }
        packageParams.put("algorithm", algorithm.getValue());
        String packageSign = createSign(packageParams, partnerKey, algorithm);
        packageParams.put("paySign", packageSign);
        return packageParams;
    }

    /**
     * 构建 v3 接口所需的 Authorization
     *
     * @param method    请求方式
     * @param urlSuffix 可通过 WxApiType 来获取，URL挂载参数需要自行拼接
     * @param mchId     商户Id
     * @param serialNo  商户 API 证书序列号
     * @param keyPath   key.pem 证书路径
     * @param body      接口请求参数
     * @param nonceStr  随机字符库
     * @param timestamp 时间戳
     * @param authType  认证类型
     * @return {@link String} 返回 v3 所需的 Authorization
     * @throws Exception 异常信息
     */
    public static String buildAuthorization(String method, String urlSuffix, String mchId,
                                            String serialNo, String keyPath, String body, String nonceStr,
                                            long timestamp, String authType) throws Exception {
        // 构建签名参数
        String buildSignMessage = Builder.buildSignMessage(method, urlSuffix, timestamp, nonceStr, body);
        String signature = Builder.createSign(buildSignMessage, keyPath, authType);
        // 根据平台规则生成请求头 authorization
        return Builder.getAuthorization(mchId, serialNo, nonceStr, String.valueOf(timestamp), signature, authType);
    }

    /**
     * 构建 v3 接口所需的 Authorization
     *
     * @param method     请求方式
     * @param urlSuffix  可通过 WxApiType 来获取，URL挂载参数需要自行拼接
     * @param mchId      商户Id
     * @param serialNo   商户 API 证书序列号
     * @param privateKey 商户私钥
     * @param body       接口请求参数
     * @param nonceStr   随机字符库
     * @param timestamp  时间戳
     * @param authType   认证类型
     * @return {@link String} 返回 v3 所需的 Authorization
     * @throws Exception 异常信息
     */
    public static String buildAuthorization(String method, String urlSuffix, String mchId,
                                            String serialNo, PrivateKey privateKey, String body, String nonceStr,
                                            long timestamp, String authType) throws Exception {
        // 构建签名参数
        String buildSignMessage = Builder.buildSignMessage(method, urlSuffix, timestamp, nonceStr, body);
        String signature = Builder.createSign(buildSignMessage, privateKey);
        // 根据平台规则生成请求头 authorization
        return Builder.getAuthorization(mchId, serialNo, nonceStr, String.valueOf(timestamp), signature, authType);
    }

    /**
     * 构建 v3 接口所需的 Authorization
     *
     * @param method    请求方式
     * @param urlSuffix 可通过 WxApiType 来获取，URL挂载参数需要自行拼接
     * @param mchId     商户Id
     * @param serialNo  商户 API 证书序列号
     * @param keyPath   key.pem 证书路径
     * @param body      接口请求参数
     * @return {@link String} 返回 v3 所需的 Authorization
     * @throws Exception 异常信息
     */
    public static String buildAuthorization(String method, String urlSuffix, String mchId,
                                            String serialNo, String keyPath, String body) throws Exception {
        return buildAuthorization(method, urlSuffix, mchId, serialNo, keyPath, body, String.valueOf(DateKit.current()), DateKit.current() / 1000, AuthType.RSA.getCode());
    }

    /**
     * 构建 v3 接口所需的 Authorization
     *
     * @param method     请求方式
     * @param urlSuffix  可通过 WxApiType 来获取，URL挂载参数需要自行拼接
     * @param mchId      商户Id
     * @param serialNo   商户 API 证书序列号
     * @param privateKey key.pem 证书路径
     * @param body       接口请求参数
     * @return {@link String} 返回 v3 所需的 Authorization
     * @throws Exception 异常信息
     */
    public static String buildAuthorization(String method, String urlSuffix, String mchId,
                                            String serialNo, PrivateKey privateKey, String body) throws Exception {
        return buildAuthorization(method, urlSuffix, mchId, serialNo, privateKey, body, String.valueOf(DateKit.current()), DateKit.current() / 1000, AuthType.RSA.getCode());
    }

    /**
     * 验证签名
     *
     * @param response 接口请求返回的 {@link Message}
     * @param certPath 平台证书路径
     * @return 签名结果
     * @throws Exception 异常信息
     */
    public static boolean verifySignature(Message response, String certPath) throws Exception {
        String timestamp = response.getHeader("Wechatpay-Timestamp");
        String nonceStr = response.getHeader("Wechatpay-Nonce");
        String signature = response.getHeader("Wechatpay-Signature");
        String signatureType = response.getHeader("Wechatpay-Signature-Type");
        String body = response.getBody();
        System.out.println("timestamp:" + timestamp);
        System.out.println("nonceStr:" + nonceStr);
        System.out.println("signature:" + signature);
        System.out.println("signatureType:" + signatureType);
        System.out.println("body:" + body);
        return verifySignature(signatureType, signature, body, nonceStr, timestamp, Builder.getCertFileInputStream(certPath));
    }

    /**
     * 验证签名
     *
     * @param response        接口请求返回的 {@link Message}
     * @param certInputStream 平台证书
     * @return 签名结果
     * @throws Exception 异常信息
     */
    public static boolean verifySignature(Message response, InputStream certInputStream) throws Exception {
        String timestamp = response.getHeader("Wechatpay-Timestamp");
        String nonceStr = response.getHeader("Wechatpay-Nonce");
        String signature = response.getHeader("Wechatpay-Signature");
        String signatureType = response.getHeader("Wechatpay-Signature-Type");
        String body = response.getBody();
        return verifySignature(signatureType, signature, body, nonceStr, timestamp, certInputStream);
    }

    /**
     * 验证签名
     *
     * @param signature 待验证的签名
     * @param body      应答主体
     * @param nonce     随机串
     * @param timestamp 时间戳
     * @param publicKey 微信支付平台公钥
     * @return 签名结果
     * @throws Exception 异常信息
     */
    public static boolean verifySignature(String signature, String body, String nonce, String timestamp, String publicKey) throws Exception {
        String buildSignMessage = Builder.buildSignMessage(timestamp, nonce, body);
        return checkByPublicKey(buildSignMessage, signature, publicKey);
    }

    /**
     * 验证签名
     *
     * @param signature 待验证的签名
     * @param body      应答主体
     * @param nonce     随机串
     * @param timestamp 时间戳
     * @param publicKey {@link PublicKey} 微信支付平台公钥
     * @return 签名结果
     * @throws Exception 异常信息
     */
    public static boolean verifySignature(String signature, String body, String nonce, String timestamp, PublicKey publicKey) throws Exception {
        String buildSignMessage = Builder.buildSignMessage(timestamp, nonce, body);
        return checkByPublicKey(buildSignMessage, signature, publicKey);
    }

    /**
     * 验证签名
     *
     * @param signatureType   签名类型
     * @param signature       待验证的签名
     * @param body            应答主体
     * @param nonce           随机串
     * @param timestamp       时间戳
     * @param certInputStream 微信支付平台证书输入流
     * @return 签名结果
     * @throws Exception 异常信息
     */
    public static boolean verifySignature(String signatureType, String signature, String body, String nonce, String timestamp, InputStream certInputStream) throws Exception {
        String buildSignMessage = Builder.buildSignMessage(timestamp, nonce, body);
        // 获取证书
        X509Certificate certificate = Builder.getCertificate(certInputStream);
        PublicKey publicKey = certificate.getPublicKey();
        if (StringKit.equals(signatureType, AuthType.SM2.getCode())) {
            return Builder.sm4Verify(publicKey, buildSignMessage, signature);
        }
        return checkByPublicKey(buildSignMessage, signature, publicKey);
    }

    /**
     * v3 支付异步通知验证签名
     *
     * @param serialNo        证书序列号
     * @param body            异步通知密文
     * @param signature       签名
     * @param nonce           随机字符串
     * @param timestamp       时间戳
     * @param key             api 密钥
     * @param certInputStream 平台证书
     * @return 异步通知明文
     * @throws Exception 异常信息
     */
    public static String verifyNotify(String serialNo, String body, String signature, String nonce,
                                      String timestamp, String key, InputStream certInputStream) throws Exception {
        // 获取平台证书序列号
        X509Certificate certificate = Builder.getCertificate(certInputStream);
        String serialNumber = certificate.getSerialNumber().toString(16).toUpperCase();
        // 验证证书序列号
        if (serialNumber.equals(serialNo)) {
            boolean verifySignature = WechatPayBuilder.verifySignature(signature, body, nonce, timestamp,
                    certificate.getPublicKey());
            if (verifySignature) {
                String json = JsonKit.toJsonString(body);
                String resource = JsonKit.getValue(json, "resource");

                String cipherText = JsonKit.getValue(resource, "ciphertext");
                String nonceStr = JsonKit.getValue(resource, "nonce");
                String associatedData = JsonKit.getValue(resource, "associated_data");

                // 密文解密
                return decryptToString(key.getBytes(StandardCharsets.UTF_8),
                        associatedData.getBytes(StandardCharsets.UTF_8),
                        nonceStr.getBytes(StandardCharsets.UTF_8),
                        cipherText
                );
            } else {
                throw new Exception("签名错误");
            }
        } else {
            throw new Exception("证书序列号错误");
        }
    }

    /**
     * v3 支付异步通知验证签名
     *
     * @param serialNo  证书序列号
     * @param body      异步通知密文
     * @param signature 签名
     * @param nonce     随机字符串
     * @param timestamp 时间戳
     * @param key       api 密钥
     * @param certPath  平台证书路径
     * @return 异步通知明文
     * @throws Exception 异常信息
     */
    public static String verifyNotify(String serialNo, String body, String signature, String nonce,
                                      String timestamp, String key, String certPath) throws Exception {
        InputStream inputStream = Builder.getCertFileInputStream(certPath);
        return verifyNotify(serialNo, body, signature, nonce, timestamp, key, inputStream);
    }

    /**
     * 生成公钥和私钥
     *
     * @throws Exception 异常信息
     */
    public static Map<String, String> getKeys() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(Algorithm.RSA.getValue());
        keyPairGen.initialize(1024);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        String publicKey = Base64.encode(keyPair.getPublic().getEncoded());
        String privateKey = Base64.encode(keyPair.getPrivate().getEncoded());

        Map<String, String> map = new HashMap<>(2);
        map.put("publicKey", publicKey);
        map.put("privateKey", privateKey);

        System.out.println("公钥\r\n" + publicKey);
        System.out.println("私钥\r\n" + privateKey);
        return map;
    }

    /**
     * 使用模和指数生成RSA公钥
     * 注意：【此代码用了默认补位方式，为RSA/None/PKCS1Padding，不同JDK默认的补位方式可能不同，如Android默认是RSA
     * /None/NoPadding】
     *
     * @param modulus  模
     * @param exponent 公钥指数
     * @return {@link RSAPublicKey}
     */
    public static RSAPublicKey getPublicKey(String modulus, String exponent) {
        try {
            BigInteger b1 = new BigInteger(modulus);
            BigInteger b2 = new BigInteger(exponent);
            KeyFactory keyFactory = KeyFactory.getInstance(Algorithm.RSA.getValue());
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(b1, b2);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 使用模和指数生成RSA私钥
     * 注意：【此代码用了默认补位方式，为RSA/None/PKCS1Padding，不同JDK默认的补位方式可能不同，如Android默认是RSA
     * /None/NoPadding】
     *
     * @param modulus  模
     * @param exponent 指数
     * @return {@link RSAPrivateKey}
     */
    public static RSAPrivateKey getPrivateKey(String modulus, String exponent) {
        try {
            BigInteger b1 = new BigInteger(modulus);
            BigInteger b2 = new BigInteger(exponent);
            KeyFactory keyFactory = KeyFactory.getInstance(Algorithm.RSA.getValue());
            RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(b1, b2);
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 公钥加密
     *
     * @param data      需要加密的数据
     * @param publicKey 公钥
     * @return 加密后的数据
     * @throws Exception 异常信息
     */
    public static String encryptByPublicKey(String data, String publicKey) throws Exception {
        return encryptByPublicKey(data, publicKey, "RSA/ECB/PKCS1Padding");
    }

    /**
     * 公钥加密
     *
     * @param data      需要加密的数据
     * @param publicKey 公钥
     * @return 加密后的数据
     * @throws Exception 异常信息
     */
    public static String encryptByPublicKeyByWx(String data, String publicKey) throws Exception {
        return encryptByPublicKey(data, publicKey, "RSA/ECB/OAEPWITHSHA-1ANDMGF1PADDING");
    }

    /**
     * 公钥加密
     *
     * @param data      需要加密的数据
     * @param publicKey 公钥
     * @param fillMode  填充模式
     * @return 加密后的数据
     * @throws Exception 异常信息
     */
    public static String encryptByPublicKey(String data, String publicKey, String fillMode) throws Exception {
        byte[] dataByte = data.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = Base64.decode(publicKey);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(Algorithm.RSA.getValue());
        Key key = keyFactory.generatePublic(x509KeySpec);
        // 对数据加密
        Cipher cipher = Cipher.getInstance(fillMode);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        int inputLen = dataByte.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(dataByte, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(dataByte, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return StringKit.toString(Base64.encode(encryptedData));
    }

    /**
     * 私钥签名
     *
     * @param data       需要加密的数据
     * @param privateKey 私钥
     * @return 加密后的数据
     * @throws Exception 异常信息
     */
    public static String encryptByPrivateKey(String data, String privateKey) throws Exception {
        PKCS8EncodedKeySpec priPkcs8 = new PKCS8EncodedKeySpec(Base64.decode(privateKey));
        KeyFactory keyFactory = KeyFactory.getInstance(Algorithm.RSA.getValue());
        PrivateKey priKey = keyFactory.generatePrivate(priPkcs8);
        Signature signature = Signature.getInstance("SHA256WithRSA");

        signature.initSign(priKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] signed = signature.sign();
        return StringKit.toString(Base64.encode(signed));
    }

    /**
     * 私钥签名
     *
     * @param data       需要加密的数据
     * @param privateKey 私钥
     * @return 加密后的数据
     * @throws Exception 异常信息
     */
    public static String encryptByPrivateKey(String data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256WithRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] signed = signature.sign();
        return StringKit.toString(Base64.encode(signed));
    }

    /**
     * 公钥验证签名
     *
     * @param data      需要加密的数据
     * @param sign      签名
     * @param publicKey 公钥
     * @return 验证结果
     * @throws Exception 异常信息
     */
    public static boolean checkByPublicKey(String data, String sign, String publicKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(Algorithm.RSA.getValue());
        byte[] encodedKey = Base64.decode(publicKey);
        PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
        Signature signature = Signature.getInstance("SHA256WithRSA");
        signature.initVerify(pubKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        return signature.verify(Base64.decode(sign.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * 公钥验证签名
     *
     * @param data      需要加密的数据
     * @param sign      签名
     * @param publicKey 公钥
     * @return 验证结果
     * @throws Exception 异常信息
     */
    public static boolean checkByPublicKey(String data, String sign, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256WithRSA");
        signature.initVerify(publicKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        return signature.verify(Base64.decode(sign.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * 私钥解密
     *
     * @param data       需要解密的数据
     * @param privateKey 私钥
     * @return 解密后的数据
     * @throws Exception 异常信息
     */
    public static String decryptByPrivateKey(String data, String privateKey) throws Exception {
        return decryptByPrivateKey(data, privateKey, "RSA/ECB/PKCS1Padding");
    }

    /**
     * 私钥解密
     *
     * @param data       需要解密的数据
     * @param privateKey 私钥
     * @return 解密后的数据
     * @throws Exception 异常信息
     */
    public static String decryptByPrivateKeyByWx(String data, String privateKey) throws Exception {
        return decryptByPrivateKey(data, privateKey, "RSA/ECB/OAEPWITHSHA-1ANDMGF1PADDING");
    }

    /**
     * 私钥解密
     *
     * @param data       需要解密的数据
     * @param privateKey 私钥
     * @param fillMode   填充模式
     * @return 解密后的数据
     * @throws Exception 异常信息
     */
    public static String decryptByPrivateKey(String data, String privateKey, String fillMode) throws Exception {
        byte[] encryptedData = Base64.decode(data);
        byte[] keyBytes = Base64.decode(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(Algorithm.RSA.getValue());
        Key key = keyFactory.generatePrivate(pkcs8KeySpec);
        Cipher cipher = Cipher.getInstance(fillMode);

        cipher.init(Cipher.DECRYPT_MODE, key);
        int inputLen = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > Normal._128) {
                cache = cipher.doFinal(encryptedData, offSet, Normal._128);
            } else {
                cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * Normal._128;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return new String(decryptedData);
    }

    /**
     * 从字符串中加载公钥
     *
     * @param publicKey 公钥数据字符串
     * @throws Exception 异常信息
     */
    public static PublicKey loadPublicKey(String publicKey) throws Exception {
        try {
            byte[] buffer = Base64.decode(publicKey);
            KeyFactory keyFactory = KeyFactory.getInstance(Algorithm.RSA.getValue());
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new Exception("公钥非法");
        } catch (NullPointerException e) {
            throw new Exception("公钥数据为空");
        }
    }

    /**
     * 从字符串中加载私钥
     * 加载时使用的是PKCS8EncodedKeySpec（PKCS#8编码的Key指令）
     *
     * @param privateKey 私钥
     * @return {@link PrivateKey}
     * @throws Exception 异常信息
     */
    public static PrivateKey loadPrivateKey(String privateKey) throws Exception {
        try {
            byte[] buffer = Base64.decode(privateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
            KeyFactory keyFactory = KeyFactory.getInstance(Algorithm.RSA.getValue());
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new Exception("私钥非法");
        } catch (NullPointerException e) {
            throw new Exception("私钥数据为空");
        }
    }

    /**
     * 证书和回调报文解密
     *
     * @param associatedData associated_data
     * @param nonce          nonce
     * @param cipherText     ciphertext
     * @return {String} 平台证书明文
     * @throws GeneralSecurityException 异常
     */
    public static String decryptToString(byte[] key, byte[] associatedData, byte[] nonce, String cipherText) throws GeneralSecurityException {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            SecretKeySpec aesKey = new SecretKeySpec(key, "AES");
            GCMParameterSpec spec = new GCMParameterSpec(128, nonce);

            cipher.init(Cipher.DECRYPT_MODE, aesKey, spec);
            cipher.updateAAD(associatedData);

            return new String(cipher.doFinal(Base64.decode(cipherText)), StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IllegalStateException(e);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
