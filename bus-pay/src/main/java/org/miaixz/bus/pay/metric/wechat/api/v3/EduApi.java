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
package org.miaixz.bus.pay.metric.wechat.api.v3;

import org.miaixz.bus.pay.Matcher;

/**
 * 微信支付 v3 接口-教培续费通相关接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum EduApi implements Matcher {

    /**
     * 通过协议号查询签约
     */
    QUERY_CONTRACTS_BY_ID("/v3/edu-papay/contracts/id/%s", "通过协议号查询签约"),

    /**
     * 预签约
     */
    PRE_SIGN("/v3/edu-papay/contracts/presign", "预签约"),

    /**
     * 解约
     */
    DELETE_CONTRACTS("/v3/edu-papay/contracts/%s", "解约"),

    /**
     * 通过用户标识查询签约
     */
    QUERY_CONTRACTS_BY_USER("/v3/edu-papay/user/%s/contracts", "通过用户标识查询签约"),

    /**
     * 受理扣款
     */
    TRANSACTIONS("/v3/edu-papay/transactions", "受理扣款"),

    /**
     * 通过微信订单号查询订单
     */
    QUERY_TRANSACTIONS_BY_TRANSACTION_ID("/v3/edu-papay/transactions/id/%s", "通过微信订单号查询订单"),

    /**
     * 通过商户订单号查询订单
     */
    QUERY_TRANSACTIONS_BY_OUT_TRADE_NO("/v3/edu-papay/transactions/out-trade-no/%s", "通过商户订单号查询订单"),

    /**
     * 发送扣款预通知
     */
    SEND_NOTIFICATION("/v3/edu-papay/user-notifications/%s/send", "发送扣款预通知");

    /**
     * 接口方法
     */
    private final String method;

    /**
     * 接口描述
     */
    private final String desc;

    EduApi(String method, String desc) {
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
