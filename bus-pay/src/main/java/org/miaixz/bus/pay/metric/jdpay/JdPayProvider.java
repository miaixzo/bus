/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * @since Java 21+
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
