package org.miaixz.bus.pay.metric.wxpay.enums;

/**
 * 分账接收方类型
 */
public enum ReceiverType {

    /**
     * 商户ID
     */
    MERCHANT("MERCHANT_ID"),
    /**
     * 个人微信号
     */
    WECHATID("PERSONAL_WECHATID"),
    /**
     * 个人 openId（由父商户 appId 转换得到）
     */
    OPENID("PERSONAL_OPENID"),
    /**
     * 个人 sub_openid（由子商户 appId 转换得到）
     */
    SUB_OPENID("PERSONAL_SUB_OPENID");


    /**
     * 类型
     */
    private final String type;

    ReceiverType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return type;
    }

}
