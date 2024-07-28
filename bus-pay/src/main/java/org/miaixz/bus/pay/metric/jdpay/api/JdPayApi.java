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
package org.miaixz.bus.pay.metric.jdpay.api;

import org.miaixz.bus.pay.Matcher;

/**
 * 京东支付 API
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum JdPayApi implements Matcher {

    /**
     * PC 在线支付接口
     */
    PC_SAVE_ORDER_URL("https://wepay.jd.com/jdpay/saveOrder", "PC 在线支付接口"),
    /**
     * H5 在线支付接口
     */
    H5_SAVE_ORDER_URL("https://h5pay.jd.com/jdpay/saveOrder", "H5 在线支付接口"),
    /**
     * 统一下单接口
     */
    UNI_ORDER_URL("https://paygate.jd.com/service/uniorder", "统一下单接口"),
    /**
     * 商户二维码支付接口
     */
    CUSTOMER_PAY_URL("https://h5pay.jd.com/jdpay/customerPay", "商户二维码支付接口"),
    /**
     * 付款码支付接口
     */
    FKM_PAY_URL("https://paygate.jd.com/service/fkmPay", "付款码支付接口"),
    /**
     * 白条分期策略查询接口
     */
    QUERY_BAI_TIAO_FQ_URL("https://paygate.jd.com/service/queryBaiTiaoFQ", "白条分期策略查询接口"),
    /**
     * 交易查询接口
     */
    QUERY_ORDER_URL("https://paygate.jd.com/service/query", "交易查询接口"),
    /**
     * 退款申请接口
     */
    REFUND_URL("https://paygate.jd.com/service/refund", "退款申请接口"),
    /**
     * 撤销申请接口
     */
    REVOKE_URL("https://paygate.jd.com/service/revoke", "撤销申请接口"),
    /**
     * 用户关系查询接口
     */
    GET_USER_RELATION_URL("https://paygate.jd.com/service/getUserRelation", "用户关系查询接口"),
    /**
     * 用户关系解绑接口
     */
    CANCEL_USER_RELATION_URL("https://paygate.jd.com/service/cancelUserRelation", "用户关系解绑接口");

    /**
     * 接口方法
     */
    private final String method;
    /**
     * 描述
     */
    private final String desc;

    JdPayApi(String method, String desc) {
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
