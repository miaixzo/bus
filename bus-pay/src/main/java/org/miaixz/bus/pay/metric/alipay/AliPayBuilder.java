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
 * 支付宝配置
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AliPayBuilder {

    private static final String CHARSET_UTF8 = "UTF-8";

    /**
     * 生成签名结果
     *
     * @param params   要签名的数组
     * @param key      签名密钥
     * @param signType 签名类型 (MD5, RSA, RSA2)
     * @return 签名结果字符串
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
     * 生成要请求的参数数组
     *
     * @param params   请求前的参数数组
     * @param key      商户的私钥
     * @param signType 签名类型
     * @return 要请求的参数数组
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
     * 除去数组中的空值和签名参数
     *
     * @param params 签名参数组
     * @return 去掉空值与签名参数后的新签名参数组
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
     * 把数组所有元素排序
     *
     * @param params 需要排序并参与字符拼接的参数组
     * @return 拼接后字符串
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
     * 从证书内容验签
     *
     * @param params                  待验签的参数Map
     * @param alipayPublicCertContent 支付宝公钥证书内容
     * @param charset                 参数内容编码集
     * @param signType                指定采用的签名方式，RSA或RSA2
     * @return true：验签通过；false：验签不通过
     */
    public static boolean rsaCertCheckV1ByContent(Map<String, String> params, String alipayPublicCertContent,
            String charset, String signType) {
        try {
            // Extract public key from certificate content
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate cert = cf.generateCertificate(new ByteArrayInputStream(alipayPublicCertContent.getBytes()));
            PublicKey publicKey = cert.getPublicKey();
            String publicKeyEncoded = Base64.encode(publicKey.getEncoded());

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
     * RSA or RSA2 signing
     *
     * @param content    Data to sign
     * @param privateKey Private key
     * @param algorithm  Signature algorithm (SHA1withRSA or SHA256withRSA)
     * @return Base64-encoded signature
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