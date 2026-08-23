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
package org.miaixz.bus.fabric.network.dns.record;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.message.DnsCodec;
import org.miaixz.bus.fabric.network.dns.message.DnsName;

/**
 * Immutable DNS RRSet model for records sharing owner name, type, class, and TTL.
 *
 * <p>
 * This class is intended for external snapshot construction and validation. It does not own sockets, threads, timers,
 * or persistent state. DNS query execution continues to use compiled immutable {@link DnsRecord} instances.
 * </p>
 *
 * @author Kimi Liu
 */
public class DnsRecordSet {

    /**
     * Canonical owner name.
     */
    private final String name;

    /**
     * Numeric DNS record type.
     */
    private final int typeCode;

    /**
     * Numeric DNS record class.
     */
    private final int recordClass;

    /**
     * Unsigned 32-bit TTL.
     */
    private final long ttl;

    /**
     * Immutable record-data values.
     */
    private final List<DnsRecordData> data;

    /**
     * Creates an Internet-class DNS RRSet.
     *
     * @param name owner name
     * @param type DNS record type
     * @param ttl  unsigned 32-bit TTL
     * @param data record-data values
     */
    public DnsRecordSet(final String name, final DnsRecordType type, final long ttl, final List<DnsRecordData> data) {
        this(name, type == null ? -1 : type.code(), DnsRecord.CLASS_IN, ttl, data);
    }

    /**
     * Creates a DNS RRSet.
     *
     * @param name        owner name
     * @param typeCode    unsigned 16-bit DNS record type code
     * @param recordClass unsigned 16-bit DNS record class
     * @param ttl         unsigned 32-bit TTL
     * @param data        record-data values
     */
    public DnsRecordSet(final String name, final int typeCode, final int recordClass, final long ttl,
            final List<DnsRecordData> data) {
        this.name = DnsName.normalize(name);
        this.typeCode = DnsCodec.validateUnsignedShort(typeCode, "DNS RRSet type");
        this.recordClass = DnsCodec.validateUnsignedShort(recordClass, "DNS RRSet class");
        this.ttl = DnsCodec.validateUnsignedInt(ttl, "DNS RRSet TTL");
        this.data = immutableData(data, this.typeCode);
    }

    /**
     * Creates an Internet-class DNS RRSet.
     *
     * @param name owner name
     * @param type DNS record type
     * @param ttl  unsigned 32-bit TTL
     * @param data record-data values
     * @return immutable RRSet
     */
    public static DnsRecordSet of(
            final String name,
            final DnsRecordType type,
            final long ttl,
            final List<DnsRecordData> data) {
        return new DnsRecordSet(name, type, ttl, data);
    }

    /**
     * Creates a DNS RRSet from existing records.
     *
     * @param records records sharing owner name, type, class, and TTL
     * @return immutable RRSet
     */
    public static DnsRecordSet fromRecords(final List<DnsRecord> records) {
        if (records == null || records.isEmpty()) {
            throw new ValidateException("DNS RRSet records must not be empty");
        }
        final DnsRecord first = records.getFirst();
        if (first == null) {
            throw new ValidateException("DNS RRSet records must not contain null");
        }
        final ArrayList<DnsRecordData> values = new ArrayList<>(records.size());
        for (final DnsRecord record : records) {
            if (record == null) {
                throw new ValidateException("DNS RRSet records must not contain null");
            }
            if (!first.name().equals(record.name()) || first.typeCode() != record.typeCode()
                    || first.recordClass() != record.recordClass() || first.ttl() != record.ttl()) {
                throw new ValidateException("DNS RRSet records must share owner name, type, class, and TTL");
            }
            values.add(DnsRecordData.fromRecord(record));
        }
        return new DnsRecordSet(first.name(), first.typeCode(), first.recordClass(), first.ttl(), values);
    }

    /**
     * Returns the owner name.
     *
     * @return canonical owner name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the known DNS record type.
     *
     * @return known record type, or {@link DnsRecordType#UNKNOWN}
     */
    public DnsRecordType type() {
        return DnsRecordType.fromCode(typeCode);
    }

    /**
     * Returns the numeric DNS record type.
     *
     * @return unsigned 16-bit type code
     */
    public int typeCode() {
        return typeCode;
    }

    /**
     * Returns the numeric DNS record class.
     *
     * @return unsigned 16-bit class code
     */
    public int recordClass() {
        return recordClass;
    }

    /**
     * Returns the RRSet TTL.
     *
     * @return unsigned 32-bit TTL
     */
    public long ttl() {
        return ttl;
    }

    /**
     * Returns record-data values.
     *
     * @return immutable record-data values
     */
    public List<DnsRecordData> data() {
        return data;
    }

    /**
     * Converts this RRSet into runtime DNS records.
     *
     * @return immutable DNS records
     */
    public List<DnsRecord> toRecords() {
        final ArrayList<DnsRecord> records = new ArrayList<>(data.size());
        for (final DnsRecordData value : data) {
            records.add(value.toRecord(name, recordClass, ttl));
        }
        return List.copyOf(records);
    }

    /**
     * Validates and copies record-data values.
     *
     * @param data     source data values
     * @param typeCode expected type code
     * @return immutable data values
     */
    private static List<DnsRecordData> immutableData(final List<DnsRecordData> data, final int typeCode) {
        if (data == null || data.isEmpty()) {
            throw new ValidateException("DNS RRSet data must not be empty");
        }
        for (final DnsRecordData value : data) {
            if (value == null) {
                throw new ValidateException("DNS RRSet data must not contain null");
            }
            if (value.typeCode() != typeCode) {
                throw new ValidateException("DNS RRSet data type must match the RRSet type");
            }
        }
        return List.copyOf(data);
    }

}
