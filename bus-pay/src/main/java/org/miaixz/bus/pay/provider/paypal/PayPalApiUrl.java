package org.miaixz.bus.pay.provider.paypal;

/**
 * PayPal 支付接口枚举
 */
public enum PayPalApiUrl {

    /**
     * 沙箱环境
     */
    SANDBOX_GATEWAY("https://api.sandbox.paypal.com"),
    /**
     * 线上环境
     */
    LIVE_GATEWAY("https://api.paypal.com"),
    /**
     * 获取 Access Token
     */
    GET_TOKEN("/v1/oauth2/token"),
    /**
     * 订单
     */
    CHECKOUT_ORDERS("/v2/checkout/orders"),
    /**
     * 确认订单
     */
    CAPTURE_ORDER("/v2/checkout/orders/%s/capture"),
    /**
     * 查询已确认订单
     */
    CAPTURE_QUERY("/v2/payments/captures/%s"),
    /**
     * 退款
     */
    REFUND("/v2/payments/captures/%s/refund"),
    /**
     * 退款查询
     */
    REFUND_QUERY("/v2/payments/refunds/%s");

    /**
     * 类型
     */
    private final String url;

    PayPalApiUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return url;
    }

}
