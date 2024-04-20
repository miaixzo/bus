package org.miaixz.bus.pay.provider.wxpay.enums;

/**
 * 微信支付可用域名
 */
public enum WxDomain {

    /**
     * 中国国内
     */
    CHINA("https://api.mch.weixin.qq.com"),
    /**
     * 中国国内(备用域名)
     */
    CHINA2("https://api2.mch.weixin.qq.com"),
    /**
     * 东南亚
     */
    HK("https://apihk.mch.weixin.qq.com"),
    /**
     * 其它
     */
    US("https://apius.mch.weixin.qq.com"),
    /**
     * 获取公钥
     */
    FRAUD("https://fraud.mch.weixin.qq.com"),
    /**
     * 活动
     */
    ACTION("https://action.weixin.qq.com"),
    /**
     * 刷脸支付
     * PAY_APP
     */
    PAY_APP("https://payapp.weixin.qq.com");

    /**
     * 域名
     */
    private final String value;

    WxDomain(String value) {
        this.value = value;
    }

    public String getType() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

}
