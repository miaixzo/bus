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
 * 商户二维码支付接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JdPayBuilder {

    private static String XML_HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static String XML_JDPAY_START = "<jdpay>";
    private static String XML_JDPAY_END = "</jdpay>";
    private static Pattern PATTERN = Pattern.compile("\t|\r|\n");
    private static String XML_SIGN_START = "<sign>";
    private static String XML_SIGN_END = "</sign>";
    private static String SIGN = "sign";

    /**
     * 在线支付接口 除了merchant（商户号）、version（版本号）、sign(签名)以外，其余字段全部采用3DES进行加密
     *
     * @return 转化后的 Map
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
     * 将支付接口返回的 xml 数据转化为 Map
     *
     * @param xml 接口返回的 xml 数据
     * @return 解析后的数据
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
     * Helper method to extract value between XML tags
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

    public static String fomatXml(String xml) {
        StringBuilder formatStr = new StringBuilder();
        Scanner scanner = new Scanner(xml);
        scanner.useDelimiter(PATTERN);
        while (scanner.hasNext()) {
            formatStr.append(scanner.next().trim());
        }
        return formatStr.toString();
    }

    public static String addXmlHead(String xml) {
        if (xml != null && !"".equals(xml) && !xml.trim().startsWith("<?xml")) {
            xml = XML_HEAD + xml;
        }
        return xml;
    }

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
     * MD5 加密
     *
     * @param data 需要加密的数据
     * @return 加密后的数据
     */
    public static String md5LowerCase(String data) {
        return Builder.md5(data).toLowerCase();
    }

    /**
     * 请求参数 Map 转化为京东支付 xml
     *
     * @param params 请求参数
     * @return
     */
    public static String toJdPayXml(Map<String, String> params) {
        return WechatPayBuilder.forEachMap(params, "<jdpay>", "</jdpay>").toString();
    }

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
     * 请求参数签名
     *
     * @param rsaPrivateKey RSA 私钥
     * @param strDesKey     DES 密钥
     * @param genSignStr    xml 数据
     * @return 签名后的数据
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
     * 解密接口返回的 xml 数据
     *
     * @param rsaPubKey RSA 公钥
     * @param strDesKey DES 密钥
     * @param encrypt   加密的 xml 数据
     * @return 解密后的数据
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
     * 明文验证签名
     *
     * @param rsaPubKey RSA 公钥
     * @param reqBody   xml 数据
     * @return 明文数据
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

    public static String encryptMerchant(String sourceSignString, String rsaPriKey) {
        try {
            String sha256SourceSignString = Builder.sha256Hex(sourceSignString);
            byte[] newsks = encryptByPrivateKey(sha256SourceSignString.getBytes("UTF-8"), rsaPriKey);
            return Base64.encode(newsks);
        } catch (Exception e) {
            throw new SignatureException("verify signature failed.", e);
        }
    }

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