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
package org.miaixz.bus.pay;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.asn1.gm.GMObjectIdentifiers;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.miaixz.bus.core.center.date.DateTime;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.io.resource.ClassPathResource;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.CompareKit;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.builtin.Certificate;
import org.miaixz.bus.pay.metric.wechat.AuthType;
import org.miaixz.bus.pay.metric.wechat.WechatPayBuilder;

/**
 * Payment-related support class.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Builder {

    /**
     * Gets the SM private key.
     *
     * @param privateKey The private key string.
     * @return The private key.
     * @throws Exception If an error occurs.
     */
    public static PrivateKey getSmPrivateKey(String privateKey) throws Exception {
        byte[] encPrivate = Base64.decode(privateKey);
        return getSmPrivateKey(encPrivate);
    }

    /**
     * Gets the SM public key.
     *
     * @param publicKey The public key string.
     * @return The public key.
     * @throws Exception If an error occurs.
     */
    public static PublicKey getSmPublicKey(String publicKey) throws Exception {
        byte[] encPublic = Base64.decode(publicKey);
        return getSmPublicKey(encPublic);
    }

    /**
     * Gets the SM private key.
     *
     * @param encPrivate The private key byte array.
     * @return The private key.
     * @throws Exception If an error occurs.
     */
    public static PrivateKey getSmPrivateKey(byte[] encPrivate) throws Exception {
        KeyFactory keyFact = KeyFactory.getInstance("EC", new BouncyCastleProvider());
        return keyFact.generatePrivate(new PKCS8EncodedKeySpec(encPrivate));
    }

    /**
     * Gets the SM public key.
     *
     * @param encPublic The public key byte array.
     * @return The public key.
     * @throws Exception If an error occurs.
     */
    public static PublicKey getSmPublicKey(byte[] encPublic) throws Exception {
        KeyFactory keyFact = KeyFactory.getInstance("EC", new BouncyCastleProvider());
        return keyFact.generatePublic(new X509EncodedKeySpec(encPublic));
    }

    /**
     * Signs the content with SM2 and SM3.
     *
     * @param privateKey The private key string.
     * @param content    The content to be signed.
     * @return The signature.
     * @throws Exception If an error occurs.
     */
    public static String sm2SignWithSm3(String privateKey, String content) throws Exception {
        PrivateKey smPrivateKey = getSmPrivateKey(privateKey);
        return sm2SignWithSm3(smPrivateKey, content);
    }

    /**
     * Signs the content with SM2 and SM3.
     *
     * @param privateKey The private key.
     * @param content    The content to be signed.
     * @return The signature.
     * @throws Exception If an error occurs.
     */
    public static String sm2SignWithSm3(PrivateKey privateKey, String content) throws Exception {
        // Generate SM2sign with sm3 signature algorithm instance
        Signature signature = Signature
                .getInstance(GMObjectIdentifiers.sm2sign_with_sm3.toString(), new BouncyCastleProvider());
        // Initialize the signature instance with the private key
        signature.initSign(privateKey);
        // Original text to be signed
        byte[] plainText = content.getBytes(Charset.UTF_8);
        // Update the algorithm with the original text
        signature.update(plainText);
        // Calculate the signature value
        byte[] signatureValue = signature.sign();
        return Base64.encode(signatureValue);
    }

    /**
     * Calculates the SM3 hash.
     *
     * @param content The original content.
     * @return The hash result.
     * @throws Exception If an error occurs.
     */
    public static byte[] sm3Hash(String content) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(Algorithm.SM3.getValue(), new BouncyCastleProvider());
        byte[] contentDigest = digest.digest(content.getBytes(Charset.UTF_8));
        return Arrays.copyOf(contentDigest, 16);
    }

    /**
     * Decrypts the platform certificate and callback notification.
     *
     * @param key3           The APIv3 key.
     * @param cipherText     The ciphertext.
     * @param nonce          The random string.
     * @param associatedData The associated data.
     * @return The decrypted plaintext.
     * @throws Exception If an error occurs.
     */
    public static String sm4DecryptToString(String key3, String cipherText, String nonce, String associatedData)
            throws Exception {
        Cipher cipher = Cipher.getInstance("SM4/GCM/NoPadding", new BouncyCastleProvider());
        byte[] keyByte = Builder.sm3Hash(key3);
        SecretKeySpec key = new SecretKeySpec(keyByte, "SM4");
        GCMParameterSpec spec = new GCMParameterSpec(128, nonce.getBytes(Charset.UTF_8));
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        cipher.updateAAD(associatedData.getBytes(Charset.UTF_8));
        return new String(cipher.doFinal(Base64.decode(cipherText)), Charset.UTF_8);
    }

    /**
     * Verifies the SM4 signature.
     *
     * @param publicKey         The public key string.
     * @param plainText         The plaintext to be verified.
     * @param originalSignature The original signature.
     * @return True if the signature is valid, false otherwise.
     * @throws Exception If an error occurs.
     */
    public static boolean sm4Verify(String publicKey, String plainText, String originalSignature) throws Exception {
        PublicKey smPublicKey = getSmPublicKey(publicKey);
        return sm4Verify(smPublicKey, plainText, originalSignature);
    }

    /**
     * Verifies the SM signature.
     *
     * @param publicKey         The platform certificate public key.
     * @param data              The original data to be verified.
     * @param originalSignature The signature value.
     * @return The verification result.
     * @throws Exception If an error occurs.
     */
    public static boolean sm4Verify(PublicKey publicKey, String data, String originalSignature) throws Exception {
        Signature signature = Signature
                .getInstance(GMObjectIdentifiers.sm2sign_with_sm3.toString(), new BouncyCastleProvider());
        signature.initVerify(publicKey);
        // Update the algorithm with the original data to be verified
        signature.update(data.getBytes(Charset.UTF_8));
        return signature.verify(Base64.decode(originalSignature.getBytes(Charset.UTF_8)));
    }

    /**
     * Decrypts data using AES.
     *
     * @param base64Data The data to be decrypted.
     * @param key        The key.
     * @return The decrypted data.
     */
    public static String decryptData(String base64Data, String key) {
        return org.miaixz.bus.crypto.Builder.aes(org.miaixz.bus.crypto.Builder.md5(key).toLowerCase().getBytes())
                .decryptString(base64Data);
    }

    /**
     * Encrypts data using AES.
     *
     * @param data The data to be encrypted.
     * @param key  The key.
     * @return The encrypted data.
     */
    public static String encryptData(String data, String key) {
        return org.miaixz.bus.crypto.Builder.aes(org.miaixz.bus.crypto.Builder.md5(key).toLowerCase().getBytes())
                .encryptBase64(data.getBytes());
    }

    /**
     * Sorts all elements.
     *
     * @param params The parameter group to be sorted and concatenated.
     * @return The concatenated string.
     */
    public static String createLinkString(Map<String, String> params) {
        return createLinkString(params, false);
    }

    /**
     * Creates a linked string from a map.
     *
     * @param params The parameter group to be sorted and concatenated.
     * @param encode Whether to perform URLEncoder.
     * @return The concatenated string.
     */
    public static String createLinkString(Map<String, String> params, boolean encode) {
        return createLinkString(params, "&", encode);
    }

    /**
     * Creates a linked string from a map.
     *
     * @param params  The parameter group to be sorted and concatenated.
     * @param connStr The connection symbol.
     * @param encode  Whether to perform URLEncoder.
     * @return The concatenated string.
     */
    public static String createLinkString(Map<String, String> params, String connStr, boolean encode) {
        return createLinkString(params, connStr, encode, false);
    }

    /**
     * Creates a linked string from a map.
     *
     * @param params  The parameter group to be sorted and concatenated.
     * @param connStr The connection symbol.
     * @param encode  Whether to perform URLEncoder.
     * @param quotes  Whether to add quotes.
     * @return The concatenated string.
     */
    public static String createLinkString(Map<String, String> params, String connStr, boolean encode, boolean quotes) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);
            // Skip blank values
            if (StringKit.isBlank(value)) {
                continue;
            }
            // Do not include the last & character
            if (i == keys.size() - 1) {
                if (quotes) {
                    content.append(key).append("=").append('"').append(encode ? urlEncode(value) : value).append('"');
                } else {
                    content.append(key).append("=").append(encode ? urlEncode(value) : value);
                }
            } else {
                if (quotes) {
                    content.append(key).append("=").append('"').append(encode ? urlEncode(value) : value).append('"')
                            .append(connStr);
                } else {
                    content.append(key).append("=").append(encode ? urlEncode(value) : value).append(connStr);
                }
            }
        }
        return content.toString();
    }

    /**
     * URL encodes a string.
     *
     * @param src The string to be encoded.
     * @return The encoded string.
     */
    public static String urlEncode(String src) {
        try {
            return URLEncoder.encode(src, Charset.DEFAULT_UTF_8).replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Iterates through a map and builds XML data.
     *
     * @param params The map to iterate through.
     * @param prefix The XML prefix.
     * @param suffix The XML suffix.
     * @return The XML string.
     */
    public static StringBuffer forEachMap(Map<String, String> params, String prefix, String suffix) {
        StringBuffer xml = new StringBuffer();
        if (StringKit.isNotEmpty(prefix)) {
            xml.append(prefix);
        }
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            // Skip empty values
            if (StringKit.isEmpty(value)) {
                continue;
            }
            xml.append("<").append(key).append(">");
            xml.append(entry.getValue());
            xml.append("</").append(key).append(">");
        }
        if (StringKit.isNotEmpty(suffix)) {
            xml.append(suffix);
        }
        return xml;
    }

    /**
     * Constructs a signature string.
     *
     * @param method    The HTTP method (e.g., GET, POST, PUT).
     * @param url       The request interface (e.g., /v3/certificates).
     * @param timestamp The system timestamp when the request is initiated.
     * @param nonceStr  The random string.
     * @param body      The request body.
     * @return The string to be signed.
     */
    public static String buildSignMessage(String method, String url, long timestamp, String nonceStr, String body) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(method);
        arrayList.add(url);
        arrayList.add(String.valueOf(timestamp));
        arrayList.add(nonceStr);
        arrayList.add(body);
        return buildSignMessage(arrayList);
    }

    /**
     * Constructs a signature string.
     *
     * @param timestamp The response timestamp.
     * @param nonceStr  The response random string.
     * @param body      The response body.
     * @return The response string to be signed.
     */
    public static String buildSignMessage(String timestamp, String nonceStr, String body) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(timestamp);
        arrayList.add(nonceStr);
        arrayList.add(body);
        return buildSignMessage(arrayList);
    }

    /**
     * Constructs a signature string.
     *
     * @param signMessage The parameters to be signed.
     * @return The constructed string to be signed.
     */
    public static String buildSignMessage(ArrayList<String> signMessage) {
        if (signMessage == null || signMessage.size() == 0) {
            return null;
        }
        StringBuilder sbf = new StringBuilder();
        for (String text : signMessage) {
            sbf.append(text).append("\n");
        }
        return sbf.toString();
    }

    /**
     * Creates a v3 interface signature.
     *
     * @param signMessage The parameters to be signed.
     * @param keyPath     The path to the key.pem certificate.
     * @param authType    The authentication type.
     * @return The generated v3 signature.
     * @throws Exception If an error occurs.
     */
    public static String createSign(ArrayList<String> signMessage, String keyPath, String authType) throws Exception {
        return createSign(buildSignMessage(signMessage), keyPath, authType);
    }

    /**
     * Creates a v3 interface signature.
     *
     * @param signMessage The parameters to be signed.
     * @param privateKey  The merchant private key.
     * @return The generated v3 signature.
     * @throws Exception If an error occurs.
     */
    public static String createSign(ArrayList<String> signMessage, PrivateKey privateKey) throws Exception {
        return createSign(buildSignMessage(signMessage), privateKey);
    }

    /**
     * Creates a v3 interface signature.
     *
     * @param signMessage The parameters to be signed.
     * @param keyPath     The path to the key.pem certificate.
     * @param authType    The authentication type.
     * @return The generated v3 signature.
     * @throws Exception If an error occurs.
     */
    public static String createSign(String signMessage, String keyPath, String authType) throws Exception {
        if (StringKit.isEmpty(signMessage)) {
            return null;
        }
        // Get merchant private key
        PrivateKey privateKey = Builder.getPrivateKey(keyPath, authType);
        // Generate signature
        if (StringKit.equals(authType, AuthType.SM2.getCode())) {
            return sm2SignWithSm3(privateKey, signMessage);
        }
        return WechatPayBuilder.encryptByPrivateKey(signMessage, privateKey);
    }

    /**
     * Creates a v3 interface signature.
     *
     * @param signMessage The parameters to be signed.
     * @param privateKey  The merchant private key.
     * @return The generated v3 signature.
     * @throws Exception If an error occurs.
     */
    public static String createSign(String signMessage, PrivateKey privateKey) throws Exception {
        if (StringKit.isEmpty(signMessage)) {
            return null;
        }
        // Generate signature
        return WechatPayBuilder.encryptByPrivateKey(signMessage, privateKey);
    }

    /**
     * Gets the authorization information.
     *
     * @param mchId     The merchant ID.
     * @param serialNo  The merchant API certificate serial number.
     * @param nonceStr  The random string for the request.
     * @param timestamp The timestamp.
     * @param signature The signature value.
     * @param authType  The authentication type.
     * @return The Authorization request header.
     */
    public static String getAuthorization(
            String mchId,
            String serialNo,
            String nonceStr,
            String timestamp,
            String signature,
            String authType) {
        Map<String, String> params = new HashMap<>(5);
        params.put("mchid", mchId);
        params.put("serial_no", serialNo);
        params.put("nonce_str", nonceStr);
        params.put("timestamp", timestamp);
        params.put("signature", signature);
        return authType.concat(Symbol.SPACE).concat(createLinkString(params, Symbol.COMMA, false, true));
    }

    /**
     * Gets the merchant private key.
     *
     * @param keyPath  The path to the merchant private key certificate.
     * @param authType The authentication type.
     * @return The merchant private key.
     * @throws Exception If an error occurs.
     */
    public static PrivateKey getPrivateKey(String keyPath, String authType) throws Exception {
        String originalKey = getCertFileContent(keyPath);
        if (StringKit.isEmpty(originalKey)) {
            throw new RuntimeException("Failed to get merchant private key certificate");
        }
        return getPrivateKeyByKeyContent(originalKey, authType);
    }

    /**
     * Gets the merchant private key from its content.
     *
     * @param originalKey The private key text content.
     * @param authType    The authentication type.
     * @return The merchant private key.
     * @throws Exception If an error occurs.
     */
    public static PrivateKey getPrivateKeyByKeyContent(String originalKey, String authType) throws Exception {
        String privateKey = getPrivateKeyByContent(originalKey);
        if (StringKit.equals(authType, AuthType.SM2.getCode())) {
            return getSmPrivateKey(privateKey);
        }
        return WechatPayBuilder.loadPrivateKey(privateKey);
    }

    /**
     * Gets the certificate content.
     *
     * @param originalKey The private key text content.
     * @return The merchant private key.
     */
    public static String getPrivateKeyByContent(String originalKey) {
        return originalKey.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
    }

    /**
     * Gets the certificate content.
     *
     * @param originalKey The public key text content.
     * @return The merchant public key.
     */
    public static String getPublicKeyByContent(String originalKey) {
        return originalKey.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
    }

    /**
     * Gets the certificate.
     *
     * @param inputStream The certificate file.
     * @return The certificate.
     */
    public static X509Certificate getCertificate(InputStream inputStream) {
        try {
            Security.addProvider(new BouncyCastleProvider());
            CertificateFactory cf = CertificateFactory.getInstance("X.509", new BouncyCastleProvider());
            X509Certificate cert = (X509Certificate) cf.generateCertificate(inputStream);
            cert.checkValidity();
            return cert;
        } catch (CertificateExpiredException e) {
            throw new RuntimeException("Certificate has expired", e);
        } catch (CertificateNotYetValidException e) {
            throw new RuntimeException("Certificate is not yet valid", e);
        } catch (CertificateException e) {
            throw new RuntimeException("Invalid certificate", e);
        }
    }

    /**
     * Gets the certificate.
     *
     * @param path The certificate path, supporting relative and absolute paths.
     * @return The certificate.
     */
    public static X509Certificate getCertificate(String path) {
        if (StringKit.isEmpty(path)) {
            return null;
        }
        InputStream inputStream;
        try {
            inputStream = getCertFileInputStream(path);
        } catch (IOException e) {
            throw new RuntimeException("Please check if the certificate path is correct", e);
        }
        return getCertificate(inputStream);
    }

    /**
     * Gets the certificate details.
     *
     * @param certificate The certificate.
     * @return The certificate details.
     */
    public static Certificate getCertificateInfo(X509Certificate certificate) {
        if (null == certificate) {
            return null;
        }

        return Certificate.builder().self(certificate).issuer(certificate.getIssuerDN())
                .subject(certificate.getSubjectDN()).version(String.valueOf(certificate.getVersion()))
                .notBefore(certificate.getNotBefore()).notAfter(certificate.getNotAfter())
                .serial(certificate.getSerialNumber().toString(16)).build();
    }

    /**
     * Gets the certificate details.
     *
     * @param path The certificate path, supporting relative and absolute paths.
     * @return The certificate details.
     */
    public static Certificate getCertificateInfo(String path) {
        return getCertificateInfo(getCertificate(path));
    }

    /**
     * Checks if the certificate is valid.
     *
     * @param certificate The certificate details.
     * @param mchId       The merchant ID.
     * @param offsetDay   The offset days, positive for future, negative for past.
     * @return True if valid, false otherwise.
     */
    public static boolean checkCertificateIsValid(Certificate certificate, String mchId, int offsetDay) {
        if (null == certificate) {
            return false;
        }
        Date notAfter = certificate.getNotAfter();
        if (null == notAfter) {
            return false;
        }
        // Certificate CN field
        if (StringKit.isNotEmpty(mchId)) {
            Principal subjectDn = certificate.getSubject();
            if (null == subjectDn || !subjectDn.getName().contains("CN=".concat(mchId.trim()))) {
                return false;
            }
        }
        // Certificate serial number is a fixed 40-byte string
        String serialNumber = certificate.getSerial();
        if (StringKit.isEmpty(serialNumber) || serialNumber.length() != 40) {
            return false;
        }
        // Offset time
        DateTime dateTime = DateKit.offsetDay(notAfter, offsetDay);
        DateTime now = DateKit.date(new Date());
        int compare = CompareKit.compare(dateTime, now);
        return compare >= 0;
    }

    /**
     * Checks if the certificate is valid.
     *
     * @param certificate The certificate.
     * @param mchId       The merchant ID.
     * @param offsetDay   The offset days, positive for future, negative for past.
     * @return True if valid, false otherwise.
     */
    public static boolean checkCertificateIsValid(X509Certificate certificate, String mchId, int offsetDay) {
        if (null == certificate) {
            return false;
        }
        return checkCertificateIsValid(getCertificateInfo(certificate), mchId, offsetDay);
    }

    /**
     * Checks if the certificate is valid.
     *
     * @param path      The certificate path, supporting relative and absolute paths.
     * @param mchId     The merchant ID.
     * @param offsetDay The offset days, positive for future, negative for past.
     * @return True if valid, false otherwise.
     */
    public static boolean checkCertificateIsValid(String path, String mchId, int offsetDay) {
        return checkCertificateIsValid(getCertificateInfo(path), mchId, offsetDay);
    }

    /**
     * Encrypts data with public key.
     *
     * @param data        The data to be encrypted.
     * @param certificate The platform public key certificate.
     * @return The encrypted data.
     * @throws Exception If an error occurs.
     */
    public static String rsaEncryptOAEP(String data, X509Certificate certificate) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, certificate.getPublicKey());

            byte[] dataByte = data.getBytes(Charset.UTF_8);
            byte[] cipherData = cipher.doFinal(dataByte);
            return Base64.encode(cipherData);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Current Java environment does not support RSA v1.5/OAEP", e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid certificate", e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new IllegalBlockSizeException(
                    "The length of the original string to be encrypted cannot exceed 214 bytes");
        }
    }

    /**
     * Decrypts data with private key.
     *
     * @param cipherText The encrypted string.
     * @param privateKey The private key.
     * @return The decrypted data.
     * @throws Exception If an error occurs.
     */
    public static String rsaDecryptOAEP(String cipherText, PrivateKey privateKey) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] data = Base64.decode(cipherText);
            return new String(cipher.doFinal(data), Charset.UTF_8);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Current Java environment does not support RSA v1.5/OAEP", e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid private key", e);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            throw new BadPaddingException("Decryption failed");
        }
    }

    /**
     * Returns an input stream for a classpath resource.
     *
     * @param classPath The classpath resource path.
     * @return The input stream.
     */
    public static InputStream getFileToStream(String classPath) {
        return new ClassPathResource(classPath).getStream();
    }

    /**
     * Returns the absolute path for a classpath resource.
     *
     * @param classPath The classpath resource path.
     * @return The absolute path.
     */
    public static String getAbsolutePath(String classPath) {
        return new ClassPathResource(classPath).getAbsolutePath();
    }

    /**
     * Gets the input stream of a certificate file by path.
     *
     * @param path The file path.
     * @return The file stream.
     * @throws IOException If an I/O error occurs.
     */
    public static InputStream getCertFileInputStream(String path) throws IOException {
        if (StringKit.isBlank(path)) {
            return null;
        }
        // Absolute path
        File file = new File(path);
        if (file.exists()) {
            return Files.newInputStream(file.toPath());
        }
        // Relative path
        return getFileToStream(path);
    }

    /**
     * Gets the content of a certificate file by path.
     *
     * @param path The file path.
     * @return The file content.
     * @throws IOException If an I/O error occurs.
     */
    public static String getCertFileContent(String path) throws IOException {
        return IoKit.read(getCertFileInputStream(path), Charset.UTF_8);
    }

    /**
     * Gets the real file path.
     *
     * @param path The file address.
     * @return The real file path.
     */
    public static String getFilePath(String path) {
        if (StringKit.startWith(path, Normal.CLASSPATH)) {
            return getAbsolutePath(path);
        } else {
            return path;
        }
    }

}
