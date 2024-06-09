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
package org.miaixz.bus.pay.metric.jdpay;

import org.miaixz.bus.pay.metric.HttpKit;

/**
 * 京东支付 Api
 */
public class JdPayProvider {

    /**
     * PC 在线支付接口
     */
    public static String PC_SAVE_ORDER_URL = "https://wepay.jd.com/jdpay/saveOrder";
    /**
     * H5 在线支付接口
     */
    public static String H5_SAVE_ORDER_URL = "https://h5pay.jd.com/jdpay/saveOrder";
    /**
     * 统一下单接口
     */
    public static String UNI_ORDER_URL = "https://paygate.jd.com/service/uniorder";
    /**
     * 商户二维码支付接口
     */
    public static String CUSTOMER_PAY_URL = "https://h5pay.jd.com/jdpay/customerPay";
    /**
     * 付款码支付接口
     */
    public static String FKM_PAY_URL = "https://paygate.jd.com/service/fkmPay";

    /**
     * 白条分期策略查询接口
     */
    public static String QUERY_BAI_TIAO_FQ_URL = "https://paygate.jd.com/service/queryBaiTiaoFQ";

    /**
     * 交易查询接口
     */
    public static String QUERY_ORDER_URL = "https://paygate.jd.com/service/query";
    /**
     * 退款申请接口
     */
    public static String REFUND_URL = "https://paygate.jd.com/service/refund";
    /**
     * 撤销申请接口
     */
    public static String REVOKE_URL = "https://paygate.jd.com/service/revoke";
    /**
     * 用户关系查询接口
     */
    public static String GET_USER_RELATION_URL = "https://paygate.jd.com/service/getUserRelation";
    /**
     * 用户关系解绑接口
     */
    public static String CANCEL_USER_RELATION_URL = "https://paygate.jd.com/service/cancelUserRelation";

    /**
     * 统一下单
     *
     * @param xml 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String uniOrder(String xml) {
        return doPost(UNI_ORDER_URL, xml);
    }

    /**
     * 付款码支付
     *
     * @param xml 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String fkmPay(String xml) {
        return doPost(FKM_PAY_URL, xml);
    }

    /**
     * 白条分期策略查询
     *
     * @param xml 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String queryBaiTiaoFq(String xml) {
        return doPost(QUERY_BAI_TIAO_FQ_URL, xml);
    }

    /**
     * 查询订单
     *
     * @param xml 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String queryOrder(String xml) {
        return doPost(QUERY_ORDER_URL, xml);
    }

    /**
     * 退款申请
     *
     * @param xml 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String refund(String xml) {
        return doPost(REFUND_URL, xml);
    }

    /**
     * 撤销申请
     *
     * @param xml 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String revoke(String xml) {
        return doPost(REVOKE_URL, xml);
    }

    /**
     * 查询用户关系
     *
     * @param xml 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String getUserRelation(String xml) {
        return doPost(GET_USER_RELATION_URL, xml);
    }

    /**
     * 解除用户关系
     *
     * @param xml 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String cancelUserRelation(String xml) {
        return doPost(GET_USER_RELATION_URL, xml);
    }

    public static String doPost(String url, String reqXml) {
        return HttpKit.post(url, reqXml);
    }

}
