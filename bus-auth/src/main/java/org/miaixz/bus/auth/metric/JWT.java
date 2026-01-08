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
package org.miaixz.bus.auth.metric;

import java.lang.reflect.Type;
import java.security.Key;
import java.security.KeyPair;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.auth.metric.jwt.JWTHeader;
import org.miaixz.bus.auth.metric.jwt.JWTPayload;
import org.miaixz.bus.auth.metric.jwt.JWTRegister;
import org.miaixz.bus.auth.metric.jwt.JWTVerifier;
import org.miaixz.bus.auth.metric.jwt.signature.JWTSigner;
import org.miaixz.bus.auth.metric.jwt.signature.JWTSignerBuilder;
import org.miaixz.bus.auth.metric.jwt.signature.NoneJWTSigner;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.JWTException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * JSON Web Token (JWT) implementation class, based on RFC 7519 standard, used to transmit claims between network
 * applications. A JWT consists of three parts:
 * <ul>
 * <li>Header: Declares the signing algorithm and other metadata.</li>
 * <li>Payload: Carries claims and plaintext data.</li>
 * <li>Signature: The cryptographic signature (JWS), ensuring data integrity.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JWT implements JWTRegister<JWT> {

    /**
     * JWT header information.
     */
    private final JWTHeader header;
    /**
     * JWT payload information.
     */
    private final JWTPayload payload;
    /**
     * Encoding charset, default is UTF-8.
     */
    private java.nio.charset.Charset charset;
    /**
     * Signer, used to generate and verify signatures.
     */
    private JWTSigner signer;
    /**
     * The parsed JWT token segments (header, payload, signature).
     */
    private List<String> tokens;

    /**
     * Creates an empty JWT object.
     *
     * @return a new JWT instance
     */
    public static JWT of() {
        return new JWT();
    }

    /**
     * Creates and parses a JWT object from a token string.
     *
     * @param token the JWT token string, in the format xxxx.yyyy.zzzz
     * @return the parsed JWT instance
     * @throws IllegalArgumentException if the token is blank or malformed
     */
    public static JWT of(final String token) {
        return new JWT(token);
    }

    /**
     * Constructor, initializes an empty JWT object and sets the default charset to UTF-8.
     */
    public JWT() {
        this.header = new JWTHeader();
        this.payload = new JWTPayload();
        this.charset = Charset.UTF_8;
    }

    /**
     * Constructor, initializes and parses a JWT token string.
     *
     * @param token the JWT token string
     */
    public JWT(final String token) {
        this();
        parse(token);
    }

    /**
     * Parses the JWT token string, splitting it into three parts: header, payload, and signature.
     *
     * @param token the JWT token string
     * @return the current JWT instance
     * @throws IllegalArgumentException if the token is blank or has an incorrect format
     */
    public JWT parse(final String token) throws IllegalArgumentException {
        Assert.notBlank(token, "Token String must be not blank!");
        final List<String> tokens = splitToken(token);
        this.tokens = tokens;
        this.header.parse(tokens.get(0), this.charset);
        this.payload.parse(tokens.get(1), this.charset);
        return this;
    }

    /**
     * Sets the character encoding used by the JWT.
     *
     * @param charset the character encoding
     * @return the current JWT instance
     */
    public JWT setCharset(final java.nio.charset.Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * Sets the signing key, defaulting to HS256 (HmacSHA256) algorithm if not specified in the header.
     *
     * @param key the signing key
     * @return the current JWT instance
     */
    public JWT setKey(final byte[] key) {
        final String algorithmId = (String) this.header.getClaim(JWTHeader.ALGORITHM);
        if (StringKit.isNotBlank(algorithmId)) {
            return setSigner(algorithmId, key);
        }
        return setSigner(JWTSignerBuilder.hs256(key));
    }

    /**
     * Sets the signing algorithm and key.
     *
     * @param algorithmId the signing algorithm ID (e.g., HS256)
     * @param key         the signing key
     * @return the current JWT instance
     */
    public JWT setSigner(final String algorithmId, final byte[] key) {
        return setSigner(JWTSignerBuilder.createSigner(algorithmId, key));
    }

    /**
     * Sets the signing algorithm and key.
     *
     * @param algorithmId the signing algorithm ID (e.g., HS256)
     * @param key         the signing key
     * @return the current JWT instance
     */
    public JWT setSigner(final String algorithmId, final Key key) {
        return setSigner(JWTSignerBuilder.createSigner(algorithmId, key));
    }

    /**
     * Sets the asymmetric signing algorithm and key pair.
     *
     * @param algorithmId the signing algorithm ID (e.g., RS256)
     * @param keyPair     the key pair
     * @return the current JWT instance
     */
    public JWT setSigner(final String algorithmId, final KeyPair keyPair) {
        return setSigner(JWTSignerBuilder.createSigner(algorithmId, keyPair));
    }

    /**
     * Sets the signer.
     *
     * @param signer the signer to use
     * @return the current JWT instance
     */
    public JWT setSigner(final JWTSigner signer) {
        this.signer = signer;
        return this;
    }

    /**
     * Retrieves the currently used signer.
     *
     * @return the {@link JWTSigner} instance
     */
    public JWTSigner getSigner() {
        return this.signer;
    }

    /**
     * Retrieves all header claims as a Map.
     *
     * @return a Map of header claims
     */
    public Map<String, Object> getHeaders() {
        return this.header.getClaimsJson();
    }

    /**
     * Retrieves the header object.
     *
     * @return the {@link JWTHeader} instance
     */
    public JWTHeader getHeader() {
        return this.header;
    }

    /**
     * Retrieves a specific header claim by its name.
     *
     * @param name the name of the header field
     * @return the value of the header field
     */
    public Object getHeader(final String name) {
        return this.header.getClaim(name);
    }

    /**
     * Retrieves the algorithm ID (alg) from the header.
     *
     * @return the algorithm ID string
     * @see JWTHeader#ALGORITHM
     */
    public String getAlgorithm() {
        return (String) this.header.getClaim(JWTHeader.ALGORITHM);
    }

    /**
     * Sets a header claim.
     *
     * @param name  the name of the header field
     * @param value the value of the header field
     * @return the current JWT instance
     */
    public JWT setHeader(final String name, final Object value) {
        this.header.setClaim(name, value);
        return this;
    }

    /**
     * Adds multiple header claims from a Map.
     *
     * @param headers a Map of header claims to add
     * @return the current JWT instance
     */
    public JWT addHeaders(final Map<String, ?> headers) {
        this.header.putAll(headers);
        return this;
    }

    /**
     * Retrieves all payload claims as a Map.
     *
     * @return a Map of payload claims
     */
    public Map<String, Object> getPayloads() {
        return this.payload.getClaimsJson();
    }

    /**
     * Retrieves the payload object.
     *
     * @return the {@link JWTPayload} instance
     */
    public JWTPayload getPayload() {
        return this.payload;
    }

    /**
     * Retrieves a specific payload claim by its name.
     *
     * @param name the name of the payload field
     * @return the value of the payload field
     */
    public Object getPayload(final String name) {
        return getPayload().getClaim(name);
    }

    /**
     * Retrieves a specific payload claim and casts it to the specified type.
     *
     * @param <T>          the target type
     * @param propertyName the name of the payload field
     * @param propertyType the target type to cast the value to
     * @return the casted payload field value, or null if unable to cast or not found
     */
    public <T> T getPayload(final String propertyName, final Type propertyType) {
        Object value = getPayload().getClaim(propertyName);
        if (value != null && propertyType instanceof Class) {
            return ((Class<T>) propertyType).cast(value);
        }
        return null;
    }

    /**
     * Sets a payload claim.
     *
     * @param name  the name of the payload field
     * @param value the value of the payload field
     * @return the current JWT instance
     */
    @Override
    public JWT setPayload(final String name, final Object value) {
        this.payload.setClaim(name, value);
        return this;
    }

    /**
     * Adds multiple payload claims from a Map.
     *
     * @param payloads a Map of payload claims to add
     * @return the current JWT instance
     */
    public JWT addPayloads(final Map<String, ?> payloads) {
        this.payload.putAll(payloads);
        return this;
    }

    /**
     * Generates the JWT string using the default signer.
     *
     * @return the JWT string (header.payload.signature)
     */
    public String sign() {
        return sign(this.signer);
    }

    /**
     * Generates the JWT string using the specified signer.
     * <p>
     * Automatically populates header information:
     * <ul>
     * <li>If "alg" is not defined, it sets the algorithm ID based on the signer.</li>
     * </ul>
     *
     * @param signer the signer to use
     * @return the JWT string
     * @throws JWTException if the signer is null
     */
    public String sign(final JWTSigner signer) {
        Assert.notNull(signer, () -> new JWTException("No Signer provided!"));
        final String algorithm = (String) this.header.getClaim(JWTHeader.ALGORITHM);
        if (StringKit.isBlank(algorithm)) {
            this.header.setClaim(JWTHeader.ALGORITHM, JWTSignerBuilder.getId(signer.getAlgorithm()));
        }
        final String headerBase64 = Base64.encodeUrlSafe(this.header.toString(), charset);
        final String payloadBase64 = Base64.encodeUrlSafe(this.payload.toString(), charset);
        final String sign = signer.sign(headerBase64, payloadBase64);
        return StringKit.format("{}.{}.{}", headerBase64, payloadBase64, sign);
    }

    /**
     * Verifies if the JWT token is valid using the default signer.
     *
     * @return true if the signature is valid, false otherwise
     */
    public boolean verify() {
        return verify(this.signer);
    }

    /**
     * Validates the JWT token, including signature and time-based claims.
     * <p>
     * Checks performed:
     * <ul>
     * <li>Signature validity</li>
     * <li>Not Before (nbf): The token's effective time must not be later than the current time.</li>
     * <li>Expires At (exp): The token's expiration time must not be earlier than the current time.</li>
     * <li>Issued At (iat): The token's issuance time must not be later than the current time.</li>
     * </ul>
     *
     * @param leeway the tolerance time in seconds, for leniency in time-based checks
     * @return true if the token is valid, false otherwise
     */
    public boolean validate(final long leeway) {
        if (!verify()) {
            return false;
        }
        try {
            JWTVerifier.of(this).validateDate(DateKit.now(), leeway);
        } catch (final ValidateException e) {
            return false;
        }
        return true;
    }

    /**
     * Verifies the JWT token using the specified signer.
     * <p>
     * If the signer is null or {@link NoneJWTSigner}, the JWT is considered unsigned, and the signature part must be
     * empty.
     *
     * @param signer the signer to use; if null, it defaults to no-signature verification
     * @return true if the signature is valid, false otherwise
     * @throws JWTException if there is no verifiable token
     */
    public boolean verify(JWTSigner signer) {
        if (null == signer) {
            signer = NoneJWTSigner.NONE;
        }
        final List<String> tokens = this.tokens;
        if (CollKit.isEmpty(tokens)) {
            throw new JWTException("No token to verify!");
        }
        return signer.verify(tokens.get(0), tokens.get(1), tokens.get(2));
    }

    /**
     * Splits the JWT token string into three parts (header, payload, signature).
     *
     * @param token the JWT token string
     * @return a List containing the three parts
     * @throws JWTException if the token format is incorrect (not three parts)
     */
    public static List<String> splitToken(final String token) {
        final List<String> tokens = StringKit.split(token, Symbol.DOT);
        if (3 != tokens.size()) {
            throw new JWTException("The token was expected 3 parts, but got {}.", tokens.size());
        }
        return tokens;
    }

}
