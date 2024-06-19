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

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.XmlKit;
import org.miaixz.bus.pay.*;
import org.miaixz.bus.pay.magic.Material;
import org.miaixz.bus.pay.magic.Message;
import org.miaixz.bus.pay.metric.AbstractProvider;
import org.miaixz.bus.pay.metric.unionpay.api.UnionPayApi;
import org.miaixz.bus.pay.metric.wechat.api.v2.*;

import java.io.File;
import java.io.InputStream;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付相关接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WechatPayProvider extends AbstractProvider<Material, Context> {

    public WechatPayProvider(Context context) {
        this(context, null);
    }

    public WechatPayProvider(Context context, Complex complex) {
        this(context, complex, null);
    }

    public WechatPayProvider(Context context, Complex complex, ExtendCache cache) {
        super(context, complex, cache);
    }

    /**
     * 获取接口请求的 URL
     *
     * @return {@link String} 返回完整的接口请求URL
     */
    public String getUrl() {
        return getUrl(this.complex);
    }

    /**
     * 获取接口请求的 URL
     *
     * @param complex {@link UnionPayApi} 支付 API 接口枚举
     * @return {@link String} 返回完整的接口请求URL
     */
    public String getUrl(Complex complex) {
        return (complex.isSandbox() ? Registry.UNIONPAY.sandbox() : Registry.UNIONPAY.service()).concat(complex.method());
    }

    /**
     * 发起请求
     *
     * @param complex 接口 URL
     * @param params  接口请求参数
     * @return {@link String} 请求返回的结果
     */
    public String execution(Complex complex, Map<String, String> params) {
        return doPost(getUrl(complex), params);
    }

    /**
     * 发起请求
     *
     * @param complex 接口 URL
     *                通过 {@link #getUrl()}
     *                或者 {@link #getUrl(Complex)} 来获取
     * @param params  接口请求参数
     * @return {@link String} 请求返回的结果
     */
    public String executionByGet(Complex complex, Map<String, String> params) {
        return doGet(getUrl(complex), params);
    }

    /**
     * 发起请求
     *
     * @param complex  接口 URL
     * @param params   接口请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String execution(Complex complex, Map<String, String> params, String certPath, String certPass) {
        return doPostSsl(getUrl(complex), params, certPath, certPass);
    }

    /**
     * 发起请求
     *
     * @param complex  接口 URL
     * @param params   接口请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @param protocol 协议
     * @return {@link String} 请求返回的结果
     */
    public String executionByProtocol(Complex complex, Map<String, String> params, String certPath, String certPass, String protocol) {
        return doPostSslByProtocol(getUrl(complex), params, certPath, certPass, protocol);
    }

    /**
     * 发起请求
     *
     * @param complex  接口 URL
     *                 通过 {@link #getUrl()}
     *                 或者 {@link #getUrl(Complex)} 来获取
     * @param params   接口请求参数
     * @param certPath 证书文件路径
     * @return {@link String} 请求返回的结果
     */
    public String execution(Complex complex, Map<String, String> params, String certPath) {
        return doPostSsl(getUrl(complex), params, certPath);
    }

    /**
     * 发起请求
     *
     * @param complex  接口 URL
     * @param params   接口请求参数
     * @param certPath 证书文件路径
     * @param protocol 协议
     * @return {@link String} 请求返回的结果
     */
    public String executionByProtocol(Complex complex, Map<String, String> params, String certPath, String protocol) {
        return doPostSslByProtocol(getUrl(complex), params, certPath, protocol);
    }

    /**
     * 发起请求
     *
     * @param complex  接口 URL
     * @param params   接口请求参数
     * @param certFile 证书文件输入流
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String execution(Complex complex, Map<String, String> params, InputStream certFile, String certPass) {
        return doPostSsl(getUrl(complex), params, certFile, certPass);
    }

    /**
     * 发起请求
     *
     * @param complex  接口 URL
     * @param params   接口请求参数
     * @param certFile 证书文件输入流
     * @param certPass 证书密码
     * @param protocol 协议
     * @return {@link String} 请求返回的结果
     */
    public String executionByProtocol(Complex complex, Map<String, String> params, InputStream certFile, String certPass, String protocol) {
        return doPostSslByProtocol(getUrl(complex), params, certFile, certPass, protocol);
    }

    public String execution(Complex complex, Map<String, String> params,
                            String certPath, String certPass, String filePath) {
        return doUploadSsl(getUrl(complex), params, certPath, certPass, filePath);
    }

    public String executionByProtocol(Complex complex, Map<String, String> params, String certPath, String certPass, String filePath, String protocol) {
        return doUploadSslByProtocol(getUrl(complex), params, certPath, certPass, filePath, protocol);
    }

    /**
     * 发起请求
     *
     * @param complex  接口 URL
     * @param params   接口请求参数
     * @param certFile 证书文件输入流
     * @return {@link String} 请求返回的结果
     */
    public String execution(Complex complex, Map<String, String> params, InputStream certFile) {
        return doPostSsl(getUrl(complex), params, certFile);
    }

    /**
     * 发起请求
     *
     * @param complex  接口 URL
     * @param params   接口请求参数
     * @param certFile 证书文件输入流
     * @param protocol 协议
     * @return {@link String} 请求返回的结果
     */
    public String executionByProtocol(Complex complex, Map<String, String> params, InputStream certFile, String protocol) {
        return doPostSslByProtocol(getUrl(complex), params, certFile, protocol);
    }

    /**
     * V3 接口统一执行入口
     *
     * @param method       接口方法
     * @param prefix       可通过 {@link Registry}来获取
     * @param suffix       可通过 {@link Complex} 来获取，URL挂载参数需要自行拼接
     * @param mchId        商户Id
     * @param serialNo     商户 API 证书序列号
     * @param platSerialNo 平台序列号，接口中包含敏感信息时必传
     * @param keyPath      apiclient_key.pem 证书路径
     * @param body         接口请求参数
     * @param nonceStr     随机字符库
     * @param timestamp    时间戳
     * @param authType     认证类型
     * @param file         文件
     * @return {@link Message} 请求返回的结果
     * @throws Exception 接口执行异常
     */
    public Message v3(String method,
                      String prefix,
                      String suffix,
                      String mchId,
                      String serialNo,
                      String platSerialNo,
                      String keyPath,
                      String body,
                      String nonceStr,
                      long timestamp,
                      String authType,
                      File file) throws Exception {
        String authorization = WechatPayBuilder.buildAuthorization(method, suffix, mchId, serialNo,
                keyPath, body, nonceStr, timestamp, authType);

        if (StringKit.isEmpty(platSerialNo)) {
            platSerialNo = serialNo;
        }

        if (Http.GET.equals(method)) {
            return get(prefix.concat(suffix), authorization, platSerialNo, null);
        } else if (Http.POST.equals(method)) {
            return post(prefix.concat(suffix), authorization, platSerialNo, body);
        } else if (Http.DELETE.equals(method)) {
            return delete(prefix.concat(suffix), authorization, platSerialNo, body);
        } else if (Http.PATCH.equals(method)) {
            return patch(prefix.concat(suffix), authorization, platSerialNo, body);
        } else if (Http.PUT.equals(method)) {
            return put(prefix.concat(suffix), authorization, platSerialNo, body);
        }
        return upload(prefix.concat(suffix), authorization, platSerialNo, body, file);
    }

    /**
     * V3 接口统一执行入口
     *
     * @param method       接口方法
     * @param prefix       可通过 {@link Registry}来获取
     * @param suffix       可通过 {@link Complex} 来获取，URL挂载参数需要自行拼接
     * @param mchId        商户Id
     * @param serialNo     商户 API 证书序列号
     * @param platSerialNo 平台序列号，接口中包含敏感信息时必传
     * @param privateKey   商户私钥
     * @param body         接口请求参数
     * @param nonceStr     随机字符库
     * @param timestamp    时间戳
     * @param authType     认证类型
     * @param file         文件
     * @return {@link Message} 请求返回的结果
     * @throws Exception 接口执行异常
     */
    public Message v3(String method,
                      String prefix,
                      String suffix,
                      String mchId,
                      String serialNo,
                      String platSerialNo,
                      PrivateKey privateKey,
                      String body,
                      String nonceStr,
                      long timestamp,
                      String authType,
                      File file) throws Exception {
        String authorization = WechatPayBuilder.buildAuthorization(method, suffix, mchId, serialNo,
                privateKey, body, nonceStr, timestamp, authType);

        if (StringKit.isEmpty(platSerialNo)) {
            platSerialNo = serialNo;
        }

        if (Http.GET.equals(method)) {
            return get(prefix.concat(suffix), authorization, platSerialNo, null);
        } else if (Http.POST.equals(method)) {
            return post(prefix.concat(suffix), authorization, platSerialNo, body);
        } else if (Http.DELETE.equals(method)) {
            return delete(prefix.concat(suffix), authorization, platSerialNo, body);
        } else if (Http.PATCH.equals(method)) {
            return patch(prefix.concat(suffix), authorization, platSerialNo, body);
        } else if (Http.PUT.equals(method)) {
            return put(prefix.concat(suffix), authorization, platSerialNo, body);
        }
        return upload(prefix.concat(suffix), authorization, platSerialNo, body, file);
    }

    /**
     * V3 接口统一执行入口
     *
     * @param method       接口方法
     * @param prefix       可通过 {@link Registry}来获取
     * @param suffix       可通过 {@link Complex} 来获取，URL挂载参数需要自行拼接
     * @param mchId        商户Id
     * @param serialNo     商户 API 证书序列号
     * @param platSerialNo 平台序列号
     * @param keyPath      apiclient_key.pem 证书路径
     * @param body         接口请求参数
     * @param authType     认证类型
     * @return {@link Message} 请求返回的结果
     * @throws Exception 接口执行异常
     */
    public Message v3(String method, String prefix, String suffix, String mchId,
                      String serialNo, String platSerialNo, String keyPath, String body, String authType) throws Exception {
        long timestamp = DateKit.current() / 1000;
        String nonceStr = String.valueOf(DateKit.current());
        return v3(method, prefix, suffix, mchId, serialNo, platSerialNo, keyPath, body, nonceStr, timestamp, authType, null);
    }

    /**
     * V3 接口统一执行入口
     *
     * @param method       接口方法
     * @param prefix       可通过 {@link Registry}来获取
     * @param suffix       可通过 {@link Complex} 来获取，URL挂载参数需要自行拼接
     * @param mchId        商户Id
     * @param serialNo     商户 API 证书序列号
     * @param platSerialNo 平台序列号
     * @param keyPath      apiclient_key.pem 证书路径
     * @param body         接口请求参数
     * @return {@link Message} 请求返回的结果
     * @throws Exception 接口执行异常
     */
    public Message v3(String method, String prefix, String suffix, String mchId,
                      String serialNo, String platSerialNo, String keyPath, String body) throws Exception {
        String authType = AuthType.RSA.getCode();
        return v3(method, prefix, suffix, mchId, serialNo, platSerialNo, keyPath, body, authType);
    }

    /**
     * V3 接口统一执行入口
     *
     * @param method       接口方法
     * @param prefix       可通过 {@link Registry}来获取
     * @param suffix       可通过 {@link Complex} 来获取，URL挂载参数需要自行拼接
     * @param mchId        商户Id
     * @param serialNo     商户 API 证书序列号
     * @param platSerialNo 平台序列号
     * @param privateKey   商户私钥
     * @param body         接口请求参数
     * @return {@link Message} 请求返回的结果
     * @throws Exception 接口执行异常
     */
    public Message v3(String method, String prefix, String suffix, String mchId,
                      String serialNo, String platSerialNo, PrivateKey privateKey, String body) throws Exception {
        long timestamp = System.currentTimeMillis() / 1000;
        String authType = AuthType.RSA.getCode();
        String nonceStr = String.valueOf(DateKit.current());
        return v3(method, prefix, suffix, mchId, serialNo, platSerialNo, privateKey, body, nonceStr, timestamp, authType, null);
    }

    /**
     * V3 接口统一执行入口
     *
     * @param method       接口方法
     * @param prefix       可通过 {@link Registry}来获取
     * @param suffix       可通过 {@link Complex} 来获取，URL挂载参数需要自行拼接
     * @param mchId        商户Id
     * @param serialNo     商户 API 证书序列号
     * @param platSerialNo 平台序列号
     * @param keyPath      apiclient_key.pem 证书路径
     * @param params       Get 接口请求参数
     * @return {@link Message} 请求返回的结果
     * @throws Exception 接口执行异常
     */
    public Message v3(String method, String prefix, String suffix,
                      String mchId, String serialNo, String platSerialNo, String keyPath,
                      Map<String, String> params) throws Exception {
        String authType = AuthType.RSA.getCode();
        return v3(method, prefix, suffix, mchId, serialNo, platSerialNo, keyPath, params, authType);
    }

    /**
     * V3 接口统一执行入口
     *
     * @param method       接口方法
     * @param prefix       可通过 {@link Registry}来获取
     * @param suffix       可通过 {@link Complex} 来获取，URL挂载参数需要自行拼接
     * @param mchId        商户Id
     * @param serialNo     商户 API 证书序列号
     * @param platSerialNo 平台序列号
     * @param keyPath      apiclient_key.pem 证书路径
     * @param params       Get 接口请求参数
     * @param authType     {@link AuthType} 授权类型
     * @return {@link Message} 请求返回的结果
     * @throws Exception 接口执行异常
     */
    public Message v3(String method, String prefix, String suffix,
                      String mchId, String serialNo, String platSerialNo, String keyPath,
                      Map<String, String> params, String authType) throws Exception {
        long timestamp = System.currentTimeMillis() / 1000;
        String nonceStr = String.valueOf(DateKit.current());
        if (null != params && !params.keySet().isEmpty()) {
            suffix = suffix.concat("?").concat(Builder.createLinkString(params, true));
        }
        return v3(method, prefix, suffix, mchId, serialNo, platSerialNo, keyPath, "", nonceStr, timestamp, authType, null);
    }

    /**
     * V3 接口统一执行入口
     *
     * @param method       接口方法
     * @param prefix       可通过 {@link Registry}来获取
     * @param suffix       可通过 {@link Complex} 来获取，URL挂载参数需要自行拼接
     * @param mchId        商户Id
     * @param serialNo     商户 API 证书序列号
     * @param platSerialNo 平台序列号
     * @param privateKey   商户私钥
     * @param params       Get 接口请求参数
     * @return {@link Message} 请求返回的结果
     * @throws Exception 接口执行异常
     */
    public Message v3(String method, String prefix, String suffix,
                      String mchId, String serialNo, String platSerialNo, PrivateKey privateKey,
                      Map<String, String> params) throws Exception {
        long timestamp = System.currentTimeMillis() / 1000;
        String authType = AuthType.RSA.getCode();
        String nonceStr = String.valueOf(DateKit.current());
        if (null != params && !params.keySet().isEmpty()) {
            suffix = suffix.concat("?").concat(Builder.createLinkString(params, true));
        }
        return v3(method, prefix, suffix, mchId, serialNo, platSerialNo, privateKey, "", nonceStr, timestamp, authType, null);
    }

    /**
     * V3 接口统一执行入口
     *
     * @param prefix       可通过 {@link Registry}来获取
     * @param suffix       可通过 {@link Complex} 来获取，URL挂载参数需要自行拼接
     * @param mchId        商户Id
     * @param serialNo     商户 API 证书序列号
     * @param platSerialNo 平台序列号
     * @param keyPath      apiclient_key.pem 证书路径
     * @param body         接口请求参数
     * @param file         文件
     * @return {@link Message} 请求返回的结果
     * @throws Exception 接口执行异常
     */
    public Message v3(String prefix, String suffix, String mchId, String serialNo, String platSerialNo, String keyPath, String body, File file) throws Exception {
        long timestamp = System.currentTimeMillis() / 1000;
        String authType = AuthType.RSA.getCode();
        String nonceStr = String.valueOf(DateKit.current());
        return v3(null, prefix, suffix, mchId, serialNo, platSerialNo, keyPath, body, nonceStr, timestamp, authType, file);
    }

    /**
     * V3 接口统一执行入口
     *
     * @param prefix       可通过 {@link Registry}来获取
     * @param suffix       可通过 {@link Complex} 来获取，URL挂载参数需要自行拼接
     * @param mchId        商户Id
     * @param serialNo     商户 API 证书序列号
     * @param platSerialNo 平台序列号
     * @param privateKey   商户私钥
     * @param body         接口请求参数
     * @param file         文件
     * @return {@link Message} 请求返回的结果
     * @throws Exception 接口执行异常
     */
    public Message v3(String prefix, String suffix, String mchId, String serialNo,
                      String platSerialNo, PrivateKey privateKey, String body, File file) throws Exception {
        long timestamp = System.currentTimeMillis() / 1000;
        String authType = AuthType.RSA.getCode();
        String nonceStr = String.valueOf(DateKit.current());
        return v3(null, prefix, suffix, mchId, serialNo, platSerialNo, privateKey, body, nonceStr, timestamp, authType, file);
    }

    /**
     * 获取验签秘钥API
     *
     * @param mchId      商户号
     * @param partnerKey API 密钥
     * @param algorithm  签名方式
     * @return {@link String} 请求返回的结果
     */
    public String getSignKey(String mchId, String partnerKey, Algorithm algorithm) {
        Map<String, String> map = new HashMap<>(3);
        String nonceStr = String.valueOf(DateKit.current());
        map.put("mch_id", mchId);
        map.put("nonce_str", nonceStr);
        map.put("sign", WechatPayBuilder.createSign(map, partnerKey, algorithm));
        return execution(PayApi.GET_SIGN_KEY, map);
    }

    /**
     * 统一下单
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String pushOrder(Map<String, String> params) {
        return execution(PayApi.UNIFIED_ORDER, params);
    }

    /**
     * 订单查询
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String orderQuery(Map<String, String> params) {
        return execution(PayApi.ORDER_QUERY, params);
    }

    /**
     * 关闭订单
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String closeOrder(Map<String, String> params) {
        return execution(PayApi.CLOSE_ORDER, params);
    }

    /**
     * 撤销订单
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String orderReverse(Map<String, String> params, String certPath, String certPass) {
        return execution(PayApi.REVERSE, params, certPath, certPass);
    }

    /**
     * 撤销订单
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String orderReverse(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(PayApi.REVERSE, params, certFile, certPass);
    }

    /**
     * 申请退款
     *
     * @param isSandbox 是否是沙盒环境
     * @param params    请求参数
     * @param certPath  证书文件路径
     * @param certPass  证书密码
     * @return {@link String} 请求返回的结果
     */
    public String orderRefund(boolean isSandbox, Map<String, String> params, String certPath, String certPass) {
        return execution(PayApi.REFUND, params, certPath, certPass);
    }

    /**
     * 申请退款
     *
     * @param isSandbox 是否是沙盒环境
     * @param params    请求参数
     * @param certPath  证书文件路径
     * @param certPass  证书密码
     * @return {@link String} 请求返回的结果
     */
    public String orderRefundByProtocol(boolean isSandbox, Map<String, String> params, String certPath, String certPass, String protocol) {
        return executionByProtocol(PayApi.REFUND, params, certPath, certPass, protocol);
    }

    /**
     * 申请退款
     *
     * @param isSandbox 是否是沙盒环境
     * @param params    请求参数
     * @param certFile  证书文件的 InputStream
     * @param certPass  证书密码
     * @return {@link String} 请求返回的结果
     */
    public String orderRefund(boolean isSandbox, Map<String, String> params, InputStream certFile, String certPass) {
        return execution(PayApi.REFUND, params, certFile, certPass);
    }

    /**
     * 申请退款
     *
     * @param isSandbox 是否是沙盒环境
     * @param params    请求参数
     * @param certFile  证书文件的 InputStream
     * @param certPass  证书密码
     * @param protocol  协议
     * @return {@link String} 请求返回的结果
     */
    public String orderRefundByProtocol(boolean isSandbox, Map<String, String> params, InputStream certFile, String certPass, String protocol) {
        return executionByProtocol(PayApi.REFUND, params, certFile, certPass, protocol);
    }

    /**
     * 查询退款
     *
     * @param isSandbox 是否是沙盒环境
     * @param params    请求参数
     * @return {@link String} 请求返回的结果
     */
    public String orderRefundQuery(boolean isSandbox, Map<String, String> params) {
        return execution(PayApi.REFUND_QUERY, params);
    }

    /**
     * 下载对账单
     *
     * @param isSandbox 是否是沙盒环境
     * @param params    请求参数
     * @return {@link String} 请求返回的结果
     */
    public String downloadBill(boolean isSandbox, Map<String, String> params) {
        return execution(PayApi.DOWNLOAD_BILL, params);
    }

    /**
     * 交易保障
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String orderReport(Map<String, String> params) {
        return execution(PayApi.REPORT, params);
    }

    /**
     * 转换短链接
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String toShortUrl(Map<String, String> params) {
        return execution(PayApi.SHORT_URL, params);
    }

    /**
     * 授权码查询 openId
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String authCodeToOpenid(Map<String, String> params) {
        return execution(PayApi.AUTH_CODE_TO_OPENID, params);
    }

    /**
     * 刷卡支付
     *
     * @param isSandbox 是否是沙盒环境
     * @param params    请求参数
     * @return {@link String} 请求返回的结果
     */
    public String microPay(boolean isSandbox, Map<String, String> params) {
        return execution(PayApi.MICRO_PAY, params);
    }

    /**
     * 企业付款到零钱
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String transfers(Map<String, String> params, String certPath, String certPass) {
        return execution(TransferApi.TRANSFER, params, certPath, certPass);
    }

    /**
     * 企业付款到零钱
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @param protocol 协议
     * @return {@link String} 请求返回的结果
     */
    public String transfersByProtocol(Map<String, String> params, String certPath, String certPass, String protocol) {
        return executionByProtocol(TransferApi.TRANSFER, params, certPath, certPass, protocol);
    }

    /**
     * 企业付款到零钱
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String transfers(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(TransferApi.TRANSFER, params, certFile, certPass);
    }

    /**
     * 企业付款到零钱
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @param protocol 协议
     * @return {@link String} 请求返回的结果
     */
    public String transfersByProtocol(Map<String, String> params, InputStream certFile, String certPass, String protocol) {
        return executionByProtocol(TransferApi.TRANSFER, params, certFile, certPass, protocol);
    }

    /**
     * 查询企业付款到零钱
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String getTransferInfo(Map<String, String> params, String certPath, String certPass) {
        return execution(TransferApi.GET_TRANSFER_INFO, params, certPath, certPass);
    }

    /**
     * 查询企业付款到零钱
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String getTransferInfo(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(TransferApi.GET_TRANSFER_INFO, params, certFile, certPass);
    }

    /**
     * 企业付款到银行
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String payBank(Map<String, String> params, String certPath, String certPass) {
        return execution(TransferApi.TRANSFER_BANK, params, certPath, certPass);
    }

    /**
     * 企业付款到银行
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @param protocol 协议
     * @return {@link String} 请求返回的结果
     */
    public String payBankByProtocol(Map<String, String> params, String certPath, String certPass, String protocol) {
        return executionByProtocol(TransferApi.TRANSFER_BANK, params, certPath, certPass, protocol);
    }

    /**
     * 企业付款到银行
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String payBank(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(TransferApi.TRANSFER_BANK, params, certFile, certPass);
    }

    /**
     * 企业付款到银行
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @param protocol 协议
     * @return {@link String} 请求返回的结果
     */
    public String payBankByProtocol(Map<String, String> params, InputStream certFile, String certPass, String protocol) {
        return executionByProtocol(TransferApi.TRANSFER_BANK, params, certFile, certPass, protocol);
    }

    /**
     * 查询企业付款到银行
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String queryBank(Map<String, String> params, String certPath, String certPass) {
        return execution(TransferApi.GET_TRANSFER_BANK_INFO, params, certPath, certPass);
    }

    /**
     * 查询企业付款到银行
     *
     * @param params   请求参数
     * @param certFile 证书文件的  InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String queryBank(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(TransferApi.GET_TRANSFER_BANK_INFO, params, certFile, certPass);
    }

    /**
     * 获取 RSA 加密公钥
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String getPublicKey(Map<String, String> params, String certPath, String certPass) {
        return execution(TransferApi.GET_PUBLIC_KEY, params, certPath, certPass);
    }

    /**
     * 获取 RSA 加密公钥
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @param protocol 协议
     * @return {@link String} 请求返回的结果
     */
    public String getPublicKeyByProtocol(Map<String, String> params, String certPath, String certPass, String protocol) {
        return execution(TransferApi.GET_PUBLIC_KEY, params, certPath, certPass, protocol);
    }

    /**
     * 获取 RSA 加密公钥
     *
     * @param params   请求参数
     * @param certFile 证书文件的   InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String getPublicKey(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(TransferApi.GET_PUBLIC_KEY, params, certFile, certPass);
    }

    /**
     * 获取 RSA 加密公钥
     *
     * @param params   请求参数
     * @param certFile 证书文件的   InputStream
     * @param certPass 证书密码
     * @param protocol 协议
     * @return {@link String} 请求返回的结果
     */
    public String getPublicKeyByProtocol(Map<String, String> params, InputStream certFile, String certPass, String protocol) {
        return executionByProtocol(TransferApi.GET_PUBLIC_KEY, params, certFile, certPass, protocol);
    }

    /**
     * 公众号纯签约
     *
     * @param params   请求参数
     * @param payModel 商户平台模式
     * @return {@link String} 请求返回的结果
     */
    public String entrustWeb(Map<String, String> params, Mode payModel) {
        if (payModel == Mode.SELLER) {
            return executionByGet(EntrustPayApi.ENTRUST_WEB, params);
        } else {
            return executionByGet(EntrustPayApi.PARTNER_ENTRUST_WEB, params);
        }
    }

    /**
     * APP 纯签约
     *
     * @param params   请求参数
     * @param payModel 商户平台模式
     * @return {@link String} 请求返回的结果
     */
    public String preEntrustWeb(Map<String, String> params, Mode payModel) {
        if (payModel == Mode.SELLER) {
            return executionByGet(EntrustPayApi.PRE_ENTRUST_WEB, params);
        } else {
            return executionByGet(EntrustPayApi.PARTNER_PRE_ENTRUST_WEB, params);
        }
    }

    /**
     * H5 纯签约
     *
     * @param params   请求参数
     * @param payModel 商户平台模式
     * @return {@link String} 请求返回的结果
     */
    public String h5EntrustWeb(Map<String, String> params, Mode payModel) {
        if (payModel == Mode.SELLER) {
            return executionByGet(EntrustPayApi.H5_ENTRUST_WEB, params);
        } else {
            return executionByGet(EntrustPayApi.PARTNER_H5_ENTRUST_WEB, params);
        }
    }

    /**
     * 支付中签约
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String contractOrder(Map<String, String> params) {
        return execution(EntrustPayApi.PAY_CONTRACT_ORDER, params);
    }

    /**
     * 查询签约关系
     *
     * @param params   请求参数
     * @param payModel 商户平台模式
     * @return {@link String} 请求返回的结果
     */
    public String queryContract(Map<String, String> params, Mode payModel) {
        if (payModel == Mode.SELLER) {
            return execution(EntrustPayApi.QUERY_ENTRUST_CONTRACT, params);
        } else {
            return execution(EntrustPayApi.PARTNER_QUERY_ENTRUST_CONTRACT, params);
        }
    }

    /**
     * 申请扣款
     *
     * @param params   请求参数
     * @param payModel 商户平台模式
     * @return {@link String} 请求返回的结果
     */
    public String papPayApply(Map<String, String> params, Mode payModel) {
        if (payModel == Mode.SELLER) {
            return execution(EntrustPayApi.PAP_PAY_APPLY, params);
        } else {
            return execution(EntrustPayApi.PARTNER_PAP_PAY_APPLY, params);
        }
    }

    /**
     * 申请解约
     *
     * @param params   请求参数
     * @param payModel 商户平台模式
     * @return {@link String} 请求返回的结果
     */
    public String deleteContract(Map<String, String> params, Mode payModel) {
        if (payModel == Mode.SELLER) {
            return execution(EntrustPayApi.DELETE_ENTRUST_CONTRACT, params);
        } else {
            return execution(EntrustPayApi.PARTNER_DELETE_ENTRUST_CONTRACT, params);
        }
    }

    /**
     * 查询签约关系对账单
     *
     * @param params   请求参数
     * @param payModel 商户平台模式
     * @return {@link String} 请求返回的结果
     */
    public String contractBill(Map<String, String> params, Mode payModel) {
        if (payModel == Mode.SELLER) {
            return execution(EntrustPayApi.QUERY_ENTRUST_CONTRACT, params);
        } else {
            return execution(EntrustPayApi.PARTNER_QUERY_ENTRUST_CONTRACT, params);
        }
    }

    /**
     * 请求单次分账
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String profitSharing(Map<String, String> params, String certPath, String certPass) {
        return execution(ProfitSharingApi.PROFIT_SHARING, params, certPath, certPass);
    }

    /**
     * 请求单次分账
     *
     * @param params   请求参数
     * @param certFile 证书文件的  InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String profitSharing(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(ProfitSharingApi.PROFIT_SHARING, params, certFile, certPass);
    }

    /**
     * 请求多次分账
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String multiProfitSharing(Map<String, String> params, String certPath, String certPass) {
        return execution(ProfitSharingApi.MULTI_PROFIT_SHARING, params, certPath, certPass);
    }

    /**
     * 请求多次分账
     *
     * @param params   请求参数
     * @param certFile 证书文件的  InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String multiProfitSharing(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(ProfitSharingApi.MULTI_PROFIT_SHARING, params, certFile, certPass);
    }

    /**
     * 查询分账结果
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String profitSharingQuery(Map<String, String> params) {
        return execution(ProfitSharingApi.PROFIT_SHARING_QUERY, params);
    }

    /**
     * 添加分账接收方
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String profitSharingAddReceiver(Map<String, String> params) {
        return execution(ProfitSharingApi.PROFIT_SHARING_ADD_RECEIVER, params);
    }

    /**
     * 删除分账接收方
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String profitSharingRemoveReceiver(Map<String, String> params) {
        return execution(ProfitSharingApi.PROFIT_SHARING_REMOVE_RECEIVER, params);
    }

    /**
     * 完结分账
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String profitSharingFinish(Map<String, String> params, String certPath, String certPass) {
        return execution(ProfitSharingApi.PROFIT_SHARING_FINISH, params, certPath, certPass);
    }

    /**
     * 完结分账
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String profitSharingFinish(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(ProfitSharingApi.PROFIT_SHARING_FINISH, params, certFile, certPass);
    }

    /**
     * 分账回退
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String profitSharingReturn(Map<String, String> params, String certPath, String certPass) {
        return execution(ProfitSharingApi.PROFIT_SHARING_RETURN, params, certPath, certPass);
    }

    /**
     * 分账回退
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String profitSharingReturn(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(ProfitSharingApi.PROFIT_SHARING_RETURN, params, certFile, certPass);
    }

    /**
     * 分账回退结果查询
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String profitSharingReturnQuery(Map<String, String> params) {
        return execution(ProfitSharingApi.PROFIT_SHARING_RETURN_QUERY, params);
    }

    /**
     * 发放代金券
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String sendCoupon(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(CouponApi.SEND_COUPON, params, certFile, certPass);
    }

    /**
     * 查询代金券批次
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String queryCouponStock(Map<String, String> params) {
        return execution(CouponApi.QUERY_COUPON_STOCK, params);
    }

    /**
     * 查询代金券信息
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String queryCouponsInfo(Map<String, String> params) {
        return execution(CouponApi.QUERY_COUPONS_INFO, params);
    }

    /**
     * 支付押金（人脸支付）
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String depositFacePay(Map<String, String> params) {
        return execution(DepositApi.FACE_PAY, params);
    }

    /**
     * 支付押金（付款码支付）
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String depositMicroPay(Map<String, String> params) {
        return execution(DepositApi.MICRO_PAY, params);
    }

    /**
     * 查询订单
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String depositOrderQuery(Map<String, String> params) {
        return execution(DepositApi.ORDER_QUERY, params);
    }

    /**
     * 撤销订单
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String depositReverse(Map<String, String> params, String certPath, String certPass) {
        return execution(DepositApi.REVERSE, params, certPath, certPass);
    }

    /**
     * 撤销订单
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String depositReverse(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(DepositApi.REVERSE, params, certFile, certPass);
    }

    /**
     * 消费押金
     *
     * @param params   请求参数
     * @param certPath 证书文件的目录
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String depositConsume(Map<String, String> params, String certPath, String certPass) {
        return execution(DepositApi.CONSUME, params, certPath, certPass);
    }

    /**
     * 消费押金
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String depositConsume(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(DepositApi.CONSUME, params, certFile, certPass);
    }

    /**
     * 申请退款（押金）
     *
     * @param params   请求参数
     * @param certPath 证书文件的目录
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String depositRefund(Map<String, String> params, String certPath, String certPass) {
        return execution(DepositApi.REFUND, params, certPath, certPass);
    }

    /**
     * 申请退款（押金）
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String depositRefund(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(DepositApi.REFUND, params, certFile, certPass);
    }

    /**
     * 查询退款（押金）
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String depositRefundQuery(Map<String, String> params) {
        return execution(DepositApi.REFUND_QUERY, params);
    }

    /**
     * 下载资金账单
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String downloadFundFlow(Map<String, String> params, String certPath, String certPass) {
        return execution(PayApi.DOWNLOAD_FUND_FLOW, params, certPath, certPass);
    }

    /**
     * 下载资金账单
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String downloadFundFlow(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(PayApi.DOWNLOAD_FUND_FLOW, params, certFile, certPass);
    }

    /**
     * 刷脸设备获取设备调用凭证
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String getAuthInfo(Map<String, String> params) {
        return execution(FacePayApi.GET_AUTH_INFO, params);
    }

    /**
     * 刷脸支付
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String facePay(Map<String, String> params) {
        return execution(FacePayApi.FACE_PAY, params);
    }

    /**
     * 查询刷脸支付订单
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String facePayQuery(Map<String, String> params) {
        return execution(FacePayApi.FACE_PAY_QUERY, params);
    }

    /**
     * 刷脸支付撤销订单
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String facePayReverse(Map<String, String> params, String certPath, String certPass) {
        return execution(FacePayApi.FACE_PAY_REVERSE, params, certPath, certPass);
    }

    /**
     * 刷脸支付撤销订单
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String facePayReverse(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(FacePayApi.FACE_PAY_REVERSE, params, certFile, certPass);
    }

    /**
     * 发放普通红包
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String sendRedPack(Map<String, String> params, String certPath, String certPass) {
        return execution(RedPackApi.SEND_RED_PACK, params, certPath, certPass);
    }

    /**
     * 发放普通红包
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @param protocol 协议
     * @return {@link String} 请求返回的结果
     */
    public String sendRedPackByProtocol(Map<String, String> params, String certPath, String certPass, String protocol) {
        return executionByProtocol(RedPackApi.SEND_RED_PACK, params, certPath, certPass, protocol);
    }

    /**
     * 发放普通红包
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String sendRedPack(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(RedPackApi.SEND_RED_PACK, params, certFile, certPass);
    }

    /**
     * 发放普通红包
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @param protocol 协议
     * @return {@link String} 请求返回的结果
     */
    public String sendRedPackByProtocol(Map<String, String> params, InputStream certFile, String certPass, String protocol) {
        return executionByProtocol(RedPackApi.SEND_RED_PACK, params, certFile, certPass, protocol);
    }

    /**
     * 发放裂变红包
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String sendGroupRedPack(Map<String, String> params, String certPath, String certPass) {
        return execution(RedPackApi.SEND_GROUP_RED_PACK, params, certPath, certPass);
    }

    /**
     * 发放裂变红包
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @param protocol 协议
     * @return {@link String} 请求返回的结果
     */
    public String sendGroupRedPackByProtocol(Map<String, String> params, String certPath, String certPass, String protocol) {
        return executionByProtocol(RedPackApi.SEND_GROUP_RED_PACK, params, certPath, certPass, protocol);
    }

    /**
     * 发放裂变红包
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String sendGroupRedPack(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(RedPackApi.SEND_GROUP_RED_PACK, params, certFile, certPass);
    }

    /**
     * 发放裂变红包
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @param protocol 协议
     * @return {@link String} 请求返回的结果
     */
    public String sendGroupRedPackByProtocol(Map<String, String> params, InputStream certFile, String certPass, String protocol) {
        return executionByProtocol(RedPackApi.SEND_GROUP_RED_PACK, params, certFile, certPass, protocol);
    }

    /**
     * 查询红包记录
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String getHbInfo(Map<String, String> params, String certPath, String certPass) {
        return execution(RedPackApi.GET_HB_INFO, params, certPath, certPass);
    }

    /**
     * 查询红包记录
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String getHbInfo(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(RedPackApi.GET_HB_INFO, params, certFile, certPass);
    }

    /**
     * 小程序发放红包接口
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String sendMiniProgramRedPack(Map<String, String> params, String certPath, String certPass) {
        return execution(RedPackApi.SEND_MINI_PROGRAM_HB, params, certPath, certPass);
    }

    /**
     * 小程序发放红包接口
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @param protocol 协议
     * @return {@link String} 请求返回的结果
     */
    public String sendMiniProgramRedPackByProtocol(Map<String, String> params, String certPath, String certPass, String protocol) {
        return executionByProtocol(RedPackApi.SEND_MINI_PROGRAM_HB, params, certPath, certPass, protocol);
    }

    /**
     * 小程序发放红包接口
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String sendMiniProgramRedPack(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(RedPackApi.SEND_MINI_PROGRAM_HB, params, certFile, certPass);
    }

    /**
     * 小程序发放红包接口
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @param protocol 协议
     * @return {@link String} 请求返回的结果
     */
    public String sendMiniProgramRedPackByProtocol(Map<String, String> params, InputStream certFile, String certPass, String protocol) {
        return executionByProtocol(RedPackApi.SEND_MINI_PROGRAM_HB, params, certFile, certPass, protocol);
    }

    /**
     * 发放企业红包
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String sendWorkWxRedPack(Map<String, String> params, String certPath, String certPass) {
        return execution(RedPackApi.SEND_WORK_WX_RED_PACK, params, certPath, certPass);
    }

    /**
     * 发放企业红包
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @param protocol 协议
     * @return {@link String} 请求返回的结果
     */
    public String sendWorkWxRedPackByProtocol(Map<String, String> params, String certPath, String certPass, String protocol) {
        return executionByProtocol(RedPackApi.SEND_WORK_WX_RED_PACK, params, certPath, certPass, protocol);
    }

    /**
     * 发放企业红包
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String sendWorkWxRedPack(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(RedPackApi.SEND_WORK_WX_RED_PACK, params, certFile, certPass);
    }

    /**
     * 发放企业红包
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @param protocol 协议
     * @return {@link String} 请求返回的结果
     */
    public String sendWorkWxRedPackByProtocol(Map<String, String> params, InputStream certFile, String certPass, String protocol) {
        return executionByProtocol(RedPackApi.SEND_WORK_WX_RED_PACK, params, certFile, certPass, protocol);
    }

    /**
     * 查询向员工付款记录
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String queryWorkWxRedPack(Map<String, String> params, String certPath, String certPass) {
        return execution(RedPackApi.QUERY_WORK_WX_RED_PACK, params, certPath, certPass);
    }

    /**
     * 查询向员工付款记录
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @param protocol 协议
     * @return {@link String} 请求返回的结果
     */
    public String queryWorkWxRedPackByProtocol(Map<String, String> params, String certPath, String certPass, String protocol) {
        return executionByProtocol(RedPackApi.QUERY_WORK_WX_RED_PACK, params, certPath, certPass, protocol);
    }

    /**
     * 查询向员工付款记录
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String queryWorkWxRedPack(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(RedPackApi.QUERY_WORK_WX_RED_PACK, params, certFile, certPass);
    }

    /**
     * 查询向员工付款记录
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @param protocol 协议
     * @return {@link String} 请求返回的结果
     */
    public String queryWorkWxRedPackByProtocol(Map<String, String> params, InputStream certFile, String certPass, String protocol) {
        return executionByProtocol(RedPackApi.QUERY_WORK_WX_RED_PACK, params, certFile, certPass, protocol);
    }

    /**
     * 向员工付款
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String trans2pocket(Map<String, String> params, String certPath, String certPass) {
        return execution(TransferApi.PAY_WWS_TRANS_2_POCKET, params, certPath, certPass);
    }

    /**
     * 向员工付款
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @param protocol 协议
     * @return {@link String} 请求返回的结果
     */
    public String trans2pocketByProtocol(Map<String, String> params, String certPath, String certPass, String protocol) {
        return executionByProtocol(TransferApi.PAY_WWS_TRANS_2_POCKET, params, certPath, certPass, protocol);
    }

    /**
     * 向员工付款
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String trans2pocket(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(TransferApi.PAY_WWS_TRANS_2_POCKET, params, certFile, certPass);
    }

    /**
     * 向员工付款
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @param protocol 协议
     * @return {@link String} 请求返回的结果
     */
    public String trans2pocketByProtocol(Map<String, String> params, InputStream certFile, String certPass, String protocol) {
        return executionByProtocol(TransferApi.PAY_WWS_TRANS_2_POCKET, params, certFile, certPass, protocol);
    }

    /**
     * 查询向员工付款记录
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String queryTrans2pocket(Map<String, String> params, String certPath, String certPass) {
        return execution(TransferApi.QUERY_WWS_TRANS_2_POCKET, params, certPath, certPass);
    }

    /**
     * 查询向员工付款记录
     *
     * @param params   请求参数
     * @param certPath 证书文件路径
     * @param certPass 证书密码
     * @param protocol 协议
     * @return {@link String} 请求返回的结果
     */
    public String queryTrans2pocketByProtocol(Map<String, String> params, String certPath, String certPass, String protocol) {
        return executionByProtocol(TransferApi.QUERY_WWS_TRANS_2_POCKET, params, certPath, certPass, protocol);
    }

    /**
     * 查询向员工付款记录
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @return {@link String} 请求返回的结果
     */
    public String queryTrans2pocket(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(TransferApi.QUERY_WWS_TRANS_2_POCKET, params, certFile, certPass);
    }

    /**
     * 查询向员工付款记录
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @param certPass 证书密码
     * @param protocol 协议
     * @return {@link String} 请求返回的结果
     */
    public String queryTrans2pocket(Map<String, String> params, InputStream certFile, String certPass, String protocol) {
        return executionByProtocol(TransferApi.QUERY_WWS_TRANS_2_POCKET, params, certFile, certPass, protocol);
    }

    /**
     * 小程序虚拟支付接口
     *
     * @param apiEnum     接口枚举
     * @param appKey      应用秘钥
     * @param accessToken 小程序Token
     * @param postBody    POST的数据包体
     * @return {@link Message} 请求返回的结果
     */
    public Message xPay(Complex apiEnum, String appKey, String accessToken, String postBody) {
        String url = apiEnum.service();
        String needSignMsg = url.concat("&").concat(postBody);
        String paySig = org.miaixz.bus.crypto.Builder.hmacSha256(appKey).digestHex(needSignMsg);
        url = url.concat("?access_token=").concat(accessToken).concat("&pay_sig=").concat(paySig);
        return post(url, postBody, null);
    }

    /**
     * @param url    请求url
     * @param params 请求参数
     * @return {@link String}    请求返回的结果
     */
    public String doGet(String url, Map<String, String> params) {
        return get(url, params);
    }

    /**
     * get 请求
     *
     * @param url           请求url
     * @param authorization 授权信息
     * @param serialNumber  公钥证书序列号
     * @param params        请求参数
     * @return {@link Message}    请求返回的结果
     */
    public Message get(String url, String authorization, String serialNumber, Map<String, String> params) {
        return get(url, params, WechatPayBuilder.getHeaders(authorization, serialNumber));
    }

    /**
     * post 请求
     *
     * @param url           请求url
     * @param authorization 授权信息
     * @param serialNumber  公钥证书序列号
     * @param data          请求参数
     * @return {@link Message}    请求返回的结果
     */
    public Message post(String url, String authorization, String serialNumber, String data) {
        return post(url, data, WechatPayBuilder.getHeaders(authorization, serialNumber));
    }

    /**
     * delete 请求
     *
     * @param url           请求url
     * @param authorization 授权信息
     * @param serialNumber  公钥证书序列号
     * @param data          请求参数
     * @return {@link Message}    请求返回的结果
     */
    public Message delete(String url, String authorization, String serialNumber, String data) {
        return post(url, data, WechatPayBuilder.getHeaders(authorization, serialNumber));
    }

    /**
     * upload 请求
     *
     * @param url     请求url
     * @param params  请求参数
     * @param headers 请求头
     * @return {@link Message}    请求返回的结果
     */
    public Message upload(String url, Map<String, String> params, Map<String, String> headers) {
        return post(url, params, headers);
    }

    /**
     * upload 请求
     *
     * @param url           请求url
     * @param authorization 授权信息
     * @param serialNumber  公钥证书序列号
     * @param data          请求参数
     * @param file          上传文件
     * @return {@link Message}    请求返回的结果
     */
    public Message upload(String url, String authorization, String serialNumber, String data, File file) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("meta", data);
        return post(url, paramMap, WechatPayBuilder.getUploadHeaders(authorization, serialNumber), file);
    }

    /**
     * patch 请求
     *
     * @param url           请求url
     * @param authorization 授权信息
     * @param serialNumber  公钥证书序列号
     * @param data          请求参数
     * @return {@link Message}    请求返回的结果
     */
    public Message patch(String url, String authorization, String serialNumber, String data) {
        return post(url, data, WechatPayBuilder.getHeaders(authorization, serialNumber));
    }

    /**
     * put 请求
     *
     * @param url           请求url
     * @param authorization 授权信息
     * @param serialNumber  公钥证书序列号
     * @param data          请求参数
     * @return {@link Message}    请求返回的结果
     */
    public Message put(String url, String authorization, String serialNumber, String data) {
        return put(url, data, WechatPayBuilder.getHeaders(authorization, serialNumber));
    }

    public String doPost(String url, Map<String, String> params) {
        return post(url, XmlKit.mapToXmlString(params));
    }

    public String doPostSsl(String url, Map<String, String> params, String certPath, String certPass) {
        return post(url, XmlKit.mapToXmlString(params), certPath, certPass, null);
    }

    public String doPostSslByProtocol(String url, Map<String, String> params, String certPath, String certPass, String protocol) {
        return post(url, XmlKit.mapToXmlString(params), certPath, certPass, protocol);
    }

    public String doPostSsl(String url, Map<String, String> params, String certPath) {
        if (params.isEmpty() || !params.containsKey("mch_id")) {
            throw new RuntimeException("请求参数中必须包含 mch_id，如接口参考中不包 mch_id， 请使用其他同名构造方法。");
        }
        String certPass = params.get("mch_id");
        return doPostSsl(url, params, certPath, certPass);
    }

    public String doPostSslByProtocol(String url, Map<String, String> params, String certPath, String protocol) {
        if (params.isEmpty() || !params.containsKey("mch_id")) {
            throw new RuntimeException("请求参数中必须包含 mch_id，如接口参考中不包 mch_id， 请使用其他同名构造方法。");
        }
        String certPass = params.get("mch_id");
        return doPostSslByProtocol(url, params, certPath, certPass, protocol);
    }

    public String doPostSsl(String url, Map<String, String> params, InputStream certFile) {
        if (params.isEmpty() || !params.containsKey("mch_id")) {
            throw new RuntimeException("请求参数中必须包含 mch_id，如接口参考中不包 mch_id， 请使用其他同名构造方法。");
        }
        String certPass = params.get("mch_id");
        return doPostSsl(url, params, certFile, certPass);
    }

    public String doPostSslByProtocol(String url, Map<String, String> params, InputStream certFile, String protocol) {
        if (params.isEmpty() || !params.containsKey("mch_id")) {
            throw new RuntimeException("请求参数中必须包含 mch_id，如接口参考中不包 mch_id， 请使用其他同名构造方法。");
        }
        String certPass = params.get("mch_id");
        return doPostSslByProtocol(url, params, certFile, certPass, protocol);
    }

    public String doPostSsl(String url, Map<String, String> params, InputStream certFile, String certPass) {
        return post(url, XmlKit.mapToXmlString(params), certFile, certPass, null);
    }

    public String doPostSslByProtocol(String url, Map<String, String> params, InputStream certFile, String certPass, String protocol) {
        return post(url, XmlKit.mapToXmlString(params), certFile, certPass, protocol);
    }

    public String doUploadSsl(String url, Map<String, String> params, String certPath, String filePath) {
        if (params.isEmpty() || !params.containsKey("mch_id")) {
            throw new RuntimeException("请求参数中必须包含 mch_id，如接口参考中不包 mch_id， 请使用其他同名构造方法。");
        }
        String certPass = params.get("mch_id");
        return doUploadSsl(url, params, certPath, certPass, filePath);
    }

    public String doUploadSslByProtocol(String url, Map<String, String> params, String certPath, String filePath, String protocol) {
        if (params.isEmpty() || !params.containsKey("mch_id")) {
            throw new RuntimeException("请求参数中必须包含 mch_id，如接口参考中不包 mch_id， 请使用其他同名构造方法。");
        }
        String certPass = params.get("mch_id");
        return doUploadSslByProtocol(url, params, certPath, certPass, filePath, protocol);
    }

    public String doUploadSsl(String url, Map<String, String> params, String certPath, String certPass, String filePath) {
        return upload(url, XmlKit.mapToXmlString(params), certPath, certPass, filePath);
    }

    public String doUploadSslByProtocol(String url, Map<String, String> params, String certPath, String certPass, String filePath, String protocol) {
        return upload(url, XmlKit.mapToXmlString(params), certPath, certPass, filePath, protocol);
    }

}
