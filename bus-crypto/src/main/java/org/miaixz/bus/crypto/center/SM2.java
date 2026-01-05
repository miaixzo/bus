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

import java.io.InputStream;
import java.io.Serial;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithID;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.signers.DSAEncoding;
import org.bouncycastle.crypto.signers.PlainDSAEncoding;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.crypto.signers.StandardDSAEncoding;
import org.bouncycastle.util.BigIntegers;
import org.bouncycastle.util.encoders.Hex;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.HexKit;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.Keeper;
import org.miaixz.bus.crypto.builtin.asymmetric.AbstractCrypto;
import org.miaixz.bus.crypto.builtin.asymmetric.KeyType;

/**
 * Implementation of the Chinese National Standard SM2 asymmetric algorithm, based on the Bouncy Castle library. The SM2
 * algorithm only supports public key encryption and private key decryption. Reference:
 * https://blog.csdn.net/pridas/article/details/86118774
 *
 * <p>
 * Chinese National Standard (Guomi) algorithms include:
 * <ol>
 * <li>Asymmetric encryption and signature: SM2</li>
 * <li>Digest signature algorithm: SM3</li>
 * <li>Symmetric encryption: SM4</li>
 * </ol>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SM2 extends AbstractCrypto<SM2> {

    @Serial
    private static final long serialVersionUID = 2852290382583L;

    /**
     * SM2 Engine.
     */
    protected SM2Engine engine;
    /**
     * Signer.
     */
    protected SM2Signer signer;
    /**
     * EC private key parameters.
     */
    private ECPrivateKeyParameters privateKeyParams;
    /**
     * EC public key parameters.
     */
    private ECPublicKeyParameters publicKeyParams;
    /**
     * DSA encoding.
     */
    private DSAEncoding encoding = StandardDSAEncoding.INSTANCE;
    /**
     * SM3 digest.
     */
    private Digest digest = new SM3Digest();
    /**
     * C1C3C2 mode.
     */
    private SM2Engine.Mode mode = SM2Engine.Mode.C1C3C2;
    /**
     * Custom random number generator.
     */
    private SecureRandom random;
    /**
     * Whether to remove the 04 uncompressed flag.
     */
    private boolean removeCompressedFlag;

    /**
     * Constructor, generates a new random private/public key pair.
     */
    public SM2() {
        this(null, (byte[]) null);
    }

    /**
     * Constructor. If both private and public keys are null, a new key pair is generated. A single key (private or
     * public) can be passed, in which case it can only be used for encryption or decryption with that key.
     *
     * @param privateKey The private key in Hex or Base64 representation, must follow PKCS#8 specification.
     * @param publicKey  The public key in Hex or Base64 representation, must follow X509 specification.
     */
    public SM2(final String privateKey, final String publicKey) {
        this(Builder.decode(privateKey), Builder.decode(publicKey));
    }

    /**
     * Constructor. If both private and public keys are null, a new key pair is generated. A single key (private or
     * public) can be passed, in which case it can only be used for encryption or decryption with that key.
     *
     * @param privateKey The private key, can be in PKCS#8, D value, or PKCS#1 format.
     * @param publicKey  The public key, can be in X509, Q value, or PKCS#1 format.
     */
    public SM2(final byte[] privateKey, final byte[] publicKey) {
        this(Keeper.generateSm2PrivateKey(privateKey), Keeper.generateSm2PublicKey(publicKey));
    }

    /**
     * Constructor. If both private and public keys are null, a new key pair is generated. A single key (private or
     * public) can be passed, in which case it can only be used for encryption or decryption with that key.
     *
     * @param privateKey  The private key in hex (D value).
     * @param privateKeyX The public key X coordinate in hex.
     * @param privateKeyY The public key Y coordinate in hex.
     */
    public SM2(final String privateKey, final String privateKeyX, final String privateKeyY) {
        this(Builder.decode(privateKey), Builder.decode(privateKeyX), Builder.decode(privateKeyY));
    }

    /**
     * Constructor. If both private and public keys are null, a new key pair is generated. A single key (private or
     * public) can be passed, in which case it can only be used for encryption or decryption with that key.
     *
     * @param privateKey The private key (D value).
     * @param publicKeyX The public key X coordinate.
     * @param publicKeyY The public key Y coordinate.
     */
    public SM2(final byte[] privateKey, final byte[] publicKeyX, final byte[] publicKeyY) {
        this(Keeper.generateSm2PrivateKey(privateKey), Keeper.generateSm2PublicKey(publicKeyX, publicKeyY));
    }

    /**
     * Constructor. If both private and public keys are null, a new key pair is generated. A single key (private or
     * public) can be passed, in which case it can only be used for encryption or decryption with that key.
     *
     * @param privateKey The private key.
     * @param publicKey  The public key.
     */
    public SM2(final PrivateKey privateKey, final PublicKey publicKey) {
        super(Algorithm.SM2.getValue(), new KeyPair(publicKey, privateKey));
        this.privateKeyParams = Keeper.toPrivateParams(this.privateKey);
        this.publicKeyParams = Keeper.toPublicParams(this.publicKey);
        this.init();
    }

    /**
     * Constructor. If both private and public keys are null, a new key pair is generated. A single key (private or
     * public) can be passed, in which case it can only be used for encryption or decryption with that key.
     *
     * @param privateKey The private key, can be null.
     * @param publicKey  The public key, can be null.
     */
    public SM2(final ECPrivateKeyParameters privateKey, final ECPublicKeyParameters publicKey) {
        super(Algorithm.SM2.getValue(), null);
        this.privateKeyParams = privateKey;
        this.publicKeyParams = publicKey;
        this.init();
    }

    /**
     * Removes the 04 uncompressed flag. Ciphertext generated by libraries like gmssl does not include the 04 prefix,
     * this provides compatibility.
     *
     * @param data The ciphertext data.
     * @return The processed data.
     */
    private static byte[] removeCompressedFlag(final byte[] data) {
        if (data[0] != 0x04) {
            return data;
        }
        final byte[] result = new byte[data.length - 1];
        System.arraycopy(data, 1, result, 0, result.length);
        return result;
    }

    /**
     * Prepends the compression flag. Checks the data, as ciphertext generated by libraries like gmssl does not include
     * the 04 prefix (uncompressed data flag), this method checks and adds it if necessary. Reference:
     * https://blog.csdn.net/softt/article/details/139978608 Depending on the public key's compression form, the
     * ciphertext can have two compressed forms: C1( 03 + X ) + C3 (32 bytes) + C2 C1( 02 + X ) + C3 (32 bytes) + C2 The
     * normal form for an uncompressed public key is 04 + X + Y. Due to differences in crypto libraries, the 04 is
     * sometimes omitted. The normal form for uncompressed ciphertext is 04 + C1 + C3 + C2.
     *
     * @param data The data to be decrypted.
     * @return The data with the compression flag added.
     */
    private static byte[] prependCompressedFlag(byte[] data) {
        if (data[0] != 0x04 && data[0] != 0x02 && data[0] != 0x03) {
            // Default to uncompressed form
            data = ArrayKit.insert(data, 0, 0x04);
        }
        return data;
    }

    /**
     * Initializes the object. If both private and public keys are null, a new key pair is generated. A single key
     * (private or public) can be passed, in which case it can only be used for encryption (signing) or decryption
     * (verification).
     *
     * @return this
     */
    public SM2 init() {
        if (null == this.privateKeyParams && null == this.publicKeyParams) {
            super.initKeys();
            this.privateKeyParams = Keeper.toPrivateParams(this.privateKey);
            this.publicKeyParams = Keeper.toPublicParams(this.publicKey);
        }
        return this;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return this instance
     */
    @Override
    public SM2 initKeys() {
        // Prevents the superclass from automatically generating a key pair.
        // This operation is handled by this class. Since the user might pass
        // Parameters instead of a Key, the key will be null at this point,
        // so no new key is generated.
        return this;
    }

    /**
     * Encrypts using the public key. The result of SM2 asymmetric encryption consists of three parts: C1, C3, and C2,
     * where:
     *
     * <pre>
     * C1 is the elliptic curve point calculated from a random number.
     * C3 is the SM3 digest value.
     * C2 is the ciphertext data.
     * </pre>
     *
     * @param data The string to be encrypted, in UTF8 encoding.
     * @return The encrypted Base64 string.
     * @throws CryptoException A wrapper exception for InvalidKeyException and InvalidCipherTextException.
     */
    public String encryptBase64(final String data) {
        return encryptBase64(data, KeyType.PublicKey);
    }

    /**
     * Encrypts using the public key. The result of SM2 asymmetric encryption consists of three parts: C1, C3, and C2,
     * where:
     *
     * <pre>
     * C1 is the elliptic curve point calculated from a random number.
     * C3 is the SM3 digest value.
     * C2 is the ciphertext data.
     * </pre>
     *
     * @param in The input stream of data to be encrypted.
     * @return The encrypted Base64 string.
     */
    public String encryptBase64(final InputStream in) {
        return encryptBase64(in, KeyType.PublicKey);
    }

    /**
     * Encrypts using the public key. The result of SM2 asymmetric encryption consists of three parts: C1, C3, and C2,
     * where:
     *
     * <pre>
     * C1 is the elliptic curve point calculated from a random number.
     * C3 is the SM3 digest value.
     * C2 is the ciphertext data.
     * </pre>
     *
     * @param data The bytes to be encrypted.
     * @return The encrypted Base64 string.
     */
    public String encryptBase64(final byte[] data) {
        return encryptBase64(data, KeyType.PublicKey);
    }

    /**
     * Encrypts using the public key. The result of SM2 asymmetric encryption consists of three parts: C1, C3, and C2,
     * where:
     *
     * <pre>
     * C1 is the elliptic curve point calculated from a random number.
     * C3 is the SM3 digest value.
     * C2 is the ciphertext data.
     * </pre>
     *
     * @param data The bytes to be encrypted.
     * @return The encrypted bytes.
     * @throws CryptoException A wrapper exception for InvalidKeyException and InvalidCipherTextException.
     */
    public byte[] encrypt(final byte[] data) throws CryptoException {
        return encrypt(data, KeyType.PublicKey);
    }

    /**
     * Encrypts. The result of SM2 asymmetric encryption consists of three parts: C1, C3, and C2, where:
     *
     * <pre>
     * C1 is the elliptic curve point calculated from a random number.
     * C3 is the SM3 digest value.
     * C2 is the ciphertext data.
     * </pre>
     *
     * @param data    The bytes to be encrypted.
     * @param keyType The key type (private or public) {@link KeyType}.
     * @return The encrypted bytes.
     * @throws CryptoException A wrapper exception for InvalidKeyException and InvalidCipherTextException.
     */
    @Override
    public byte[] encrypt(final byte[] data, final KeyType keyType) throws CryptoException {
        if (KeyType.PublicKey != keyType) {
            throw new IllegalArgumentException("Encrypt is only support by public data");
        }
        return encrypt(data, new ParametersWithRandom(getCipherParameters(keyType), this.random));
    }

    /**
     * Encrypts using the public key. The result of SM2 asymmetric encryption consists of three parts: C1, C3, and C2,
     * where:
     *
     * <pre>
     * C1 is the elliptic curve point calculated from a random number.
     * C3 is the SM3 digest value.
     * C2 is the ciphertext data.
     * </pre>
     *
     * @param data The string to be encrypted, in UTF8 encoding.
     * @return The encrypted bytes.
     * @throws CryptoException A wrapper exception for InvalidKeyException and InvalidCipherTextException.
     */
    public byte[] encrypt(final String data) {
        return encrypt(data, KeyType.PublicKey);
    }

    /**
     * Encrypts using the public key. The result of SM2 asymmetric encryption consists of three parts: C1, C3, and C2,
     * where:
     *
     * <pre>
     * C1 is the elliptic curve point calculated from a random number.
     * C3 is the SM3 digest value.
     * C2 is the ciphertext data.
     * </pre>
     *
     * @param in The input stream of data to be encrypted.
     * @return The encrypted bytes.
     */
    public byte[] encrypt(final InputStream in) {
        return encrypt(in, KeyType.PublicKey);
    }

    /**
     * Encrypts. The result of SM2 asymmetric encryption consists of three parts: C1, C2, and C3, where:
     *
     * <pre>
     * C1 is the elliptic curve point calculated from a random number.
     * C2 is the ciphertext data.
     * C3 is the SM3 digest value.
     * </pre>
     *
     * @param data             The bytes to be encrypted.
     * @param pubKeyParameters The public key parameters.
     * @return The encrypted bytes.
     * @throws CryptoException A wrapper exception for InvalidKeyException and InvalidCipherTextException.
     */
    public byte[] encrypt(final byte[] data, final CipherParameters pubKeyParameters) throws CryptoException {
        lock.lock();
        final SM2Engine engine = getEngine();
        try {
            engine.init(true, pubKeyParameters);
            final byte[] result = engine.processBlock(data, 0, data.length);
            return this.removeCompressedFlag ? removeCompressedFlag(result) : result;
        } catch (final InvalidCipherTextException e) {
            throw new CryptoException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Decrypts using the private key.
     *
     * @param data The SM2 ciphertext, as a Hex or Base64 string.
     * @return The decrypted string, in UTF-8 encoding.
     */
    public String decryptString(final String data) {
        return decryptString(data, KeyType.PrivateKey);
    }

    /**
     * Decrypts using the private key.
     *
     * @param data    The SM2 ciphertext, as a Hex or Base64 string.
     * @param charset The character set.
     * @return The decrypted bytes.
     * @throws CryptoException A wrapper exception for InvalidKeyException and InvalidCipherTextException.
     */
    public String decryptString(final String data, final Charset charset) {
        return decryptString(data, KeyType.PrivateKey, charset);
    }

    /**
     * Decrypts using the private key.
     *
     * @param data The SM2 ciphertext, which actually contains three parts: the ECC public key, the real ciphertext, and
     *             the SM3-HASH of the public key and original text.
     * @return The encrypted bytes.
     * @throws CryptoException A wrapper exception for InvalidKeyException and InvalidCipherTextException.
     */
    public byte[] decrypt(final byte[] data) throws CryptoException {
        return decrypt(data, KeyType.PrivateKey);
    }

    /**
     * Decrypts.
     *
     * @param data    The SM2 ciphertext, which actually contains three parts: the ECC public key, the real ciphertext,
     *                and the SM3-HASH of the public key and original text.
     * @param keyType The key type (private or public) {@link KeyType}.
     * @return The decrypted bytes.
     * @throws CryptoException A wrapper exception for InvalidKeyException and InvalidCipherTextException.
     */
    @Override
    public byte[] decrypt(final byte[] data, final KeyType keyType) throws CryptoException {
        if (KeyType.PrivateKey != keyType) {
            throw new IllegalArgumentException("Decrypt is only support by private data");
        }
        return decrypt(data, getCipherParameters(keyType));
    }

    /**
     * Decrypts using the private key.
     *
     * @param in The ciphertext input stream.
     * @return The decrypted bytes.
     */
    public byte[] decrypt(final InputStream in) {
        return super.decrypt(in, KeyType.PrivateKey);
    }

    /**
     * Generates a digital signature for the information using the private key.
     *
     * @param dataHex The data to be signed (in Hex format).
     * @return The signature.
     */
    public String signHexFromHex(final String dataHex) {
        return signHexFromHex(dataHex, null);
    }

    /**
     * Generates a digital signature for the information using the private key.
     *
     * @param dataHex The data to be signed (in Hex format).
     * @param idHex   Can be null. If null, the default ID is the byte array "1234567812345678".getBytes().
     * @return The signature.
     */
    public String signHexFromHex(final String dataHex, final String idHex) {
        return HexKit.encodeString(sign(HexKit.decode(dataHex), HexKit.decode(idHex)));
    }

    /**
     * Generates a digital signature for the information using the private key.
     *
     * @param data The data to be signed.
     * @return The signature.
     */
    public String signHex(final byte[] data) {
        return signHex(data, null);
    }

    /**
     * Generates a digital signature for the information using the private key.
     *
     * @param data The data to be signed.
     * @param id   Can be null. If null, the default ID is the byte array "1234567812345678".getBytes().
     * @return The signature.
     */
    public String signHex(final byte[] data, final byte[] id) {
        return HexKit.encodeString(sign(data, id));
    }

    /**
     * Generates a digital signature for the information using the private key. The signature format is ASN.1. In
     * hardware signing, the result is R+S, which can be converted by calling the {@link Builder#rsAsn1ToPlain(byte[])}
     * method.
     *
     * @param data The data to be encrypted.
     * @return The signature.
     */
    public byte[] sign(final byte[] data) {
        return sign(data, null);
    }

    /**
     * Generates a digital signature for the information using the private key. The signature format is ASN.1. In
     * hardware signing, the result is R+S, which can be converted by calling the {@link Builder#rsAsn1ToPlain(byte[])}
     * method.
     *
     * @param data The data to be signed.
     * @param id   Can be null. If null, the default ID is the byte array "1234567812345678".getBytes().
     * @return The signature.
     */
    public byte[] sign(final byte[] data, final byte[] id) {
        lock.lock();
        final SM2Signer signer = getSigner();
        try {
            CipherParameters param = new ParametersWithRandom(getCipherParameters(KeyType.PrivateKey), this.random);
            if (id != null) {
                param = new ParametersWithID(param, id);
            }
            signer.init(true, param);
            signer.update(data, 0, data.length);
            return signer.generateSignature();
        } catch (final org.bouncycastle.crypto.CryptoException e) {
            throw new CryptoException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Verifies the validity of a digital signature using the public key.
     *
     * @param dataHex The data in Hex format.
     * @param signHex The signature in Hex format.
     * @return Whether the verification passes.
     */
    public boolean verifyHex(final String dataHex, final String signHex) {
        return verifyHex(dataHex, signHex, null);
    }

    /**
     * Verifies the validity of a digital signature using the public key.
     *
     * @param data The data.
     * @param sign The signature.
     * @return Whether the verification passes.
     */
    public boolean verify(final byte[] data, final byte[] sign) {
        return verify(data, sign, null);
    }

    /**
     * Verifies the validity of a digital signature using the public key.
     *
     * @param dataHex The Hex value of the data.
     * @param signHex The Hex value of the signature.
     * @param idHex   The Hex value of the ID.
     * @return Whether the verification passes.
     */
    public boolean verifyHex(final String dataHex, final String signHex, final String idHex) {
        return verify(HexKit.decode(dataHex), HexKit.decode(signHex), HexKit.decode(idHex));
    }

    /**
     * Verifies the validity of a digital signature using the public key.
     *
     * @param data The data.
     * @param sign The signature.
     * @param id   Can be null. If null, the default ID is the byte array "1234567812345678".getBytes().
     * @return Whether the verification passes.
     */
    public boolean verify(final byte[] data, final byte[] sign, final byte[] id) {
        lock.lock();
        final SM2Signer signer = getSigner();
        try {
            CipherParameters param = getCipherParameters(KeyType.PublicKey);
            if (id != null) {
                param = new ParametersWithID(param, id);
            }
            signer.init(false, param);
            signer.update(data, 0, data.length);
            return signer.verifySignature(sign);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Re-initializes key parameters to prevent update failure when resetting the key.
     *
     * @param privateKey Description inherited from parent class or interface.
     * @return this instance
     */
    @Override
    public SM2 setPrivateKey(final PrivateKey privateKey) {
        super.setPrivateKey(privateKey);

        // Re-initialize key parameters to prevent update failure when resetting the key.
        this.privateKeyParams = Keeper.toPrivateParams(privateKey);

        return this;
    }

    /**
     * Sets the private key parameters.
     *
     * @param privateKeyParams The private key parameters.
     * @return this
     */
    public SM2 setPrivateKeyParams(final ECPrivateKeyParameters privateKeyParams) {
        this.privateKeyParams = privateKeyParams;
        return this;
    }

    /**
     * Sets the random number generator. A custom random seed can be used.
     *
     * @param random The random number generator.
     * @return this
     */
    public SM2 setRandom(final SecureRandom random) {
        this.random = random;
        return this;
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Re-initializes key parameters to prevent update failure when resetting the key.
     *
     * @param publicKey Description inherited from parent class or interface.
     * @return this instance
     */
    @Override
    public SM2 setPublicKey(final PublicKey publicKey) {
        super.setPublicKey(publicKey);

        // Re-initialize key parameters to prevent update failure when resetting the key.
        this.publicKeyParams = Keeper.toPublicParams(publicKey);

        return this;
    }

    /**
     * Sets the public key parameters.
     *
     * @param publicKeyParams The public key parameters.
     * @return this
     */
    public SM2 setPublicKeyParams(final ECPublicKeyParameters publicKeyParams) {
        this.publicKeyParams = publicKeyParams;
        return this;
    }

    /**
     * Decrypts using the private key.
     *
     * @param data The SM2 ciphertext, which actually contains three parts: the ECC public key, the real ciphertext, and
     *             the SM3-HASH of the public key and original text.
     * @return The decrypted bytes.
     */
    public byte[] decrypt(final String data) {
        return super.decrypt(data, KeyType.PrivateKey);
    }

    /**
     * Sets the encoding for DSA signatures to PlainDSAEncoding.
     *
     * @return this
     */
    public SM2 usePlainEncoding() {
        return setEncoding(PlainDSAEncoding.INSTANCE);
    }

    /**
     * Sets the encoding for DSA signatures.
     *
     * @param encoding An implementation of {@link DSAEncoding}.
     * @return this
     */
    public SM2 setEncoding(final DSAEncoding encoding) {
        this.encoding = encoding;
        this.signer = null;
        return this;
    }

    /**
     * Sets the Hash algorithm.
     *
     * @param digest An implementation of {@link Digest}.
     * @return this
     */
    public SM2 setDigest(final Digest digest) {
        this.digest = digest;
        this.engine = null;
        this.signer = null;
        return this;
    }

    /**
     * Sets the SM2 mode. The old version is C1C2C3, the new version is C1C3C2.
     *
     * @param mode {@link SM2Engine.Mode}
     * @return this
     */
    public SM2 setMode(final SM2Engine.Mode mode) {
        this.mode = mode;
        this.engine = null;
        return this;
    }

    /**
     * Gets the private key D value (encoded private key).
     *
     * @return The D value.
     */
    public byte[] getD() {
        return BigIntegers.asUnsignedByteArray(32, getDBigInteger());
    }

    /**
     * Gets the private key D value (encoded private key) as a hex string.
     *
     * @return The D value in hex.
     */
    public String getDHex() {
        return new String(Hex.encode(getD()));
    }

    /**
     * Gets the private key D value.
     *
     * @return The D value.
     */
    public BigInteger getDBigInteger() {
        return this.privateKeyParams.getD();
    }

    /**
     * Gets the public key Q value (encoded public key).
     *
     * @param isCompressed Whether to compress the key.
     * @return The Q value.
     */
    public byte[] getQ(final boolean isCompressed) {
        return this.publicKeyParams.getQ().getEncoded(isCompressed);
    }

    /**
     * Gets the {@link CipherParameters} object corresponding to the key type.
     *
     * @param keyType The key type enum, including private or public key.
     * @return {@link CipherParameters}
     */
    private CipherParameters getCipherParameters(final KeyType keyType) {
        return switch (keyType) {
            case PublicKey -> {
                Assert.notNull(this.publicKeyParams, "PublicKey must be not null !");
                yield this.publicKeyParams;
            }
            case PrivateKey -> {
                Assert.notNull(this.privateKeyParams, "PrivateKey must be not null !");
                yield this.privateKeyParams;
            }
            default -> null;
        };

    }

    /**
     * Gets the {@link SM2Engine}, which is lazily loaded.
     *
     * @return {@link SM2Engine}
     */
    private SM2Engine getEngine() {
        if (null == this.engine) {
            Assert.notNull(this.digest, "digest must be not null !");
            this.engine = new SM2Engine(this.digest, this.mode);
        }
        this.digest.reset();
        return this.engine;
    }

    /**
     * Gets the {@link SM2Signer}, which is lazily loaded.
     *
     * @return {@link SM2Signer}
     */
    private SM2Signer getSigner() {
        if (null == this.signer) {
            Assert.notNull(this.digest, "digest must be not null !");
            this.signer = new SM2Signer(this.encoding, this.digest);
        }
        this.digest.reset();
        return this.signer;
    }

    /**
     * Decrypts.
     *
     * @param data                 The SM2 ciphertext, which actually contains three parts: the ECC public key, the real
     *                             ciphertext, and the SM3-HASH of the public key and original text.
     * @param privateKeyParameters The private key parameters.
     * @return The decrypted bytes.
     * @throws CryptoException A wrapper exception for InvalidKeyException and InvalidCipherTextException.
     */
    public byte[] decrypt(byte[] data, final CipherParameters privateKeyParameters) throws CryptoException {
        Assert.isTrue(data.length > 1, "Invalid SM2 cipher text, must be at least 1 byte long");
        data = prependCompressedFlag(data);

        lock.lock();
        final SM2Engine engine = getEngine();
        try {
            engine.init(false, privateKeyParameters);
            return engine.processBlock(data, 0, data.length);
        } catch (final InvalidCipherTextException e) {
            throw new CryptoException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Sets whether to remove the compression flag, default is false. After removal, the ciphertext is compatible with
     * libraries like gmssl.
     *
     * @param removeCompressedFlag Whether to remove the compression flag.
     * @return this
     */
    public SM2 setRemoveCompressedFlag(final boolean removeCompressedFlag) {
        this.removeCompressedFlag = removeCompressedFlag;
        return this;
    }

}
