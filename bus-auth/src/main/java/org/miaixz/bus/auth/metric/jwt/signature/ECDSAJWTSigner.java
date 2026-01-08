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
package org.miaixz.bus.auth.metric.jwt.signature;

import java.security.Key;
import java.security.KeyPair;

import org.miaixz.bus.core.lang.exception.JWTException;

/**
 * Elliptic Curve Digital Signature Algorithm (ECDSA) JWT signer, extending from {@link RSAJWTSigner}.
 * <p>
 * According to <a href="https://datatracker.ietf.org/doc/html/rfc7518#section-3.4">RFC 7518</a>, ECDSA signatures
 * require conversion from DER format to (R, S) pair, or vice versa, for signing and verification. Supports ES256,
 * ES384, and ES512 algorithms.
 * </p>
 *
 * @see RSAJWTSigner
 * @author Kimi Liu
 * @since Java 17+
 */
public class ECDSAJWTSigner extends RSAJWTSigner {

    /**
     * Constructor, initializes the ECDSA signer.
     *
     * @param algorithm the algorithm identifier (e.g., ES256, SHA256withECDSA)
     * @param key       the ECDSA key (public or private key)
     * @throws IllegalArgumentException if the key or algorithm is invalid
     */
    public ECDSAJWTSigner(final String algorithm, final Key key) {
        super(algorithm, key);
    }

    /**
     * Constructor, initializes the ECDSA signer.
     *
     * @param algorithm the algorithm identifier (e.g., ES256, SHA256withECDSA)
     * @param keyPair   the ECDSA key pair (containing public and private keys)
     * @throws IllegalArgumentException if the key pair or algorithm is invalid
     */
    public ECDSAJWTSigner(final String algorithm, final KeyPair keyPair) {
        super(algorithm, keyPair);
    }

    /**
     * Retrieves the signature byte array length for a given ECDSA algorithm.
     * <p>
     * Returns the corresponding signature length (64, 96, or 132 bytes) based on the algorithm (e.g., ES256, ES384,
     * ES512).
     * </p>
     *
     * @param alg the algorithm identifier (e.g., ES256, SHA256withECDSA)
     * @return the signature byte array length
     * @throws JWTException if the algorithm is not supported
     */
    private static int getSignatureByteArrayLength(final String alg) throws JWTException {
        // Check the algorithm and return the corresponding signature length
        switch (alg) {
            case "ES256":
            case "SHA256withECDSA":
                return 64;

            case "ES384":
            case "SHA384withECDSA":
                return 96;

            case "ES512":
            case "SHA512withECDSA":
                return 132;

            default:
                // Throw an exception for unsupported algorithms
                throw new JWTException("Unsupported Algorithm: {}", alg);
        }
    }

    /**
     * Converts a DER-encoded signature to the JWS-required (R, S) concatenated format.
     * <p>
     * Parses the DER format, extracts the R and S components, and concatenates them into a fixed-length byte array.
     * </p>
     *
     * @param derSignature the DER-encoded signature
     * @param outputLength the target signature length (determined by the algorithm)
     * @return the (R, S) concatenated byte array
     * @throws JWTException if the DER format is invalid
     */
    private static byte[] derToConcat(final byte[] derSignature, final int outputLength) throws JWTException {
        // Validate DER signature length and starting byte (0x30 for sequence)
        if (derSignature.length < 8 || derSignature[0] != 48) {
            throw new JWTException("Invalid ECDSA signature format");
        }

        // Determine offset, handle length bytes (0x81 for long length)
        final int offset;
        if (derSignature[1] > 0) {
            offset = 2;
        } else if (derSignature[1] == (byte) 0x81) {
            offset = 3;
        } else {
            throw new JWTException("Invalid ECDSA signature format");
        }

        // Get the length of the R component
        final byte rLength = derSignature[offset + 1];
        // Skip leading zero bytes, determine effective length of R
        int i = rLength;
        while ((i > 0) && (derSignature[(offset + 2 + rLength) - i] == 0)) {
            i--;
        }

        // Get the length of the S component
        final byte sLength = derSignature[offset + 2 + rLength + 1];
        // Skip leading zero bytes, determine effective length of S
        int j = sLength;
        while ((j > 0) && (derSignature[(offset + 2 + rLength + 2 + sLength) - j] == 0)) {
            j--;
        }

        // Determine the length of the output array, taking the maximum of R, S, and target length
        int rawLen = Math.max(i, j);
        rawLen = Math.max(rawLen, outputLength / 2);

        // Validate the integrity of the DER format
        if ((derSignature[offset - 1] & 0xff) != derSignature.length - offset
                || (derSignature[offset - 1] & 0xff) != 2 + rLength + 2 + sLength || derSignature[offset] != 2
                || derSignature[offset + 2 + rLength] != 2) {
            throw new JWTException("Invalid ECDSA signature format");
        }

        // Create the output array with length 2 * rawLen
        final byte[] concatSignature = new byte[2 * rawLen];
        // Copy the R component to the output array
        System.arraycopy(derSignature, (offset + 2 + rLength) - i, concatSignature, rawLen - i, i);
        // Copy the S component to the output array
        System.arraycopy(derSignature, (offset + 2 + rLength + 2 + sLength) - j, concatSignature, 2 * rawLen - j, j);

        return concatSignature;
    }

