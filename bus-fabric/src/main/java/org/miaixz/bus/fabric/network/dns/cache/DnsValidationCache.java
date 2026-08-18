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
package org.miaixz.bus.fabric.network.dns.cache;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.message.DnsCodec;
import org.miaixz.bus.fabric.network.dns.message.DnsDecodedResponse;
import org.miaixz.bus.fabric.network.dns.message.DnsName;
import org.miaixz.bus.fabric.network.dns.record.DnsRecord;
import org.miaixz.bus.fabric.network.dns.record.DnsRecordType;

/**
 * In-memory DNSSEC validation-result cache.
 *
 * @author Kimi Liu
 */
public final class DnsValidationCache {

    /**
     * Default maximum validation cache TTL.
     */
    public static final Duration DEFAULT_MAX_TTL = Duration.ofMinutes(5);

    /**
     * Cache key kind for a complete validated RRSet.
     */
    private static final int RESPONSE_TYPE = 0;

    /**
     * Maximum TTL cap.
     */
    private final Duration maxTtl;

    /**
     * Cache entries.
     */
    private final ConcurrentHashMap<Key, Entry> entries;

    /**
     * Cache hit counter.
     */
    private final AtomicLong hits;

    /**
     * Successful write counter.
     */
    private final AtomicLong writes;

    /**
     * Creates a validation cache with the default maximum TTL.
     */
    public DnsValidationCache() {
        this(DEFAULT_MAX_TTL);
    }

    /**
     * Creates a validation cache.
     *
     * @param maxTtl maximum validation TTL
     */
    public DnsValidationCache(final Duration maxTtl) {
        if (maxTtl == null || maxTtl.isNegative() || maxTtl.isZero()) {
            throw new ValidateException("DNS validation cache max ttl must be positive");
        }
        this.maxTtl = maxTtl;
        this.entries = new ConcurrentHashMap<>();
        this.hits = new AtomicLong();
        this.writes = new AtomicLong();
    }

    /**
     * Returns whether a successful validation result exists.
     *
     * @param kind    validation kind
     * @param owner   owner name
     * @param type    record type code
     * @param records validated records
     * @param now     current instant
     * @return true when the entry exists and is not expired
     */
    public boolean contains(
            final Kind kind,
            final String owner,
            final int type,
            final List<DnsRecord> records,
            final Instant now) {
        final Key key = key(kind, owner, type, records);
        final Entry entry = entries.get(key);
        if (entry == null) {
            return false;
        }
        if (!entry.expiresAt.isAfter(validateNow(now))) {
            entries.remove(key, entry);
            return false;
        }
        hits.incrementAndGet();
        return true;
    }

    /**
     * Stores a successful validation result.
     *
     * @param kind            validation kind
     * @param owner           owner name
     * @param type            record type code
     * @param records         validated records
     * @param rrsigExpiration nearest RRSIG expiration
     * @param now             current instant
     */
    public void putSuccess(
            final Kind kind,
            final String owner,
            final int type,
            final List<DnsRecord> records,
            final Instant rrsigExpiration,
            final Instant now) {
        final Instant checkedNow = validateNow(now);
        final Duration ttl = effectiveTtl(records, rrsigExpiration, checkedNow);
        if (ttl.isZero() || ttl.isNegative()) {
            return;
        }
        entries.put(key(kind, owner, type, records), new Entry(checkedNow.plus(ttl)));
        writes.incrementAndGet();
    }

    /**
     * Returns whether a complete decoded response validation result is cached.
     *
     * @param decoded decoded response
     * @param now     current instant
     * @return true when the response validation result is cached
     */
    public boolean containsResponse(final DnsDecodedResponse decoded, final Instant now) {
        final List<DnsRecord> records = responseRecords(decoded);
        if (records.isEmpty()) {
            return false;
        }
        return contains(Kind.RRSET, DnsName.ROOT, RESPONSE_TYPE, records, now);
    }

    /**
     * Stores a complete decoded response validation result.
     *
     * @param decoded decoded response
     * @param now     current instant
     */
    public void putResponseSuccess(final DnsDecodedResponse decoded, final Instant now) {
        final List<DnsRecord> records = responseRecords(decoded);
        if (records.isEmpty()) {
            return;
        }
        putSuccess(Kind.RRSET, DnsName.ROOT, RESPONSE_TYPE, records, nearestRrsigExpiration(records), now);
    }

    /**
     * Returns cache hit count.
     *
     * @return cache hits
     */
    public long hits() {
        return hits.get();
    }

    /**
     * Returns successful write count.
     *
     * @return successful writes
     */
    public long writes() {
        return writes.get();
    }

    /**
     * Clears all validation entries.
     */
    public void clear() {
        entries.clear();
        hits.set(0L);
        writes.set(0L);
    }

    /**
     * Builds a validation key.
     *
     * @param kind    validation kind
     * @param owner   owner name
     * @param type    record type code
     * @param records records
     * @return validation key
     */
    private static Key key(final Kind kind, final String owner, final int type, final List<DnsRecord> records) {
        if (kind == null) {
            throw new ValidateException("DNS validation cache kind must not be null");
        }
        return new Key(kind, DnsName.normalize(owner), type, recordsHash(records));
    }

