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
package org.miaixz.bus.crypto.center;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serial;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Set;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.HexKit;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.Keeper;
import org.miaixz.bus.crypto.builtin.asymmetric.Asymmetric;

/**
 * Signature wrapper class for {@link Signature}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Sign extends Asymmetric<Sign> {

    @Serial
    private static final long serialVersionUID = 2852290831738L;

    /**
     * The {@link Signature} object used for signing and verification.
     */
    protected Signature signature;

    /**
     * Constructs a {@code Sign} instance, generating a new private-public key pair.
     *
     * @param algorithm The {@link Algorithm} to use for signing.
     */
    public Sign(final Algorithm algorithm) {
        this(algorithm, null, (byte[]) null);
    }

    /**
     * Constructs a {@code Sign} instance, generating a new private-public key pair.
     *
     * @param algorithm The name of the algorithm to use for signing.
     */
    public Sign(final String algorithm) {
        this(algorithm, null, (byte[]) null);
    }

    /**
     * Constructs a {@code Sign} instance with the specified algorithm and key pair. If both private and public keys
     * within the {@code keyPair} are {@code null}, a new key pair will be generated. If only one key is provided, the
     * object can only be used for signing or verification corresponding to that key.
     *
     * @param algorithm The algorithm name, see {@link Algorithm}.
     * @param keyPair   The {@link KeyPair} containing the private and public keys. If {@code null}, a new random key
     *                  pair is generated.
     */
    public Sign(final String algorithm, final KeyPair keyPair) {
        super(algorithm, keyPair);
    }

    /**
     * Constructs a {@code Sign} instance with the specified algorithm and key pair. If both private and public keys
     * within the {@code keyPair} are {@code null}, a new key pair will be generated. If only one key is provided, the
     * object can only be used for signing or verification corresponding to that key.
     *
     * @param algorithm The {@link Algorithm} to use for signing.
     * @param keyPair   The {@link KeyPair} containing the private and public keys. If {@code null}, a new random key
     *                  pair is generated.
     */
    public Sign(final Algorithm algorithm, final KeyPair keyPair) {
        this(algorithm.getValue(), keyPair);
    }

    /**
     * Constructs a {@code Sign} instance with the specified algorithm and private/public keys provided as Base64
     * encoded strings. If both private and public keys are {@code null}, a new key pair will be generated. If only one
     * key is provided, the object can only be used for signing or verification corresponding to that key.
     *
     * @param algorithm  The asymmetric encryption algorithm name.
     * @param privateKey The private key as a Base64 encoded string.
     * @param publicKey  The public key as a Base64 encoded string.
     */
    public Sign(final String algorithm, final String privateKey, final String publicKey) {
        this(algorithm, Base64.decode(privateKey), Base64.decode(publicKey));
    }

    /**
     * Constructs a {@code Sign} instance with the specified algorithm and private/public keys provided as byte arrays.
     * If both private and public keys are {@code null}, a new key pair will be generated. If only one key is provided,
     * the object can only be used for signing or verification corresponding to that key.
     *
     * @param algorithm  The algorithm name.
     * @param privateKey The private key as a byte array.
     * @param publicKey  The public key as a byte array.
     */
    public Sign(final String algorithm, final byte[] privateKey, final byte[] publicKey) {
        this(algorithm, new KeyPair(Keeper.generatePublicKey(algorithm, publicKey),
                Keeper.generatePrivateKey(algorithm, privateKey)));
    }

    /**
     * Constructs a {@code Sign} instance with the specified algorithm and private/public keys provided as Hex or Base64
     * encoded strings. If both private and public keys are {@code null}, a new key pair will be generated. If only one
     * key is provided, the object can only be used for signing or verification corresponding to that key.
     *
     * @param algorithm  The {@link Algorithm} to use for signing.
     * @param privateKey The private key as a Hex or Base64 encoded string.
     * @param publicKey  The public key as a Hex or Base64 encoded string.
     */
    public Sign(final Algorithm algorithm, final String privateKey, final String publicKey) {
        this(algorithm.getValue(), Builder.decode(privateKey), Builder.decode(publicKey));
    }

    /**
     * Constructs a {@code Sign} instance with the specified algorithm and private/public keys provided as byte arrays.
     * If both private and public keys are {@code null}, a new key pair will be generated. If only one key is provided,
     * the object can only be used for signing or verification corresponding to that key.
     *
     * @param algorithm  The {@link Algorithm} to use for signing.
     * @param privateKey The private key as a byte array.
     * @param publicKey  The public key as a byte array.
     */
    public Sign(final Algorithm algorithm, final byte[] privateKey, final byte[] publicKey) {
        this(algorithm.getValue(), privateKey, publicKey);
    }

    /**
     * Initializes the {@code Sign} instance with the specified algorithm and key pair. If {@code keyPair} is
     * {@code null}, a new key pair will be randomly generated.
     *
     * @param algorithm The algorithm name.
     * @param keyPair   The {@link KeyPair} containing the private and public keys. If {@code null}, a new random key
     *                  pair is generated.
     * @return This {@code Sign} instance.
     */
    @Override
    public Sign init(final String algorithm, final KeyPair keyPair) {
        signature = Builder.createSignature(algorithm);
        super.init(algorithm, keyPair);
        return this;
    }

    /**
     * Sets the algorithm parameters for the signature operation.
     *
     * @param params The {@link AlgorithmParameterSpec} to set.
     * @return This {@code Sign} instance.
     * @throws CryptoException if setting the parameters fails.
     */
    public Sign setParameter(final AlgorithmParameterSpec params) {
        try {
            this.signature.setParameter(params);
        } catch (final InvalidAlgorithmParameterException e) {
            throw new CryptoException(e);
        }
        return this;
    }

    /**
     * Generates a digital signature for the given data and returns it as a hexadecimal string.
     *
     * @param data The data to be signed.
     * @return The generated signature as a hexadecimal string.
     */
    public String signHex(final byte[] data) {
        return HexKit.encodeString(sign(data));
    }

    /**
     * Generates a digital signature for the data from an input stream and returns it as a hexadecimal string. Uses a
     * default buffer size of {@link Normal#_8192}.
     *
     * @param data The input stream containing the data to be signed.
     * @return The generated signature as a hexadecimal string.
     */
    public String signHex(final InputStream data) {
        return signHex(data, -1);
    }

    /**
     * Generates a digital signature for the data from an input stream with a specified buffer length and returns it as
     * a hexadecimal string. If {@code bufferLength} is less than 1, {@link Normal#_8192} is used as default.
     *
     * @param data         The input stream containing the data to be signed.
     * @param bufferLength The buffer length to use for reading the stream.
     * @return The generated signature as a hexadecimal string.
     */
    public String signHex(final InputStream data, final int bufferLength) {
        return HexKit.encodeString(sign(data, bufferLength));
    }

    /**
     * Generates a digital signature for the given data using the private key.
     *
     * @param data The data to be signed.
     * @return The generated signature as a byte array.
     */
    public byte[] sign(final byte[] data) {
        return sign(new ByteArrayInputStream(data), -1);
    }

    /**
     * Generates a digital signature for the data from an input stream using the private key. Uses a default buffer size
     * of {@link Normal#_8192}.
     *
     * @param data The {@link InputStream} containing the data to be signed.
     * @return The generated signature as a byte array.
     */
    public byte[] sign(final InputStream data) {
        return sign(data, -1);
    }

    /**
     * Generates a digital signature for the data from an input stream with a specified buffer length using the private
     * key. If {@code bufferLength} is less than 1, {@link Normal#_8192} is used as default.
     *
     * @param data         The {@link InputStream} containing the data to be signed.
     * @param bufferLength The buffer length to use for reading the stream.
     * @return The generated signature as a byte array.
     * @throws CryptoException if the signing operation fails.
     */
    public byte[] sign(final InputStream data, int bufferLength) {
        if (bufferLength < 1) {
            bufferLength = Normal._8192;
        }

        final byte[] buffer = new byte[bufferLength];
        lock.lock();
        try {
            signature.initSign(this.privateKey);
            final byte[] result;
            try {
                int read = data.read(buffer, 0, bufferLength);
                while (read > -1) {
                    signature.update(buffer, 0, read);
                    read = data.read(buffer, 0, bufferLength);
                }
                result = signature.sign();
            } catch (final Exception e) {
                throw new CryptoException(e);
            }
            return result;
        } catch (final Exception e) {
            throw new CryptoException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Verifies the digital signature of the given data using the public key.
     *
     * @param data The original data that was signed.
     * @param sign The digital signature to verify.
     * @return {@code true} if the signature is valid, {@code false} otherwise.
     * @throws CryptoException if the verification operation fails.
     */
    public boolean verify(final byte[] data, final byte[] sign) {
        lock.lock();
        try {
            signature.initVerify(this.publicKey);
            signature.update(data);
            return signature.verify(sign);
        } catch (final Exception e) {
            throw new CryptoException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves the underlying {@link Signature} object.
     *
     * @return The {@link Signature} instance.
     */
    public Signature getSignature() {
        return signature;
    }

    /**
     * Sets the {@link Signature} object to be used.
     *
     * @param signature The {@link Signature} object.
     * @return This {@code Sign} instance.
     */
    public Sign setSignature(final Signature signature) {
        this.signature = signature;
        return this;
    }

    /**
     * Sets the public key from a {@link Certificate}. If the certificate is an {@link X509Certificate}, it checks for
     * critical Key Usage extensions.
     *
     * @param certificate The {@link Certificate} containing the public key.
     * @return This {@code Sign} instance.
     * @throws CryptoException if the certificate has a critical Key Usage extension that disallows digital signatures.
     */
    public Sign setCertificate(final Certificate certificate) {
        // If the certificate is of type X509Certificate,
        // we should check whether it has a Key Usage
        // extension marked as critical.
        if (certificate instanceof X509Certificate) {
            // Check whether the cert has a key usage extension
            // marked as a critical extension.
            // The OID for KeyUsage extension is 2.5.29.15.
            final X509Certificate cert = (X509Certificate) certificate;
            final Set<String> critSet = cert.getCriticalExtensionOIDs();

            if (CollKit.isNotEmpty(critSet) && critSet.contains("2.5.29.15")) {
                final boolean[] keyUsageInfo = cert.getKeyUsage();
                // keyUsageInfo[0] is for digitalSignature.
                if ((keyUsageInfo != null) && (keyUsageInfo[0] == false)) {
                    throw new CryptoException("Wrong key usage");
                }
            }
        }
        this.publicKey = certificate.getPublicKey();
        return this;
    }

}
