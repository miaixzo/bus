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
package org.miaixz.bus.pay.metric.wechat.api.v2;

import org.miaixz.bus.pay.Matcher;

/**
 * 微信支付 v2 版本-支付押金相关接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum DepositApi implements Matcher {

    /**
     * 支付押金（JSAPI、APP 下单）
     */
    PAY("/deposit/unifiedorder", "支付押金（JSAPI、APP 下单）"),

    /**
     * 支付押金（人脸支付）
     */
    FACE_PAY("/deposit/facepay", "支付押金（人脸支付）"),

    /**
     * 支付押金（付款码支付）
     */
    MICRO_PAY("/deposit/micropay", "支付押金（付款码支付）"),

    /**
     * 查询订单
     */
    ORDER_QUERY("/deposit/orderquery", "查询订单"),

    /**
     * 撤销订单
     */
    REVERSE("/deposit/reverse", "撤销订单"),

    /**
     * 消费押金
     */
    CONSUME("/deposit/consume", "消费押金"),

    /**
     * 申请退款
     */
    REFUND("/deposit/refund", "申请退款"),

    /**
     * 查询退款
     */
    REFUND_QUERY("deposit/refundquery", "查询退款");

    /**
     * 接口方法
     */
    private final String method;

    /**
     * 接口描述
     */
    private final String desc;

    DepositApi(String method, String desc) {
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
