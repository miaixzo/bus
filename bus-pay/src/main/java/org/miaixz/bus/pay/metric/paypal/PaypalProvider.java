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
package org.miaixz.bus.pay.metric.paypal;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.pay.Complex;
import org.miaixz.bus.pay.Context;
import org.miaixz.bus.pay.Registry;
import org.miaixz.bus.pay.cache.PayCache;
import org.miaixz.bus.pay.magic.Material;
import org.miaixz.bus.pay.magic.Message;
import org.miaixz.bus.pay.metric.AbstractProvider;
import org.miaixz.bus.pay.metric.paypal.api.PayPalApi;
import org.miaixz.bus.pay.metric.paypal.entity.AccessToken;

/**
 * PayPal Api
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PaypalProvider extends AbstractProvider<Material, Context> {

    public PaypalProvider(Context context) {
        super(context);
    }

    public PaypalProvider(Context context, Complex complex) {
        super(context, complex);
    }

    public PaypalProvider(Context context, Complex complex, ExtendCache cache) {
        super(context, complex, cache);
    }

    public static Map<String, String> getBaseHeaders(AccessToken accessToken) {
        return getBaseHeaders(accessToken, String.valueOf(DateKit.current()), null, null);
    }

    public static Map<String, String> getBaseHeaders(AccessToken accessToken, String payPalRequestId,
            String payPalPartnerAttributionId, String prefer) {
        if (accessToken == null || StringKit.isEmpty(accessToken.getTokenType())
                || StringKit.isEmpty(accessToken.getAccessToken())) {
            throw new RuntimeException("accessToken is null");
        }
        Map<String, String> headers = new HashMap<>(3);
        headers.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put("Authorization", accessToken.getTokenType().concat(" ").concat(accessToken.getAccessToken()));
        if (StringKit.isNotEmpty(payPalRequestId)) {
            headers.put("PayPal-Request-Id", payPalRequestId);
        }
        if (StringKit.isNotEmpty(payPalPartnerAttributionId)) {
            headers.put("PayPal-Partner-Attribution-Id", payPalPartnerAttributionId);
        }
        if (StringKit.isNotEmpty(prefer)) {
            headers.put("Prefer", prefer);
        }
        return headers;
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
     * @param complex {@link PayPalApi} 支付 API 接口枚举
     * @return {@link String} 返回完整的接口请求URL
     */
    public String getUrl(Complex complex) {
        return (complex.isSandbox() ? Registry.PAYPAL.sandbox() : Registry.PAYPAL.service()).concat(complex.method());
    }

    /**
     * 获取 AccessToken
     *
     * @return {@link Message} 请求返回的结果
     */
    public Message getToken() {
        Map<String, String> headers = new HashMap<>(3);
        headers.put("Accept", MediaType.APPLICATION_JSON);
        headers.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        headers.put("Authorization",
                "Basic ".concat(Base64.encode((this.context.getAppKey().concat(":").concat(this.context.getAppSecret()))
                        .getBytes(StandardCharsets.UTF_8))));
        Map<String, String> params = new HashMap<>(1);
        params.put("grant_type", "client_credentials");
        return post(getUrl(PayPalApi.GET_TOKEN), params, headers);
    }

    /**
     * 创建订单
     *
     * @param data 请求参数
     * @return {@link Message} 请求返回的结果
     */
    public Message createOrder(String data) {
        AccessToken accessToken = getAccessToken(false);
        return post(getUrl(PayPalApi.CHECKOUT_ORDERS), data, getBaseHeaders(accessToken));
    }

    /**
     * 更新订单
     *
     * @param id   订单号
     * @param data 请求参数
     * @return {@link Message} 请求返回的结果
     */
    public Message updateOrder(String id, String data) {
        AccessToken accessToken = getAccessToken(false);
        String url = getUrl(PayPalApi.CHECKOUT_ORDERS).concat("/").concat(id);
        return post(url, data, getBaseHeaders(accessToken));
    }

    /**
     * 查询订单
     *
     * @param orderId 订单号
     * @return {@link Message} 请求返回的结果
     */
    public Message queryOrder(String orderId) {
        AccessToken accessToken = getAccessToken(false);
        String url = getUrl(PayPalApi.CHECKOUT_ORDERS).concat("/").concat(orderId);
        return get(url, null, getBaseHeaders(accessToken));
    }

    /**
     * 确认订单
     *
     * @param id   订单号
     * @param data 请求参数
     * @return {@link Message} 请求返回的结果
     */
    public Message captureOrder(String id, String data) {
        AccessToken accessToken = getAccessToken(false);
        String url = String.format(getUrl(PayPalApi.CAPTURE_ORDER), id);
        return post(url, data, getBaseHeaders(accessToken));
    }

    /**
     * 查询确认的订单
     *
     * 
     * @param captureId 订单号
     * @return {@link Message} 请求返回的结果
     */
    public Message captureQuery(String captureId) {
        AccessToken accessToken = getAccessToken(false);
        String url = String.format(getUrl(PayPalApi.CAPTURE_QUERY), captureId);
        return get(url, null, getBaseHeaders(accessToken));
    }

    /**
     * 退款
     *
     * 
     * @param captureId 订单号
     * @param data      请求参数
     * @return {@link Message} 请求返回的结果
     */
    public Message refund(String captureId, String data) {
        AccessToken accessToken = getAccessToken(false);
        String url = String.format(getUrl(PayPalApi.REFUND), captureId);
        return post(url, data, getBaseHeaders(accessToken));
    }

    /**
     * 查询退款
     * 
     * @param id 订单号
     * @return {@link Message} 请求返回的结果
     */
    public Message refundQuery(String id) {
        AccessToken accessToken = getAccessToken(false);
        String url = String.format(getUrl(PayPalApi.REFUND_QUERY), id);
        return get(url, null, getBaseHeaders(accessToken));
    }

    /**
     * 通过 clientId 来获取 AccessToken
     *
     * @param forceRefresh 是否强制刷新
     * @return {@link AccessToken}
     */
    public AccessToken getAccessToken(boolean forceRefresh) {
        PayCache accessTokenCache = PayCache.INSTANCE;
        // 从缓存中获取 AccessToken
        if (!forceRefresh) {
            String json = (String) accessTokenCache.get(this.context.getAppKey());
            if (StringKit.isNotEmpty(json)) {
                AccessToken accessToken = new AccessToken(json, 200);
                if (accessToken.isAvailable()) {
                    return accessToken;
                }
            }
        }

        AccessToken result = PaypalBuilder.retryOnException(3, () -> {
            Message response = getToken();
            return new AccessToken(response.getBody(), response.getStatus());
        });

        // 三次请求如果仍然返回了不可用的 AccessToken 仍然 put 进去，便于上层通过 AccessToken 中的属性判断底层的情况
        if (null != result) {
            // 利用 clientId 与 accessToken 建立关联，支持多账户
            accessTokenCache.cache(this.context.getAppKey(), result.getCacheJson());
        }
        return result;
    }

}
