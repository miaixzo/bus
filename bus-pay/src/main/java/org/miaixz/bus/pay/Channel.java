package org.miaixz.bus.pay;

/**
 * 支付通道
 */
public enum Channel {

    /**
     * 微信公众号支付或者小程序支付
     */
    JSAPI("JSAPI"),
    /**
     * 微信扫码支付
     */
    NATIVE("NATIVE"),
    /**
     * 微信APP支付
     */
    APP("APP"),
    /**
     * 付款码支付
     */
    MICROPAY("MICROPAY"),
    /**
     * H5支付
     */
    MWEB("MWEB");

    /**
     * 交易类型
     */
    private final String value;

    Channel(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
