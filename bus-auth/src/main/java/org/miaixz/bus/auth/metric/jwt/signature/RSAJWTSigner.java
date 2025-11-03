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
package org.miaixz.bus.auth.metric.jwt.signature;

import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.center.Sign;

/**
 * RSA asymmetric encryption JWT signer.
 * <p>
 * Implements the {@link JWTSigner} interface, using RSA algorithms (e.g., RS256, RS384, RS512) for JWT signing and
 * verification. Supports public key for signature verification and private key for signature generation. Default
 * encoding is UTF-8.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 * @see JWTSigner
 */
public class RSAJWTSigner implements JWTSigner {

    /**
     * The core implementation for RSA signing and verification.
     */
    private final Sign sign;

    /**
     * The character encoding, default is UTF-8.
     */
    private java.nio.charset.Charset charset = Charset.UTF_8;

    /**
     * Constructor, initializes the RSA signer.
     * <p>
     * Initializes the signer based on the provided algorithm and key (public or private key). A public key is used for
     * signature verification, and a private key is used for signature generation.
     * </p>
     *
     * @param algorithm the algorithm identifier (e.g., RS256, SHA256withRSA)
     * @param key       the key ({@link PublicKey} or {@link PrivateKey})
     * @throws IllegalArgumentException if the algorithm or key is invalid
     */
    public RSAJWTSigner(final String algorithm, final Key key) {
        // Extract public or private key
        final PublicKey publicKey = key instanceof PublicKey ? (PublicKey) key : null;
        final PrivateKey privateKey = key instanceof PrivateKey ? (PrivateKey) key : null;
        // Initialize the signer with a KeyPair
        this.sign = new Sign(algorithm, new KeyPair(publicKey, privateKey));
    }

    /**
     * Constructor, initializes the RSA signer.
     * <p>
     * Initializes the signer using a key pair (containing both public and private keys).
     * </p>
     *
     * @param algorithm the algorithm identifier (e.g., RS256, SHA256withRSA)
     * @param keyPair   the key pair (containing public and private keys)
     * @throws IllegalArgumentException if the algorithm or key pair is invalid
     */
    public RSAJWTSigner(final String algorithm, final KeyPair keyPair) {
        // Initialize the signer with the KeyPair
        this.sign = new Sign(algorithm, keyPair);
    }

    /**
     * Sets the character encoding.
     *
     * @param charset the character encoding (e.g., UTF-8)
     * @return this object, supporting method chaining
     * @throws IllegalArgumentException if the charset is invalid
     */
    public RSAJWTSigner setCharset(final java.nio.charset.Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * Performs RSA signing on the JWT header and payload.
     * <p>
     * Concatenates the Base64 encoded header and payload in "header.payload" format, generates a signature using the
     * RSA algorithm, and returns the Base64 encoded result.
     * </p>
     *
     * @param headerBase64  Base64 encoded JWT header
     * @param payloadBase64 Base64 encoded JWT payload
     * @return the Base64 encoded signature
     * @throws IllegalStateException if the private key is unavailable
     */
    @Override
    public String sign(final String headerBase64, final String payloadBase64) {
        // Concatenate header and payload in "header.payload" format
        final String data = StringKit.format("{}.{}", headerBase64, payloadBase64);
        // Convert the string to a byte array and sign
        byte[] signedData = sign(ByteKit.toBytes(data, charset));
        // Return the Base64 URL-safe signature
        return Base64.encodeUrlSafe(signedData);
    }

    /**
     * Performs RSA signing on byte array data.
     * <p>
     * Generates a signature using the configured private key and algorithm.
     * </p>
     *
     * @param data the data to be signed
     * @return the signed byte array
     * @throws IllegalStateException if the private key is unavailable
     */
    protected byte[] sign(final byte[] data) {
        return sign.sign(data);
    }

    /**
     * Verifies the JWT signature.
     * <p>
     * Uses the public key to verify if the Base64 encoded signature matches the header and payload.
     * </p>
     *
     * @param headerBase64  Base64 encoded JWT header
     * @param payloadBase64 Base64 encoded JWT payload
     * @param signBase64    Base64 encoded signature to be verified
     * @return true if the verification passes, false otherwise
     * @throws IllegalStateException if the public key is unavailable
     */
    @Override
    public boolean verify(final String headerBase64, final String payloadBase64, final String signBase64) {
        // Concatenate header and payload in "header.payload" format
        byte[] data = ByteKit.toBytes(StringKit.format("{}.{}", headerBase64, payloadBase64), charset);
        // Decode the Base64 signature
        byte[] signed = Base64.decode(signBase64);
        // Verify the signature
        return verify(data, signed);
    }

    /**
     * Verifies the RSA signature of the data.
     * <p>
     * Uses the configured public key to verify the match between the data and the signature.
     * </p>
     *
     * @param data   the data to be verified
     * @param signed the signed byte array
     * @return true if the verification passes, false otherwise
     * @throws IllegalStateException if the public key is unavailable
     */
    protected boolean verify(final byte[] data, final byte[] signed) {
        return sign.verify(data, signed);
    }

    /**
     * Retrieves the name of the signing algorithm.
     *
     * @return the algorithm name (e.g., SHA256withRSA)
     */
    @Override
    public String getAlgorithm() {
        return this.sign.getSignature().getAlgorithm();
    }

}
