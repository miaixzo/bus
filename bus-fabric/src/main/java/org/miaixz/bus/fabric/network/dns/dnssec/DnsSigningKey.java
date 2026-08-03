/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.fabric.network.dns.dnssec;

import java.time.Instant;
import java.util.Arrays;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.message.DnsCodec;
import org.miaixz.bus.fabric.network.dns.message.DnsName;
import org.miaixz.bus.fabric.network.dns.record.DnsRecord;
import org.miaixz.bus.fabric.network.dns.record.DnsRecordType;

/**
 * Immutable externally supplied DNSSEC signing key snapshot.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DnsSigningKey {

    /**
     * DNSKEY RDATA byte length before the public key.
     */
    private static final int DNSKEY_FIXED_BYTES = 4;

    /**
     * DNSKEY protocol field value required by DNSSEC.
     */
    private static final int DNSKEY_PROTOCOL_DNSSEC = 3;

    /**
     * Canonical key owner name.
     */
    private final String keyName;

    /**
     * DNSSEC algorithm code.
     */
    private final int algorithm;

    /**
     * DNSSEC key tag.
     */
    private final int keyTag;

    /**
     * Public DNSKEY RDATA bytes.
     */
    private final byte[] publicDnskeyRdata;

    /**
     * Private key bytes supplied by the external control project.
     */
    private final byte[] privateKeyBytes;

    /**
     * First instant at which the key may be used.
     */
    private final Instant notBefore;

    /**
     * First instant after which the key must not be used.
     */
    private final Instant notAfter;

    /**
     * Creates an immutable signing key snapshot.
     *
     * @param keyName           DNSKEY owner name
     * @param algorithm         DNSSEC algorithm code
     * @param keyTag            DNSSEC key tag
     * @param publicDnskeyRdata public DNSKEY RDATA bytes
     * @param privateKeyBytes   private key bytes
     * @param notBefore         first valid instant
     * @param notAfter          first invalid instant
     */
    public DnsSigningKey(final String keyName, final int algorithm, final int keyTag, final byte[] publicDnskeyRdata,
            final byte[] privateKeyBytes, final Instant notBefore, final Instant notAfter) {
        this.keyName = DnsName.normalize(keyName);
        this.algorithm = DnsCodec.validateUnsignedByte(algorithm, "DNSSEC signing key algorithm");
        this.keyTag = DnsCodec.validateUnsignedShort(keyTag, "DNSSEC signing key tag");
        this.publicDnskeyRdata = validateDnskeyRdata(publicDnskeyRdata, this.algorithm, this.keyTag);
        this.privateKeyBytes = validatePrivateKey(privateKeyBytes);
        this.notBefore = validateInstant(notBefore, "DNSSEC signing key notBefore");
        this.notAfter = validateInstant(notAfter, "DNSSEC signing key notAfter");
        if (!this.notBefore.isBefore(this.notAfter)) {
            throw new ValidateException("DNSSEC signing key notBefore must be before notAfter");
        }
    }

    /**
     * Returns the DNSKEY owner name.
     *
     * @return canonical key owner name
     */
    public String keyName() {
        return keyName;
    }

    /**
     * Returns the DNSSEC algorithm code.
     *
     * @return unsigned 8-bit algorithm code
     */
    public int algorithm() {
        return algorithm;
    }

    /**
     * Returns the DNSSEC key tag.
     *
     * @return unsigned 16-bit key tag
     */
    public int keyTag() {
        return keyTag;
    }

    /**
     * Computes the DNSKEY key tag defined for DNSSEC key material.
     *
     * @param rdata DNSKEY RDATA bytes
     * @return unsigned 16-bit DNSKEY key tag
     */
    public static int keyTag(final byte[] rdata) {
        if (rdata == null) {
            throw new ValidateException("DNSSEC DNSKEY RDATA must not be null");
        }
        long sum = 0L;
        for (int index = 0; index < rdata.length; index++) {
            sum += (index & Normal._1) == Normal._0 ? DnsCodec.readUnsignedByte(rdata, index) << Byte.SIZE
                    : DnsCodec.readUnsignedByte(rdata, index);
        }
        sum += (sum >> Short.SIZE) & Normal._65535;
        return (int) (sum & Normal._65535);
    }

    /**
     * Returns a defensive copy of public DNSKEY RDATA bytes.
     *
     * @return DNSKEY RDATA bytes
     */
    public byte[] publicDnskeyRdata() {
        return Arrays.copyOf(publicDnskeyRdata, publicDnskeyRdata.length);
    }

    /**
     * Returns a defensive copy of private key bytes.
     *
     * @return private key bytes
     */
    public byte[] privateKeyBytes() {
        return Arrays.copyOf(privateKeyBytes, privateKeyBytes.length);
    }

    /**
     * Returns the first instant at which the key may be used.
     *
     * @return first valid instant
     */
    public Instant notBefore() {
        return notBefore;
    }

    /**
     * Returns the first instant after which the key must not be used.
     *
     * @return first invalid instant
     */
    public Instant notAfter() {
        return notAfter;
    }

    /**
     * Returns whether the key is active at a given instant.
     *
     * @param instant instant to test
     * @return true when instant is inside the key validity window
     */
    public boolean activeAt(final Instant instant) {
        final Instant checked = validateInstant(instant, "DNSSEC signing key active instant");
        return !checked.isBefore(notBefore) && checked.isBefore(notAfter);
    }

    /**
     * Converts this key into a DNSKEY record.
     *
     * @param ttl unsigned 32-bit DNSKEY TTL
     * @return DNSKEY record
     */
    public DnsRecord dnskeyRecord(final long ttl) {
        return DnsRecord.raw(keyName, DnsRecordType.DNSKEY.code(), DnsRecord.CLASS_IN, ttl, publicDnskeyRdata());
    }

    /**
     * Validates public DNSKEY RDATA bytes.
     *
     * @param rdata     DNSKEY RDATA bytes
     * @param algorithm expected algorithm code
     * @param keyTag    expected key tag
     * @return copied DNSKEY RDATA bytes
     */
    private static byte[] validateDnskeyRdata(final byte[] rdata, final int algorithm, final int keyTag) {
        if (rdata == null) {
            throw new ValidateException("DNSSEC signing key DNSKEY RDATA must not be null");
        }
        if (rdata.length <= DNSKEY_FIXED_BYTES) {
            throw new ValidateException("DNSSEC signing key DNSKEY RDATA must contain public key bytes");
        }
        if (DnsCodec.readUnsignedByte(rdata, 2) != DNSKEY_PROTOCOL_DNSSEC) {
            throw new ValidateException("DNSSEC signing key DNSKEY protocol must be 3");
        }
        if (DnsCodec.readUnsignedByte(rdata, 3) != algorithm) {
            throw new ValidateException("DNSSEC signing key algorithm does not match DNSKEY RDATA");
        }
        if (keyTag(rdata) != keyTag) {
            throw new ValidateException("DNSSEC signing key tag does not match DNSKEY RDATA");
        }
        return Arrays.copyOf(rdata, rdata.length);
    }

    /**
     * Validates private key bytes.
     *
     * @param privateKeyBytes private key bytes
     * @return copied private key bytes
     */
    private static byte[] validatePrivateKey(final byte[] privateKeyBytes) {
        if (privateKeyBytes == null || privateKeyBytes.length == 0) {
            throw new ValidateException("DNSSEC signing key private key bytes must not be empty");
        }
        return Arrays.copyOf(privateKeyBytes, privateKeyBytes.length);
    }

    /**
     * Validates an instant.
     *
     * @param instant instant to validate
     * @param name    diagnostic name
     * @return validated instant
     */
    private static Instant validateInstant(final Instant instant, final String name) {
        if (instant == null) {
            throw new ValidateException(name + " must not be null");
        }
        return instant;
    }

}