    /**
     * Returns all DNSSEC-relevant response records.
     *
     * @param decoded decoded response
     * @return response records
     */
    private static List<DnsRecord> responseRecords(final DnsDecodedResponse decoded) {
        if (decoded == null) {
            throw new ValidateException("DNS validation cache response must not be null");
        }
        final java.util.ArrayList<DnsRecord> records = new java.util.ArrayList<>();
        records.addAll(decoded.answers());
        records.addAll(decoded.authorities());
        records.addAll(decoded.additionals());
        return List.copyOf(records);
    }

    /**
     * Computes an effective validation TTL.
     *
     * @param records         validated records
     * @param rrsigExpiration nearest RRSIG expiration
     * @param now             current instant
     * @return effective TTL
     */
    private Duration effectiveTtl(final List<DnsRecord> records, final Instant rrsigExpiration, final Instant now) {
        long ttlSeconds = Long.MAX_VALUE;
        for (final DnsRecord record : records) {
            ttlSeconds = Math.min(ttlSeconds, record.ttl());
        }
        if (ttlSeconds == Long.MAX_VALUE || ttlSeconds <= 0L) {
            return Duration.ZERO;
        }
        Duration ttl = Duration.ofSeconds(ttlSeconds);
        if (rrsigExpiration != null) {
            ttl = min(ttl, Duration.between(now, rrsigExpiration));
        }
        return min(ttl, maxTtl);
    }

    /**
     * Finds nearest RRSIG expiration when available.
     *
     * @param records response records
     * @return nearest expiration, or {@code null}
     */
    private static Instant nearestRrsigExpiration(final List<DnsRecord> records) {
        Instant nearest = null;
        for (final DnsRecord record : records) {
            if (record.typeCode() == DnsRecordType.RRSIG.code() && record.wireData().length >= 12) {
                final long expiration = DnsCodec.readUnsignedInt(record.wireData(), 8);
                final Instant instant = Instant.ofEpochSecond(expiration);
                nearest = nearest == null || instant.isBefore(nearest) ? instant : nearest;
            }
        }
        return nearest;
    }

    /**
     * Computes a stable records hash.
     *
     * @param records records to hash
     * @return records hash
     */
    private static int recordsHash(final List<DnsRecord> records) {
        if (records == null || records.isEmpty()) {
            throw new ValidateException("DNS validation cache records must not be empty");
        }
        final ArrayList<byte[]> canonicalRecords = new ArrayList<>();
        for (final DnsRecord record : records) {
            if (record == null) {
                throw new ValidateException("DNS validation cache records must not contain null");
            }
            canonicalRecords.add(recordHashBytes(record));
        }
        canonicalRecords.sort(DnsCodec::compareUnsignedBytes);
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        for (final byte[] canonicalRecord : canonicalRecords) {
            bytes.writeBytes(canonicalRecord);
        }
        return Arrays.hashCode(bytes.toByteArray());
    }

    /**
     * Builds canonical bytes for one record cache-key component.
     *
     * @param record record to encode
     * @return canonical record bytes
     */
    private static byte[] recordHashBytes(final DnsRecord record) {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bytes.writeBytes(record.name().getBytes(Charset.US_ASCII));
        bytes.write((record.typeCode() >>> 8) & 0xff);
        bytes.write(record.typeCode() & 0xff);
        bytes.write((record.recordClass() >>> 8) & 0xff);
        bytes.write(record.recordClass() & 0xff);
        bytes.writeBytes(record.wireData());
        return bytes.toByteArray();
    }

    /**
     * Returns the smaller duration.
     *
     * @param first  first duration
     * @param second second duration
     * @return smaller duration
     */
    private static Duration min(final Duration first, final Duration second) {
        return first.compareTo(second) <= 0 ? first : second;
    }

    /**
     * Validates current instant.
     *
     * @param now current instant
     * @return validated instant
     */
    private static Instant validateNow(final Instant now) {
        if (now == null) {
            throw new ValidateException("DNS validation cache clock instant must not be null");
        }
        return now;
    }

    /**
     * Validation cache kind.
     *
     * @author Kimi Liu
     */
    public enum Kind {

        /**
         * RRSet validation result.
         */
        RRSET,

        /**
         * DNSKEY validation result.
         */
        DNSKEY,

        /**
         * DS digest validation result.
         */
        DS,

        /**
         * NSEC negative-proof validation result.
         */
        NSEC,

        /**
         * NSEC3 negative-proof validation result.
         */
        NSEC3

    }

    /**
     * Immutable validation cache key.
     *
     * @param kind        validation kind
     * @param owner       owner name
     * @param type        record type code
     * @param recordsHash records hash
     * @author Kimi Liu
     */
    private record Key(Kind kind, String owner, int type, int recordsHash) {

        /**
         * Creates a validation cache key.
         *
         * @param kind        validation kind
         * @param owner       owner name
         * @param type        record type code
         * @param recordsHash records hash
         */
        private Key {
            if (kind == null) {
                throw new ValidateException("DNS validation cache key kind must not be null");
            }
            owner = DnsName.normalize(owner);
        }

    }

    /**
     * Immutable validation cache entry.
     *
     * @param expiresAt expiration time
     * @author Kimi Liu
     */
    private record Entry(Instant expiresAt) {

        /**
         * Creates a validation cache entry.
         *
         * @param expiresAt expiration time
         */
        private Entry {
            if (expiresAt == null) {
                throw new ValidateException("DNS validation cache entry expiration must not be null");
            }
        }

    }

}
