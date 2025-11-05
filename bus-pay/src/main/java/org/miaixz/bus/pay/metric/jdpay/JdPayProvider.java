/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.pay.Complex;
import org.miaixz.bus.pay.Context;
import org.miaixz.bus.pay.Registry;
import org.miaixz.bus.pay.magic.Voucher;
import org.miaixz.bus.pay.metric.AbstractProvider;
import org.miaixz.bus.pay.metric.jdpay.api.JdPayApi;

/**
 * JD Pay provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JdPayProvider extends AbstractProvider<Voucher, Context> {

    /**
     * Constructs a new JdPayProvider.
     *
     * @param context The context.
     */
    public JdPayProvider(Context context) {
        super(context);
    }

    /**
     * Constructs a new JdPayProvider.
     *
     * @param context The context.
     * @param complex The complex object.
     */
    public JdPayProvider(Context context, Complex complex) {
        super(context, complex);
    }

    /**
     * Constructs a new JdPayProvider.
     *
     * @param context The context.
     * @param complex The complex object.
     * @param cache   The cache.
     */
    public JdPayProvider(Context context, Complex complex, CacheX cache) {
        super(context, complex, cache);
    }

    /**
     * Unified order.
     *
     * @param xml The request parameters in XML format.
     * @return The result of the request.
     */
    public static String uniOrder(String xml) {
        return doPost(JdPayApi.UNI_ORDER_URL.method(), xml);
    }

    /**
     * Payment code payment.
     *
     * @param xml The request parameters in XML format.
     * @return The result of the request.
     */
    public static String fkmPay(String xml) {
        return doPost(JdPayApi.FKM_PAY_URL.method(), xml);
    }

    /**
     * Query for Baitiao installment plan.
     *
     * @param xml The request parameters in XML format.
     * @return The result of the request.
     */
    public static String queryBaiTiaoFq(String xml) {
        return doPost(JdPayApi.QUERY_BAI_TIAO_FQ_URL.method(), xml);
    }

    /**
     * Query order.
     *
     * @param xml The request parameters in XML format.
     * @return The result of the request.
     */
    public static String queryOrder(String xml) {
        return doPost(JdPayApi.QUERY_ORDER_URL.method(), xml);
    }

    /**
     * Refund application.
     *
     * @param xml The request parameters in XML format.
     * @return The result of the request.
     */
    public static String refund(String xml) {
        return doPost(JdPayApi.REFUND_URL.method(), xml);
    }

    /**
     * Revocation application.
     *
     * @param xml The request parameters in XML format.
     * @return The result of the request.
     */
    public static String revoke(String xml) {
        return doPost(JdPayApi.REVOKE_URL.method(), xml);
    }

    /**
     * Query user relationship.
     *
     * @param xml The request parameters in XML format.
     * @return The result of the request.
     */
    public static String getUserRelation(String xml) {
        return doPost(JdPayApi.GET_USER_RELATION_URL.method(), xml);
    }

    /**
     * Cancel user relationship.
     *
     * @param xml The request parameters in XML format.
     * @return The result of the request.
     */
    public static String cancelUserRelation(String xml) {
        return doPost(JdPayApi.GET_USER_RELATION_URL.method(), xml);
    }

    /**
     * Performs a POST request.
     *
     * @param url    The request URL.
     * @param reqXml The request XML.
     * @return The response from the server.
     */
    public static String doPost(String url, String reqXml) {
        return post(url, reqXml);
    }

    /**
     * Gets the complete URL for the API request.
     *
     * @return The complete URL.
     */
    public String getUrl() {
        return getUrl(this.complex);
    }

    /**
     * Gets the complete URL for the API request.
     *
     * @param complex The payment API interface enumeration.
     * @return The complete URL.
     */
    public String getUrl(Complex complex) {
        return (complex.isSandbox() ? Registry.JDPAY.sandbox() : Registry.JDPAY.service()).concat(complex.method());
    }

}
