package org.aoju.bus.pay;

/**
 * 内置的各api需要的url, 用枚举类分平台类型管理
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Registry {

    ALIPAY("ALIPAY"),
    QQ("QQ"),
    JD("JD"),
    PAYPAL("PAYPAL"),
    UNIONPAY("UNIONPAY"),
    WEPAY("WEPAY");

    Registry(String alipay) {
    }

}
