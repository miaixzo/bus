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
package org.miaixz.bus.fabric.network.dns.zone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.dnssec.DnsSigningKey;
import org.miaixz.bus.fabric.network.dns.forward.DnsUpstream;
import org.miaixz.bus.fabric.network.dns.message.DnsCodec;
import org.miaixz.bus.fabric.network.dns.message.DnsName;
import org.miaixz.bus.fabric.network.dns.record.DnsRecord;
import org.miaixz.bus.fabric.network.dns.record.DnsRecordType;

/**
 * Immutable DNS zone containing records for one origin.
 *
 * @author Kimi Liu
 */
public class DnsZone {

    /**
     * Canonical zone origin.
     */
    private final String origin;

    /**
     * Zone resolution mode.
     */
    private final DnsZoneMode mode;

    /**
     * Immutable zone records.
     */
    private final List<DnsRecord> records;

    /**
     * Zone-specific upstreams for forward and stub zones.
     */
    private final List<DnsUpstream> upstreams;

    /**
     * Zone-specific DNSSEC signing keys supplied by external control data.
     */
    private final List<DnsSigningKey> signingKeys;

    /**
     * Immutable records grouped by owner name.
     */
    private final Map<String, List<DnsRecord>> recordsByName;

    /**
     * Creates a zone.
     *
     * @param origin  canonical zone origin
     * @param mode    resolution mode
     * @param records resource records
     */
    public DnsZone(final String origin, final DnsZoneMode mode, final List<DnsRecord> records) {
        this(origin, mode, records, List.of());
    }

    /**
     * Creates a zone.
     *
     * @param origin    canonical zone origin
     * @param mode      resolution mode
     * @param records   resource records
     * @param upstreams zone-specific upstream DNS servers
     */
    public DnsZone(final String origin, final DnsZoneMode mode, final List<DnsRecord> records,
            final List<DnsUpstream> upstreams) {
        this(origin, mode, records, upstreams, List.of());
    }

    /**
     * Creates a zone.
     *
     * @param origin      canonical zone origin
     * @param mode        resolution mode
     * @param records     resource records
     * @param upstreams   zone-specific upstream DNS servers
     * @param signingKeys zone-specific DNSSEC signing keys
     */
    public DnsZone(final String origin, final DnsZoneMode mode, final List<DnsRecord> records,
            final List<DnsUpstream> upstreams, final List<DnsSigningKey> signingKeys) {
        this.origin = DnsName.normalize(origin);
        if (mode == null) {
            throw new ValidateException("DNS zone mode must not be null");
        }
        this.mode = mode;
        this.records = immutableRecords(records);
        this.upstreams = immutableUpstreams(upstreams);
        this.signingKeys = immutableSigningKeys(signingKeys);
        this.recordsByName = indexRecords(this.records);
        validateZoneSemantics();
    }

    /**
     * Creates an authoritative zone.
     *
     * @param origin  canonical zone origin
     * @param records resource records
     * @return authoritative zone
     */
    public static DnsZone authoritative(final String origin, final List<DnsRecord> records) {
        return new DnsZone(origin, DnsZoneMode.AUTHORITATIVE, records);
    }

    /**
     * Creates an authoritative zone with external DNSSEC signing keys.
     *
     * @param origin      canonical zone origin
     * @param records     resource records
     * @param signingKeys DNSSEC signing keys
     * @return authoritative zone
     */
    public static DnsZone authoritative(
            final String origin,
            final List<DnsRecord> records,
            final List<DnsSigningKey> signingKeys) {
        return new DnsZone(origin, DnsZoneMode.AUTHORITATIVE, records, List.of(), signingKeys);
    }

    /**
     * Creates an override zone.
     *
     * @param origin  canonical zone origin
     * @param records resource records
     * @return override zone
     */
    public static DnsZone override(final String origin, final List<DnsRecord> records) {
        return new DnsZone(origin, DnsZoneMode.OVERRIDE, records);
    }

    /**
     * Creates a forward-only zone.
     *
     * @param origin    canonical zone origin
     * @param upstreams zone-specific upstream DNS servers
     * @return forward zone
     */
    public static DnsZone forward(final String origin, final List<DnsUpstream> upstreams) {
        return new DnsZone(origin, DnsZoneMode.FORWARD, List.of(), upstreams);
    }

