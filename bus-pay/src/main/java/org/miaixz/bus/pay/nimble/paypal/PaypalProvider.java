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
package org.miaixz.bus.pay.nimble.paypal;

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.pay.Complex;
import org.miaixz.bus.pay.Context;
import org.miaixz.bus.pay.Registry;
import org.miaixz.bus.pay.cache.PayCache;
import org.miaixz.bus.pay.magic.Message;
import org.miaixz.bus.pay.magic.Voucher;
import org.miaixz.bus.pay.nimble.AbstractProvider;
import org.miaixz.bus.pay.nimble.paypal.api.PayPalApi;
import org.miaixz.bus.pay.nimble.paypal.entity.AccessToken;

/**
 * PayPal API provider.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PaypalProvider extends AbstractProvider<Voucher, Context> {

    /**
     * Constructs a new PaypalProvider.
     *
     * @param context The context.
     */
    public PaypalProvider(Context context) {
        super(context);
    }

    /**
     * Constructs a new PaypalProvider.
     *
     * @param context The context.
     * @param complex The complex object.
     */
    public PaypalProvider(Context context, Complex complex) {
        super(context, complex);
    }

    /**
     * Constructs a new PaypalProvider.
     *
     * @param context The context.
     * @param complex The complex object.
     * @param cache   The cache.
     */
    public PaypalProvider(Context context, Complex complex, CacheX cache) {
        super(context, complex, cache);
    }

    /**
     * Gets the base headers for API requests.
     *
     * @param accessToken The access token.
     * @return A map of base headers.
     */
    public static Map<String, String> getBaseHeaders(AccessToken accessToken) {
        return getBaseHeaders(accessToken, String.valueOf(DateKit.current()), null, null);
    }

    /**
     * Gets the base headers for API requests.
     *
     * @param accessToken                The access token.
     * @param payPalRequestId            The PayPal request ID.
     * @param payPalPartnerAttributionId The PayPal partner attribution ID.
     * @param prefer                     The prefer header value.
     * @return A map of base headers.
     */
    public static Map<String, String> getBaseHeaders(
            AccessToken accessToken,
            String payPalRequestId,
            String payPalPartnerAttributionId,
            String prefer) {
        if (accessToken == null || StringKit.isEmpty(accessToken.getTokenType())
                || StringKit.isEmpty(accessToken.getAccessToken())) {
            Logger.warn(false, "Pay", "PayPal header build rejected: reason=missingAccessToken");
            throw new RuntimeException("accessToken is null");
        }
        Logger.debug(
                true,
                "Pay",
                "PayPal header build started: requestIdPresent={}, partnerAttributionPresent={}, preferPresent={}",
                StringKit.isNotEmpty(payPalRequestId),
                StringKit.isNotEmpty(payPalPartnerAttributionId),
                StringKit.isNotEmpty(prefer));
        Map<String, String> headers = new HashMap<>(3);
        headers.put(Http.Header.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(
                Http.Header.AUTHORIZATION,
                accessToken.getTokenType().concat(Symbol.SPACE).concat(accessToken.getAccessToken()));
        if (StringKit.isNotEmpty(payPalRequestId)) {
            headers.put("PayPal-Request-Id", payPalRequestId);
        }
        if (StringKit.isNotEmpty(payPalPartnerAttributionId)) {
            headers.put("PayPal-Partner-Attribution-Id", payPalPartnerAttributionId);
        }
        if (StringKit.isNotEmpty(prefer)) {
            headers.put("Prefer", prefer);
        }
        Logger.debug(
                false,
                "Pay",
                "PayPal header build completed: headerCount={}, tokenType={}",
                headers.size(),
                accessToken.getTokenType());
        return headers;
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
        return (complex.isSandbox() ? Registry.PAYPAL.sandbox() : Registry.PAYPAL.service()).concat(complex.method());
    }

    /**
     * Gets the access token.
     *
     * @return The result of the request as a {@link Message}.
     */
    public Message getToken() {
        Logger.info(
                true,
                "Pay",
                "PayPal access token request started: clientPresent={}",
                StringKit.isNotEmpty(this.context.getAppKey()));
        Map<String, String> headers = new HashMap<>(3);
        headers.put(Http.Header.ACCEPT, MediaType.APPLICATION_JSON);
        headers.put(Http.Header.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        headers.put(
                Http.Header.AUTHORIZATION,
                "Basic ".concat(
                        Base64.encode(
                                (this.context.getAppKey().concat(Symbol.COLON).concat(this.context.getAppSecret()))
                                        .getBytes(Charset.UTF_8))));
        Map<String, String> params = new HashMap<>(1);
        params.put("grant_type", "client_credentials");
        Message response = post(getUrl(PayPalApi.GET_TOKEN), params, headers);
        Logger.info(
                false,
                "Pay",
                "PayPal access token request completed: status={}, responseBytes={}",
                response == null ? null : response.getStatus(),
                response == null || response.getBody() == null ? 0 : response.getBody().length());
        return response;
    }

    /**
     * Creates an order.
     *
     * @param data The request data.
     * @return The result of the request as a {@link Message}.
     */
    public Message createOrder(String data) {
        Logger.info(true, "Pay", "PayPal order create started: dataBytes={}", data == null ? 0 : data.length());
        AccessToken accessToken = getAccessToken(false);
        Message response = post(getUrl(PayPalApi.CHECKOUT_ORDERS), data, getBaseHeaders(accessToken));
        Logger.info(
                false,
                "Pay",
                "PayPal order create completed: status={}, responseBytes={}",
                response == null ? null : response.getStatus(),
                response == null || response.getBody() == null ? 0 : response.getBody().length());
        return response;
    }

    /**
     * Updates an order.
     *
     * @param id   The order ID.
     * @param data The request data.
     * @return The result of the request as a {@link Message}.
     */
    public Message updateOrder(String id, String data) {
        Logger.info(
                true,
                "Pay",
                "PayPal order update started: orderId={}, dataBytes={}",
                id,
                data == null ? 0 : data.length());
        AccessToken accessToken = getAccessToken(false);
        String url = getUrl(PayPalApi.CHECKOUT_ORDERS).concat(Symbol.SLASH).concat(id);
        Message response = post(url, data, getBaseHeaders(accessToken));
        Logger.info(
                false,
                "Pay",
                "PayPal order update completed: orderId={}, status={}, responseBytes={}",
                id,
                response == null ? null : response.getStatus(),
                response == null || response.getBody() == null ? 0 : response.getBody().length());
        return response;
    }

    /**
     * Queries an order.
     *
     * @param orderId The order ID.
     * @return The result of the request as a {@link Message}.
     */
    public Message queryOrder(String orderId) {
        Logger.info(true, "Pay", "PayPal order query started: orderId={}", orderId);
        AccessToken accessToken = getAccessToken(false);
        String url = getUrl(PayPalApi.CHECKOUT_ORDERS).concat(Symbol.SLASH).concat(orderId);
        Message response = get(url, null, getBaseHeaders(accessToken));
        Logger.info(
                false,
                "Pay",
                "PayPal order query completed: orderId={}, status={}, responseBytes={}",
                orderId,
                response == null ? null : response.getStatus(),
                response == null || response.getBody() == null ? 0 : response.getBody().length());
        return response;
    }

    /**
     * Captures an order.
     *
     * @param id   The order ID.
     * @param data The request data.
     * @return The result of the request as a {@link Message}.
     */
    public Message captureOrder(String id, String data) {
        Logger.info(
                true,
                "Pay",
                "PayPal order capture started: orderId={}, dataBytes={}",
                id,
                data == null ? 0 : data.length());
        AccessToken accessToken = getAccessToken(false);
        String url = String.format(getUrl(PayPalApi.CAPTURE_ORDER), id);
        Message response = post(url, data, getBaseHeaders(accessToken));
        Logger.info(
                false,
                "Pay",
                "PayPal order capture completed: orderId={}, status={}, responseBytes={}",
                id,
                response == null ? null : response.getStatus(),
                response == null || response.getBody() == null ? 0 : response.getBody().length());
        return response;
    }

    /**
     * Queries a captured order.
     *
     * @param captureId The capture ID.
     * @return The result of the request as a {@link Message}.
     */
    public Message captureQuery(String captureId) {
        Logger.info(true, "Pay", "PayPal capture query started: captureId={}", captureId);
        AccessToken accessToken = getAccessToken(false);
        String url = String.format(getUrl(PayPalApi.CAPTURE_QUERY), captureId);
        Message response = get(url, null, getBaseHeaders(accessToken));
        Logger.info(
                false,
                "Pay",
                "PayPal capture query completed: captureId={}, status={}, responseBytes={}",
                captureId,
                response == null ? null : response.getStatus(),
                response == null || response.getBody() == null ? 0 : response.getBody().length());
        return response;
    }

    /**
     * Refunds a captured payment.
     *
     * @param captureId The capture ID.
     * @param data      The request data.
     * @return The result of the request as a {@link Message}.
     */
    public Message refund(String captureId, String data) {
        Logger.info(
                true,
                "Pay",
                "PayPal refund started: captureId={}, dataBytes={}",
                captureId,
                data == null ? 0 : data.length());
        AccessToken accessToken = getAccessToken(false);
        String url = String.format(getUrl(PayPalApi.REFUND), captureId);
        Message response = post(url, data, getBaseHeaders(accessToken));
        Logger.info(
                false,
                "Pay",
                "PayPal refund completed: captureId={}, status={}, responseBytes={}",
                captureId,
                response == null ? null : response.getStatus(),
                response == null || response.getBody() == null ? 0 : response.getBody().length());
        return response;
    }

    /**
     * Queries a refund.
     *
     * @param id The refund ID.
     * @return The result of the request as a {@link Message}.
     */
    public Message refundQuery(String id) {
        Logger.info(true, "Pay", "PayPal refund query started: refundId={}", id);
        AccessToken accessToken = getAccessToken(false);
        String url = String.format(getUrl(PayPalApi.REFUND_QUERY), id);
        Message response = get(url, null, getBaseHeaders(accessToken));
        Logger.info(
                false,
                "Pay",
                "PayPal refund query completed: refundId={}, status={}, responseBytes={}",
                id,
                response == null ? null : response.getStatus(),
                response == null || response.getBody() == null ? 0 : response.getBody().length());
        return response;
    }

    /**
     * Gets the access token, retrieving it from the cache if available or fetching a new one.
     *
     * @param forceRefresh Whether to force a refresh of the token.
     * @return The {@link AccessToken}.
     */
    public AccessToken getAccessToken(boolean forceRefresh) {
        Logger.info(
                true,
                "Pay",
                "PayPal access token resolve started: forceRefresh={}, clientPresent={}",
                forceRefresh,
                StringKit.isNotEmpty(this.context.getAppKey()));
        PayCache accessTokenCache = PayCache.INSTANCE;
        // Get AccessToken from cache
        if (!forceRefresh) {
            String json = (String) accessTokenCache.read(this.context.getAppKey());
            if (StringKit.isNotEmpty(json)) {
                AccessToken accessToken = new AccessToken(json, 200);
                if (accessToken.isAvailable()) {
                    Logger.info(
                            false,
                            "Pay",
                            "PayPal access token resolve completed: source=cache, available={}",
                            true);
                    return accessToken;
                }
                Logger.warn(false, "Pay", "PayPal cached access token ignored: reason=unavailable");
            }
        }

        AccessToken result = PaypalBuilder.retryOnException(3, () -> {
            Message response = getToken();
            return new AccessToken(response.getBody(), response.getStatus());
        });

        // If the AccessToken is still unavailable after three requests, it is still put into the cache
        // to facilitate troubleshooting by the upper layer through the properties in the AccessToken.
        if (null != result) {
            // Use clientId and accessToken to establish an association, supporting multiple accounts
            accessTokenCache.write(this.context.getAppKey(), result.getCacheJson());
        }
        Logger.info(
                false,
                "Pay",
                "PayPal access token resolve completed: source=remote, available={}",
                result != null && result.isAvailable());
        return result;
    }

}
