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
package org.miaixz.bus.pay.metric.jdpay;

import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.regex.Pattern;

import javax.crypto.Cipher;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.exception.PaymentException;
import org.miaixz.bus.core.lang.exception.SignatureException;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.XmlKit;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.pay.metric.wechat.WechatPayBuilder;

import lombok.SneakyThrows;

/**
 * Utility class for JD Pay, providing methods for signing, encryption, decryption, and XML manipulation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JdPayBuilder {

    private static final String XML_HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static final String XML_JDPAY_START = "<jdpay>";
    private static final String XML_JDPAY_END = "</jdpay>";
    private static final Pattern PATTERN = Pattern.compile("\t|\r|\n");
    private static final String XML_SIGN_START = "<sign>";
    private static final String XML_SIGN_END = "</sign>";
    private static final String SIGN = "sign";

    /**
     * Encrypts the values of a map using 3DES for online payment interfaces. All fields except for 'merchant',
     * 'version', and 'sign' are encrypted.
     *
     * @param map    The map containing the parameters.
     * @param desKey The 3DES key for encryption.
     * @return A new map with encrypted values.
     */
    public static Map<String, String> toMap(Map<String, String> map, String desKey) {
        HashMap<String, String> tempMap = new HashMap<>(map.size());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            if (StringKit.isNotEmpty(value)) {
                if ("merchant".equals(name) || "version".equals(name) || "sign".equals(name)) {
                    tempMap.put(name, value);
                } else {
                    tempMap.put(name, Builder.des(Base64.decode(desKey)).encryptHex(value));
                }
            }
        }
        return tempMap;
    }

    /**
     * Parses the XML data returned from the payment interface into a Map.
     *
     * @param xml The XML data returned from the interface.
     * @return A map containing the parsed data.
     */
    public static Map<String, String> parse(String xml) {
        if (StringKit.isEmpty(xml)) {
            return null;
        }
        Map<String, String> map = new HashMap<>(3);
        xml = XmlKit.cleanInvalid(xml); // Clean invalid XML characters
        String code = extractTagValue(xml, "code");
        String desc = extractTagValue(xml, "desc");
        map.put("code", code);
        map.put("desc", desc);
        if ("000000".equals(code)) {
            String encrypt = extractTagValue(xml, "encrypt");
            map.put("encrypt", encrypt);
        }
        return map;
    }

    /**
     * Helper method to extract the value between XML tags.
     *
     * @param xml     The XML string.
     * @param tagName The name of the tag.
     * @return The value within the specified tag, or null if not found.
     */
    private static String extractTagValue(String xml, String tagName) {
        String startTag = "<" + tagName + ">";
        String endTag = "</" + tagName + ">";
        int startIndex = xml.indexOf(startTag);
        int endIndex = xml.indexOf(endTag);
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return xml.substring(startIndex + startTag.length(), endIndex);
        }
        return null;
    }

    /**
     * Formats an XML string by removing tabs, carriage returns, and newlines.
     *
     * @param xml The XML string to format.
     * @return The formatted XML string.
     */
    public static String fomatXml(String xml) {
        StringBuilder formatStr = new StringBuilder();
        try (Scanner scanner = new Scanner(xml)) {
            scanner.useDelimiter(PATTERN);
            while (scanner.hasNext()) {
                formatStr.append(scanner.next().trim());
            }
        }
        return formatStr.toString();
    }

    /**
     * Adds an XML header to the given XML string if it doesn't already have one.
     *
     * @param xml The XML string.
     * @return The XML string with a header.
     */
    public static String addXmlHead(String xml) {
        if (xml != null && !"".equals(xml) && !xml.trim().startsWith("<?xml")) {
            xml = XML_HEAD + xml;
        }
        return xml;
    }

    /**
     * Adds the XML header and the jdpay element to the XML string if they are not present.
     *
     * @param xml The XML string.
     * @return The modified XML string.
     */
    public static String addXmlHeadAndElJdPay(String xml) {
        if (xml != null && !"".equals(xml)) {
            if (!xml.contains(XML_JDPAY_START)) {
                xml = XML_JDPAY_START + xml;
            }
            if (!xml.contains(XML_JDPAY_END)) {
                xml = xml + XML_JDPAY_END;
            }
            if (!xml.trim().startsWith("<?xml")) {
                xml = XML_HEAD + xml;
            }
        }
        return xml;
    }

    /**
     * Gets the content of a specified XML element.
     *
     * @param xml    The XML string.
     * @param elName The name of the element.
     * @return The content of the XML element.
     */
    public static String getXmlElm(String xml, String elName) {
        String result = "";
        String elStart = "<" + elName + ">";
        String elEnd = "</" + elName + ">";
        if (xml.contains(elStart) && xml.contains(elEnd)) {
            int from = xml.indexOf(elStart) + elStart.length();
            int to = xml.lastIndexOf(elEnd);
            result = xml.substring(from, to);
        }
        return result;
    }

    /**
     * Deletes a specified XML element from the XML string.
     *
     * @param xml     The XML string.
     * @param elmName The name of the element to delete.
     * @return The XML string with the element removed.
     */
    public static String delXmlElm(String xml, String elmName) {
        String elStart = "<" + elmName + ">";
        String elEnd = "</" + elmName + ">";
        if (xml.contains(elStart) && xml.contains(elEnd)) {
            int i1 = xml.indexOf(elStart);
            int i2 = xml.lastIndexOf(elEnd);
            String start = xml.substring(0, i1);
            int length = elEnd.length();
            String end = xml.substring(i2 + length, xml.length());
            xml = start + end;
        }
        return xml;
    }

    /**
     * Encrypts data using MD5 and converts it to lowercase.
     *
     * @param data The data to be encrypted.
     * @return The encrypted data in lowercase.
     */
    public static String md5LowerCase(String data) {
        return Builder.md5(data).toLowerCase();
    }

    /**
     * Converts a map of request parameters to a JD Pay XML string.
     *
     * @param params The request parameters.
     * @return The resulting XML string.
     */
    public static String toJdPayXml(Map<String, String> params) {
        return WechatPayBuilder.forEachMap(params, "<jdpay>", "</jdpay>").toString();
    }

    /**
     * Creates a signature for an object, excluding specified keys.
     *
     * @param object      The object to be signed.
     * @param rsaPriKey   The RSA private key.
     * @param signKeyList A list of keys to be excluded from signing.
     * @return The Base64-encoded signature.
     */
    public static String signRemoveSelectedKeys(Object object, String rsaPriKey, List<String> signKeyList) {
        String result = "";
        try {
            String sourceSignString = signString(object, signKeyList);
            String sha256SourceSignString = Builder.sha256(sourceSignString);
            byte[] newK = encryptByPrivateKey(sha256SourceSignString.getBytes("UTF-8"), rsaPriKey);
            result = Base64.encode(newK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Creates a string for signing from an object's properties, excluding specified keys.
     *
     * @param object The object.
     * @param list   A list of keys to exclude.
     * @return A string formatted for signing.
     * @throws IllegalArgumentException if an error occurs.
     */
    public static String signString(Object object, List<String> list) throws IllegalArgumentException {
        Map<String, Object> map = BeanKit.beanToMap(object, null);
        StringBuilder sb = new StringBuilder();
        for (String text : list) {
            map.remove(text);
        }

        Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = iterator.next();
            if (entry.getValue() == null) {
                continue;
            }
            String value = (String) entry.getValue();
            if (value.trim().length() > 0) {
                sb.append((String) entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }

        String result = sb.toString();
        if (result.endsWith("&")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    /**
     * Signs data using MD5withRSA.
     *
     * @param data       The data to be signed.
     * @param privateKey The private key.
     * @return The Base64-encoded signature.
     * @throws Exception if an error occurs.
     */
    public static String sign(byte[] data, String privateKey) throws Exception {
        byte[] keyBytes = Base64.decode(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey priKey = keyFactory.generatePrivate(pkcs8KeySpec);
        Signature signature = Signature.getInstance("MD5withRSA");
        signature.initSign(priKey);
        signature.update(data);
        return Base64.encode(signature.sign());
    }

    /**
     * Encrypts and signs the request data.
     *
     * @param rsaPrivateKey The RSA private key.
     * @param strDesKey     The DES key.
     * @param genSignStr    The XML data to be signed.
     * @return The encrypted and signed data.
     */
    public static String encrypt(String rsaPrivateKey, String strDesKey, String genSignStr) {
        if (StringKit.isNotEmpty(rsaPrivateKey) && StringKit.isNotEmpty(strDesKey)
                && StringKit.isNotEmpty(genSignStr)) {
            try {
                genSignStr = fomatXml(addXmlHeadAndElJdPay(genSignStr));
                genSignStr = delXmlElm(genSignStr, SIGN);
                String sign = encryptMerchant(genSignStr, rsaPrivateKey);
                String data = genSignStr.substring(0, genSignStr.length() - XML_JDPAY_END.length()) + XML_SIGN_START
                        + sign + XML_SIGN_END + XML_JDPAY_END;
                return Base64.encode(Builder.des(Base64.decode(strDesKey)).encryptHex(data));
            } catch (Exception e) {
                throw new SignatureException("signature failed");
            }
        }
        return null;
    }

    /**
     * Decrypts the XML data returned from the interface.
     *
     * @param rsaPubKey The RSA public key.
     * @param strDesKey The DES key.
     * @param encrypt   The encrypted XML data.
     * @return The decrypted data.
     */
    public static String decrypt(String rsaPubKey, String strDesKey, String encrypt) {
        try {
            String reqBody = Builder.des(Base64.decode(strDesKey)).decryptString(Base64.decode(encrypt));
            String inputSign = getXmlElm(reqBody, SIGN);
            reqBody = fomatXml(addXmlHead(reqBody));
            boolean verify = decryptMerchant(delXmlElm(reqBody, SIGN), inputSign, rsaPubKey);
            if (!verify) {
                throw new SignatureException("verify signature failed");
            }
            return reqBody;
        } catch (Exception e) {
            throw new PaymentException("data decrypt failed");
        }
    }

    /**
     * Verifies the signature of plaintext data.
     *
     * @param rsaPubKey The RSA public key.
     * @param reqBody   The XML data.
     * @return The plaintext data if the signature is valid.
     */
    public static String decrypt(String rsaPubKey, String reqBody) {
        try {
            String inputSign = getXmlElm(reqBody, SIGN);
            String req = fomatXml(addXmlHead(reqBody));
            boolean verify = decryptMerchant(delXmlElm(req, SIGN), inputSign, rsaPubKey);
            if (!verify) {
                throw new SignatureException("verify signature failed");
            }
            return req;
        } catch (Exception e) {
            throw new PaymentException("data decrypt failed");
        }
    }

    /**
     * Creates a signature for the merchant.
     *
     * @param sourceSignString The string to be signed.
     * @param rsaPriKey        The RSA private key.
     * @return The Base64-encoded signature.
     */
    public static String encryptMerchant(String sourceSignString, String rsaPriKey) {
        try {
            String sha256SourceSignString = Builder.sha256Hex(sourceSignString);
            byte[] newsks = encryptByPrivateKey(sha256SourceSignString.getBytes("UTF-8"), rsaPriKey);
            return Base64.encode(newsks);
        } catch (Exception e) {
            throw new SignatureException("verify signature failed.", e);
        }
    }

    /**
     * Verifies the merchant's signature.
     *
     * @param strSourceData The original data.
     * @param signData      The signature data.
     * @param rsaPubKey     The RSA public key.
     * @return true if the signature is valid, false otherwise.
     */
    public static boolean decryptMerchant(String strSourceData, String signData, String rsaPubKey) {
        if (signData == null || signData.isEmpty()) {
            throw new IllegalArgumentException("Argument 'signData' is null or empty");
        }
        if (rsaPubKey == null || rsaPubKey.isEmpty()) {
            throw new IllegalArgumentException("Argument 'key' is null or empty");
        }
        try {
            String sha256SourceSignString = Builder.sha256Hex(strSourceData);
            byte[] signByte = Base64.decode(signData);
            byte[] decryptArr = decryptByPublicKey(signByte, rsaPubKey);
            String decryptStr = ByteKit.byteArrayToHexString(decryptArr);
            if (sha256SourceSignString.equals(decryptStr)) {
                return true;
            } else {
                throw new SignatureException("Signature verification failed.");
            }
        } catch (RuntimeException e) {
            throw new SignatureException("verify signature failed.", e);
        }
    }

    /**
     * Decrypts data using the public key.
     *
     * @param data The data to be decrypted.
     * @param key  The public key.
     * @return The decrypted data.
     */
    @SneakyThrows
    public static byte[] decryptByPublicKey(byte[] data, String key) {
        byte[] keyBytes = Base64.decode(key);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        Key publicKey = keyFactory.generatePublic(x509KeySpec);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(2, publicKey);
        return cipher.doFinal(data);
    }

    /**
     * Encrypts data using the private key.
     *
     * @param data The data to be encrypted.
     * @param key  The private key.
     * @return The encrypted data.
     */
    @SneakyThrows
    public static byte[] encryptByPrivateKey(byte[] data, String key) {
        byte[] keyBytes = Base64.decode(key);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(1, privateKey);
        return cipher.doFinal(data);
    }

}