    /**
     * Creates a stub zone.
     *
     * @param origin    canonical zone origin
     * @param upstreams zone-specific upstream DNS servers
     * @return stub zone
     */
    public static DnsZone stub(final String origin, final List<DnsUpstream> upstreams) {
        return new DnsZone(origin, DnsZoneMode.STUB, List.of(), upstreams);
    }

    /**
     * Creates a blocking zone.
     *
     * @param origin canonical zone origin
     * @return blocking zone
     */
    public static DnsZone block(final String origin) {
        return new DnsZone(origin, DnsZoneMode.BLOCK, List.of());
    }

    /**
     * Returns the zone origin.
     *
     * @return canonical zone origin
     */
    public String origin() {
        return origin;
    }

    /**
     * Returns the zone mode.
     *
     * @return resolution mode
     */
    public DnsZoneMode mode() {
        return mode;
    }

    /**
     * Returns all zone records.
     *
     * @return immutable resource records
     */
    public List<DnsRecord> records() {
        return records;
    }

    /**
     * Returns zone-specific upstream DNS servers.
     *
     * @return immutable upstream DNS servers
     */
    public List<DnsUpstream> upstreams() {
        return upstreams;
    }

    /**
     * Returns zone-specific DNSSEC signing keys.
     *
     * @return immutable signing keys
     */
    public List<DnsSigningKey> signingKeys() {
        return signingKeys;
    }

    /**
     * Returns records for a name, type, and class.
     *
     * @param name        owner name
     * @param typeCode    query type code
     * @param recordClass query class code
     * @return matching records
     */
    public List<DnsRecord> records(final String name, final int typeCode, final int recordClass) {
        final List<DnsRecord> named = recordsByName.get(DnsName.normalize(name));
        if (named == null) {
            return List.of();
        }
        final ArrayList<DnsRecord> matches = new ArrayList<>();
        for (final DnsRecord record : named) {
            if (record.matches(typeCode, recordClass)) {
                matches.add(record);
            }
        }
        return List.copyOf(matches);
    }

    /**
     * Returns CNAME records for a name.
     *
     * @param name        owner name
     * @param recordClass query class code
     * @return matching CNAME records
     */
    public List<DnsRecord> cnameRecords(final String name, final int recordClass) {
        return records(name, DnsRecordType.CNAME.code(), recordClass);
    }

    /**
     * Returns DNAME records for a name.
     *
     * @param name        owner name
     * @param recordClass query class code
     * @return matching DNAME records
     */
    public List<DnsRecord> dnameRecords(final String name, final int recordClass) {
        return records(name, DnsRecordType.DNAME.code(), recordClass);
    }

    /**
     * Returns wildcard records expanded to the query owner name.
     *
     * @param name        query owner name
     * @param typeCode    query type code
     * @param recordClass query class code
     * @return matching wildcard records with owner rewritten to the query name
     */
    public List<DnsRecord> wildcardRecords(final String name, final int typeCode, final int recordClass) {
        final String normalized = DnsName.normalize(name);
        if (hasName(normalized) || DnsName.ROOT.equals(normalized)) {
            return List.of();
        }
        final String[] labels = DnsName.labels(normalized);
        for (int index = 1; index < labels.length; index++) {
            final String candidate = DnsName.wildcardFromLabels(labels, index, labels.length);
            if (!contains(candidate)) {
                continue;
            }
            final List<DnsRecord> records = records(candidate, typeCode, recordClass);
            if (!records.isEmpty()) {
                return rewriteOwner(records, normalized);
            }
        }
        return List.of();
    }

    /**
     * Finds the nearest DNAME record covering a descendant query name.
     *
     * @param name        query owner name
     * @param recordClass query class code
     * @return nearest DNAME record, or {@code null}
     */
    public DnsRecord coveringDname(final String name, final int recordClass) {
        final String normalized = DnsName.normalize(name);
        final String[] labels = DnsName.labels(normalized);
        for (int index = 1; index < labels.length; index++) {
            final String owner = DnsName.fromLabels(labels, index, labels.length);
            if (!contains(owner) || owner.equals(normalized)) {
                continue;
            }
            final List<DnsRecord> records = dnameRecords(owner, recordClass);
            if (!records.isEmpty()) {
                return records.getFirst();
            }
        }
        return null;
    }

    /**
     * Returns SOA records for this zone origin.
     *
     * @return SOA records at the zone origin
     */
    public List<DnsRecord> soaRecords() {
        return records(origin, DnsRecordType.SOA.code(), DnsRecord.CLASS_IN);
    }

