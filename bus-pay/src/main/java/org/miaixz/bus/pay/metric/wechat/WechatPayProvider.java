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
package org.miaixz.bus.pay.metric.wechat;

import java.io.File;
import java.io.InputStream;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.XmlKit;
import org.miaixz.bus.pay.*;
import org.miaixz.bus.pay.magic.Voucher;
import org.miaixz.bus.pay.magic.Message;
import org.miaixz.bus.pay.metric.AbstractProvider;
import org.miaixz.bus.pay.metric.unionpay.api.UnionPayApi;
import org.miaixz.bus.pay.metric.wechat.api.v2.*;

/**
 * Provider for WeChat Pay APIs.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WechatPayProvider extends AbstractProvider<Voucher, Context> {

    /**
     * Constructs a WechatPayProvider with the given context.
     *
     * @param context The context containing configuration information.
     */
    public WechatPayProvider(Context context) {
        this(context, null);
    }

    /**
     * Constructs a WechatPayProvider with the given context and API complex.
     *
     * @param context The context containing configuration information.
     * @param complex The API complex to be used.
     */
    public WechatPayProvider(Context context, Complex complex) {
        this(context, complex, null);
    }

    /**
     * Constructs a WechatPayProvider with the given context, API complex, and cache.
     *
     * @param context The context containing configuration information.
     * @param complex The API complex to be used.
     * @param cache   The cache to be used.
     */
    public WechatPayProvider(Context context, Complex complex, CacheX cache) {
        super(context, complex, cache);
    }

    /**
     * Gets the request URL for the API.
     *
     * @return The complete API request URL.
     */
    public String getUrl() {
        return getUrl(this.complex);
    }

    /**
     * Gets the request URL for the API.
     *
     * @param complex The {@link UnionPayApi} payment API enum.
     * @return The complete API request URL.
     */
    public String getUrl(Complex complex) {
        return (complex.isSandbox() ? Registry.UNIONPAY.sandbox() : Registry.UNIONPAY.service())
                .concat(complex.method());
    }

    /**
     * Executes a request.
     *
     * @param complex The API URL.
     * @param params  The request parameters.
     * @return The result of the request.
     */
    public String execution(Complex complex, Map<String, String> params) {
        return doPost(getUrl(complex), params);
    }

    /**
     * Executes a GET request.
     *
     * @param complex The API URL obtained via {@link #getUrl()} or {@link #getUrl(Complex)}.
     * @param params  The request parameters.
     * @return The result of the request.
     */
    public String executionByGet(Complex complex, Map<String, String> params) {
        return doGet(getUrl(complex), params);
    }

    /**
     * Executes a request with SSL.
     *
     * @param complex  The API URL.
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String execution(Complex complex, Map<String, String> params, String certPath, String certPass) {
        return doPostSsl(getUrl(complex), params, certPath, certPass);
    }

    /**
     * Executes a request with SSL and a specific protocol.
     *
     * @param complex  The API URL.
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String executionByProtocol(
            Complex complex,
            Map<String, String> params,
            String certPath,
            String certPass,
            String protocol) {
        return doPostSslByProtocol(getUrl(complex), params, certPath, certPass, protocol);
    }

    /**
     * Executes a request with SSL.
     *
     * @param complex  The API URL obtained via {@link #getUrl()} or {@link #getUrl(Complex)}.
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @return The result of the request.
     */
    public String execution(Complex complex, Map<String, String> params, String certPath) {
        return doPostSsl(getUrl(complex), params, certPath);
    }

    /**
     * Executes a request with SSL and a specific protocol.
     *
     * @param complex  The API URL.
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String executionByProtocol(Complex complex, Map<String, String> params, String certPath, String protocol) {
        return doPostSslByProtocol(getUrl(complex), params, certPath, protocol);
    }

    /**
     * Executes a request with SSL.
     *
     * @param complex  The API URL.
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String execution(Complex complex, Map<String, String> params, InputStream certFile, String certPass) {
        return doPostSsl(getUrl(complex), params, certFile, certPass);
    }

    /**
     * Executes a request with SSL and a specific protocol.
     *
     * @param complex  The API URL.
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String executionByProtocol(
            Complex complex,
            Map<String, String> params,
            InputStream certFile,
            String certPass,
            String protocol) {
        return doPostSslByProtocol(getUrl(complex), params, certFile, certPass, protocol);
    }

    public String execution(
            Complex complex,
            Map<String, String> params,
            String certPath,
            String certPass,
            String filePath) {
        return doUploadSsl(getUrl(complex), params, certPath, certPass, filePath);
    }

    public String executionByProtocol(
            Complex complex,
            Map<String, String> params,
            String certPath,
            String certPass,
            String filePath,
            String protocol) {
        return doUploadSslByProtocol(getUrl(complex), params, certPath, certPass, filePath, protocol);
    }

    /**
     * Executes a request with SSL.
     *
     * @param complex  The API URL.
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @return The result of the request.
     */
    public String execution(Complex complex, Map<String, String> params, InputStream certFile) {
        return doPostSsl(getUrl(complex), params, certFile);
    }

    /**
     * Executes a request with SSL and a specific protocol.
     *
     * @param complex  The API URL.
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String executionByProtocol(
            Complex complex,
            Map<String, String> params,
            InputStream certFile,
            String protocol) {
        return doPostSslByProtocol(getUrl(complex), params, certFile, protocol);
    }

    /**
     * Unified execution entry for V3 APIs.
     *
     * @param method       The HTTP method.
     * @param prefix       The URL prefix, obtainable via {@link Registry}.
     * @param suffix       The URL suffix, obtainable via {@link Complex}; URL parameters need to be appended manually.
     * @param mchId        The merchant ID.
     * @param serialNo     The merchant API certificate serial number.
     * @param platSerialNo The platform serial number, required for APIs containing sensitive information.
     * @param keyPath      The path to the apiclient_key.pem certificate.
     * @param body         The request body.
     * @param nonceStr     The random string.
     * @param timestamp    The timestamp.
     * @param authType     The authentication type.
     * @param file         The file to upload.
     * @return A {@link Message} with the result of the request.
     * @throws Exception if an error occurs during execution.
     */
    public Message v3(
            String method,
            String prefix,
            String suffix,
            String mchId,
            String serialNo,
            String platSerialNo,
            String keyPath,
            String body,
            String nonceStr,
            long timestamp,
            String authType,
            File file) throws Exception {
        String authorization = WechatPayBuilder
                .buildAuthorization(method, suffix, mchId, serialNo, keyPath, body, nonceStr, timestamp, authType);

        if (StringKit.isEmpty(platSerialNo)) {
            platSerialNo = serialNo;
        }

        if (HTTP.GET.equals(method)) {
            return get(prefix.concat(suffix), authorization, platSerialNo, null);
        } else if (HTTP.POST.equals(method)) {
            return post(prefix.concat(suffix), authorization, platSerialNo, body);
        } else if (HTTP.DELETE.equals(method)) {
            return delete(prefix.concat(suffix), authorization, platSerialNo, body);
        } else if (HTTP.PATCH.equals(method)) {
            return patch(prefix.concat(suffix), authorization, platSerialNo, body);
        } else if (HTTP.PUT.equals(method)) {
            return put(prefix.concat(suffix), authorization, platSerialNo, body);
        }
        return upload(prefix.concat(suffix), authorization, platSerialNo, body, file);
    }

    /**
     * Unified execution entry for V3 APIs.
     *
     * @param method       The HTTP method.
     * @param prefix       The URL prefix, obtainable via {@link Registry}.
     * @param suffix       The URL suffix, obtainable via {@link Complex}; URL parameters need to be appended manually.
     * @param mchId        The merchant ID.
     * @param serialNo     The merchant API certificate serial number.
     * @param platSerialNo The platform serial number, required for APIs containing sensitive information.
     * @param privateKey   The merchant's private key.
     * @param body         The request body.
     * @param nonceStr     The random string.
     * @param timestamp    The timestamp.
     * @param authType     The authentication type.
     * @param file         The file to upload.
     * @return A {@link Message} with the result of the request.
     * @throws Exception if an error occurs during execution.
     */
    public Message v3(
            String method,
            String prefix,
            String suffix,
            String mchId,
            String serialNo,
            String platSerialNo,
            PrivateKey privateKey,
            String body,
            String nonceStr,
            long timestamp,
            String authType,
            File file) throws Exception {
        String authorization = WechatPayBuilder
                .buildAuthorization(method, suffix, mchId, serialNo, privateKey, body, nonceStr, timestamp, authType);

        if (StringKit.isEmpty(platSerialNo)) {
            platSerialNo = serialNo;
        }

        if (HTTP.GET.equals(method)) {
            return get(prefix.concat(suffix), authorization, platSerialNo, null);
        } else if (HTTP.POST.equals(method)) {
            return post(prefix.concat(suffix), authorization, platSerialNo, body);
        } else if (HTTP.DELETE.equals(method)) {
            return delete(prefix.concat(suffix), authorization, platSerialNo, body);
        } else if (HTTP.PATCH.equals(method)) {
            return patch(prefix.concat(suffix), authorization, platSerialNo, body);
        } else if (HTTP.PUT.equals(method)) {
            return put(prefix.concat(suffix), authorization, platSerialNo, body);
        }
        return upload(prefix.concat(suffix), authorization, platSerialNo, body, file);
    }

    /**
     * Unified execution entry for V3 APIs.
     *
     * @param method       The HTTP method.
     * @param prefix       The URL prefix, obtainable via {@link Registry}.
     * @param suffix       The URL suffix, obtainable via {@link Complex}; URL parameters need to be appended manually.
     * @param mchId        The merchant ID.
     * @param serialNo     The merchant API certificate serial number.
     * @param platSerialNo The platform serial number.
     * @param keyPath      The path to the apiclient_key.pem certificate.
     * @param body         The request body.
     * @param authType     The authentication type.
     * @return A {@link Message} with the result of the request.
     * @throws Exception if an error occurs during execution.
     */
    public Message v3(
            String method,
            String prefix,
            String suffix,
            String mchId,
            String serialNo,
            String platSerialNo,
            String keyPath,
            String body,
            String authType) throws Exception {
        long timestamp = DateKit.current() / 1000;
        String nonceStr = String.valueOf(DateKit.current());
        return v3(
                method,
                prefix,
                suffix,
                mchId,
                serialNo,
                platSerialNo,
                keyPath,
                body,
                nonceStr,
                timestamp,
                authType,
                null);
    }

    /**
     * Unified execution entry for V3 APIs.
     *
     * @param method       The HTTP method.
     * @param prefix       The URL prefix, obtainable via {@link Registry}.
     * @param suffix       The URL suffix, obtainable via {@link Complex}; URL parameters need to be appended manually.
     * @param mchId        The merchant ID.
     * @param serialNo     The merchant API certificate serial number.
     * @param platSerialNo The platform serial number.
     * @param keyPath      The path to the apiclient_key.pem certificate.
     * @param body         The request body.
     * @return A {@link Message} with the result of the request.
     * @throws Exception if an error occurs during execution.
     */
    public Message v3(
            String method,
            String prefix,
            String suffix,
            String mchId,
            String serialNo,
            String platSerialNo,
            String keyPath,
            String body) throws Exception {
        String authType = AuthType.RSA.getCode();
        return v3(method, prefix, suffix, mchId, serialNo, platSerialNo, keyPath, body, authType);
    }

    /**
     * Unified execution entry for V3 APIs.
     *
     * @param method       The HTTP method.
     * @param prefix       The URL prefix, obtainable via {@link Registry}.
     * @param suffix       The URL suffix, obtainable via {@link Complex}; URL parameters need to be appended manually.
     * @param mchId        The merchant ID.
     * @param serialNo     The merchant API certificate serial number.
     * @param platSerialNo The platform serial number.
     * @param privateKey   The merchant's private key.
     * @param body         The request body.
     * @return A {@link Message} with the result of the request.
     * @throws Exception if an error occurs during execution.
     */
    public Message v3(
            String method,
            String prefix,
            String suffix,
            String mchId,
            String serialNo,
            String platSerialNo,
            PrivateKey privateKey,
            String body) throws Exception {
        long timestamp = System.currentTimeMillis() / 1000;
        String authType = AuthType.RSA.getCode();
        String nonceStr = String.valueOf(DateKit.current());
        return v3(
                method,
                prefix,
                suffix,
                mchId,
                serialNo,
                platSerialNo,
                privateKey,
                body,
                nonceStr,
                timestamp,
                authType,
                null);
    }

    /**
     * Unified execution entry for V3 APIs.
     *
     * @param method       The HTTP method.
     * @param prefix       The URL prefix, obtainable via {@link Registry}.
     * @param suffix       The URL suffix, obtainable via {@link Complex}; URL parameters need to be appended manually.
     * @param mchId        The merchant ID.
     * @param serialNo     The merchant API certificate serial number.
     * @param platSerialNo The platform serial number.
     * @param keyPath      The path to the apiclient_key.pem certificate.
     * @param params       The request parameters for a GET request.
     * @return A {@link Message} with the result of the request.
     * @throws Exception if an error occurs during execution.
     */
    public Message v3(
            String method,
            String prefix,
            String suffix,
            String mchId,
            String serialNo,
            String platSerialNo,
            String keyPath,
            Map<String, String> params) throws Exception {
        String authType = AuthType.RSA.getCode();
        return v3(method, prefix, suffix, mchId, serialNo, platSerialNo, keyPath, params, authType);
    }

    /**
     * Unified execution entry for V3 APIs.
     *
     * @param method       The HTTP method.
     * @param prefix       The URL prefix, obtainable via {@link Registry}.
     * @param suffix       The URL suffix, obtainable via {@link Complex}; URL parameters need to be appended manually.
     * @param mchId        The merchant ID.
     * @param serialNo     The merchant API certificate serial number.
     * @param platSerialNo The platform serial number.
     * @param keyPath      The path to the apiclient_key.pem certificate.
     * @param params       The request parameters for a GET request.
     * @param authType     The {@link AuthType} authorization type.
     * @return A {@link Message} with the result of the request.
     * @throws Exception if an error occurs during execution.
     */
    public Message v3(
            String method,
            String prefix,
            String suffix,
            String mchId,
            String serialNo,
            String platSerialNo,
            String keyPath,
            Map<String, String> params,
            String authType) throws Exception {
        long timestamp = System.currentTimeMillis() / 1000;
        String nonceStr = String.valueOf(DateKit.current());
        if (null != params && !params.keySet().isEmpty()) {
            suffix = suffix.concat("?").concat(Builder.createLinkString(params, true));
        }
        return v3(
                method,
                prefix,
                suffix,
                mchId,
                serialNo,
                platSerialNo,
                keyPath,
                "",
                nonceStr,
                timestamp,
                authType,
                null);
    }

    /**
     * Unified execution entry for V3 APIs.
     *
     * @param method       The HTTP method.
     * @param prefix       The URL prefix, obtainable via {@link Registry}.
     * @param suffix       The URL suffix, obtainable via {@link Complex}; URL parameters need to be appended manually.
     * @param mchId        The merchant ID.
     * @param serialNo     The merchant API certificate serial number.
     * @param platSerialNo The platform serial number.
     * @param privateKey   The merchant's private key.
     * @param params       The request parameters for a GET request.
     * @return A {@link Message} with the result of the request.
     * @throws Exception if an error occurs during execution.
     */
    public Message v3(
            String method,
            String prefix,
            String suffix,
            String mchId,
            String serialNo,
            String platSerialNo,
            PrivateKey privateKey,
            Map<String, String> params) throws Exception {
        long timestamp = System.currentTimeMillis() / 1000;
        String authType = AuthType.RSA.getCode();
        String nonceStr = String.valueOf(DateKit.current());
        if (null != params && !params.keySet().isEmpty()) {
            suffix = suffix.concat("?").concat(Builder.createLinkString(params, true));
        }
        return v3(
                method,
                prefix,
                suffix,
                mchId,
                serialNo,
                platSerialNo,
                privateKey,
                "",
                nonceStr,
                timestamp,
                authType,
                null);
    }

    /**
     * Unified execution entry for V3 APIs.
     *
     * @param prefix       The URL prefix, obtainable via {@link Registry}.
     * @param suffix       The URL suffix, obtainable via {@link Complex}; URL parameters need to be appended manually.
     * @param mchId        The merchant ID.
     * @param serialNo     The merchant API certificate serial number.
     * @param platSerialNo The platform serial number.
     * @param keyPath      The path to the apiclient_key.pem certificate.
     * @param body         The request body.
     * @param file         The file to upload.
     * @return A {@link Message} with the result of the request.
     * @throws Exception if an error occurs during execution.
     */
    public Message v3(
            String prefix,
            String suffix,
            String mchId,
            String serialNo,
            String platSerialNo,
            String keyPath,
            String body,
            File file) throws Exception {
        long timestamp = System.currentTimeMillis() / 1000;
        String authType = AuthType.RSA.getCode();
        String nonceStr = String.valueOf(DateKit.current());
        return v3(
                null,
                prefix,
                suffix,
                mchId,
                serialNo,
                platSerialNo,
                keyPath,
                body,
                nonceStr,
                timestamp,
                authType,
                file);
    }

    /**
     * Unified execution entry for V3 APIs.
     *
     * @param prefix       The URL prefix, obtainable via {@link Registry}.
     * @param suffix       The URL suffix, obtainable via {@link Complex}; URL parameters need to be appended manually.
     * @param mchId        The merchant ID.
     * @param serialNo     The merchant API certificate serial number.
     * @param platSerialNo The platform serial number.
     * @param privateKey   The merchant's private key.
     * @param body         The request body.
     * @param file         The file to upload.
     * @return A {@link Message} with the result of the request.
     * @throws Exception if an error occurs during execution.
     */
    public Message v3(
            String prefix,
            String suffix,
            String mchId,
            String serialNo,
            String platSerialNo,
            PrivateKey privateKey,
            String body,
            File file) throws Exception {
        long timestamp = System.currentTimeMillis() / 1000;
        String authType = AuthType.RSA.getCode();
        String nonceStr = String.valueOf(DateKit.current());
        return v3(
                null,
                prefix,
                suffix,
                mchId,
                serialNo,
                platSerialNo,
                privateKey,
                body,
                nonceStr,
                timestamp,
                authType,
                file);
    }

    /**
     * Gets the signing key from the API.
     *
     * @param mchId      The merchant ID.
     * @param partnerKey The API key.
     * @param algorithm  The signing algorithm.
     * @return The result of the request.
     */
    public String getSignKey(String mchId, String partnerKey, Algorithm algorithm) {
        Map<String, String> map = new HashMap<>(3);
        String nonceStr = String.valueOf(DateKit.current());
        map.put("mch_id", mchId);
        map.put("nonce_str", nonceStr);
        map.put("sign", WechatPayBuilder.createSign(map, partnerKey, algorithm));
        return execution(PayApi.GET_SIGN_KEY, map);
    }

    /**
     * Creates a unified order.
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String pushOrder(Map<String, String> params) {
        return execution(PayApi.UNIFIED_ORDER, params);
    }

    /**
     * Queries an order.
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String orderQuery(Map<String, String> params) {
        return execution(PayApi.ORDER_QUERY, params);
    }

    /**
     * Closes an order.
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String closeOrder(Map<String, String> params) {
        return execution(PayApi.CLOSE_ORDER, params);
    }

    /**
     * Reverses an order.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String orderReverse(Map<String, String> params, String certPath, String certPass) {
        return execution(PayApi.REVERSE, params, certPath, certPass);
    }

    /**
     * Reverses an order.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String orderReverse(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(PayApi.REVERSE, params, certFile, certPass);
    }

    /**
     * Applies for a refund.
     *
     * @param isSandbox Whether it is a sandbox environment.
     * @param params    The request parameters.
     * @param certPath  The path to the certificate file.
     * @param certPass  The certificate password.
     * @return The result of the request.
     */
    public String orderRefund(boolean isSandbox, Map<String, String> params, String certPath, String certPass) {
        return execution(PayApi.REFUND, params, certPath, certPass);
    }

    /**
     * Applies for a refund with a specific protocol.
     *
     * @param isSandbox Whether it is a sandbox environment.
     * @param params    The request parameters.
     * @param certPath  The path to the certificate file.
     * @param certPass  The certificate password.
     * @return The result of the request.
     */
    public String orderRefundByProtocol(
            boolean isSandbox,
            Map<String, String> params,
            String certPath,
            String certPass,
            String protocol) {
        return executionByProtocol(PayApi.REFUND, params, certPath, certPass, protocol);
    }

    /**
     * Applies for a refund.
     *
     * @param isSandbox Whether it is a sandbox environment.
     * @param params    The request parameters.
     * @param certFile  The input stream of the certificate file.
     * @param certPass  The certificate password.
     * @return The result of the request.
     */
    public String orderRefund(boolean isSandbox, Map<String, String> params, InputStream certFile, String certPass) {
        return execution(PayApi.REFUND, params, certFile, certPass);
    }

    /**
     * Applies for a refund with a specific protocol.
     *
     * @param isSandbox Whether it is a sandbox environment.
     * @param params    The request parameters.
     * @param certFile  The input stream of the certificate file.
     * @param certPass  The certificate password.
     * @param protocol  The protocol.
     * @return The result of the request.
     */
    public String orderRefundByProtocol(
            boolean isSandbox,
            Map<String, String> params,
            InputStream certFile,
            String certPass,
            String protocol) {
        return executionByProtocol(PayApi.REFUND, params, certFile, certPass, protocol);
    }

    /**
     * Queries a refund.
     *
     * @param isSandbox Whether it is a sandbox environment.
     * @param params    The request parameters.
     * @return The result of the request.
     */
    public String orderRefundQuery(boolean isSandbox, Map<String, String> params) {
        return execution(PayApi.REFUND_QUERY, params);
    }

    /**
     * Downloads a bill.
     *
     * @param isSandbox Whether it is a sandbox environment.
     * @param params    The request parameters.
     * @return The result of the request.
     */
    public String downloadBill(boolean isSandbox, Map<String, String> params) {
        return execution(PayApi.DOWNLOAD_BILL, params);
    }

    /**
     * Reports a transaction.
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String orderReport(Map<String, String> params) {
        return execution(PayApi.REPORT, params);
    }

    /**
     * Converts a URL to a short URL.
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String toShortUrl(Map<String, String> params) {
        return execution(PayApi.SHORT_URL, params);
    }

    /**
     * Queries an OpenID from an authorization code.
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String authCodeToOpenid(Map<String, String> params) {
        return execution(PayApi.AUTH_CODE_TO_OPENID, params);
    }

    /**
     * Performs a micropay.
     *
     * @param isSandbox Whether it is a sandbox environment.
     * @param params    The request parameters.
     * @return The result of the request.
     */
    public String microPay(boolean isSandbox, Map<String, String> params) {
        return execution(PayApi.MICRO_PAY, params);
    }

    /**
     * Transfers funds to a user's wallet.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String transfers(Map<String, String> params, String certPath, String certPass) {
        return execution(TransferApi.TRANSFER, params, certPath, certPass);
    }

    /**
     * Transfers funds to a user's wallet with a specific protocol.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String transfersByProtocol(Map<String, String> params, String certPath, String certPass, String protocol) {
        return executionByProtocol(TransferApi.TRANSFER, params, certPath, certPass, protocol);
    }

    /**
     * Transfers funds to a user's wallet.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String transfers(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(TransferApi.TRANSFER, params, certFile, certPass);
    }

    /**
     * Transfers funds to a user's wallet with a specific protocol.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String transfersByProtocol(
            Map<String, String> params,
            InputStream certFile,
            String certPass,
            String protocol) {
        return executionByProtocol(TransferApi.TRANSFER, params, certFile, certPass, protocol);
    }

    /**
     * Queries a transfer to a user's wallet.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String getTransferInfo(Map<String, String> params, String certPath, String certPass) {
        return execution(TransferApi.GET_TRANSFER_INFO, params, certPath, certPass);
    }

    /**
     * Queries a transfer to a user's wallet.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String getTransferInfo(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(TransferApi.GET_TRANSFER_INFO, params, certFile, certPass);
    }

    /**
     * Pays to a bank.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String payBank(Map<String, String> params, String certPath, String certPass) {
        return execution(TransferApi.TRANSFER_BANK, params, certPath, certPass);
    }

    /**
     * Pays to a bank with a specific protocol.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String payBankByProtocol(Map<String, String> params, String certPath, String certPass, String protocol) {
        return executionByProtocol(TransferApi.TRANSFER_BANK, params, certPath, certPass, protocol);
    }

    /**
     * Pays to a bank.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String payBank(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(TransferApi.TRANSFER_BANK, params, certFile, certPass);
    }

    /**
     * Pays to a bank with a specific protocol.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String payBankByProtocol(
            Map<String, String> params,
            InputStream certFile,
            String certPass,
            String protocol) {
        return executionByProtocol(TransferApi.TRANSFER_BANK, params, certFile, certPass, protocol);
    }

    /**
     * Queries a payment to a bank.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String queryBank(Map<String, String> params, String certPath, String certPass) {
        return execution(TransferApi.GET_TRANSFER_BANK_INFO, params, certPath, certPass);
    }

    /**
     * Queries a payment to a bank.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String queryBank(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(TransferApi.GET_TRANSFER_BANK_INFO, params, certFile, certPass);
    }

    /**
     * Gets the RSA public key.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String getPublicKey(Map<String, String> params, String certPath, String certPass) {
        return execution(TransferApi.GET_PUBLIC_KEY, params, certPath, certPass);
    }

    /**
     * Gets the RSA public key with a specific protocol.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String getPublicKeyByProtocol(
            Map<String, String> params,
            String certPath,
            String certPass,
            String protocol) {
        return execution(TransferApi.GET_PUBLIC_KEY, params, certPath, certPass, protocol);
    }

    /**
     * Gets the RSA public key.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String getPublicKey(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(TransferApi.GET_PUBLIC_KEY, params, certFile, certPass);
    }

    /**
     * Gets the RSA public key with a specific protocol.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String getPublicKeyByProtocol(
            Map<String, String> params,
            InputStream certFile,
            String certPass,
            String protocol) {
        return executionByProtocol(TransferApi.GET_PUBLIC_KEY, params, certFile, certPass, protocol);
    }

    /**
     * Official Account pure contract signing.
     *
     * @param params   The request parameters.
     * @param payModel The merchant platform mode.
     * @return The result of the request.
     */
    public String entrustWeb(Map<String, String> params, Mode payModel) {
        if (payModel == Mode.SELLER) {
            return executionByGet(EntrustPayApi.ENTRUST_WEB, params);
        } else {
            return executionByGet(EntrustPayApi.PARTNER_ENTRUST_WEB, params);
        }
    }

    /**
     * APP pure contract signing.
     *
     * @param params   The request parameters.
     * @param payModel The merchant platform mode.
     * @return The result of the request.
     */
    public String preEntrustWeb(Map<String, String> params, Mode payModel) {
        if (payModel == Mode.SELLER) {
            return executionByGet(EntrustPayApi.PRE_ENTRUST_WEB, params);
        } else {
            return executionByGet(EntrustPayApi.PARTNER_PRE_ENTRUST_WEB, params);
        }
    }

    /**
     * H5 pure contract signing.
     *
     * @param params   The request parameters.
     * @param payModel The merchant platform mode.
     * @return The result of the request.
     */
    public String h5EntrustWeb(Map<String, String> params, Mode payModel) {
        if (payModel == Mode.SELLER) {
            return executionByGet(EntrustPayApi.H5_ENTRUST_WEB, params);
        } else {
            return executionByGet(EntrustPayApi.PARTNER_H5_ENTRUST_WEB, params);
        }
    }

    /**
     * Contract signing during payment.
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String contractOrder(Map<String, String> params) {
        return execution(EntrustPayApi.PAY_CONTRACT_ORDER, params);
    }

    /**
     * Queries a contract relationship.
     *
     * @param params   The request parameters.
     * @param payModel The merchant platform mode.
     * @return The result of the request.
     */
    public String queryContract(Map<String, String> params, Mode payModel) {
        if (payModel == Mode.SELLER) {
            return execution(EntrustPayApi.QUERY_ENTRUST_CONTRACT, params);
        } else {
            return execution(EntrustPayApi.PARTNER_QUERY_ENTRUST_CONTRACT, params);
        }
    }

    /**
     * Applies for a deduction.
     *
     * @param params   The request parameters.
     * @param payModel The merchant platform mode.
     * @return The result of the request.
     */
    public String papPayApply(Map<String, String> params, Mode payModel) {
        if (payModel == Mode.SELLER) {
            return execution(EntrustPayApi.PAP_PAY_APPLY, params);
        } else {
            return execution(EntrustPayApi.PARTNER_PAP_PAY_APPLY, params);
        }
    }

    /**
     * Applies to terminate a contract.
     *
     * @param params   The request parameters.
     * @param payModel The merchant platform mode.
     * @return The result of the request.
     */
    public String deleteContract(Map<String, String> params, Mode payModel) {
        if (payModel == Mode.SELLER) {
            return execution(EntrustPayApi.DELETE_ENTRUST_CONTRACT, params);
        } else {
            return execution(EntrustPayApi.PARTNER_DELETE_ENTRUST_CONTRACT, params);
        }
    }

    /**
     * Queries a contract bill.
     *
     * @param params   The request parameters.
     * @param payModel The merchant platform mode.
     * @return The result of the request.
     */
    public String contractBill(Map<String, String> params, Mode payModel) {
        if (payModel == Mode.SELLER) {
            return execution(EntrustPayApi.QUERY_ENTRUST_CONTRACT, params);
        } else {
            return execution(EntrustPayApi.PARTNER_QUERY_ENTRUST_CONTRACT, params);
        }
    }

    /**
     * Requests a single profit sharing.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String profitSharing(Map<String, String> params, String certPath, String certPass) {
        return execution(ProfitSharingApi.PROFIT_SHARING, params, certPath, certPass);
    }

    /**
     * Requests a single profit sharing.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String profitSharing(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(ProfitSharingApi.PROFIT_SHARING, params, certFile, certPass);
    }

    /**
     * Requests multiple profit sharings.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String multiProfitSharing(Map<String, String> params, String certPath, String certPass) {
        return execution(ProfitSharingApi.MULTI_PROFIT_SHARING, params, certPath, certPass);
    }

    /**
     * Requests multiple profit sharings.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String multiProfitSharing(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(ProfitSharingApi.MULTI_PROFIT_SHARING, params, certFile, certPass);
    }

    /**
     * Queries a profit sharing result.
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String profitSharingQuery(Map<String, String> params) {
        return execution(ProfitSharingApi.PROFIT_SHARING_QUERY, params);
    }

    /**
     * Adds a profit sharing receiver.
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String profitSharingAddReceiver(Map<String, String> params) {
        return execution(ProfitSharingApi.PROFIT_SHARING_ADD_RECEIVER, params);
    }

    /**
     * Removes a profit sharing receiver.
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String profitSharingRemoveReceiver(Map<String, String> params) {
        return execution(ProfitSharingApi.PROFIT_SHARING_REMOVE_RECEIVER, params);
    }

    /**
     * Finishes a profit sharing.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String profitSharingFinish(Map<String, String> params, String certPath, String certPass) {
        return execution(ProfitSharingApi.PROFIT_SHARING_FINISH, params, certPath, certPass);
    }

    /**
     * Finishes a profit sharing.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String profitSharingFinish(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(ProfitSharingApi.PROFIT_SHARING_FINISH, params, certFile, certPass);
    }

    /**
     * Returns a profit sharing.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String profitSharingReturn(Map<String, String> params, String certPath, String certPass) {
        return execution(ProfitSharingApi.PROFIT_SHARING_RETURN, params, certPath, certPass);
    }

    /**
     * Returns a profit sharing.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String profitSharingReturn(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(ProfitSharingApi.PROFIT_SHARING_RETURN, params, certFile, certPass);
    }

    /**
     * Queries a profit sharing return.
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String profitSharingReturnQuery(Map<String, String> params) {
        return execution(ProfitSharingApi.PROFIT_SHARING_RETURN_QUERY, params);
    }

    /**
     * Sends a coupon.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String sendCoupon(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(CouponApi.SEND_COUPON, params, certFile, certPass);
    }

    /**
     * Queries a coupon stock.
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String queryCouponStock(Map<String, String> params) {
        return execution(CouponApi.QUERY_COUPON_STOCK, params);
    }

    /**
     * Queries coupon information.
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String queryCouponsInfo(Map<String, String> params) {
        return execution(CouponApi.QUERY_COUPONS_INFO, params);
    }

    /**
     * Face payment deposit.
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String depositFacePay(Map<String, String> params) {
        return execution(DepositApi.FACE_PAY, params);
    }

    /**
     * Micropay deposit.
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String depositMicroPay(Map<String, String> params) {
        return execution(DepositApi.MICRO_PAY, params);
    }

    /**
     * Queries an order.
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String depositOrderQuery(Map<String, String> params) {
        return execution(DepositApi.ORDER_QUERY, params);
    }

    /**
     * Reverses an order.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String depositReverse(Map<String, String> params, String certPath, String certPass) {
        return execution(DepositApi.REVERSE, params, certPath, certPass);
    }

    /**
     * Reverses an order.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String depositReverse(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(DepositApi.REVERSE, params, certFile, certPass);
    }

    /**
     * Consumes a deposit.
     *
     * @param params   The request parameters.
     * @param certPath The directory of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String depositConsume(Map<String, String> params, String certPath, String certPass) {
        return execution(DepositApi.CONSUME, params, certPath, certPass);
    }

    /**
     * Consumes a deposit.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String depositConsume(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(DepositApi.CONSUME, params, certFile, certPass);
    }

    /**
     * Applies for a refund (deposit).
     *
     * @param params   The request parameters.
     * @param certPath The directory of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String depositRefund(Map<String, String> params, String certPath, String certPass) {
        return execution(DepositApi.REFUND, params, certPath, certPass);
    }

    /**
     * Applies for a refund (deposit).
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String depositRefund(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(DepositApi.REFUND, params, certFile, certPass);
    }

    /**
     * Queries a refund (deposit).
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String depositRefundQuery(Map<String, String> params) {
        return execution(DepositApi.REFUND_QUERY, params);
    }

    /**
     * Downloads a fund flow bill.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String downloadFundFlow(Map<String, String> params, String certPath, String certPass) {
        return execution(PayApi.DOWNLOAD_FUND_FLOW, params, certPath, certPass);
    }

    /**
     * Downloads a fund flow bill.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String downloadFundFlow(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(PayApi.DOWNLOAD_FUND_FLOW, params, certFile, certPass);
    }

    /**
     * Gets the device invocation credentials for face payment.
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String getAuthInfo(Map<String, String> params) {
        return execution(FacePayApi.GET_AUTH_INFO, params);
    }

    /**
     * Performs a face payment.
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String facePay(Map<String, String> params) {
        return execution(FacePayApi.FACE_PAY, params);
    }

    /**
     * Queries a face payment order.
     *
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String facePayQuery(Map<String, String> params) {
        return execution(FacePayApi.FACE_PAY_QUERY, params);
    }

    /**
     * Reverses a face payment order.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String facePayReverse(Map<String, String> params, String certPath, String certPass) {
        return execution(FacePayApi.FACE_PAY_REVERSE, params, certPath, certPass);
    }

    /**
     * Reverses a face payment order.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String facePayReverse(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(FacePayApi.FACE_PAY_REVERSE, params, certFile, certPass);
    }

    /**
     * Sends a normal red packet.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String sendRedPack(Map<String, String> params, String certPath, String certPass) {
        return execution(RedPackApi.SEND_RED_PACK, params, certPath, certPass);
    }

    /**
     * Sends a normal red packet with a specific protocol.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String sendRedPackByProtocol(Map<String, String> params, String certPath, String certPass, String protocol) {
        return executionByProtocol(RedPackApi.SEND_RED_PACK, params, certPath, certPass, protocol);
    }

    /**
     * Sends a normal red packet.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String sendRedPack(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(RedPackApi.SEND_RED_PACK, params, certFile, certPass);
    }

    /**
     * Sends a normal red packet with a specific protocol.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String sendRedPackByProtocol(
            Map<String, String> params,
            InputStream certFile,
            String certPass,
            String protocol) {
        return executionByProtocol(RedPackApi.SEND_RED_PACK, params, certFile, certPass, protocol);
    }

    /**
     * Sends a fission red packet.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String sendGroupRedPack(Map<String, String> params, String certPath, String certPass) {
        return execution(RedPackApi.SEND_GROUP_RED_PACK, params, certPath, certPass);
    }

    /**
     * Sends a fission red packet with a specific protocol.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String sendGroupRedPackByProtocol(
            Map<String, String> params,
            String certPath,
            String certPass,
            String protocol) {
        return executionByProtocol(RedPackApi.SEND_GROUP_RED_PACK, params, certPath, certPass, protocol);
    }

    /**
     * Sends a fission red packet.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String sendGroupRedPack(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(RedPackApi.SEND_GROUP_RED_PACK, params, certFile, certPass);
    }

    /**
     * Sends a fission red packet with a specific protocol.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String sendGroupRedPackByProtocol(
            Map<String, String> params,
            InputStream certFile,
            String certPass,
            String protocol) {
        return executionByProtocol(RedPackApi.SEND_GROUP_RED_PACK, params, certFile, certPass, protocol);
    }

    /**
     * Queries a red packet record.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String getHbInfo(Map<String, String> params, String certPath, String certPass) {
        return execution(RedPackApi.GET_HB_INFO, params, certPath, certPass);
    }

    /**
     * Queries a red packet record.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String getHbInfo(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(RedPackApi.GET_HB_INFO, params, certFile, certPass);
    }

    /**
     * Sends a Mini Program red packet.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String sendMiniProgramRedPack(Map<String, String> params, String certPath, String certPass) {
        return execution(RedPackApi.SEND_MINI_PROGRAM_HB, params, certPath, certPass);
    }

    /**
     * Sends a Mini Program red packet with a specific protocol.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String sendMiniProgramRedPackByProtocol(
            Map<String, String> params,
            String certPath,
            String certPass,
            String protocol) {
        return executionByProtocol(RedPackApi.SEND_MINI_PROGRAM_HB, params, certPath, certPass, protocol);
    }

    /**
     * Sends a Mini Program red packet.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String sendMiniProgramRedPack(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(RedPackApi.SEND_MINI_PROGRAM_HB, params, certFile, certPass);
    }

    /**
     * Sends a Mini Program red packet with a specific protocol.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String sendMiniProgramRedPackByProtocol(
            Map<String, String> params,
            InputStream certFile,
            String certPass,
            String protocol) {
        return executionByProtocol(RedPackApi.SEND_MINI_PROGRAM_HB, params, certFile, certPass, protocol);
    }

    /**
     * Sends a corporate red packet.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String sendWorkWxRedPack(Map<String, String> params, String certPath, String certPass) {
        return execution(RedPackApi.SEND_WORK_WX_RED_PACK, params, certPath, certPass);
    }

    /**
     * Sends a corporate red packet with a specific protocol.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String sendWorkWxRedPackByProtocol(
            Map<String, String> params,
            String certPath,
            String certPass,
            String protocol) {
        return executionByProtocol(RedPackApi.SEND_WORK_WX_RED_PACK, params, certPath, certPass, protocol);
    }

    /**
     * Sends a corporate red packet.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String sendWorkWxRedPack(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(RedPackApi.SEND_WORK_WX_RED_PACK, params, certFile, certPass);
    }

    /**
     * Sends a corporate red packet with a specific protocol.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String sendWorkWxRedPackByProtocol(
            Map<String, String> params,
            InputStream certFile,
            String certPass,
            String protocol) {
        return executionByProtocol(RedPackApi.SEND_WORK_WX_RED_PACK, params, certFile, certPass, protocol);
    }

    /**
     * Queries a payment record to an employee.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String queryWorkWxRedPack(Map<String, String> params, String certPath, String certPass) {
        return execution(RedPackApi.QUERY_WORK_WX_RED_PACK, params, certPath, certPass);
    }

    /**
     * Queries a payment record to an employee with a specific protocol.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String queryWorkWxRedPackByProtocol(
            Map<String, String> params,
            String certPath,
            String certPass,
            String protocol) {
        return executionByProtocol(RedPackApi.QUERY_WORK_WX_RED_PACK, params, certPath, certPass, protocol);
    }

    /**
     * Queries a payment record to an employee.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String queryWorkWxRedPack(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(RedPackApi.QUERY_WORK_WX_RED_PACK, params, certFile, certPass);
    }

    /**
     * Queries a payment record to an employee with a specific protocol.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String queryWorkWxRedPackByProtocol(
            Map<String, String> params,
            InputStream certFile,
            String certPass,
            String protocol) {
        return executionByProtocol(RedPackApi.QUERY_WORK_WX_RED_PACK, params, certFile, certPass, protocol);
    }

    /**
     * Pays an employee.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String trans2pocket(Map<String, String> params, String certPath, String certPass) {
        return execution(TransferApi.PAY_WWS_TRANS_2_POCKET, params, certPath, certPass);
    }

    /**
     * Pays an employee with a specific protocol.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String trans2pocketByProtocol(
            Map<String, String> params,
            String certPath,
            String certPass,
            String protocol) {
        return executionByProtocol(TransferApi.PAY_WWS_TRANS_2_POCKET, params, certPath, certPass, protocol);
    }

    /**
     * Pays an employee.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String trans2pocket(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(TransferApi.PAY_WWS_TRANS_2_POCKET, params, certFile, certPass);
    }

    /**
     * Pays an employee with a specific protocol.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String trans2pocketByProtocol(
            Map<String, String> params,
            InputStream certFile,
            String certPass,
            String protocol) {
        return executionByProtocol(TransferApi.PAY_WWS_TRANS_2_POCKET, params, certFile, certPass, protocol);
    }

    /**
     * Queries a payment record to an employee.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String queryTrans2pocket(Map<String, String> params, String certPath, String certPass) {
        return execution(TransferApi.QUERY_WWS_TRANS_2_POCKET, params, certPath, certPass);
    }

    /**
     * Queries a payment record to an employee with a specific protocol.
     *
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String queryTrans2pocketByProtocol(
            Map<String, String> params,
            String certPath,
            String certPass,
            String protocol) {
        return executionByProtocol(TransferApi.QUERY_WWS_TRANS_2_POCKET, params, certPath, certPass, protocol);
    }

    /**
     * Queries a payment record to an employee.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String queryTrans2pocket(Map<String, String> params, InputStream certFile, String certPass) {
        return execution(TransferApi.QUERY_WWS_TRANS_2_POCKET, params, certFile, certPass);
    }

    /**
     * Queries a payment record to an employee with a specific protocol.
     *
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String queryTrans2pocket(
            Map<String, String> params,
            InputStream certFile,
            String certPass,
            String protocol) {
        return executionByProtocol(TransferApi.QUERY_WWS_TRANS_2_POCKET, params, certFile, certPass, protocol);
    }

    /**
     * Mini Program virtual payment API.
     *
     * @param apiEnum     The API enum.
     * @param appKey      The application secret.
     * @param accessToken The Mini Program token.
     * @param postBody    The POST data body.
     * @return A {@link Message} with the result of the request.
     */
    public Message xPay(Complex apiEnum, String appKey, String accessToken, String postBody) {
        String url = apiEnum.service();
        String needSignMsg = url.concat("&").concat(postBody);
        String paySig = org.miaixz.bus.crypto.Builder.hmacSha256(appKey).digestHex(needSignMsg);
        url = url.concat("?access_token=").concat(accessToken).concat("&pay_sig=").concat(paySig);
        return post(url, postBody, null);
    }

    /**
     * Sends a GET request.
     *
     * @param url    The request URL.
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String doGet(String url, Map<String, String> params) {
        return get(url, params);
    }

    /**
     * Sends a GET request.
     *
     * @param url           The request URL.
     * @param authorization The authorization information.
     * @param serialNumber  The public key certificate serial number.
     * @param params        The request parameters.
     * @return A {@link Message} with the result of the request.
     */
    public Message get(String url, String authorization, String serialNumber, Map<String, String> params) {
        return get(url, params, WechatPayBuilder.getHeaders(authorization, serialNumber));
    }

    /**
     * Sends a POST request.
     *
     * @param url           The request URL.
     * @param authorization The authorization information.
     * @param serialNumber  The public key certificate serial number.
     * @param data          The request data.
     * @return A {@link Message} with the result of the request.
     */
    public Message post(String url, String authorization, String serialNumber, String data) {
        return post(url, data, WechatPayBuilder.getHeaders(authorization, serialNumber));
    }

    /**
     * Sends a DELETE request.
     *
     * @param url           The request URL.
     * @param authorization The authorization information.
     * @param serialNumber  The public key certificate serial number.
     * @param data          The request data.
     * @return A {@link Message} with the result of the request.
     */
    public Message delete(String url, String authorization, String serialNumber, String data) {
        return post(url, data, WechatPayBuilder.getHeaders(authorization, serialNumber));
    }

    /**
     * Sends an upload request.
     *
     * @param url     The request URL.
     * @param params  The request parameters.
     * @param headers The request headers.
     * @return A {@link Message} with the result of the request.
     */
    public Message upload(String url, Map<String, String> params, Map<String, String> headers) {
        return post(url, params, headers);
    }

    /**
     * Sends an upload request.
     *
     * @param url           The request URL.
     * @param authorization The authorization information.
     * @param serialNumber  The public key certificate serial number.
     * @param data          The request data.
     * @param file          The file to upload.
     * @return A {@link Message} with the result of the request.
     */
    public Message upload(String url, String authorization, String serialNumber, String data, File file) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("meta", data);
        return post(url, paramMap, WechatPayBuilder.getUploadHeaders(authorization, serialNumber), file);
    }

    /**
     * Sends a PATCH request.
     *
     * @param url           The request URL.
     * @param authorization The authorization information.
     * @param serialNumber  The public key certificate serial number.
     * @param data          The request data.
     * @return A {@link Message} with the result of the request.
     */
    public Message patch(String url, String authorization, String serialNumber, String data) {
        return post(url, data, WechatPayBuilder.getHeaders(authorization, serialNumber));
    }

    /**
     * Sends a PUT request.
     *
     * @param url           The request URL.
     * @param authorization The authorization information.
     * @param serialNumber  The public key certificate serial number.
     * @param data          The request data.
     * @return A {@link Message} with the result of the request.
     */
    public Message put(String url, String authorization, String serialNumber, String data) {
        return put(url, data, WechatPayBuilder.getHeaders(authorization, serialNumber));
    }

    /**
     * Sends a POST request with parameters converted to XML.
     *
     * @param url    The request URL.
     * @param params The request parameters.
     * @return The result of the request.
     */
    public String doPost(String url, Map<String, String> params) {
        return post(url, XmlKit.mapToXmlString(params));
    }

    /**
     * Sends a POST request with SSL.
     *
     * @param url      The request URL.
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String doPostSsl(String url, Map<String, String> params, String certPath, String certPass) {
        return post(url, XmlKit.mapToXmlString(params), certPath, certPass, null);
    }

    /**
     * Sends a POST request with SSL and a specific protocol.
     *
     * @param url      The request URL.
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String doPostSslByProtocol(
            String url,
            Map<String, String> params,
            String certPath,
            String certPass,
            String protocol) {
        return post(url, XmlKit.mapToXmlString(params), certPath, certPass, protocol);
    }

    /**
     * Sends a POST request with SSL.
     *
     * @param url      The request URL.
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @return The result of the request.
     */
    public String doPostSsl(String url, Map<String, String> params, String certPath) {
        if (params.isEmpty() || !params.containsKey("mch_id")) {
            throw new RuntimeException(
                    "Request parameters must contain mch_id. If the API does not include mch_id, please use another constructor.");
        }
        String certPass = params.get("mch_id");
        return doPostSsl(url, params, certPath, certPass);
    }

    /**
     * Sends a POST request with SSL and a specific protocol.
     *
     * @param url      The request URL.
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String doPostSslByProtocol(String url, Map<String, String> params, String certPath, String protocol) {
        if (params.isEmpty() || !params.containsKey("mch_id")) {
            throw new RuntimeException(
                    "Request parameters must contain mch_id. If the API does not include mch_id, please use another constructor.");
        }
        String certPass = params.get("mch_id");
        return doPostSslByProtocol(url, params, certPath, certPass, protocol);
    }

    /**
     * Sends a POST request with SSL.
     *
     * @param url      The request URL.
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @return The result of the request.
     */
    public String doPostSsl(String url, Map<String, String> params, InputStream certFile) {
        if (params.isEmpty() || !params.containsKey("mch_id")) {
            throw new RuntimeException(
                    "Request parameters must contain mch_id. If the API does not include mch_id, please use another constructor.");
        }
        String certPass = params.get("mch_id");
        return doPostSsl(url, params, certFile, certPass);
    }

    /**
     * Sends a POST request with SSL and a specific protocol.
     *
     * @param url      The request URL.
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String doPostSslByProtocol(String url, Map<String, String> params, InputStream certFile, String protocol) {
        if (params.isEmpty() || !params.containsKey("mch_id")) {
            throw new RuntimeException(
                    "Request parameters must contain mch_id. If the API does not include mch_id, please use another constructor.");
        }
        String certPass = params.get("mch_id");
        return doPostSslByProtocol(url, params, certFile, certPass, protocol);
    }

    /**
     * Sends a POST request with SSL.
     *
     * @param url      The request URL.
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @return The result of the request.
     */
    public String doPostSsl(String url, Map<String, String> params, InputStream certFile, String certPass) {
        return post(url, XmlKit.mapToXmlString(params), certFile, certPass, null);
    }

    /**
     * Sends a POST request with SSL and a specific protocol.
     *
     * @param url      The request URL.
     * @param params   The request parameters.
     * @param certFile The input stream of the certificate file.
     * @param certPass The certificate password.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String doPostSslByProtocol(
            String url,
            Map<String, String> params,
            InputStream certFile,
            String certPass,
            String protocol) {
        return post(url, XmlKit.mapToXmlString(params), certFile, certPass, protocol);
    }

    /**
     * Sends an upload request with SSL.
     *
     * @param url      The request URL.
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param filePath The path to the file to upload.
     * @return The result of the request.
     */
    public String doUploadSsl(String url, Map<String, String> params, String certPath, String filePath) {
        if (params.isEmpty() || !params.containsKey("mch_id")) {
            throw new RuntimeException(
                    "Request parameters must contain mch_id. If the API does not include mch_id, please use another constructor.");
        }
        String certPass = params.get("mch_id");
        return doUploadSsl(url, params, certPath, certPass, filePath);
    }

    /**
     * Sends an upload request with SSL and a specific protocol.
     *
     * @param url      The request URL.
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param filePath The path to the file to upload.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String doUploadSslByProtocol(
            String url,
            Map<String, String> params,
            String certPath,
            String filePath,
            String protocol) {
        if (params.isEmpty() || !params.containsKey("mch_id")) {
            throw new RuntimeException(
                    "Request parameters must contain mch_id. If the API does not include mch_id, please use another constructor.");
        }
        String certPass = params.get("mch_id");
        return doUploadSslByProtocol(url, params, certPath, certPass, filePath, protocol);
    }

    /**
     * Sends an upload request with SSL.
     *
     * @param url      The request URL.
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @param filePath The path to the file to upload.
     * @return The result of the request.
     */
    public String doUploadSsl(
            String url,
            Map<String, String> params,
            String certPath,
            String certPass,
            String filePath) {
        return upload(url, XmlKit.mapToXmlString(params), certPath, certPass, filePath);
    }

    /**
     * Sends an upload request with SSL and a specific protocol.
     *
     * @param url      The request URL.
     * @param params   The request parameters.
     * @param certPath The path to the certificate file.
     * @param certPass The certificate password.
     * @param filePath The path to the file to upload.
     * @param protocol The protocol.
     * @return The result of the request.
     */
    public String doUploadSslByProtocol(
            String url,
            Map<String, String> params,
            String certPath,
            String certPass,
            String filePath,
            String protocol) {
        return upload(url, XmlKit.mapToXmlString(params), certPath, certPass, filePath, protocol);
    }

}
