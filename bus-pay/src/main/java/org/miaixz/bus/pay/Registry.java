package org.miaixz.bus.pay;

/**
 * 支付平台类型
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Registry {

    /**
     * 支付宝
     */
    ALIPAY("ALIPAY"),
    /**
     * 京东
     */
    JD("JD"),
    /**
     * Paypal
     */
    PAYPAL("PAYPAL"),
    /**
     * QQ
     */
    QQ("QQ"),
    /**
     * 银联
     */
    UNIONPAY("UNIONPAY"),
    /**
     * 微信
     */
    WECHAT("WECHAT");


    private final String value;

    Registry(String value) {
        this.value = value;
    }

    public String getType() {
        return value;
    }

}