    /**
     * Returns whether the zone contains any record at a name.
     *
     * @param name owner name
     * @return true when at least one record exists for the name
     */
    public boolean hasName(final String name) {
        return recordsByName.containsKey(DnsName.normalize(name));
    }

    /**
     * Returns whether a name belongs to this zone.
     *
     * @param name candidate owner name
     * @return true when the name lies at or below the origin
     */
    public boolean contains(final String name) {
        return DnsName.inZone(name, origin);
    }

    /**
     * Validates and copies resource records.
     *
     * @param records source records
     * @return immutable records
     */
    private List<DnsRecord> immutableRecords(final List<DnsRecord> records) {
        if (records == null) {
            throw new ValidateException("DNS zone records must not be null");
        }
        for (final DnsRecord record : records) {
            if (record == null) {
                throw new ValidateException("DNS zone records must not contain null");
            }
            if (!contains(record.name())) {
                throw new ValidateException("DNS record is outside zone origin");
            }
        }
        return List.copyOf(records);
    }

    /**
     * Validates and copies zone-specific upstreams.
     *
     * @param upstreams source upstreams
     * @return immutable upstreams
     */
    private static List<DnsUpstream> immutableUpstreams(final List<DnsUpstream> upstreams) {
        if (upstreams == null) {
            throw new ValidateException("DNS zone upstreams must not be null");
        }
        for (final DnsUpstream upstream : upstreams) {
            if (upstream == null) {
                throw new ValidateException("DNS zone upstreams must not contain null");
            }
        }
        return List.copyOf(upstreams);
    }

    /**
     * Validates and copies zone-specific signing keys.
     *
     * @param signingKeys source signing keys
     * @return immutable signing keys
     */
    private List<DnsSigningKey> immutableSigningKeys(final List<DnsSigningKey> signingKeys) {
        if (signingKeys == null) {
            throw new ValidateException("DNS zone signing keys must not be null");
        }
        for (final DnsSigningKey signingKey : signingKeys) {
            if (signingKey == null) {
                throw new ValidateException("DNS zone signing keys must not contain null");
            }
            if (!contains(signingKey.keyName())) {
                throw new ValidateException("DNS zone signing key is outside zone origin");
            }
        }
        return List.copyOf(signingKeys);
    }