    /**
     * Converts a JWS (R, S) concatenated format signature to DER format.
     * <p>
     * Extracts the R and S components from the (R, S) byte array and constructs a signature compliant with ASN.1 DER
     * encoding.
     * </p>
     *
     * @param jwsSignature the JWS format signature ((R, S) concatenated)
     * @return the DER-encoded signature byte array
     * @throws JWTException if the signature format is invalid or too long
     */
    private static byte[] concatToDER(final byte[] jwsSignature) {
        // Calculate half length for R and S components
        final int rawLen = jwsSignature.length / 2;

        // Skip leading zero bytes for R component
        int i = rawLen;
        while ((i > 0) && (jwsSignature[rawLen - i] == 0)) {
            i--;
        }

        // Determine actual length of R, considering sign bit
        int j = i;
        if (jwsSignature[rawLen - i] < 0) {
            j += 1;
        }

        // Skip leading zero bytes for S component
        int k = rawLen;
        while ((k > 0) && (jwsSignature[2 * rawLen - k] == 0)) {
            k--;
        }

        // Determine actual length of S, considering sign bit
        int l = k;
        if (jwsSignature[2 * rawLen - k] < 0) {
            l += 1;
        }

        // Calculate total length for DER encoding
        final int len = 2 + j + 2 + l;
        // Validate length (must not exceed 255)
        if (len > 255) {
            throw new JWTException("Invalid ECDSA signature format");
        }

        // Initialize DER output array
        final byte[] derSignature;
        int offset;
        // Choose encoding method based on length (short or long form)
        if (len < 128) {
            derSignature = new byte[2 + 2 + j + 2 + l];
            offset = 1;
        } else {
            derSignature = new byte[3 + 2 + j + 2 + l];
            derSignature[1] = (byte) 0x81;
            offset = 2;
        }

        // Set DER sequence header (0x30)
        derSignature[0] = 48;
        // Set total sequence length
        derSignature[offset++] = (byte) len;
        // Set R component identifier (0x02)
        derSignature[offset++] = 2;
        // Set R component length
        derSignature[offset++] = (byte) j;
        // Copy R component
        System.arraycopy(jwsSignature, rawLen - i, derSignature, (offset + j) - i, i);
        // Update offset
        offset += j;
        // Set S component identifier (0x02)
        derSignature[offset++] = 2;
        // Set S component length
        derSignature[offset++] = (byte) l;
        // Copy S component
        System.arraycopy(jwsSignature, 2 * rawLen - k, derSignature, (offset + l) - k, k);

        return derSignature;
    }

    /**
     * Performs ECDSA signing on data and converts the result from DER format to an (R, S) pair.
     * <p>
     * After calling the parent's signing method, the DER-encoded signature is converted to the JWS-required (R, S)
     * concatenated format.
     * </p>
     *
     * @param data the data to be signed
     * @return the signed (R, S) byte array
     * @throws JWTException if the signing process fails or the format is invalid
     */
    @Override
    protected byte[] sign(final byte[] data) {
        // Call the parent RSAJWTSigner's sign method to generate a DER-encoded signature
        byte[] derSignature = super.sign(data);
        // Convert to the JWS-required (R, S) concatenated format
        return derToConcat(derSignature, getSignatureByteArrayLength(getAlgorithm()));
    }

    /**
     * Verifies an ECDSA signature, converting the input (R, S) format to DER format first.
     * <p>
     * Converts the JWS (R, S) concatenated format to DER format, then calls the parent's verification method.
     * </p>
     *
     * @param data   the data to be verified
     * @param signed the signature byte array (in (R, S) format)
     * @return true if the verification passes, false otherwise
     * @throws JWTException if the verification process fails or the format is invalid
     */
    @Override
    protected boolean verify(final byte[] data, final byte[] signed) {
        // Convert (R, S) format to DER format
        byte[] derSignature = concatToDER(signed);
        // Call the parent RSAJWTSigner's verify method
        return super.verify(data, derSignature);
    }

}
