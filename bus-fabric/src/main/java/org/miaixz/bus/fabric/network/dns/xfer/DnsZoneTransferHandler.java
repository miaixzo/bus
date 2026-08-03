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
package org.miaixz.bus.fabric.network.dns.xfer;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.message.DnsCodec;
import org.miaixz.bus.fabric.network.dns.message.DnsName;
import org.miaixz.bus.fabric.network.dns.message.DnsQuery;
import org.miaixz.bus.fabric.network.dns.message.DnsResponse;
import org.miaixz.bus.fabric.network.dns.message.DnsResponseCode;
import org.miaixz.bus.fabric.network.dns.record.DnsRecord;
import org.miaixz.bus.fabric.network.dns.record.DnsRecordType;
import org.miaixz.bus.fabric.network.dns.resolve.RuntimeIndex;
import org.miaixz.bus.fabric.network.dns.zone.CidrBlock;
import org.miaixz.bus.fabric.network.dns.zone.DnsZone;
import org.miaixz.bus.fabric.network.dns.zone.DnsZoneMode;

/**
 * Zone-transfer response builder for AXFR and IXFR DNS queries.
 *
 * <p>
 * Instances are thread-safe. The handler owns only immutable ACL configuration and a per-server in-flight counter used
 * to enforce the fixed zone-transfer concurrency limit. It does not open sockets, write files, or retain mutable
 * snapshot data after a response is built.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DnsZoneTransferHandler {

    /**
     * Fixed zone-transfer concurrency limit used until a dedicated transfer option is introduced.
     */
    private static final int DEFAULT_MAX_CONCURRENT_TRANSFERS = 1;

    /**
     * Half of the unsigned 32-bit serial space, used by RFC 1982 serial arithmetic.
     */
    private static final long SERIAL_HALF_RANGE = 0x8000_0000L;

    /**
     * Unsigned 32-bit mask used by RFC 1982 serial arithmetic.
     */
    private static final long SERIAL_MASK = DnsCodec.UNSIGNED_INT_MAX;

    /**
     * Client CIDR blocks allowed to request zone transfers.
     */
    private final List<CidrBlock> allowedCidrs;

    /**
     * Maximum simultaneous zone-transfer responses.
     */
    private final int maxConcurrentTransfers;

    /**
     * Current in-flight zone-transfer responses.
     */
    private final AtomicInteger activeTransfers;

    /**
     * Creates a zone-transfer handler with the fixed concurrency limit.
     *
     * @param allowedCidrs client CIDR blocks allowed to request zone transfers
     */
    public DnsZoneTransferHandler(final List<CidrBlock> allowedCidrs) {
        this(allowedCidrs, DEFAULT_MAX_CONCURRENT_TRANSFERS);
    }

    /**
     * Creates a zone-transfer handler.
     *
     * @param allowedCidrs           client CIDR blocks allowed to request zone transfers
     * @param maxConcurrentTransfers maximum simultaneous zone-transfer responses
     */
    public DnsZoneTransferHandler(final List<CidrBlock> allowedCidrs, final int maxConcurrentTransfers) {
        this.allowedCidrs = immutableCidrs(allowedCidrs);
        this.maxConcurrentTransfers = validateMaxConcurrentTransfers(maxConcurrentTransfers);
        this.activeTransfers = new AtomicInteger();
    }

    /**
     * Returns whether a query requests zone transfer data.
     *
     * @param query decoded DNS query
     * @return true when the query type is AXFR or IXFR
     */
    public static boolean transferQuery(final DnsQuery query) {
        if (query == null) {
            throw new ValidateException("DNS zone-transfer query must not be null");
        }
        final int type = query.question().typeCode();
        return type == DnsRecordType.AXFR.code() || type == DnsRecordType.IXFR.code();
    }

    /**
     * Builds a zone-transfer response from the active in-memory runtime index.
     *
     * @param current                  active runtime index
     * @param query                    decoded AXFR or IXFR query
     * @param transferCapableTransport true when the query arrived over TCP, DoT, or DoQ
     * @param clientAddress            client address, or {@code null} when unavailable
     * @return DNS response model
     */
    public DnsResponse handle(
            final RuntimeIndex current,
            final DnsQuery query,
            final boolean transferCapableTransport,
            final InetAddress clientAddress) {
        if (current == null) {
            throw new ValidateException("DNS runtime index must not be null");
        }
        if (!transferQuery(query)) {
            return DnsResponse.empty(query, DnsResponseCode.REFUSED, false);
        }
        if (!transferCapableTransport || !allowed(clientAddress) || !acquire()) {
            return DnsResponse.empty(query, DnsResponseCode.REFUSED, false);
        }
        try {
            return answer(current, query, clientAddress);
        } finally {
            release();
        }
    }

    /**
     * Builds a response after transport, ACL, and concurrency checks pass.
     *
     * @param current       active runtime index
     * @param query         decoded AXFR or IXFR query
     * @param clientAddress client address, or {@code null} when unavailable
     * @return DNS response model
     */
    private DnsResponse answer(final RuntimeIndex current, final DnsQuery query, final InetAddress clientAddress) {
        final DnsZone zone = current.findZone(query.question().name(), clientAddress);
        if (zone == null || zone.mode() != DnsZoneMode.AUTHORITATIVE) {
            return DnsResponse.empty(query, DnsResponseCode.REFUSED, false);
        }
        if (query.question().typeCode() == DnsRecordType.IXFR.code() && query.ixfrSerial() != null) {
            return ixfrResponse(query, zone);
        }
        return fullTransfer(query, zone);
    }

    /**
     * Builds an IXFR response using RFC 1982 serial comparison.
     *
     * @param query decoded IXFR query
     * @param zone  authoritative zone
     * @return DNS response model
     */
    private static DnsResponse ixfrResponse(final DnsQuery query, final DnsZone zone) {
        final int comparison = compareSerial(zoneSerial(zone), query.ixfrSerial());
        if (comparison == 0) {
            return new DnsResponse(query, DnsResponseCode.NOERROR, true, false, false, zone.soaRecords(), List.of(),
                    List.of());
        }
        if (comparison < 0) {
            return DnsResponse.empty(query, DnsResponseCode.SERVFAIL, false);
        }
        return fullTransfer(query, zone);
    }

    /**
     * Builds a full AXFR response with SOA records bracketing zone content.
     *
     * @param query decoded AXFR or IXFR query
     * @param zone  authoritative zone
     * @return DNS response model
     */
    private static DnsResponse fullTransfer(final DnsQuery query, final DnsZone zone) {
        final List<DnsRecord> soaRecords = zone.soaRecords();
        final ArrayList<DnsRecord> answers = new ArrayList<>(zone.records().size() + soaRecords.size() + 1);
        answers.addAll(soaRecords);
        for (final DnsRecord record : zone.records()) {
            if (!soaRecords.contains(record)) {
                answers.add(record);
            }
        }
        answers.addAll(soaRecords);
        return new DnsResponse(query, DnsResponseCode.NOERROR, true, false, false, answers, List.of(), List.of());
    }

    /**
     * Compares server and client SOA serial values using RFC 1982 arithmetic.
     *
     * @param serverSerial server zone SOA serial
     * @param clientSerial client IXFR base SOA serial
     * @return positive when the server is newer, zero when equal, and negative when the client is newer
     */
    private static int compareSerial(final long serverSerial, final long clientSerial) {
        final long normalizedServer = serverSerial & SERIAL_MASK;
        final long normalizedClient = clientSerial & SERIAL_MASK;
        if (normalizedServer == normalizedClient) {
            return 0;
        }
        final long distance = (normalizedServer - normalizedClient) & SERIAL_MASK;
        return distance < SERIAL_HALF_RANGE ? 1 : -1;
    }

    /**
     * Returns the current SOA serial for a zone.
     *
     * @param zone authoritative zone
     * @return unsigned 32-bit SOA serial
     */
    private static long zoneSerial(final DnsZone zone) {
        final List<DnsRecord> soaRecords = zone.soaRecords();
        if (soaRecords.isEmpty()) {
            return 0L;
        }
        return soaSerial(soaRecords.getFirst());
    }

    /**
     * Reads the serial from a zone SOA record.
     *
     * @param record SOA record
     * @return unsigned 32-bit SOA serial
     */
    private static long soaSerial(final DnsRecord record) {
        final byte[] data = record.wireData();
        final DnsName.ReadResult primary = DnsName.read(data, 0);
        final DnsName.ReadResult responsible = DnsName.read(data, primary.nextOffset());
        if (responsible.nextOffset() + 20 > data.length) {
            return 0L;
        }
        return readUnsignedInt(data, responsible.nextOffset());
    }

    /**
     * Reads an unsigned int from a byte array.
     *
     * @param bytes  source bytes
     * @param offset source offset
     * @return unsigned 32-bit value represented as a Java long
     */
    private static long readUnsignedInt(final byte[] bytes, final int offset) {
        return DnsCodec.readUnsignedInt(bytes, offset);
    }

    /**
     * Returns whether a client address is allowed to request zone transfers.
     *
     * @param clientAddress client address, or {@code null} when unavailable
     * @return true when the client is allowed
     */
    private boolean allowed(final InetAddress clientAddress) {
        if (clientAddress == null) {
            return false;
        }
        for (final CidrBlock cidr : allowedCidrs) {
            if (cidr.contains(clientAddress)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Attempts to reserve one zone-transfer slot.
     *
     * @return true when a slot was reserved
     */
    private boolean acquire() {
        while (true) {
            final int current = activeTransfers.get();
            if (current >= maxConcurrentTransfers) {
                return false;
            }
            if (activeTransfers.compareAndSet(current, current + 1)) {
                return true;
            }
        }
    }

    /**
     * Releases one zone-transfer slot.
     */
    private void release() {
        activeTransfers.decrementAndGet();
    }

    /**
     * Validates and copies CIDR blocks.
     *
     * @param cidrs source CIDR blocks
     * @return immutable CIDR blocks
     */
    private static List<CidrBlock> immutableCidrs(final List<CidrBlock> cidrs) {
        if (cidrs == null) {
            throw new ValidateException("DNS zone-transfer ACL CIDRs must not be null");
        }
        for (final CidrBlock cidr : cidrs) {
            if (cidr == null) {
                throw new ValidateException("DNS zone-transfer ACL CIDRs must not contain null");
            }
        }
        return List.copyOf(cidrs);
    }

    /**
     * Validates a zone-transfer concurrency limit.
     *
     * @param value candidate concurrency limit
     * @return validated concurrency limit
     */
    private static int validateMaxConcurrentTransfers(final int value) {
        if (value < 1) {
            throw new ValidateException("DNS zone-transfer max concurrency must be positive");
        }
        return value;
    }

}