    /**
     * Builds the owner-name index.
     *
     * @param records records to index
     * @return immutable owner-name index
     */
    private static Map<String, List<DnsRecord>> indexRecords(final List<DnsRecord> records) {
        final HashMap<String, List<DnsRecord>> mutable = new HashMap<>();
        for (final DnsRecord record : records) {
            mutable.computeIfAbsent(record.name(), ignored -> new ArrayList<>()).add(record);
        }
        final HashMap<String, List<DnsRecord>> immutable = new HashMap<>();
        for (final Map.Entry<String, List<DnsRecord>> entry : mutable.entrySet()) {
            immutable.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Map.copyOf(immutable);
    }

    /**
     * Rewrites record owner names for wildcard expansion.
     *
     * @param records source records
     * @param owner   replacement owner name
     * @return records with rewritten owner names
     */
    private static List<DnsRecord> rewriteOwner(final List<DnsRecord> records, final String owner) {
        final ArrayList<DnsRecord> rewritten = new ArrayList<>(records.size());
        for (final DnsRecord record : records) {
            rewritten.add(record.withName(owner));
        }
        return List.copyOf(rewritten);
    }

    /**
     * Validates mode-specific zone invariants.
     */
    private void validateZoneSemantics() {
        if (mode == DnsZoneMode.AUTHORITATIVE) {
            requireAuthoritativeBase();
        }
        if ((mode == DnsZoneMode.FORWARD || mode == DnsZoneMode.STUB) && upstreams.isEmpty()) {
            throw new ValidateException("DNS forward and stub zones must contain at least one upstream");
        }
        if (mode != DnsZoneMode.AUTHORITATIVE && !signingKeys.isEmpty()) {
            throw new ValidateException("DNS signing keys are allowed only on authoritative zones");
        }
        validateCnameExclusivity();
        validateDnameDescendants();
        validateDnssecCoverage();
    }

    /**
     * Requires SOA and NS records in an authoritative zone.
     */
    private void requireAuthoritativeBase() {
        if (records(origin, DnsRecordType.SOA.code(), DnsRecord.CLASS_IN).isEmpty()) {
            throw new ValidateException("Authoritative DNS zone must contain an SOA record");
        }
        if (records(origin, DnsRecordType.NS.code(), DnsRecord.CLASS_IN).isEmpty()) {
            throw new ValidateException("Authoritative DNS zone must contain an NS record");
        }
    }

    /**
     * Rejects owner names where CNAME coexists with other record types.
     */
    private void validateCnameExclusivity() {
        for (final List<DnsRecord> named : recordsByName.values()) {
            boolean cname = false;
            for (final DnsRecord record : named) {
                if (record.typeCode() == DnsRecordType.CNAME.code()) {
                    cname = true;
                    break;
                }
            }
            if (cname && containsNonCnameCompanion(named)) {
                throw new ValidateException("DNS CNAME records must not coexist with other owner data");
            }
        }
    }

    /**
     * Returns whether an owner that contains CNAME has disallowed companion records.
     *
     * @param named records sharing one owner name
     * @return true when a non-CNAME companion is not DNSSEC metadata allowed at the same owner
     */
    private static boolean containsNonCnameCompanion(final List<DnsRecord> named) {
        for (final DnsRecord record : named) {
            if (record.typeCode() != DnsRecordType.CNAME.code() && !sameOwnerDnssecMetadata(record)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether a record type may coexist with CNAME at the same owner.
     *
     * @param record record being inspected
     * @return true when the record is DNSSEC metadata tied to the same owner name
     */
    private static boolean sameOwnerDnssecMetadata(final DnsRecord record) {
        final int typeCode = record.typeCode();
        return typeCode == DnsRecordType.RRSIG.code() || typeCode == DnsRecordType.NSEC.code()
                || typeCode == DnsRecordType.NSEC3.code();
    }

    /**
     * Rejects records located below a DNAME owner name.
     */
    private void validateDnameDescendants() {
        for (final DnsRecord dname : records) {
            if (dname.typeCode() == DnsRecordType.DNAME.code()) {
                validateDnameDescendants(dname.name());
            }
        }
    }

    /**
     * Rejects records located below one DNAME owner name.
     *
     * @param dnameOwner DNAME owner name
     */
    private void validateDnameDescendants(final String dnameOwner) {
        for (final String owner : recordsByName.keySet()) {
            if (DnsName.descendantOf(owner, dnameOwner)) {
                throw new ValidateException("DNS DNAME owner must not have descendant records");
            }
        }
    }

    /**
     * Requires RRSIG coverage for each RRSet when the zone publishes DNSKEY records.
     */
    private void validateDnssecCoverage() {
        if (!containsType(DnsRecordType.DNSKEY) || !signingKeys.isEmpty()) {
            return;
        }
        for (final Map.Entry<String, List<DnsRecord>> entry : recordsByName.entrySet()) {
            validateDnssecCoverage(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Requires RRSIG coverage for each non-RRSIG RRSet at one owner name.
     *
     * @param owner owner name being validated
     * @param named records sharing the owner name
     */
    private static void validateDnssecCoverage(final String owner, final List<DnsRecord> named) {
        for (final DnsRecord record : named) {
            if (record.typeCode() != DnsRecordType.RRSIG.code()
                    && !hasCoveringRrsig(named, record.typeCode(), record.recordClass())) {
                throw new ValidateException("DNSSEC signed zone RRSet lacks RRSIG coverage: " + owner);
            }
        }
    }

    /**
     * Returns whether one owner name has an RRSIG covering a type and class.
     *
     * @param named       records sharing one owner name
     * @param typeCode    covered type code
     * @param recordClass covered record class
     * @return true when a matching RRSIG exists
     */
    private static boolean hasCoveringRrsig(final List<DnsRecord> named, final int typeCode, final int recordClass) {
        for (final DnsRecord record : named) {
            if (record.typeCode() == DnsRecordType.RRSIG.code() && record.recordClass() == recordClass
                    && rrsigTypeCovered(record) == typeCode) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reads the covered type from RRSIG RDATA.
     *
     * @param record RRSIG record
     * @return covered DNS type code
     */
    private static int rrsigTypeCovered(final DnsRecord record) {
        final byte[] data = record.wireData();
        if (data.length < Short.BYTES) {
            throw new ValidateException("DNSSEC RRSIG record is truncated");
        }
        return DnsCodec.readUnsignedShort(data, 0);
    }

    /**
     * Returns whether the zone contains at least one record of a type.
     *
     * @param type record type
     * @return true when the type is present
     */
    private boolean containsType(final DnsRecordType type) {
        for (final DnsRecord record : records) {
            if (record.typeCode() == type.code()) {
                return true;
            }
        }
        return false;
    }

}
