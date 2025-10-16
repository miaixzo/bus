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
package org.miaixz.bus.auth;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.url.UrlDecoder;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.core.xyz.StringKit;

import lombok.Getter;
import lombok.Setter;

/**
 * URL construction utility class, supporting OAuth-related functionalities. Provides methods to build URLs with query
 * parameters, handle OAuth signatures, generate PKCE verification codes, and other authentication flow features.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class Builder {

    /**
     * Query parameter map.
     */
    private final Map<String, String> params = new LinkedHashMap<>(7);
    /**
     * Base URL.
     */
    private String baseUrl;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private Builder() {

    }

    /**
     * Creates a Builder instance from a base URL.
     *
     * @param baseUrl the base URL
     * @return a new Builder instance
     */
    public static Builder fromUrl(String baseUrl) {
        Builder builder = new Builder();
        builder.setBaseUrl(baseUrl);
        return builder;
    }

    /**
     * Parses a string into a key-value map. The string format is {@code key=value&key=value}.
     *
     * @param text the string to parse
     * @return a key-value map
     */
    public static Map<String, String> parseStringToMap(String text) {
        Map<String, String> res;
        if (text.contains(Symbol.AND)) {
            String[] fields = text.split(Symbol.AND);
            res = new HashMap<>((int) (fields.length / 0.75 + 1));
            for (String field : fields) {
                if (field.contains(Symbol.EQUAL)) {
                    String[] keyValue = field.split(Symbol.EQUAL);
                    res.put(
                            UrlDecoder.decode(keyValue[0]),
                            keyValue.length == 2 ? UrlDecoder.decode(keyValue[1]) : null);
                }
            }
        } else {
            res = new HashMap<>(0);
        }
        return res;
    }

    /**
     * Converts a key-value map to a string. The format is {@code key=value&key=value}.
     *
     * @param map    the map to convert
     * @param encode whether to URL-encode the values
     * @return the converted string, or an empty string if the map is null or empty
     */
    public static String parseMapToString(Map<String, String> map, boolean encode) {
        if (null == map || map.isEmpty()) {
            return Normal.EMPTY;
        }
        List<String> paramList = new ArrayList<>();
        map.forEach((k, v) -> {
            if (null == v) {
                paramList.add(k + Symbol.EQUAL);
            } else {
                paramList.add(k + Symbol.EQUAL + (encode ? UrlEncoder.encodeAll(v) : v));
            }
        });
        return String.join(Symbol.AND, paramList);
    }

    /**
     * Generates an HMAC signature.
     *
     * @param key       the signing key
     * @param data      the data to be signed
     * @param algorithm the signing algorithm (e.g., HMAC-SHA1)
     * @return the signature as a byte array
     * @throws AuthorizedException if the algorithm is not supported or the key is invalid
     */
    public static byte[] sign(byte[] key, byte[] data, String algorithm) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(key, algorithm));
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException ex) {
            throw new AuthorizedException("Unsupported algorithm: " + algorithm, ex);
        } catch (InvalidKeyException ex) {
            throw new AuthorizedException("Invalid key: " + ArrayKit.toString(key), ex);
        }
    }

    /**
     * Generates a Code Verifier for OAuth 2.0 PKCE (Proof Key for Code Exchange).
     *
     * @return a Base64 URL-safe random string
     */
    public static String codeVerifier() {
        return Base64.encodeUrlSafe(RandomKit.randomString(50));
    }

    /**
     * Generates a Code Challenge for OAuth 2.0 PKCE. Reference: https://tools.ietf.org/html/rfc7636#section-4.2
     *
     * @param codeChallengeMethod the code challenge method (e.g., "S256" or "plain")
     * @param codeVerifier        the code verifier generated by the client
     * @return the code challenge
     */
    public static String codeChallenge(String codeChallengeMethod, String codeVerifier) {
        if (Algorithm.SHA256.getValue().equalsIgnoreCase(codeChallengeMethod)) {
            // code_challenge = BASE64URL-ENCODE(SHA256(ASCII(code_verifier)))
            return new String(Base64.encode(digest(codeVerifier), true), Charset.US_ASCII);
        } else {
            return codeVerifier;
        }
    }

    /**
     * Digests a string using the SHA-256 algorithm.
     *
     * @param str the string to digest
     * @return the digested byte array, or null if the algorithm is unavailable
     */
    public static byte[] digest(String str) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(Algorithm.SHA256.getValue());
            messageDigest.update(str.getBytes(Charset.UTF_8));
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Builds the URL without encoding parameter values.
     *
     * @return the constructed URL
     */
    public String build() {
        return this.build(false);
    }

    /**
     * Builds the URL, with an option to URL-encode parameter values.
     *
     * @param encode whether to URL-encode the parameter values
     * @return the constructed URL
     */
    public String build(boolean encode) {
        if (MapKit.isEmpty(this.params)) {
            return this.baseUrl;
        }
        String baseUrl = StringKit.appendIfMissing(this.baseUrl, Symbol.QUESTION_MARK, Symbol.AND);
        String paramString = parseMapToString(this.params, encode);
        return baseUrl + paramString;
    }

    /**
     * Retrieves a read-only map of query parameters.
     *
     * @return an unmodifiable map of parameters
     */
    public Map<String, Object> getReadOnlyParams() {
        return Collections.unmodifiableMap(params);
    }

    /**
     * Adds a query parameter to the URL.
     *
     * @param key   the parameter name
     * @param value the parameter value
     * @return the current Builder instance
     * @throws RuntimeException if the parameter name is empty
     */
    public Builder queryParam(String key, Object value) {
        if (StringKit.isEmpty(key)) {
            throw new RuntimeException("Parameter name cannot be empty");
        }
        String valueAsString = (value != null ? value.toString() : null);
        this.params.put(key, valueAsString);
        return this;
    }

}
