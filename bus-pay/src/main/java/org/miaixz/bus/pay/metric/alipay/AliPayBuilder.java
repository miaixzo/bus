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
package org.miaixz.bus.pay.metric.alipay;

import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Builder;

/**
 * Alipay configuration and utility class.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AliPayBuilder {

    private static final String CHARSET_UTF8 = "UTF-8";

    /**
     * Generates the signature result.
     *
     * @param params   The array to be signed.
     * @param key      The signing key.
     * @param signType The signature type (MD5, RSA, RSA2).
     * @return The signature result string.
     * @throws IllegalArgumentException if the sign_type is not supported.
     */
    public static String buildRequestMySign(Map<String, String> params, String key, String signType)
            throws IllegalArgumentException {
        String preStr = createLinkString(params);
        if (Algorithm.MD5.getValue().equals(signType)) {
            return Builder.md5(preStr.concat(key));
        } else if (Algorithm.RSA2.getValue().equals(signType)) {
            return rsaSign(preStr, key, "SHA256withRSA");
        } else if (Algorithm.RSA.getValue().equals(signType)) {
            return rsaSign(preStr, key, "SHA1withRSA");
        }
        throw new IllegalArgumentException("Unsupported sign_type: " + signType);
    }

    /**
     * Generates the parameter array to be requested.
     *
     * @param params   The parameter array before the request.
     * @param key      The merchant's private key.
     * @param signType The signature type.
     * @return The parameter array to be requested.
     */
    public static Map<String, String> buildRequestPara(Map<String, String> params, String key, String signType) {
        Map<String, String> tempMap = paraFilter(params);
        String mySign;
        try {
            mySign = buildRequestMySign(params, key, signType);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
        tempMap.put("sign", mySign);
        tempMap.put("sign_type", signType);
        return tempMap;
    }

    /**
     * Removes empty values and signature parameters from the array.
     *
     * @param params The signature parameter group.
     * @return The new signature parameter group after removing empty values and signature parameters.
     */
    public static Map<String, String> paraFilter(Map<String, String> params) {
        if (params == null) {
            return null;
        }
        Map<String, String> result = new HashMap<>(params.size());
        if (params.size() <= 0) {
            return result;
        }
        for (String key : params.keySet()) {
            String value = params.get(key);
            if (value == null || "".equals(value) || "sign".equalsIgnoreCase(key)
                    || "sign_type".equalsIgnoreCase(key)) {
                continue;
            }
            result.put(key, value);
        }
        return result;
    }

    /**
     * Sorts all elements of the array.
     *
     * @param params The parameter group to be sorted and involved in character splicing.
     * @return The spliced string.
     */
    public static String createLinkString(Map<String, String> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        StringBuilder content = new StringBuilder();
        for (String key : keys) {
            String value = params.get(key);
            content.append(key).append("=").append(value).append("&");
        }
        if (content.lastIndexOf("&") == content.length() - 1) {
            content.deleteCharAt(content.length() - 1);
        }
        return content.toString();
    }

    /**
     * Verifies the signature from the certificate content.
     *
     * @param params                  The parameter map to be verified.
     * @param alipayPublicCertContent The content of the Alipay public key certificate.
     * @param charset                 The character set of the parameter content.
     * @param signType                The signature method to be used, RSA or RSA2.
     * @return true: verification passed; false: verification failed.
     */
    public static boolean rsaCertCheckV1ByContent(
            Map<String, String> params,
            String alipayPublicCertContent,
            String charset,
            String signType) {
        try {
            // Extract public key from certificate content
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate cert = cf.generateCertificate(new ByteArrayInputStream(alipayPublicCertContent.getBytes()));
            PublicKey publicKey = cert.getPublicKey();

            // Prepare data for verification
            String content = createLinkString(paraFilter(params));
            String sign = params.get("sign");
            if (StringKit.isEmpty(sign)) {
                return false;
            }

            // Verify signature
            String algorithm = Algorithm.RSA2.getValue().equals(signType) ? "SHA256withRSA" : "SHA1withRSA";
            Signature signature = Signature.getInstance(algorithm);
            signature.initVerify(publicKey);
            signature.update(content.getBytes(charset != null ? charset : CHARSET_UTF8));
            return signature.verify(Base64.decode(sign));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Performs RSA or RSA2 signing.
     *
     * @param content    The data to be signed.
     * @param privateKey The private key.
     * @param algorithm  The signature algorithm (SHA1withRSA or SHA256withRSA).
     * @return The Base64-encoded signature.
     */
    private static String rsaSign(String content, String privateKey, String algorithm) {
        try {
            byte[] keyBytes = Base64.decode(privateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            java.security.PrivateKey priKey = keyFactory.generatePrivate(keySpec);
            Signature signature = Signature.getInstance(algorithm);
            signature.initSign(priKey);
            signature.update(content.getBytes(CHARSET_UTF8));
            return Base64.encode(signature.sign());
        } catch (Exception e) {
            throw new IllegalArgumentException("RSA signing failed: " + e.getMessage());
        }
    }

}
