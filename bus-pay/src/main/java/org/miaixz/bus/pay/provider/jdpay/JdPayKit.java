package org.miaixz.bus.pay.provider.jdpay;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.core.toolkit.XmlKit;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.pay.metric.WxPayKit;
import org.w3c.dom.Document;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.xpath.XPathConstants;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.regex.Pattern;

/**
 * <p>IJPay 让支付触手可及，封装了微信支付、支付宝支付、银联支付等常用的支付方式以及各种常用的接口。</p>
 *
 * <p>不依赖任何第三方 mvc 框架，仅仅作为工具使用简单快速完成支付模块的开发，可轻松嵌入到任何系统里。 </p>
 *
 * <p>IJPay 交流群: 723992875</p>
 *
 * <p>Node.js 版: https://gitee.com/javen205/TNWX</p>
 *
 * <p>商户二维码支付接口 Model</p>
 */
public class JdPayKit {

    public static final byte[] DEFAULT_KEY = {
            49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52};

    public static final String KEY_ALGORITHM = "RSA";
    public static final String KEY_ALGORITHM_DETAIL = "RSA/ECB/PKCS1Padding";
    public static final String SIGNATURE_ALGORITHM = "MD5withRSA";
    public static final String PUBLIC_KEY = "RSAPublicKey";
    public static final String PRIVATE_KEY = "RSAPrivateKey";
    private static final String hexString = "0123456789ABCDEF";

    private static final String XML_HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static final String XML_JDPAY_START = "<jdpay>";
    private static final String XML_JDPAY_END = "</jdpay>";
    private static final Pattern PATTERN = Pattern.compile("\t|\r|\n");

    private static final String XML_SIGN_START = "<sign>";
    private static final String XML_SIGN_END = "</sign>";
    private static final String SIGN = "sign";
    private static final String RESULT = "result";
    private static final List<String> unSignKeyList = Arrays.asList("merchantSign", "token", "version");

    public static String fomatXmlString(String xml) {
        StringBuilder formatStr = new StringBuilder();
        Scanner scanner = new Scanner(xml);
        scanner.useDelimiter(PATTERN);
        while (scanner.hasNext()) {
            formatStr.append(scanner.next().trim());
        }
        return formatStr.toString();
    }

    public static String addXmlHead(String xml) {
        if (xml != null && !"".equals(xml) &&
                !xml.trim().startsWith("<?xml")) {
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
            String end = xml.substring(i2 + length);
            xml = start + end;
        }
        return xml;
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
        System.out.println("genSignStr>" + genSignStr);
        String encrypt = null;
        if (StringKit.isNotEmpty(rsaPrivateKey)
                && StringKit.isNotEmpty(strDesKey)
                && StringKit.isNotEmpty(genSignStr)) {

            try {
                genSignStr = addXmlHeadAndElJdPay(genSignStr);

                genSignStr = fomatXmlString(genSignStr);

                genSignStr = delXmlElm(genSignStr, SIGN);

                String sign = encryptMerchant(genSignStr, rsaPrivateKey);
                System.out.println("sign>" + sign);
                String data = genSignStr.substring(0, genSignStr.length() - XML_JDPAY_END.length()) + XML_SIGN_START + sign + XML_SIGN_END + XML_JDPAY_END;

                encrypt = Base64.encode(encrypt2HexString(decryptBASE64(strDesKey), data).getBytes(Charset.UTF_8));
            } catch (Exception e) {
                throw new RuntimeException("signature failed");
            }
        }
        return encrypt;
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
        String reqBody;
        try {
            reqBody = decrypt4HexString(decryptBASE64(strDesKey), new String(Base64.decode(encrypt), Charset.UTF_8));
            String inputSign = getXmlElm(reqBody, SIGN);
            reqBody = addXmlHead(reqBody);
            reqBody = fomatXmlString(reqBody);
            String genSignStr = delXmlElm(reqBody, SIGN);
            boolean verifyResult = decryptMerchant(genSignStr, inputSign, rsaPubKey);
            if (!verifyResult) {
                throw new RuntimeException("verify signature failed");
            }
        } catch (Exception e) {
            throw new RuntimeException("data decrypt failed");
        }
        return reqBody;
    }

