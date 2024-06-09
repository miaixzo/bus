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
package org.miaixz.bus.pay.metric.unionpay.enums;

/**
 * 云闪付Api
 */
public enum ApiUrl {

    /**
     * 刷卡支付
     */
    MICRO_PAY("unified.trade.micropay"),
    /**
     * 扫码支付
     */
    NATIVE("unified.trade.native"),
    /**
     * 微信公众号、小程序支付统一下单
     */
    WEI_XIN_JS_PAY("pay.weixin.jspay"),
    /**
     * 微信 App 支付
     */
    WEI_XIN_APP_PAY("pay.weixin.raw.app"),
    /**
     * 查询订单
     */
    QUERY("unified.trade.query"),
    /**
     * 申请退款
     */
    REFUND("unified.trade.refund"),
    /**
     * 退款查询
     */
    REFUND_QUERY("unified.trade.refundquery"),
    /**
     * 关闭订单
     */
    CLOSE("unified.trade.close"),
    /**
     * 撤销订单
     */
    MICRO_PAY_REVERSE("unified.micropay.reverse"),
    /**
     * 授权码查询 openid
     */
    AUTH_CODE_TO_OPENID("unified.tools.authcodetoopenid"),
    /**
     * 银联 JS 支付获取 userId
     */
    UNION_PAY_USER_ID("pay.unionpay.userid"),
    /**
     * 银联 JS 支付下单
     */
    UNION_JS_PAY("pay.unionpay.jspay"),
    /**
     * 支付宝服务窗口支付
     */
    ALI_PAY_JS_PAY("pay.alipay.jspay"),
    /**
     * 下载单个商户时的对账单
     */
    BILL_MERCHANT("pay.bill.merchant"),
    /**
     * 下载连锁商户下所有门店的对账单
     */
    BILL_BIG_MERCHANT("pay.bill.bigMerchant"),
    /**
     * 下载某内部机构/外包服务机构下所有商户的对账单
     */
    BILL_AGENT("pay.bill.agent");

    /**
     * 接口类型
     */
    private final String service;

    ApiUrl(String domain) {
        this.service = domain;
    }

    @Override
    public String toString() {
        return service;
    }

}
