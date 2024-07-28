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
package org.miaixz.bus.pay.metric.alipay.api;

import org.miaixz.bus.pay.Matcher;

/**
 * 支付宝交易API
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum AliPayApi implements Matcher {

    /**
     * 网页支付
     */
    PAGE("alipay.trade.page.pay", "网页支付"),
    /**
     * APP支付
     */
    APP("alipay.trade.app.pay", "APP支付"),
    /**
     * 手机网站支付
     */
    WAP("alipay.trade.wap.pay", "手机网站支付"),
    /**
     * 扫码付
     */
    SWEEPPAY("alipay.trade.precreate", "扫码付"),
    /**
     * 条码付
     */
    BAR_CODE("alipay.trade.pay", "条码付"),
    /**
     * 声波付
     */
    WAVE_CODE("alipay.trade.pay", "声波付"),
    /**
     * 小程序
     */
    MINAPP("alipay.trade.create", "小程序"),
    /**
     * 刷脸付
     */
    SECURITY_CODE("alipay.trade.pay", "刷脸付"),
    /**
     * 统一收单交易结算接口
     */
    SETTLE("alipay.trade.order.settle", "统一收单交易结算接口"),
    /**
     * 交易订单查询
     */
    QUERY("alipay.trade.query", "交易订单查询"),
    /**
     * 交易订单关闭
     */
    CLOSE("alipay.trade.close", "交易订单关闭"),
    /**
     * 交易订单撤销
     */
    CANCEL("alipay.trade.cancel", "交易订单撤销"),
    /**
     * 退款
     */
    REFUND("alipay.trade.refund", "退款"),
    /**
     * 退款查询
     */
    REFUNDQUERY("alipay.trade.fastpay.refund.query", "退款查询"),
    /**
     * 收单退款冲退完成通知 退款存在退到银行卡场景下时，收单会根据银行回执消息发送退款完成信息
     */
    REFUND_DEPOSITBACK_COMPLETED("alipay.trade.refund.depositback.completed", "收单退款冲退完成通知"),
    /**
     * 下载对账单
     */
    DOWNLOADBILL("alipay.data.dataservice.bill.downloadurl.query", "下载对账单");

    /**
     * 接口方法
     */
    private final String method;
    /**
     * 描述
     */
    private final String desc;

    AliPayApi(String method, String desc) {
        this.method = method;
        this.desc = desc;
    }

    /**
     * 交易类型
     *
     * @return the string
     */
    @Override
    public String type() {
        return this.name();
    }

    /**
     * 类型描述
     *
     * @return the string
     */
    @Override
    public String desc() {
        return this.desc;
    }

    /**
     * 接口方法
     *
     * @return the string
     */
    @Override
    public String method() {
        return this.method;
    }

}