    /**
     * 明文验证签名
     *
     * @param rsaPubKey RSA 公钥
     * @param reqBody   xml 数据
     * @return 明文数据
     */
    public static String decrypt(String rsaPubKey, String reqBody) {
        String req;
        try {
            String inputSign = getXmlElm(reqBody, SIGN);
            req = addXmlHead(reqBody);
            req = fomatXmlString(req);
            String genSignStr = delXmlElm(req, SIGN);
            boolean verifyResult = decryptMerchant(genSignStr, inputSign, rsaPubKey);
            if (!verifyResult) {
                throw new RuntimeException("verify signature failed");
            }
        } catch (Exception e) {
            throw new RuntimeException("data decrypt failed");
        }
        return req;
    }

    public static byte[] encrypt(byte[] keybyte, byte[] src) {
        try {
            SecretKey deskey = new SecretKeySpec(keybyte, "DESede");

            Cipher c1 = Cipher.getInstance("DESede/ECB/NoPadding");
            c1.init(1, deskey);
            return c1.doFinal(src);
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (NoSuchPaddingException e2) {
            e2.printStackTrace();
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        return null;
    }

    private static byte[] decrypt(byte[] keybyte, byte[] src) {
        try {
            SecretKey deskey = new SecretKeySpec(keybyte, "DESede");

            Cipher c1 = Cipher.getInstance("DESede/ECB/NoPadding");
            c1.init(2, deskey);
            return c1.doFinal(src);
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (NoSuchPaddingException e2) {
            e2.printStackTrace();
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        return null;
    }

    private static String byte2Hex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
            if (n < b.length - 1) hs = hs + ":";
        }
        return hs.toUpperCase();
    }

    public static String encrypt2HexString(byte[] keys, String sourceData) {
        byte[] source = sourceData.getBytes(Charset.UTF_8);
        int merchantData = source.length;
        int x = (merchantData + 4) % 8;
        int y = (x == 0) ? 0 : (8 - x);
        byte[] sizeByte = intToByteArray(merchantData);
        byte[] resultByte = new byte[merchantData + 4 + y];
        resultByte[0] = sizeByte[0];
        resultByte[1] = sizeByte[1];
        resultByte[2] = sizeByte[2];
        resultByte[3] = sizeByte[3];
        for (int i = 0; i < merchantData; i++) {
            resultByte[4 + i] = source[i];
        }
        for (int i = 0; i < y; i++) {
            resultByte[merchantData + 4 + i] = 0;
        }
        byte[] desdata = encrypt(keys, resultByte);
        return bytes2Hex(desdata);
    }

    public static String decrypt4HexString(byte[] keys, String data) {
        byte[] hexSourceData;
        try {
            hexSourceData = hex2byte(data.getBytes(Charset.UTF_8));
            byte[] unDesResult = decrypt(keys, hexSourceData);
            byte[] dataSizeByte = new byte[4];
            dataSizeByte[0] = unDesResult[0];
            dataSizeByte[1] = unDesResult[1];
            dataSizeByte[2] = unDesResult[2];
            dataSizeByte[3] = unDesResult[3];
            int dsb = byteArrayToInt(dataSizeByte, 0);
            if (dsb > 16384) {
                throw new RuntimeException("msg over MAX_MSG_LENGTH or msg error");
            }
            byte[] tempData = new byte[dsb];
            for (int i = 0; i < dsb; i++) {
                tempData[i] = unDesResult[4 + i];
            }
            return hex2bin(toHexString(tempData));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String hex2bin(String hex) throws UnsupportedEncodingException {
        String digital = "0123456789abcdef";
        char[] hex2char = hex.toCharArray();
        byte[] bytes = new byte[hex.length() / 2];

        for (int i = 0; i < bytes.length; i++) {
            int temp = digital.indexOf(hex2char[2 * i]) * 16;
            temp += digital.indexOf(hex2char[2 * i + 1]);
            bytes[i] = (byte) (temp & 0xFF);
        }

        return new String(bytes, Charset.UTF_8);
    }

    private static String toHexString(byte[] ba) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < ba.length; i++) {
            text.append(String.format("%x", Byte.valueOf(ba[i])));
        }
        return text.toString();
    }

    private static String bytes2Hex(byte[] bts) {
        String des = Normal.EMPTY;
        String tmp;
        for (int i = 0; i < bts.length; i++) {
            tmp = Integer.toHexString(bts[i] & 0xFF);
            if (tmp.length() == 1) {
                des = des + "0";
            }
            des = des + tmp;
        }
        return des;
    }

    public static byte[] hex2byte(byte[] b) {
        if (b.length % 2 != 0) {
            throw new IllegalArgumentException("长度不是偶数");
        }
        byte[] b2 = new byte[b.length / 2];
        for (int n = 0; n < b.length; n += 2) {
            String item = new String(b, n, 2);

            b2[n / 2] = (byte) Integer.parseInt(item, 16);
        }
        b = null;
        return b2;
    }

    private static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) (i >> 24 & 0xFF);
        result[1] = (byte) (i >> 16 & 0xFF);
        result[2] = (byte) (i >> 8 & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }

    private static int byteArrayToInt(byte[] b, int offset) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (3 - i) * 8;
            value += ((b[i + offset] & 0xFF) << shift);
        }
        return value;
    }

    public static String encryptMerchant(String sourceSignString, String rsaPriKey) {
        String result;
        try {
            String sha256SourceSignString = encrypt(sourceSignString, null);
            byte[] newsks = encryptByPrivateKey(sha256SourceSignString.getBytes(Charset.UTF_8), rsaPriKey);
            result = Base64.encode(newsks);
        } catch (Exception e) {
            throw new RuntimeException("verify signature failed.", e);
        }
        return result;
    }

    public static boolean decryptMerchant(String strSourceData, String signData, String rsaPubKey) {
        if (signData == null || signData.isEmpty()) {
            throw new IllegalArgumentException("Argument 'signData' is null or empty");
        }
        if (rsaPubKey == null || rsaPubKey.isEmpty()) {
            throw new IllegalArgumentException("Argument 'key' is null or empty");
        }

        try {
            String sha256SourceSignString = encrypt(strSourceData, null);
            byte[] signByte = Base64.decode(signData);
            byte[] decryptArr = decryptByPublicKey(signByte, rsaPubKey);
            String decryptStr = bytesToString(decryptArr);
            if (sha256SourceSignString.equals(decryptStr)) {
                return true;
            } else {
                throw new RuntimeException("Signature verification failed.");
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("verify signature failed.", e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("verify signature failed.", e);
        }
    }

    public static String encrypt(String strSrc, String encName) {
        MessageDigest md;
        String strDes;
        byte[] bt;
        bt = strSrc.getBytes(Charset.UTF_8);
        try {
            if (encName == null || encName.equals("")) {
                encName = "SHA-256";
            }
            md = MessageDigest.getInstance(encName);
            md.update(bt);
            strDes = bytes2Hex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        return strDes;
    }

    public static String sign4SelectedKeys(Object object, String rsaPriKey, List<String> signKeyList) {
        String result = "";

        try {
            String sourceSignString = signString4SelectedKeys(object, signKeyList);
            String sha256SourceSignString = encrypt(sourceSignString, null);

            byte[] newsks = encryptByPrivateKey(sha256SourceSignString.getBytes(Charset.UTF_8), rsaPriKey);
            result = Base64.encode(newsks);
        } catch (Exception e) {
            throw new RuntimeException("sign4SelectedKeys>error", e);
        }
        return result;
    }


    public static String signRemoveSelectedKeys(Object object, String rsaPriKey, List<String> signKeyList) {
        String result = "";

        try {
            String sourceSignString = signString(object, signKeyList);
            String sha256SourceSignString = encrypt(sourceSignString, null);
            byte[] newK = encryptByPrivateKey(sha256SourceSignString.getBytes(Charset.UTF_8), rsaPriKey);
            result = Base64.encode(newK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public static String signString(Object object, List<String> unSignKeyList) throws IllegalArgumentException, IllegalAccessException {
        TreeMap<String, Object> map = objectToMap(object);


        StringBuilder sb = new StringBuilder();

        for (String text : unSignKeyList) {
            map.remove(text);
        }

        Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            if (entry.getValue() == null) {
                continue;
            }
            String value = (String) entry.getValue();
            if (value.trim().length() > 0) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }

        String result = sb.toString();
        if (result.endsWith("&")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }


    private static String signString4SelectedKeys(Object object, List<String> signedKeyList) throws IllegalArgumentException, IllegalAccessException {
        TreeMap<String, Object> map = objectToMap(object);
        if (map == null || map.isEmpty() || signedKeyList == null || signedKeyList.isEmpty()) {
            return null;
        }

        TreeMap<String, Object> signMap = new TreeMap<>();

        StringBuilder sb = new StringBuilder();

        for (String text : signedKeyList) {
            Object o = map.get(text);
            if (o != null) {
                signMap.put(text, o);
                continue;
            }
            signMap.put(text, Normal.EMPTY);
        }


        Iterator iterator = signMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            sb.append(entry.getKey() + "=" + (
                    (entry.getValue() == null) ? "" : entry.getValue()) + "&");
        }

        String result = sb.toString();
        if (result.endsWith("&")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }


    public static TreeMap<String, Object> objectToMap(Object object) throws IllegalArgumentException, IllegalAccessException {
        TreeMap<String, Object> map = new TreeMap<>();
        if (object instanceof Map) {
            Map<String, Object> objectMap = (Map) object;
            for (String text : objectMap.keySet()) {
                map.put(text, objectMap.get(text));
            }
            return map;
        }

        for (Class<?> cls = object.getClass(); cls != Object.class; cls = cls.getSuperclass()) {

            Field[] fields = cls.getDeclaredFields();
            for (Field f : fields) {
                f.setAccessible(true);
                map.put(f.getName(), f.get(object));
            }
        }
        return map;
    }


    public static String sign4PCString(Object object, List<String> unSignKeyList) throws IllegalArgumentException, IllegalAccessException {
        TreeMap<String, Object> map = objectToMap(object);
        StringBuilder sb = new StringBuilder();
        for (String text : unSignKeyList) {
            map.remove(text);
        }

        Iterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            sb.append(entry.getKey() + "=" + ((entry.getValue() == null) ? "" : entry.getValue()) + "&");
        }

        String result = sb.toString();
        if (result.endsWith("&")) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }


    public static String sign(byte[] data, String privateKey) throws Exception {
        byte[] keyBytes = decryptBASE64(privateKey);


        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);


        KeyFactory keyFactory = KeyFactory.getInstance("RSA");


        PrivateKey priKey = keyFactory.generatePrivate(pkcs8KeySpec);


        Signature signature = Signature.getInstance("MD5withRSA");
        signature.initSign(priKey);
        signature.update(data);

        return encryptBASE64(signature.sign());
    }


    public static boolean verify(byte[] data, String publicKey, String sign) throws Exception {
        byte[] keyBytes = decryptBASE64(publicKey);


        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);


        KeyFactory keyFactory = KeyFactory.getInstance("RSA");


        PublicKey pubKey = keyFactory.generatePublic(keySpec);

        Signature signature = Signature.getInstance("MD5withRSA");
        signature.initVerify(pubKey);
        signature.update(data);


        return signature.verify(decryptBASE64(sign));
    }


    public static byte[] decryptByPrivateKey(byte[] data, String key) throws Exception {
        byte[] keyBytes = decryptBASE64(key);


        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);


        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(2, privateKey);

        return cipher.doFinal(data);
    }


    public static byte[] decryptByPublicKey(byte[] data, String key) throws Exception {
        byte[] keyBytes = decryptBASE64(key);


        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        Key publicKey = keyFactory.generatePublic(x509KeySpec);


        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(2, publicKey);
        return cipher.doFinal(data);
    }


    public static byte[] encryptByPublicKey(byte[] data, String key) throws Exception {
        byte[] keyBytes = decryptBASE64(key);


        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        Key publicKey = keyFactory.generatePublic(x509KeySpec);


        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(1, publicKey);

        return cipher.doFinal(data);
    }


    public static byte[] encryptByPrivateKey(byte[] data, String key) throws Exception {
        byte[] keyBytes = decryptBASE64(key);


        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);


        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(1, privateKey);

        return cipher.doFinal(data);
    }


    public static String getPrivateKey(Map<String, Object> keyMap) throws Exception {
        Key key = (Key) keyMap.get("RSAPrivateKey");

        return encryptBASE64(key.getEncoded());
    }


    public static String getPublicKey(Map<String, Object> keyMap) throws Exception {
        Key key = (Key) keyMap.get("RSAPublicKey");

        return encryptBASE64(key.getEncoded());
    }


    public static Map<String, Object> initKey() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(1024);
        KeyPair keyPair = keyPairGen.generateKeyPair();

        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        Map<String, Object> keyMap = new HashMap<String, Object>(2);
        keyMap.put("RSAPublicKey", publicKey);
        keyMap.put("RSAPrivateKey", privateKey);
        return keyMap;
    }

    public static byte[] decryptBASE64(String key) {
        return Base64.decode(key);
    }


    public static String encryptBASE64(byte[] key) {
        return Base64.encode(key);
    }


    public static byte[] encryptMD5(byte[] data) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(data);
        return md5.digest();
    }


    public static byte[] encryptSHA(byte[] data) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA");
        sha.update(data);
        return sha.digest();
    }


    public static String initMacKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacMD5");
        SecretKey secretKey = keyGenerator.generateKey();
        return encryptBASE64(secretKey.getEncoded());
    }


    public static byte[] encryptHMAC(byte[] data, String key) throws Exception {
        SecretKey secretKey = new SecretKeySpec(decryptBASE64(key), "HmacMD5");
        Mac mac = Mac.getInstance(secretKey.getAlgorithm());
        mac.init(secretKey);
        return mac.doFinal(data);
    }


    public static String bytesToString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        String bytes = stringBuilder.toString();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length() / 2);

        for (int i = 0; i < bytes.length(); i += 2)
            baos.write(hexString.indexOf(bytes.charAt(i)) << 4 | hexString.indexOf(bytes.charAt(i + 1)));
        return baos.toString();
    }

    /**
     * MD5 加密
     *
     * @param data 需要加密的数据
     * @return 加密后的数据
     */
    public static String md5LowerCase(String data) {
        return Builder.md5Hex(data).toLowerCase();
    }

    /**
     * 请求参数 Map 转化为京东支付 xml
     *
     * @param params 请求参数
     * @return
     */
    public static String toJdXml(Map<String, String> params) {
        return WxPayKit.forEachMap(params, "<jdpay>", "</jdpay>").toString();
    }

    /**
     * 3DES加密
     *
     * @param desKey     DES 密钥
     * @param sourceData 需要加密的字符串
     * @return 加密后的字符串
     */
    public static String threeDesEncrypt(String desKey, String sourceData) {
        byte[] key = Base64.decode(desKey);
        return encrypt2HexString(key, sourceData);
    }

    /**
     * 3DES解密
     *
     * @param desKey     DES 密钥
     * @param sourceData 需要解密的字符串
     * @return 解密后的字符串
     */
    public static String threeDecDecrypt(String desKey, String sourceData) {
        byte[] key = Base64.decode(desKey);
        return decrypt4HexString(key, sourceData);
    }


    /**
     * <p>在线支付接口</p>
     * <p>除了merchant（商户号）、version（版本号）、sign(签名)以外，其余字段全部采用3DES进行加密。</p>
     *
     * @return 转化后的 Map
     */
    public static Map<String, String> threeDesToMap(Map<String, String> map, String desKey) {
        HashMap<String, String> tempMap = new HashMap<>(map.size());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            if (StringKit.isNotEmpty(value)) {
                if ("merchant".equals(name) || "version".equals(name) || "sign".equals(name)) {
                    tempMap.put(name, value);
                } else {
                    tempMap.put(name, threeDesEncrypt(desKey, value));
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
    public static Map<String, String> parseResp(String xml) {
        if (StringKit.isEmpty(xml)) {
            return null;
        }
        Map<String, String> map = new HashMap<String, String>(3);
        Document docResult = XmlKit.parseXml(xml);
        String code = (String) XmlKit.getByXPath("//jdpay/result/code", docResult, XPathConstants.STRING);
        String desc = (String) XmlKit.getByXPath("//jdpay/result/desc", docResult, XPathConstants.STRING);
        map.put("code", code);
        map.put("desc", desc);
        if ("000000".equals(code)) {
            String encrypt = (String) XmlKit.getByXPath("//jdpay/encrypt", docResult, XPathConstants.STRING);
            map.put("encrypt", encrypt);
        }
        return map;
    }

    public static void main(String[] args) {
        String rsaPubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3Q7knFjJBC0ZzQep5Wq3G+LcGtvPrCfUv6Wfo8Tz1rIfGvHFYjDz2z2iDCP6b5tRSePWfVjdz4O8OEdF+fQWFhJhAPNDIbT8wOALuKQ2MvpDRtZL9hOOr1K4eZNiRw2ppCXKPmi/obSf6maCP7URfhAa6rUixfcPJ5QCEaCWlteuRNYbWFRhORFGIrCOw4pULY42E2yXbgD+N7ORdNxzRZFCrdcwMyIDQ8dmFeWc17mHo/ZTxbVZxMgUQ1m3eBDc+5OGd7jiqSisohli7DvMt+VIwm1f0S5c7QtEFS0FBeul3sgLOM55mz5VlOZOdB61lDw+dh3RHf2ex6BABOb6ywIDAQAB";
        String rsaPrivateKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDdDuScWMkELRnNB6nlarcb4twa28+sJ9S/pZ+jxPPWsh8a8cViMPPbPaIMI/pvm1FJ49Z9WN3Pg7w4R0X59BYWEmEA80MhtPzA4Au4pDYy+kNG1kv2E46vUrh5k2JHDamkJco+aL+htJ/qZoI/tRF+EBrqtSLF9w8nlAIRoJaW165E1htYVGE5EUYisI7DilQtjjYTbJduAP43s5F03HNFkUKt1zAzIgNDx2YV5ZzXuYej9lPFtVnEyBRDWbd4ENz7k4Z3uOKpKKyiGWLsO8y35UjCbV/RLlztC0QVLQUF66XeyAs4znmbPlWU5k50HrWUPD52HdEd/Z7HoEAE5vrLAgMBAAECggEAbZfTmPufdPWTJAXXogh9DVM0QhCV7ci1fenzsjKTnO4j46zXaa3RR/FPZGt13l0HOPW+wdgL57Rs3Q3g0GHFjV3BP8JaltxuroSk6v5mbHGMZxMZB798bsk48fUytP0+DEY79SLjVp0A5ym5CzKOoIwFfDUfLzwkBEApommWHuOUeW12yXWa4xEttB+JAoARBgg2mhGRN0s1N6x5XWLrc0epYNs0syx58YhetQ3X77aXvgyig0haHDOG671UAB+gPCatByR3u7DgzqGDfJp9s6MXZgSp2KmfYl4JLec065XNbThMURgqKpiZgpE3Mx6EChzO9dUXExbMnbsUvf3XCQKBgQD+OU8xtdrs9Het1oaKEMwnP9xJPVIr3j7E+WIi3zhqW2IQ+4gkhvAmnsHsy/f2bM0rYckeZZWz1iWOkqzAAFnPONJiVx4jIwQ86/1s+mbX1MlreyT+9AphXadjTc1QLyAgjqWwiabiAmuFc83Ql+LEK+jLGTc9PnhVhx1Jv4dX9QKBgQDemkPuQJ7ooRGSZMjSFydRanVysTY/Ng9C74eLnLzS4t0gyKxGUFa6MHCOixOIauM5k9cuFIBEPBrl8nLOH1aMMtHV5aw5l/3XMQdTDRX6/hCc26dWolnR+Wp7+11yTTha0B3d5lQHPOZaM47+xouTa60BBCpy7+L0jCU+g5EPvwKBgQDFO3cqjPlNjxjuwJnescuBw/TGyZFfwWwXa5dskJv3P/CkVlE4bYwRmme/rDszbxP6TUI4l/196W134GmwCFWlBGOMsiQKhJc8IKacDuUNG+Qsw/xe5LzM71j3HRxl0jntqF35ycG0ZMZAYijSZZQkOCDCuUx28mlviYT6e2KopQKBgQCIFMZyYA7FJ7IWTIZ36K+glfQ2qR8AhYvO3599OdQ1F3sXD5ZBZdue9v3YJi1KuA0wpbBl+yJulE/dQtnsKDxAeNDOchlXHBOR+ecAXn+RcL+3JJCn5ZgDRPZT1NbLiWlqGtAnVycHRbOMcPh5x+aLuMeKV4Gbwgp8dTBPhx6nAQKBgAHNFHusDx5zFIkS13mlN+7rG9oDJKwr+gLp0zqGOfzLznslXGS9dze56cmWRhHQdSQBYji51Bcb5TP6Pgwv18d6M0g8NiXanIktc3OtdCw9K1lB2nZpJP0hKxkBni15wURzN5Kj0MRtPoe5vXhKF/uDu9IUwY9/x2jzJgh2o8o9";

        String strDesKey = "GX/CH6HIAT2Ubtn3ZKjfYdbxa6HgSVca";


        String param = "<jdpay><version>2.0</version><merchant>110025845001</merchant><tradeNum>201604080000055</tradeNum><tradeType>0</tradeType></jdpay>";
        String encrypt = encrypt(rsaPrivateKey, strDesKey, param);
        System.out.println("encrypt:" + encrypt);


        String decrypt = decrypt(rsaPubKey, strDesKey, encrypt);
        System.out.println("decrypt:" + decrypt);

    }

}
