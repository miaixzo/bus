/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.pay.Builder;
import org.miaixz.bus.pay.magic.Message;

/**
 * Utility class for WeChat Pay.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WechatPayBuilder {

    private static final String OS = Keys.get(Keys.OS_NAME) + "/" + Keys.get(Keys.OS_VERSION);
    private static final String VERSION = Keys.get(Keys.JAVA_VERSION);

    private static final String FIELD_SIGN = "sign";
    private static final String FIELD_SIGN_TYPE = "sign_type";
    /**
     * Maximum plaintext size for RSA encryption.
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /**
     * Gets base headers for WeChat Pay API requests.
     *
     * @param authorization The authorization string.
     * @return A map of base headers.
     */
    public static Map<String, String> getBaseHeaders(String authorization) {
        String userAgent = String.format(
                "Wechatpay-Http/%s (%s) Java/%s",
                WechatPayBuilder.class.getPackage().getImplementationVersion(),
                OS,
                VERSION == null ? "Unknown" : VERSION);

        Map<String, String> headers = new HashMap<>(5);
        headers.put("Accept", MediaType.APPLICATION_JSON);
        headers.put(HTTP.AUTHORIZATION, authorization);
        headers.put("User-Agent", userAgent);
        return headers;
    }

    /**
     * Gets headers for WeChat Pay API requests, including Content-Type and Wechatpay-Serial.
     *
     * @param authorization The authorization string.
     * @param serialNumber  The merchant API certificate serial number.
     * @return A map of headers.
     */
    public static Map<String, String> getHeaders(String authorization, String serialNumber) {
        Map<String, String> headers = getBaseHeaders(authorization);
        headers.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        if (StringKit.isNotEmpty(serialNumber)) {
            headers.put("Wechatpay-Serial", serialNumber);
        }
        return headers;
    }

    /**
     * Gets upload headers for WeChat Pay API requests, including Content-Type for multipart form data.
     *
     * @param authorization The authorization string.
     * @param serialNumber  The merchant API certificate serial number.
     * @return A map of upload headers.
     */
    public static Map<String, String> getUploadHeaders(String authorization, String serialNumber) {
        Map<String, String> headers = getBaseHeaders(authorization);
        headers.put(HTTP.CONTENT_TYPE, "multipart/form-data;boundary=\"boundary\"");
        if (StringKit.isNotEmpty(serialNumber)) {
            headers.put("Wechatpay-Serial", serialNumber);
        }
        return headers;
    }

    /**
     * Builds a response map from a {@link Message} object.
     *
     * @param response The {@link Message} object containing the API response.
     * @return A map containing timestamp, nonceStr, serialNumber, signature, body, and status.
     */
    public static Map<String, Object> buildResMap(Message response) {
        if (response == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>(6);
        String timestamp = response.getHeader("Wechatpay-Timestamp");
        String nonceStr = response.getHeader("Wechatpay-Nonce");
        String serialNo = response.getHeader("Wechatpay-Serial");
        String signature = response.getHeader("Wechatpay-Signature");
        String body = response.getBody();
        int status = response.getStatus();
        map.put("timestamp", timestamp);
        map.put("nonceStr", nonceStr);
        map.put("serialNumber", serialNo);
        map.put("signature", signature);
        map.put("body", body);
        map.put("status", status);
        return map;
    }

    /**
     * Verifies the signature of an asynchronous payment notification.
     *
     * @param params     The parameters from the notification.
     * @param partnerKey The payment key.
     * @return {@code true} if the signature is valid, {@code false} otherwise.
     */
    public static boolean verifyNotify(Map<String, String> params, String partnerKey) {
        String sign = params.get(FIELD_SIGN);
        String localSign = createSign(params, partnerKey, Algorithm.MD5);
        return sign.equals(localSign);
    }

    /**
     * Verifies the signature of an asynchronous payment notification.
     *
     * @param params     The parameters from the notification.
     * @param partnerKey The payment key.
     * @param algorithm  The signature algorithm.
     * @param signKey    The key used for the signature field.
     * @return {@code true} if the signature is valid, {@code false} otherwise.
     */
    public static boolean verifyNotify(
            Map<String, String> params,
            String partnerKey,
            Algorithm algorithm,
            String signKey) {
        if (StringKit.isEmpty(signKey)) {
            signKey = FIELD_SIGN;
        }
        String sign = params.get(signKey);
        String localSign = createSign(params, partnerKey, algorithm, signKey);
        return sign.equals(localSign);
    }

    /**
     * Verifies the signature of an asynchronous payment notification.
     *
     * @param params     The parameters from the notification.
     * @param partnerKey The payment key.
     * @param signKey    The key used for the signature field.
     * @return {@code true} if the signature is valid, {@code false} otherwise.
     */
    public static boolean verifyNotify(Map<String, String> params, String partnerKey, String signKey) {
        return verifyNotify(params, partnerKey, Algorithm.MD5, signKey);
    }

    /**
     * Verifies the signature of an asynchronous payment notification.
     *
     * @param params     The parameters from the notification.
     * @param partnerKey The payment key.
     * @param algorithm  The signature algorithm.
     * @return {@code true} if the signature is valid, {@code false} otherwise.
     */
    public static boolean verifyNotify(Map<String, String> params, String partnerKey, Algorithm algorithm) {
        return verifyNotify(params, partnerKey, algorithm, null);
    }

    /**
     * Generates a signature.
     *
     * @param params     The parameters to be signed.
     * @param partnerKey The key.
     * @param algorithm  The signature algorithm.
     * @return The signed data.
     */
    public static String createSign(Map<String, String> params, String partnerKey, Algorithm algorithm) {
        return createSign(params, partnerKey, algorithm, null);
    }

    /**
     * Generates a signature.
     *
     * @param params     The parameters to be signed.
     * @param partnerKey The key.
     * @param algorithm  The signature algorithm.
     * @param signKey    The key used for the signature field.
     * @return The signed data.
     */
    public static String createSign(
            Map<String, String> params,
            String partnerKey,
            Algorithm algorithm,
            String signKey) {
        if (algorithm == null) {
            algorithm = Algorithm.MD5;
        }
        if (StringKit.isEmpty(signKey)) {
            signKey = FIELD_SIGN;
        }
        // Remove sign before generating signature
        params.remove(signKey);
        String tempStr = Builder.createLinkString(params);
        String stringSignTemp = tempStr + "&key=" + partnerKey;
        if (algorithm == Algorithm.MD5) {
            return org.miaixz.bus.crypto.Builder.md5(stringSignTemp).toUpperCase();
        } else {
            return org.miaixz.bus.crypto.Builder.hmac(Algorithm.HMACSHA256, partnerKey.toUpperCase())
                    .digestHex(stringSignTemp);
        }
    }

    /**
     * Generates a signature for WeChat Enterprise Pay.
     *
     * @param params The parameters to be signed.
     * @param secret The secret of the WeChat Enterprise Pay application.
     * @return The signed data.
     */
    public static String createSign(Map<String, String> params, String secret) {
        // Remove sign before generating signature
        params.remove(FIELD_SIGN);
        String tempStr = Builder.createLinkString(params);
        String stringSignTemp = tempStr + "&secret=" + secret;
        return org.miaixz.bus.crypto.Builder.md5(stringSignTemp).toUpperCase();
    }

    /**
     * Builds a signature.
     *
     * @param params     The parameters to be signed.
     * @param partnerKey The key.
     * @param algorithm  The signature algorithm.
     * @return A map containing the signed parameters.
     */
    public static Map<String, String> buildSign(Map<String, String> params, String partnerKey, Algorithm algorithm) {
        return buildSign(params, partnerKey, algorithm, true);
    }

    /**
     * Builds a signature.
     *
     * @param params       The parameters to be signed.
     * @param partnerKey   The key.
     * @param algorithm    The signature algorithm.
     * @param haveSignType Whether the signature includes the sign_type field.
     * @return A map containing the signed parameters.
     */
    public static Map<String, String> buildSign(
            Map<String, String> params,
            String partnerKey,
            Algorithm algorithm,
            boolean haveSignType) {
        return buildSign(params, partnerKey, algorithm, null, null, haveSignType);
    }

    /**
     * Builds a signature.
     *
     * @param params       The parameters to be signed.
     * @param partnerKey   The key.
     * @param algorithm    The signature algorithm.
     * @param signKey      The string for the signature field.
     * @param signTypeKey  The string for the signature type field.
     * @param haveSignType Whether the signature includes the signature type string.
     * @return A map containing the signed parameters.
     */
    public static Map<String, String> buildSign(
            Map<String, String> params,
            String partnerKey,
            Algorithm algorithm,
            String signKey,
            String signTypeKey,
            boolean haveSignType) {
        if (StringKit.isEmpty(signKey)) {
            signKey = FIELD_SIGN;
        }
        if (haveSignType) {
            if (StringKit.isEmpty(signTypeKey)) {
                signTypeKey = FIELD_SIGN_TYPE;
            }
            params.put(signTypeKey, algorithm.getValue());
        }
        String sign = createSign(params, partnerKey, algorithm);
        params.put(signKey, sign);
        return params;
    }

    /**
     * Iterates through a map and builds an XML string with a prefix and suffix.
     *
     * @param params The map to iterate through.
     * @param prefix The XML prefix.
     * @param suffix The XML suffix.
     * @return A StringBuffer containing the XML string.
     */
    public static StringBuffer forEachMap(Map<String, String> params, String prefix, String suffix) {
        return Builder.forEachMap(params, prefix, suffix);
    }

    /**
     * <p>
     * Generates a QR code link.
     * </p>
     * <p>
     * Native payment interface mode one (scan code mode one).
     * </p>
     *
     * @param sign      The signature.
     * @param appId     The public account ID.
     * @param mchId     The merchant ID.
     * @param productId The product ID.
     * @param timeStamp The timestamp.
     * @param nonceStr  The random string.
     * @return The QR code link string.
     */
    public static String bizPayUrl(
            String sign,
            String appId,
            String mchId,
            String productId,
            String timeStamp,
            String nonceStr) {
        String rules = "weixin://wxpay/bizpayurl?sign=Temp&appid=Temp&mch_id=Temp&product_id=Temp&time_stamp=Temp&nonce_str=Temp";
        return replace(rules, "Temp", sign, appId, mchId, productId, timeStamp, nonceStr);
    }

    /**
     * <p>
     * Generates a QR code link.
     * </p>
     * <p>
     * Native payment interface mode one (scan code mode one).
     * </p>
     *
     * @param partnerKey The key.
     * @param appId      The public account ID.
     * @param mchId      The merchant ID.
     * @param productId  The product ID.
     * @param timeStamp  The timestamp.
     * @param nonceStr   The random string.
     * @param algorithm  The signature algorithm.
     * @return The QR code link string.
     */
    public static String bizPayUrl(
            String partnerKey,
            String appId,
            String mchId,
            String productId,
            String timeStamp,
            String nonceStr,
            Algorithm algorithm) {
        HashMap<String, String> map = new HashMap<>(5);
        map.put("appid", appId);
        map.put("mch_id", mchId);
        map.put(
                "time_stamp",
                StringKit.isEmpty(timeStamp) ? Long.toString(System.currentTimeMillis() / 1000) : timeStamp);
        map.put("nonce_str", StringKit.isEmpty(nonceStr) ? String.valueOf(DateKit.current()) : nonceStr);
        map.put("product_id", productId);
        return bizPayUrl(createSign(map, partnerKey, algorithm), appId, mchId, productId, timeStamp, nonceStr);
    }

    /**
     * <p>
     * Generates a QR code link.
     * </p>
     * <p>
     * Native payment interface mode one (scan code mode one).
     * </p>
     *
     * @param partnerKey The key.
     * @param appId      The public account ID.
     * @param mchId      The merchant ID.
     * @param productId  The product ID.
     * @return The QR code link string.
     */
    public static String bizPayUrl(String partnerKey, String appId, String mchId, String productId) {
        String timeStamp = Long.toString(System.currentTimeMillis() / 1000);
        String nonceStr = String.valueOf(DateKit.current());
        HashMap<String, String> map = new HashMap<>(5);
        map.put("appid", appId);
        map.put("mch_id", mchId);
        map.put("time_stamp", timeStamp);
        map.put("nonce_str", nonceStr);
        map.put("product_id", productId);
        return bizPayUrl(createSign(map, partnerKey, null), appId, mchId, productId, timeStamp, nonceStr);
    }

    /**
     * Replaces parameters in a URL string.
     *
     * @param text  The original string.
     * @param regex The regular expression to match.
     * @param args  The replacement strings.
     * @return The string with parameters replaced.
     */
    public static String replace(String text, String regex, String... args) {
        for (String arg : args) {
            text = text.replaceFirst(regex, arg);
        }
        return text;
    }

    /**
     * Checks if the API response code indicates success.
     *
     * @param codeValue The code value from the API response.
     * @return {@code true} if the code is "SUCCESS", {@code false} otherwise.
     */
    public static boolean codeIsOk(String codeValue) {
        return StringKit.isNotEmpty(codeValue) && "SUCCESS".equals(codeValue);
    }

    /**
     * <p>
     * Public account payment - re-sign a pre-payment order.
     * </p>
     * <p>
     * Note that the signature method here must be consistent with the signature type of the unified order.
     * </p>
     *
     * @param prepayId   The pre-payment order ID.
     * @param appId      The application ID.
     * @param partnerKey The API Key.
     * @param algorithm  The signature method.
     * @return A map containing the re-signed parameters.
     */
    public static Map<String, String> prepayIdCreateSign(
            String prepayId,
            String appId,
            String partnerKey,
            Algorithm algorithm) {
        Map<String, String> packageParams = new HashMap<>(6);
        packageParams.put("appId", appId);
        packageParams.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        packageParams.put("nonceStr", String.valueOf(System.currentTimeMillis()));
        packageParams.put("package", "prepay_id=" + prepayId);
        if (algorithm == null) {
            algorithm = Algorithm.MD5;
        }
        packageParams.put("algorithm", algorithm.getValue());
        String packageSign = WechatPayBuilder.createSign(packageParams, partnerKey, algorithm);
        packageParams.put("paySign", packageSign);
        return packageParams;
    }

    /**
     * Generates a signature for JS API payment.
     *
     * @param appId    The application ID.
     * @param prepayId The pre-payment order ID.
     * @param keyPath  The path to the key.pem certificate.
     * @return The parameters required to initiate payment.
     * @throws Exception If an error occurs.
     */
    public static Map<String, String> jsApiCreateSign(String appId, String prepayId, String keyPath) throws Exception {
        return jsApiCreateSign(appId, prepayId, Builder.getPrivateKey(keyPath, AuthType.RSA.getCode()));
    }

    /**
     * Generates a signature for JS API payment.
     *
     * @param appId      The application ID.
     * @param prepayId   The pre-payment order ID.
     * @param privateKey The merchant's private key.
     * @return The parameters required to initiate payment.
     * @throws Exception If an error occurs.
     */
    public static Map<String, String> jsApiCreateSign(String appId, String prepayId, PrivateKey privateKey)
            throws Exception {
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = String.valueOf(System.currentTimeMillis());
        String packageStr = "prepay_id=" + prepayId;
        Map<String, String> packageParams = new HashMap<>(6);
        packageParams.put("appId", appId);
        packageParams.put("timeStamp", timeStamp);
        packageParams.put("nonceStr", nonceStr);
        packageParams.put("package", packageStr);
        packageParams.put("algorithm", Algorithm.RSA.toString());
        ArrayList<String> list = new ArrayList<>();
        list.add(appId);
        list.add(timeStamp);
        list.add(nonceStr);
        list.add(packageStr);
        String packageSign = Builder.createSign(Builder.buildSignMessage(list), privateKey);
        packageParams.put("paySign", packageSign);
        return packageParams;
    }

    /**
     * <p>
     * APP payment - re-sign a pre-payment order.
     * </p>
     * <p>
     * Note that the signature method here must be consistent with the signature type of the unified order.
     * </p>
     *
     * @param appId      The application ID.
     * @param partnerId  The merchant ID.
     * @param prepayId   The pre-payment order ID.
     * @param partnerKey The API Key.
     * @param algorithm  The signature method.
     * @return A map containing the re-signed parameters.
     */
    public static Map<String, String> appPrepayIdCreateSign(
            String appId,
            String partnerId,
            String prepayId,
            String partnerKey,
            Algorithm algorithm) {
        Map<String, String> packageParams = new HashMap<>(8);
        packageParams.put("appid", appId);
        packageParams.put("partnerid", partnerId);
        packageParams.put("prepayid", prepayId);
        packageParams.put("package", "Sign=WXPay");
        packageParams.put("noncestr", String.valueOf(System.currentTimeMillis()));
        packageParams.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        if (algorithm == null) {
            algorithm = Algorithm.MD5;
        }
        String packageSign = createSign(packageParams, partnerKey, algorithm);
        packageParams.put("sign", packageSign);
        return packageParams;
    }

    /**
     * Generates a signature for App payment.
     *
     * @param appId     The application ID.
     * @param partnerId The merchant ID.
     * @param prepayId  The pre-payment order ID.
     * @param keyPath   The path to the key.pem certificate.
     * @return The parameters required to initiate payment.
     * @throws Exception If an error occurs.
     */
    public static Map<String, String> appCreateSign(String appId, String partnerId, String prepayId, String keyPath)
            throws Exception {
        return appCreateSign(appId, partnerId, prepayId, Builder.getPrivateKey(keyPath, AuthType.RSA.getCode()));
    }

    /**
     * Generates a signature for App payment.
     *
     * @param appId      The application ID.
     * @param partnerId  The merchant ID.
     * @param prepayId   The pre-payment order ID.
     * @param privateKey The merchant's private key.
     * @return The parameters required to initiate payment.
     * @throws Exception If an error occurs.
     */
    public static Map<String, String> appCreateSign(
            String appId,
            String partnerId,
            String prepayId,
            PrivateKey privateKey) throws Exception {
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = String.valueOf(System.currentTimeMillis());
        Map<String, String> packageParams = new HashMap<>(8);
        packageParams.put("appid", appId);
        packageParams.put("partnerid", partnerId);
        packageParams.put("prepayid", prepayId);
        packageParams.put("package", "Sign=WXPay");
        packageParams.put("timestamp", timeStamp);
        packageParams.put("noncestr", nonceStr);
        packageParams.put("algorithm", Algorithm.RSA.toString());
        ArrayList<String> list = new ArrayList<>();
        list.add(appId);
        list.add(timeStamp);
        list.add(nonceStr);
        list.add(prepayId);
        String packageSign = Builder.createSign(Builder.buildSignMessage(list), privateKey);
        packageParams.put("sign", packageSign);
        return packageParams;
    }

    /**
     * <p>
     * Mini Program payment - re-sign a pre-payment order.
     * </p>
     * <p>
     * Note that the signature method here must be consistent with the signature type of the unified order.
     * </p>
     *
     * @param appId      The application ID.
     * @param prepayId   The pre-payment order ID.
     * @param partnerKey The API Key.
     * @param algorithm  The signature method.
     * @return A map containing the re-signed parameters.
     */
    public static Map<String, String> miniAppPrepayIdCreateSign(
            String appId,
            String prepayId,
            String partnerKey,
            Algorithm algorithm) {
        Map<String, String> packageParams = new HashMap<>(6);
        packageParams.put("appId", appId);
        packageParams.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        packageParams.put("nonceStr", String.valueOf(System.currentTimeMillis()));
        packageParams.put("package", "prepay_id=" + prepayId);
        if (algorithm == null) {
            algorithm = Algorithm.MD5;
        }
        packageParams.put("algorithm", algorithm.getValue());
        String packageSign = createSign(packageParams, partnerKey, algorithm);
        packageParams.put("paySign", packageSign);
        return packageParams;
    }

    /**
     * Builds the Authorization header required for WeChat Pay v3 API requests.
     *
     * @param method    The HTTP request method.
     * @param urlSuffix The URL suffix, which can be obtained from WxApiType. URL parameters need to be concatenated
     *                  manually.
     * @param mchId     The merchant ID.
     * @param serialNo  The merchant API certificate serial number.
     * @param keyPath   The path to the key.pem certificate.
     * @param body      The interface request parameters.
     * @param nonceStr  The random string.
     * @param timestamp The timestamp.
     * @param authType  The authentication type.
     * @return The Authorization header string required for v3.
     * @throws Exception If an error occurs.
     */
    public static String buildAuthorization(
            String method,
            String urlSuffix,
            String mchId,
            String serialNo,
            String keyPath,
            String body,
            String nonceStr,
            long timestamp,
            String authType) throws Exception {
        // Build signature parameters
        String buildSignMessage = Builder.buildSignMessage(method, urlSuffix, timestamp, nonceStr, body);
        String signature = Builder.createSign(buildSignMessage, keyPath, authType);
        // Generate request header authorization according to platform rules
        return Builder.getAuthorization(mchId, serialNo, nonceStr, String.valueOf(timestamp), signature, authType);
    }

    /**
     * Builds the Authorization header required for WeChat Pay v3 API requests.
     *
     * @param method     The HTTP request method.
     * @param urlSuffix  The URL suffix, which can be obtained from WxApiType. URL parameters need to be concatenated
     *                   manually.
     * @param mchId      The merchant ID.
     * @param serialNo   The merchant API certificate serial number.
     * @param privateKey The merchant's private key.
     * @param body       The interface request parameters.
     * @param nonceStr   The random string.
     * @param timestamp  The timestamp.
     * @param authType   The authentication type.
     * @return The Authorization header string required for v3.
     * @throws Exception If an error occurs.
     */
    public static String buildAuthorization(
            String method,
            String urlSuffix,
            String mchId,
            String serialNo,
            PrivateKey privateKey,
            String body,
            String nonceStr,
            long timestamp,
            String authType) throws Exception {
        // Build signature parameters
        String buildSignMessage = Builder.buildSignMessage(method, urlSuffix, timestamp, nonceStr, body);
        String signature = Builder.createSign(buildSignMessage, privateKey);
        // Generate request header authorization according to platform rules
        return Builder.getAuthorization(mchId, serialNo, nonceStr, String.valueOf(timestamp), signature, authType);
    }

    /**
     * Builds the Authorization header required for WeChat Pay v3 API requests.
     *
     * @param method    The HTTP request method.
     * @param urlSuffix The URL suffix, which can be obtained from WxApiType. URL parameters need to be concatenated
     *                  manually.
     * @param mchId     The merchant ID.
     * @param serialNo  The merchant API certificate serial number.
     * @param keyPath   The path to the key.pem certificate.
     * @param body      The interface request parameters.
     * @return The Authorization header string required for v3.
     * @throws Exception If an error occurs.
     */
    public static String buildAuthorization(
            String method,
            String urlSuffix,
            String mchId,
            String serialNo,
            String keyPath,
            String body) throws Exception {
        return buildAuthorization(
                method,
                urlSuffix,
                mchId,
                serialNo,
                keyPath,
                body,
                String.valueOf(DateKit.current()),
                DateKit.current() / 1000,
                AuthType.RSA.getCode());
    }

    /**
     * Builds the Authorization header required for WeChat Pay v3 API requests.
     *
     * @param method     The HTTP request method.
     * @param urlSuffix  The URL suffix, which can be obtained from WxApiType. URL parameters need to be concatenated
     *                   manually.
     * @param mchId      The merchant ID.
     * @param serialNo   The merchant API certificate serial number.
     * @param privateKey The merchant's private key.
     * @param body       The interface request parameters.
     * @return The Authorization header string required for v3.
     * @throws Exception If an error occurs.
     */
    public static String buildAuthorization(
            String method,
            String urlSuffix,
            String mchId,
            String serialNo,
            PrivateKey privateKey,
            String body) throws Exception {
        return buildAuthorization(
                method,
                urlSuffix,
                mchId,
                serialNo,
                privateKey,
                body,
                String.valueOf(DateKit.current()),
                DateKit.current() / 1000,
                AuthType.RSA.getCode());
    }

    /**
     * Verifies the signature of an API response.
     *
     * @param response The {@link Message} object containing the API response.
     * @param certPath The path to the platform certificate.
     * @return {@code true} if the signature is valid, {@code false} otherwise.
     * @throws Exception If an error occurs.
     */
    public static boolean verifySignature(Message response, String certPath) throws Exception {
        String timestamp = response.getHeader("Wechatpay-Timestamp");
        String nonceStr = response.getHeader("Wechatpay-Nonce");
        String signature = response.getHeader("Wechatpay-Signature");
        String signatureType = response.getHeader("Wechatpay-Signature-Type");
        String body = response.getBody();
        Logger.info("timestamp:" + timestamp);
        Logger.info("nonceStr:" + nonceStr);
        Logger.info("signature:" + signature);
        Logger.info("signatureType:" + signatureType);
        Logger.info("body:" + body);
        return verifySignature(
                signatureType,
                signature,
                body,
                nonceStr,
                timestamp,
                Builder.getCertFileInputStream(certPath));
    }

    /**
     * Verifies the signature of an API response.
     *
     * @param response        The {@link Message} object containing the API response.
     * @param certInputStream The input stream of the platform certificate.
     * @return {@code true} if the signature is valid, {@code false} otherwise.
     * @throws Exception If an error occurs.
     */
    public static boolean verifySignature(Message response, InputStream certInputStream) throws Exception {
        String timestamp = response.getHeader("Wechatpay-Timestamp");
        String nonceStr = response.getHeader("Wechatpay-Nonce");
        String signature = response.getHeader("Wechatpay-Signature");
        String signatureType = response.getHeader("Wechatpay-Signature-Type");
        String body = response.getBody();
        return verifySignature(signatureType, signature, body, nonceStr, timestamp, certInputStream);
    }

    /**
     * Verifies the signature.
     *
     * @param signature The signature to be verified.
     * @param body      The response body.
     * @param nonce     The random string.
     * @param timestamp The timestamp.
     * @param publicKey The WeChat Pay platform public key.
     * @return {@code true} if the signature is valid, {@code false} otherwise.
     * @throws Exception If an error occurs.
     */
    public static boolean verifySignature(
            String signature,
            String body,
            String nonce,
            String timestamp,
            String publicKey) throws Exception {
        String buildSignMessage = Builder.buildSignMessage(timestamp, nonce, body);
        return checkByPublicKey(buildSignMessage, signature, publicKey);
    }

    /**
     * Verifies the signature.
     *
     * @param signature The signature to be verified.
     * @param body      The response body.
     * @param nonce     The random string.
     * @param timestamp The timestamp.
     * @param publicKey The WeChat Pay platform public key.
     * @return {@code true} if the signature is valid, {@code false} otherwise.
     * @throws Exception If an error occurs.
     */
    public static boolean verifySignature(
            String signature,
            String body,
            String nonce,
            String timestamp,
            PublicKey publicKey) throws Exception {
        String buildSignMessage = Builder.buildSignMessage(timestamp, nonce, body);
        return checkByPublicKey(buildSignMessage, signature, publicKey);
    }

    /**
     * Verifies the signature.
     *
     * @param signatureType   The signature type.
     * @param signature       The signature to be verified.
     * @param body            The response body.
     * @param nonce           The random string.
     * @param timestamp       The timestamp.
     * @param certInputStream The input stream of the WeChat Pay platform certificate.
     * @return {@code true} if the signature is valid, {@code false} otherwise.
     * @throws Exception If an error occurs.
     */
    public static boolean verifySignature(
            String signatureType,
            String signature,
            String body,
            String nonce,
            String timestamp,
            InputStream certInputStream) throws Exception {
        String buildSignMessage = Builder.buildSignMessage(timestamp, nonce, body);
        // Get certificate
        X509Certificate certificate = Builder.getCertificate(certInputStream);
        PublicKey publicKey = certificate.getPublicKey();
        if (StringKit.equals(signatureType, AuthType.SM2.getCode())) {
            return Builder.sm4Verify(publicKey, buildSignMessage, signature);
        }
        return checkByPublicKey(buildSignMessage, signature, publicKey);
    }

    /**
     * Verifies the signature of a v3 payment asynchronous notification.
     *
     * @param serialNo        The certificate serial number.
     * @param body            The asynchronous notification ciphertext.
     * @param signature       The signature.
     * @param nonce           The random string.
     * @param timestamp       The timestamp.
     * @param key             The API key.
     * @param certInputStream The platform certificate input stream.
     * @return The plaintext of the asynchronous notification.
     * @throws Exception If an error occurs.
     */
    public static String verifyNotify(
            String serialNo,
            String body,
            String signature,
            String nonce,
            String timestamp,
            String key,
            InputStream certInputStream) throws Exception {
        // Get platform certificate serial number
        X509Certificate certificate = Builder.getCertificate(certInputStream);
        String serialNumber = certificate.getSerialNumber().toString(16).toUpperCase();
        // Verify certificate serial number
        if (serialNumber.equals(serialNo)) {
            boolean verifySignature = WechatPayBuilder
                    .verifySignature(signature, body, nonce, timestamp, certificate.getPublicKey());
            if (verifySignature) {
                String json = JsonKit.toJsonString(body);
                String resource = JsonKit.getValue(json, "resource");

                String cipherText = JsonKit.getValue(resource, "ciphertext");
                String nonceStr = JsonKit.getValue(resource, "nonce");
                String associatedData = JsonKit.getValue(resource, "associated_data");

                // Decrypt ciphertext
                return decryptToString(
                        key.getBytes(Charset.UTF_8),
                        associatedData.getBytes(Charset.UTF_8),
                        nonceStr.getBytes(Charset.UTF_8),
                        cipherText);
            } else {
                throw new Exception("Signature error");
            }
        } else {
            throw new Exception("Certificate serial number error");
        }
    }

    /**
     * Verifies the signature of a v3 payment asynchronous notification.
     *
     * @param serialNo  The certificate serial number.
     * @param body      The asynchronous notification ciphertext.
     * @param signature The signature.
     * @param nonce     The random string.
     * @param timestamp The timestamp.
     * @param key       The API key.
     * @param certPath  The path to the platform certificate.
     * @return The plaintext of the asynchronous notification.
     * @throws Exception If an error occurs.
     */
    public static String verifyNotify(
            String serialNo,
            String body,
            String signature,
            String nonce,
            String timestamp,
            String key,
            String certPath) throws Exception {
        InputStream inputStream = Builder.getCertFileInputStream(certPath);
        return verifyNotify(serialNo, body, signature, nonce, timestamp, key, inputStream);
    }

    /**
     * Generates RSA public and private keys.
     *
     * @return A map containing the public and private keys.
     * @throws Exception If an error occurs during key generation.
     */
    public static Map<String, String> getKeys() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(Algorithm.RSA.getValue());
        keyPairGen.initialize(1024);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        String publicKey = Base64.encode(keyPair.getPublic().getEncoded());
        String privateKey = Base64.encode(keyPair.getPrivate().getEncoded());

        Map<String, String> map = new HashMap<>(2);
        map.put("publicKey", publicKey);
        map.put("privateKey", privateKey);

        Logger.info("Public Key\r\n" + publicKey);
        Logger.info("Private Key\r\n" + privateKey);
        return map;
    }

    /**
     * Generates an RSA public key using modulus and exponent. Note: [This code uses the default padding method,
     * RSA/None/PKCS1Padding. Different JDKs may have different default padding methods, such as Android's default
     * RSA/None/NoPadding].
     *
     * @param modulus  The modulus.
     * @param exponent The public exponent.
     * @return The {@link RSAPublicKey}.
     */
    public static RSAPublicKey getPublicKey(String modulus, String exponent) {
        try {
            BigInteger b1 = new BigInteger(modulus);
            BigInteger b2 = new BigInteger(exponent);
            KeyFactory keyFactory = KeyFactory.getInstance(Algorithm.RSA.getValue());
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(b1, b2);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generates an RSA private key using modulus and exponent. Note: [This code uses the default padding method,
     * RSA/None/PKCS1Padding. Different JDKs may have different default padding methods, such as Android's default
     * RSA/None/NoPadding].
     *
     * @param modulus  The modulus.
     * @param exponent The exponent.
     * @return The {@link RSAPrivateKey}.
     */
    public static RSAPrivateKey getPrivateKey(String modulus, String exponent) {
        try {
            BigInteger b1 = new BigInteger(modulus);
            BigInteger b2 = new BigInteger(exponent);
            KeyFactory keyFactory = KeyFactory.getInstance(Algorithm.RSA.getValue());
            RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(b1, b2);
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Encrypts data using a public key.
     *
     * @param data      The data to be encrypted.
     * @param publicKey The public key.
     * @return The encrypted data.
     * @throws Exception If an error occurs during encryption.
     */
    public static String encryptByPublicKey(String data, String publicKey) throws Exception {
        return encryptByPublicKey(data, publicKey, "RSA/ECB/PKCS1Padding");
    }

    /**
     * Encrypts data using a public key with OAEP padding for WeChat Pay.
     *
     * @param data      The data to be encrypted.
     * @param publicKey The public key.
     * @return The encrypted data.
     * @throws Exception If an error occurs during encryption.
     */
    public static String encryptByPublicKeyByWx(String data, String publicKey) throws Exception {
        return encryptByPublicKey(data, publicKey, "RSA/ECB/OAEPWITHSHA-1ANDMGF1PADDING");
    }

    /**
     * Encrypts data using a public key with a specified padding mode.
     *
     * @param data      The data to be encrypted.
     * @param publicKey The public key.
     * @param fillMode  The padding mode.
     * @return The encrypted data.
     * @throws Exception If an error occurs during encryption.
     */
    public static String encryptByPublicKey(String data, String publicKey, String fillMode) throws Exception {
        byte[] dataByte = data.getBytes(Charset.UTF_8);
        byte[] keyBytes = Base64.decode(publicKey);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(Algorithm.RSA.getValue());
        Key key = keyFactory.generatePublic(x509KeySpec);
        // Encrypt data
        Cipher cipher = Cipher.getInstance(fillMode);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        int inputLen = dataByte.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // Encrypt data in segments
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(dataByte, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(dataByte, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return StringKit.toString(Base64.encode(encryptedData));
    }

    /**
     * Signs data using a private key.
     *
     * @param data       The data to be signed.
     * @param privateKey The private key.
     * @return The signed data.
     * @throws Exception If an error occurs during signing.
     */
    public static String encryptByPrivateKey(String data, String privateKey) throws Exception {
        PKCS8EncodedKeySpec priPkcs8 = new PKCS8EncodedKeySpec(Base64.decode(privateKey));
        KeyFactory keyFactory = KeyFactory.getInstance(Algorithm.RSA.getValue());
        PrivateKey priKey = keyFactory.generatePrivate(priPkcs8);
        Signature signature = Signature.getInstance("SHA256WithRSA");

        signature.initSign(priKey);
        signature.update(data.getBytes(Charset.UTF_8));
        byte[] signed = signature.sign();
        return StringKit.toString(Base64.encode(signed));
    }

    /**
     * Signs data using a private key.
     *
     * @param data       The data to be signed.
     * @param privateKey The private key.
     * @return The signed data.
     * @throws Exception If an error occurs during signing.
     */
    public static String encryptByPrivateKey(String data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256WithRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes(Charset.UTF_8));
        byte[] signed = signature.sign();
        return StringKit.toString(Base64.encode(signed));
    }

    /**
     * Verifies a signature using a public key.
     *
     * @param data      The data that was signed.
     * @param sign      The signature.
     * @param publicKey The public key.
     * @return {@code true} if the signature is valid, {@code false} otherwise.
     * @throws Exception If an error occurs during verification.
     */
    public static boolean checkByPublicKey(String data, String sign, String publicKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(Algorithm.RSA.getValue());
        byte[] encodedKey = Base64.decode(publicKey);
        PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
        Signature signature = Signature.getInstance("SHA256WithRSA");
        signature.initVerify(pubKey);
        signature.update(data.getBytes(Charset.UTF_8));
        return signature.verify(Base64.decode(sign.getBytes(Charset.UTF_8)));
    }

    /**
     * Verifies a signature using a public key.
     *
     * @param data      The data that was signed.
     * @param sign      The signature.
     * @param publicKey The public key.
     * @return {@code true} if the signature is valid, {@code false} otherwise.
     * @throws Exception If an error occurs during verification.
     */
    public static boolean checkByPublicKey(String data, String sign, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256WithRSA");
        signature.initVerify(publicKey);
        signature.update(data.getBytes(Charset.UTF_8));
        return signature.verify(Base64.decode(sign.getBytes(Charset.UTF_8)));
    }

    /**
     * Decrypts data using a private key.
     *
     * @param data       The data to be decrypted.
     * @param privateKey The private key.
     * @return The decrypted data.
     * @throws Exception If an error occurs during decryption.
     */
    public static String decryptByPrivateKey(String data, String privateKey) throws Exception {
        return decryptByPrivateKey(data, privateKey, "RSA/ECB/PKCS1Padding");
    }

    /**
     * Decrypts data using a private key with OAEP padding for WeChat Pay.
     *
     * @param data       The data to be decrypted.
     * @param privateKey The private key.
     * @return The decrypted data.
     * @throws Exception If an error occurs during decryption.
     */
    public static String decryptByPrivateKeyByWx(String data, String privateKey) throws Exception {
        return decryptByPrivateKey(data, privateKey, "RSA/ECB/OAEPWITHSHA-1ANDMGF1PADDING");
    }

    /**
     * Decrypts data using a private key with a specified padding mode.
     *
     * @param data       The data to be decrypted.
     * @param privateKey The private key.
     * @param fillMode   The padding mode.
     * @return The decrypted data.
     * @throws Exception If an error occurs during decryption.
     */
    public static String decryptByPrivateKey(String data, String privateKey, String fillMode) throws Exception {
        byte[] encryptedData = Base64.decode(data);
        byte[] keyBytes = Base64.decode(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(Algorithm.RSA.getValue());
        Key key = keyFactory.generatePrivate(pkcs8KeySpec);
        Cipher cipher = Cipher.getInstance(fillMode);

        cipher.init(Cipher.DECRYPT_MODE, key);
        int inputLen = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // Decrypt data in segments
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > Normal._128) {
                cache = cipher.doFinal(encryptedData, offSet, Normal._128);
            } else {
                cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * Normal._128;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return new String(decryptedData);
    }

    /**
     * Loads a public key from a string.
     *
     * @param publicKey The public key data string.
     * @return The {@link PublicKey}.
     * @throws Exception If an error occurs during key loading.
     */
    public static PublicKey loadPublicKey(String publicKey) throws Exception {
        try {
            byte[] buffer = Base64.decode(publicKey);
            KeyFactory keyFactory = KeyFactory.getInstance(Algorithm.RSA.getValue());
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("No such algorithm");
        } catch (InvalidKeySpecException e) {
            throw new Exception("Invalid public key");
        } catch (NullPointerException e) {
            throw new Exception("Public key data is null");
        }
    }

    /**
     * Loads a private key from a string. Uses PKCS8EncodedKeySpec (PKCS#8 encoded key instruction) for loading.
     *
     * @param privateKey The private key string.
     * @return The {@link PrivateKey}.
     * @throws Exception If an error occurs during key loading.
     */
    public static PrivateKey loadPrivateKey(String privateKey) throws Exception {
        try {
            byte[] buffer = Base64.decode(privateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
            KeyFactory keyFactory = KeyFactory.getInstance(Algorithm.RSA.getValue());
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("No such algorithm");
        } catch (InvalidKeySpecException e) {
            throw new Exception("Invalid private key");
        } catch (NullPointerException e) {
            throw new Exception("Private key data is null");
        }
    }

    /**
     * Decrypts certificate and callback messages.
     *
     * @param key            The key for decryption.
     * @param associatedData The associated data.
     * @param nonce          The nonce.
     * @param cipherText     The ciphertext.
     * @return The plaintext of the platform certificate.
     * @throws GeneralSecurityException If a security-related error occurs.
     */
    public static String decryptToString(byte[] key, byte[] associatedData, byte[] nonce, String cipherText)
            throws GeneralSecurityException {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            SecretKeySpec aesKey = new SecretKeySpec(key, "AES");
            GCMParameterSpec spec = new GCMParameterSpec(128, nonce);

            cipher.init(Cipher.DECRYPT_MODE, aesKey, spec);
            cipher.updateAAD(associatedData);

            return new String(cipher.doFinal(Base64.decode(cipherText)), Charset.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IllegalStateException(e);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
