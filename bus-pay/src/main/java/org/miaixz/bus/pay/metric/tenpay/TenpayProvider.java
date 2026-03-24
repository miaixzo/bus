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
package org.miaixz.bus.pay.metric.tenpay;

import java.io.InputStream;
import java.util.Map;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.xyz.XmlKit;
import org.miaixz.bus.pay.Complex;
import org.miaixz.bus.pay.Context;
import org.miaixz.bus.pay.Registry;
import org.miaixz.bus.pay.magic.Voucher;
import org.miaixz.bus.pay.metric.AbstractProvider;
import org.miaixz.bus.pay.metric.tenpay.api.TenpayApi;

/**
 * QQ Wallet payment provider.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class TenpayProvider extends AbstractProvider<Voucher, Context> {

    /**
     * Constructs a new TenpayProvider.
     *
     * @param context The context.
     */
    public TenpayProvider(Context context) {
        super(context);
    }

    /**
     * Constructs a new TenpayProvider.
     *
     * @param context The context.
     * @param complex The complex object.
     */
    public TenpayProvider(Context context, Complex complex) {
        super(context, complex);
    }

    /**
     * Constructs a new TenpayProvider.
     *
     * @param context The context.
     * @param complex The complex object.
     * @param cache   The cache.
     */
    public TenpayProvider(Context context, Complex complex, CacheX cache) {
        super(context, complex, cache);
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
        return (complex.isSandbox() ? Registry.TENPAY.sandbox() : Registry.TENPAY.service()).concat(complex.method());
    }

    /**
     * Submits a micropay payment.
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String microPay(Map<String, String> params) {
        return doPost(TenpayApi.MICRO_PAY_URL, params);
    }

    /**
     * Creates a unified order.
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String unifiedOrder(Map<String, String> params) {
        return doPost(TenpayApi.UNIFIED_ORDER_URL, params);
    }

    /**
     * Queries an order.
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String orderQuery(Map<String, String> params) {
        return doPost(TenpayApi.ORDER_QUERY_URL, params);
    }

    /**
     * Closes an order.
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String closeOrder(Map<String, String> params) {
        return doPost(TenpayApi.CLOSE_ORDER_URL, params);
    }

    /**
     * Reverses an order.
     *
     * @param params   The request parameters.
     * @param cerPath  The certificate file path.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String orderReverse(Map<String, String> params, String cerPath, String certPass) {
        return doPost(TenpayApi.ORDER_REVERSE_URL, params, cerPath, certPass);
    }

    /**
     * Reverses an order.
     *
     * @param params   The request parameters.
     * @param certFile The InputStream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String orderReverse(Map<String, String> params, InputStream certFile, String certPass) {
        return doPost(TenpayApi.ORDER_REVERSE_URL, params, certFile, certPass);
    }

    /**
     * Applies for a refund.
     *
     * @param params   The request parameters.
     * @param cerPath  The certificate file path.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String orderRefund(Map<String, String> params, String cerPath, String certPass) {
        return doPost(TenpayApi.ORDER_REFUND_URL, params, cerPath, certPass);
    }

    /**
     * Applies for a refund.
     *
     * @param params   The request parameters.
     * @param certFile The InputStream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String orderRefund(Map<String, String> params, InputStream certFile, String certPass) {
        return doPost(TenpayApi.ORDER_REFUND_URL, params, certFile, certPass);
    }

    /**
     * Queries a refund.
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String refundQuery(Map<String, String> params) {
        return doPost(TenpayApi.REFUND_QUERY_URL, params);
    }

    /**
     * Downloads a bill.
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String downloadBill(Map<String, String> params) {
        return doPost(TenpayApi.DOWNLOAD_BILL_URL, params);
    }

    /**
     * Creates a red packet.
     *
     * @param params   The request parameters.
     * @param cerPath  The certificate file path.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String createReadPack(Map<String, String> params, String cerPath, String certPass) {
        return doPost(TenpayApi.CREATE_READ_PACK_URL, params, cerPath, certPass);
    }

    /**
     * Creates a red packet.
     *
     * @param params   The request parameters.
     * @param certFile The InputStream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String createReadPack(Map<String, String> params, InputStream certFile, String certPass) {
        return doPost(TenpayApi.CREATE_READ_PACK_URL, params, certFile, certPass);
    }

    /**
     * Gets red packet information.
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String getHbInfo(Map<String, String> params) {
        return doPost(TenpayApi.GET_HB_INFO_URL, params);
    }

    /**
     * Downloads a red packet bill.
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String downloadHbBill(Map<String, String> params) {
        return doPost(TenpayApi.DOWNLOAD_HB_BILL_URL, params);
    }

    /**
     * Transfers to balance.
     *
     * @param params   The request parameters.
     * @param cerPath  The certificate file path.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String transfer(Map<String, String> params, String cerPath, String certPass) {
        return doPost(TenpayApi.TRANSFER_URL, params, cerPath, certPass);
    }

    /**
     * Transfers to balance.
     *
     * @param params   The request parameters.
     * @param certFile The InputStream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String transfer(Map<String, String> params, InputStream certFile, String certPass) {
        return doPost(TenpayApi.TRANSFER_URL, params, certFile, certPass);
    }

    /**
     * Gets transfer information.
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String getTransferInfo(Map<String, String> params) {
        return doPost(TenpayApi.GET_TRANSFER_INFO_URL, params);
    }

    /**
     * Downloads a transfer bill.
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String downloadTransferBill(Map<String, String> params) {
        return doPost(TenpayApi.DOWNLOAD_TRANSFER_BILL_URL, params);
    }

    /**
     * Performs a POST request.
     *
     * @param complex The API endpoint.
     * @param params  The request parameters.
     * @return The response from the server.
     */
    public String doPost(Complex complex, Map<String, String> params) {
        return post(getUrl(complex), XmlKit.mapToXmlString(params));
    }

    /**
     * Performs a POST request with a certificate.
     *
     * @param complex  The API endpoint.
     * @param params   The request parameters.
     * @param certPath The certificate file path.
     * @param certPass The certificate password.
     * @return The response from the server.
     */
    public String doPost(Complex complex, Map<String, String> params, String certPath, String certPass) {
        return post(getUrl(complex), XmlKit.mapToXmlString(params), certPath, certPass, null);
    }

    /**
     * Performs a POST request with a certificate.
     *
     * @param complex  The API endpoint.
     * @param params   The request parameters.
     * @param certFile The InputStream of the certificate file.
     * @param certPass The certificate password.
     * @return The response from the server.
     */
    public String doPost(Complex complex, Map<String, String> params, InputStream certFile, String certPass) {
        return post(getUrl(complex), XmlKit.mapToXmlString(params), certFile, certPass, null);
    }

}
