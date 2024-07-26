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

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.pay.Complex;
import org.miaixz.bus.pay.Context;
import org.miaixz.bus.pay.Registry;
import org.miaixz.bus.pay.magic.Material;
import org.miaixz.bus.pay.metric.AbstractProvider;
import org.miaixz.bus.pay.metric.jdpay.api.JdPayApi;

/**
 * 京东支付
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JdPayProvider extends AbstractProvider<Material, Context> {

    public JdPayProvider(Context context) {
        super(context);
    }

    public JdPayProvider(Context context, Complex complex) {
        super(context, complex);
    }

    public JdPayProvider(Context context, Complex complex, ExtendCache cache) {
        super(context, complex, cache);
    }

    /**
     * 统一下单
     *
     * @param xml 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String uniOrder(String xml) {
        return doPost(JdPayApi.UNI_ORDER_URL.method(), xml);
    }

    /**
     * 付款码支付
     *
     * @param xml 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String fkmPay(String xml) {
        return doPost(JdPayApi.FKM_PAY_URL.method(), xml);
    }

    /**
     * 白条分期策略查询
     *
     * @param xml 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String queryBaiTiaoFq(String xml) {
        return doPost(JdPayApi.QUERY_BAI_TIAO_FQ_URL.method(), xml);
    }

    /**
     * 查询订单
     *
     * @param xml 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String queryOrder(String xml) {
        return doPost(JdPayApi.QUERY_ORDER_URL.method(), xml);
    }

    /**
     * 退款申请
     *
     * @param xml 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String refund(String xml) {
        return doPost(JdPayApi.REFUND_URL.method(), xml);
    }

    /**
     * 撤销申请
     *
     * @param xml 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String revoke(String xml) {
        return doPost(JdPayApi.REVOKE_URL.method(), xml);
    }

    /**
     * 查询用户关系
     *
     * @param xml 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String getUserRelation(String xml) {
        return doPost(JdPayApi.GET_USER_RELATION_URL.method(), xml);
    }

    /**
     * 解除用户关系
     *
     * @param xml 请求参数
     * @return {@link String} 请求返回的结果
     */
    public static String cancelUserRelation(String xml) {
        return doPost(JdPayApi.GET_USER_RELATION_URL.method(), xml);
    }

    public static String doPost(String url, String reqXml) {
        return post(url, reqXml);
    }

    /**
     * 获取接口请求的 URL
     *
     * @return {@link String} 返回完整的接口请求URL
     */
    public String getUrl() {
        return getUrl(this.complex);
    }

    /**
     * 获取接口请求的 URL
     *
     * @param complex {@link JdPayApi} 支付 API 接口枚举
     * @return {@link String} 返回完整的接口请求URL
     */
    public String getUrl(Complex complex) {
        return (complex.isSandbox() ? Registry.JDPAY.sandbox() : Registry.JDPAY.service()).concat(complex.method());
    }

}
