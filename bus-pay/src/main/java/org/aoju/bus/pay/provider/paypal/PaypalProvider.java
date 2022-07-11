package org.aoju.bus.pay.provider.paypal;

import org.aoju.bus.core.codec.Base64;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.MediaType;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.pay.Builder;
import org.aoju.bus.pay.Context;
import org.aoju.bus.pay.magic.Results;
import org.aoju.bus.pay.metric.HttpKit;

import java.util.HashMap;
import java.util.Map;

/**
 * PayPal Api
 */
public class PaypalProvider {

    /**
     * 获取接口请求的 URL
     *
     * @param payPalApiUrl {@link PayPalApiUrl} 支付 API 接口枚举
     * @param isSandBox    是否是沙箱环境
     * @return {@link String} 返回完整的接口请求URL
     */
    public static String getReqUrl(PayPalApiUrl payPalApiUrl, boolean isSandBox) {
        return (isSandBox ? PayPalApiUrl.SANDBOX_GATEWAY.getUrl() : PayPalApiUrl.LIVE_GATEWAY.getUrl())
                .concat(payPalApiUrl.getUrl());
    }

    /**
     * 获取 AccessToken
     *
     * @param context {@link Context} 支付配置
     * @return {@link Results} 请求返回的结果
     */
    public static Results getToken(Context context) {
        Map<String, String> headers = new HashMap<>(3);
        headers.put("Accept", MediaType.APPLICATION_JSON);
        headers.put("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
        headers.put("Authorization", "Basic ".concat(Base64.encode((context.getClientId().concat(":").concat(context.getSecret())).getBytes(Charset.UTF_8))));
        Map<String, Object> params = new HashMap<>(1);
        params.put("grant_type", "client_credentials");
        return post(getReqUrl(PayPalApiUrl.GET_TOKEN, context.isSandBox()), params, headers);
    }

    /**
     * 创建订单
     *
     * @param context {@link Context} 支付配置
     * @param data    请求参数
     * @return {@link Results} 请求返回的结果
     */
    public static Results createOrder(Context context, String data) {
        AccessToken accessToken = AccessTokenKit.get(context.getClientId());
        return post(getReqUrl(PayPalApiUrl.CHECKOUT_ORDERS, context.isSandBox()), data, getBaseHeaders(accessToken));
    }

    /**
     * 更新订单
     *
     * @param context {@link Context} 支付配置
     * @param id      订单号
     * @param data    请求参数
     * @return {@link Results} 请求返回的结果
     */
    public static Results updateOrder(Context context, String id, String data) {
        AccessToken accessToken = AccessTokenKit.get(context.getClientId());
        String url = getReqUrl(PayPalApiUrl.CHECKOUT_ORDERS, context.isSandBox()).concat("/").concat(id);
        return patch(url, data, getBaseHeaders(accessToken));
    }

    /**
     * 查询订单
     *
     * @param context {@link Context} 支付配置
     * @param orderId 订单号
     * @return {@link Results} 请求返回的结果
     */
    public static Results queryOrder(Context context, String orderId) {
        AccessToken accessToken = AccessTokenKit.get(context.getClientId());
        String url = getReqUrl(PayPalApiUrl.CHECKOUT_ORDERS, context.isSandBox()).concat("/").concat(orderId);
        return get(url, null, getBaseHeaders(accessToken));
    }

    /**
     * 确认订单
     *
     * @param context {@link Context} 支付配置
     * @param id      订单号
     * @param data    请求参数
     * @return {@link Results} 请求返回的结果
     */
    public static Results captureOrder(Context context, String id, String data) {
        AccessToken accessToken = AccessTokenKit.get(context.getClientId());
        String url = String.format(getReqUrl(PayPalApiUrl.CAPTURE_ORDER, context.isSandBox()), id);
        return post(url, data, getBaseHeaders(accessToken));
    }

    /**
     * 查询确认的订单
     *
     * @param context   {@link Context} 支付配置
     * @param captureId 订单号
     * @return {@link Results} 请求返回的结果
     */
    public static Results captureQuery(Context context, String captureId) {
        AccessToken accessToken = AccessTokenKit.get(context.getClientId());
        String url = String.format(getReqUrl(PayPalApiUrl.CAPTURE_QUERY, context.isSandBox()), captureId);
        return get(url, null, getBaseHeaders(accessToken));
    }

    /**
     * 退款
     *
     * @param context   {@link Context} 支付配置
     * @param captureId 订单号
     * @param data      请求参数
     * @return {@link Results} 请求返回的结果
     */
    public static Results refund(Context context, String captureId, String data) {
        AccessToken accessToken = AccessTokenKit.get(context.getClientId());
        String url = String.format(getReqUrl(PayPalApiUrl.REFUND, context.isSandBox()), captureId);
        return post(url, data, getBaseHeaders(accessToken));
    }

    /**
     * 查询退款
     *
     * @param context {@link Context} 支付配置
     * @param id      订单号
     * @return {@link Results} 请求返回的结果
     */
    public static Results refundQuery(Context context, String id) {
        AccessToken accessToken = AccessTokenKit.get(context.getClientId());
        String url = String.format(getReqUrl(PayPalApiUrl.REFUND_QUERY, context.isSandBox()), id);
        return get(url, null, getBaseHeaders(accessToken));
    }

    /**
     * post 请求
     *
     * @param url     请求 url
     * @param params  {@link Map} 请求参数
     * @param headers {@link Map} 请求头
     * @return {@link Results} 请求返回的结果
     */
    public static Results post(String url, Map<String, Object> params, Map<String, String> headers) {
        return HttpKit.post(url, params, headers);
    }

    /**
     * get 请求
     *
     * @param url     请求 url
     * @param params  {@link Map} 请求参数
     * @param headers {@link Map} 请求头
     * @return {@link Results} 请求返回的结果
     */
    public static Results get(String url, Map<String, Object> params, Map<String, String> headers) {
        return HttpKit.get(url, params, headers);
    }

    /**
     * post 请求
     *
     * @param url     请求 url
     * @param data    {@link String} 请求参数
     * @param headers {@link Map} 请求头
     * @return {@link Results} 请求返回的结果
     */
    public static Results post(String url, String data, Map<String, String> headers) {
        return HttpKit.post(url, data, headers);
    }

    /**
     * patch 请求
     *
     * @param url     请求 url
     * @param data    {@link String} 请求参数
     * @param headers {@link Map} 请求头
     * @return {@link Results} 请求返回的结果
     */
    public static Results patch(String url, String data, Map<String, String> headers) {
        return HttpKit.patch(url, data, headers);
    }

    public static Map<String, String> getBaseHeaders(AccessToken accessToken) {
        return getBaseHeaders(accessToken, Builder.generateString(), null, null);
    }

    public static Map<String, String> getBaseHeaders(AccessToken accessToken,
                                                     String payPalRequestId,
                                                     String payPalPartnerAttributionId,
                                                     String prefer) {
        if (accessToken == null ||
                StringKit.isEmpty(accessToken.getTokenType()) ||
                StringKit.isEmpty(accessToken.getAccessToken())) {
            throw new RuntimeException("accessToken is null");
        }
        Map<String, String> headers = new HashMap<>(3);
        headers.put("Content-Type", MediaType.APPLICATION_JSON);
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

}
