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
package org.miaixz.bus.crypto.builtin.asymmetric;

import java.io.Serial;
import java.io.Serializable;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.lang.thread.lock.NoLock;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.crypto.Keeper;

/**
 * Base class for asymmetric cryptography, providing lock and key pair holders.
 *
 * @param <T> The type of this class.
 * @author Kimi Liu
 * @since Java 17+
 */
public class Asymmetric<T extends Asymmetric<T>> implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852288538932L;

    /**
     * The algorithm.
     */
    protected String algorithm;
    /**
     * The public key.
     */
    protected PublicKey publicKey;
    /**
     * The private key.
     */
    protected PrivateKey privateKey;
    /**
     * The lock for thread safety.
     */
    protected Lock lock = new ReentrantLock();

    /**
     * Constructor.
     * <p>
     * If both private and public keys are null, a new key pair is generated. A single key can be provided, in which
     * case the instance can only be used for encryption or decryption with that key.
     *
     * @param algorithm The algorithm to use.
     * @param keyPair   The key pair, which includes the private and public key.
     */
    public Asymmetric(final String algorithm, final KeyPair keyPair) {
        init(algorithm, keyPair);
    }

    /**
     * Initializes the object.
     * <p>
     * If both private and public keys in the key pair are null, a new key pair is generated. If only one key is
     * provided, it can only be used for encryption (signing) or decryption (verification) with that specific key.
     *
     * @param algorithm The algorithm to use.
     * @param keyPair   The key pair, which includes the private and public key.
     * @return this instance.
     */
    protected T init(final String algorithm, final KeyPair keyPair) {
        this.algorithm = algorithm;

        final PrivateKey privateKey = ObjectKit.apply(keyPair, KeyPair::getPrivate);
        final PublicKey publicKey = ObjectKit.apply(keyPair, KeyPair::getPublic);
        if (null == privateKey && null == publicKey) {
            initKeys();
        } else {
            if (null != privateKey) {
                this.privateKey = privateKey;
            }
            if (null != publicKey) {
                this.publicKey = publicKey;
            }
        }
        return (T) this;
    }

    /**
     * Generates a random public and private key pair.
     *
     * @return this instance.
     */
    public T initKeys() {
        final KeyPair keyPair = Keeper.generateKeyPair(this.algorithm);
        this.publicKey = keyPair.getPublic();
        this.privateKey = keyPair.getPrivate();
        return (T) this;
    }

    /**
     * Sets a custom lock. Use {@link NoLock} for no locking.
     *
     * @param lock The custom lock.
     * @return this instance.
     */
    public T setLock(final Lock lock) {
        this.lock = lock;
        return (T) this;
    }

    /**
     * Gets the public key.
     *
     * @return The public key.
     */
    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    /**
     * Sets the public key.
     *
     * @param publicKey The public key.
     * @return this instance.
     */
    public T setPublicKey(final PublicKey publicKey) {
        this.publicKey = publicKey;
        return (T) this;
    }

    /**
     * Gets the public key as a Base64 encoded string.
     *
     * @return The Base64 encoded public key string.
     */
    public String getPublicKeyBase64() {
        final PublicKey publicKey = getPublicKey();
        return (null == publicKey) ? null : Base64.encode(publicKey.getEncoded());
    }

    /**
     * Gets the private key.
     *
     * @return The private key.
     */
    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    /**
     * Sets the private key.
     *
     * @param privateKey The private key.
     * @return this instance.
     */
    public T setPrivateKey(final PrivateKey privateKey) {
        this.privateKey = privateKey;
        return (T) this;
    }

    /**
     * Gets the private key as a Base64 encoded string.
     *
     * @return The Base64 encoded private key string.
     */
    public String getPrivateKeyBase64() {
        final PrivateKey privateKey = getPrivateKey();
        return (null == privateKey) ? null : Base64.encode(privateKey.getEncoded());
    }

    /**
     * Sets the key, which can be a {@link PublicKey} or a {@link PrivateKey}.
     *
     * @param key The key, which can be a {@link PublicKey} or a {@link PrivateKey}.
     * @return this instance.
     */
    public T setKey(final Key key) {
        Assert.notNull(key, "data must be not null !");

        if (key instanceof PublicKey) {
            return setPublicKey((PublicKey) key);
        } else if (key instanceof PrivateKey) {
            return setPrivateKey((PrivateKey) key);
        }
        throw new CryptoException("Unsupported data type: {}", key.getClass());
    }

    /**
     * Gets the key by its type.
     *
     * @param type The key type, see {@link KeyType}.
     * @return The {@link Key}.
     */
    protected Key getKeyByType(final KeyType type) {
        switch (type) {
            case PrivateKey:
                if (null == this.privateKey) {
                    throw new NullPointerException("Private data must not be null when used!");
                }
                return this.privateKey;

            case PublicKey:
                if (null == this.publicKey) {
                    throw new NullPointerException("Public data must not be null when used!");
                }
                return this.publicKey;
        }
        throw new CryptoException("Unsupported data type: " + type);
    }

}
