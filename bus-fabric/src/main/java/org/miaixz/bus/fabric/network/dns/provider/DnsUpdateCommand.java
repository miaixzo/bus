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
package org.miaixz.bus.fabric.network.dns.provider;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.message.DnsName;
import org.miaixz.bus.fabric.network.dns.message.DnsQuestion;
import org.miaixz.bus.fabric.network.dns.record.DnsRecord;
import org.miaixz.bus.fabric.network.dns.record.DnsRecordType;

/**
 * Immutable Dynamic Update command passed to an external control project.
 *
 * <p>
 * The command preserves the original wire message and also exposes the RFC 2136 zone, prerequisite, update, and
 * additional sections as immutable structured data.
 * </p>
 *
 * @author Kimi Liu
 */
public class DnsUpdateCommand {

    /**
     * DNS class NONE used by RFC 2136 deletion and absence prerequisites.
     */
    public static final int CLASS_NONE = 254;

    /**
     * DNS class ANY used by RFC 2136 existence prerequisites and delete-all operations.
     */
    public static final int CLASS_ANY = 255;

    /**
     * Zone section question.
     */
    private final DnsQuestion zone;

    /**
     * Zone name from the update message zone section.
     */
    private final String zoneName;

    /**
     * Client address that sent the update, or {@code null} when unavailable.
     */
    private final InetAddress clientAddress;

    /**
     * Original DNS update wire message.
     */
    private final byte[] message;

    /**
     * Immutable prerequisite section entries.
     */
    private final List<Prerequisite> prerequisites;

    /**
     * Immutable update section entries.
     */
    private final List<Update> updates;

    /**
     * Immutable non-EDNS and non-TSIG additional section records.
     */
    private final List<DnsRecord> additionals;

    /**
     * Creates a backward-compatible Dynamic Update command containing only the zone and original wire message.
     *
     * @param zoneName      zone name from the update message zone section
     * @param clientAddress client address that sent the update, or {@code null} when unavailable
     * @param message       original DNS update wire message
     */
    public DnsUpdateCommand(final String zoneName, final InetAddress clientAddress, final byte[] message) {
        this(new DnsQuestion(zoneName, DnsRecordType.SOA.code(), DnsRecord.CLASS_IN), clientAddress, message, List.of(),
                List.of(), List.of());
    }

    /**
     * Creates a structured Dynamic Update command.
     *
     * @param zone          zone section question
     * @param clientAddress client address that sent the update, or {@code null} when unavailable
     * @param message       original DNS update wire message
     * @param prerequisites prerequisite section entries
     * @param updates       update section entries
     * @param additionals   non-EDNS and non-TSIG additional section records
     */
    public DnsUpdateCommand(final DnsQuestion zone, final InetAddress clientAddress, final byte[] message,
            final List<Prerequisite> prerequisites, final List<Update> updates, final List<DnsRecord> additionals) {
        if (zone == null) {
            throw new ValidateException("DNS update zone must not be null");
        }
        if (zone.typeCode() != DnsRecordType.SOA.code()) {
            throw new ValidateException("DNS update zone type must be SOA");
        }
        this.zone = zone;
        this.zoneName = DnsName.normalize(zone.name());
        this.clientAddress = clientAddress;
        this.message = copyMessage(message);
        this.prerequisites = immutablePrerequisites(prerequisites);
        this.updates = immutableUpdates(updates);
        this.additionals = immutableRecords(additionals, "DNS update additional records");
    }

    /**
     * Classifies a prerequisite section record according to RFC 2136.
     *
     * @param record    prerequisite section record
     * @param zoneClass zone class from the zone section
     * @return structured prerequisite entry
     */
    public static Prerequisite prerequisite(final DnsRecord record, final int zoneClass) {
        if (record == null) {
            throw new ProtocolException("DNS update prerequisite record must not be null");
        }
        requireZeroTtl(record, "DNS update prerequisite");
        final boolean empty = record.wireData().length == 0;
        if (record.recordClass() == CLASS_ANY && record.typeCode() == DnsRecordType.ANY.code() && empty) {
            return new Prerequisite(PrerequisiteKind.NAME_IN_USE, record);
        }
        if (record.recordClass() == CLASS_NONE && record.typeCode() == DnsRecordType.ANY.code() && empty) {
            return new Prerequisite(PrerequisiteKind.NAME_NOT_IN_USE, record);
        }
        if (record.recordClass() == CLASS_ANY && empty) {
            return new Prerequisite(PrerequisiteKind.RRSET_EXISTS, record);
        }
        if (record.recordClass() == CLASS_NONE && empty) {
            return new Prerequisite(PrerequisiteKind.RRSET_NOT_EXISTS, record);
        }
        if (record.recordClass() == zoneClass) {
            return new Prerequisite(PrerequisiteKind.RRSET_EXISTS, record);
        }
        throw new ProtocolException("DNS update prerequisite class is invalid");
    }

    /**
     * Classifies an update section record according to RFC 2136.
     *
     * @param record    update section record
     * @param zoneClass zone class from the zone section
     * @return structured update entry
     */
    public static Update update(final DnsRecord record, final int zoneClass) {
        if (record == null) {
            throw new ProtocolException("DNS update record must not be null");
        }
        final boolean empty = record.wireData().length == 0;
        if (record.recordClass() == zoneClass && record.typeCode() != DnsRecordType.ANY.code()) {
            return new Update(UpdateKind.ADD_RR, record);
        }
        requireZeroTtl(record, "DNS update deletion");
        if (record.recordClass() == CLASS_ANY && record.typeCode() == DnsRecordType.ANY.code() && empty) {
            return new Update(UpdateKind.DELETE_ALL_RRSETS, record);
        }
        if (record.recordClass() == CLASS_ANY && record.typeCode() != DnsRecordType.ANY.code() && empty) {
            return new Update(UpdateKind.DELETE_RRSET, record);
        }
        if (record.recordClass() == CLASS_NONE && record.typeCode() != DnsRecordType.ANY.code() && !empty) {
            return new Update(UpdateKind.DELETE_EXACT_RR, record);
        }
        throw new ProtocolException("DNS update record class or RDATA is invalid");
    }

    /**
     * Returns a copy of this command with a different client address.
     *
     * @param clientAddress replacement client address, or {@code null} when unavailable
     * @return command copy using the supplied client address
     */
    public DnsUpdateCommand withClientAddress(final InetAddress clientAddress) {
        return new DnsUpdateCommand(zone, clientAddress, message, prerequisites, updates, additionals);
    }

    /**
     * Returns the zone section question.
     *
     * @return zone section question
     */
    public DnsQuestion zone() {
        return zone;
    }

    /**
     * Returns the update zone name.
     *
     * @return canonical zone name
     */
    public String zoneName() {
        return zoneName;
    }

    /**
     * Returns the update client address.
     *
     * @return client address, or {@code null} when unavailable
     */
    public InetAddress clientAddress() {
        return clientAddress;
    }

    /**
     * Returns the original DNS update wire message.
     *
     * @return defensive copy of the wire message
     */
    public byte[] message() {
        return Arrays.copyOf(message, message.length);
    }

    /**
     * Returns prerequisite section entries.
     *
     * @return immutable prerequisite entries
     */
    public List<Prerequisite> prerequisites() {
        return prerequisites;
    }

    /**
     * Returns update section entries.
     *
     * @return immutable update entries
     */
    public List<Update> updates() {
        return updates;
    }

    /**
     * Returns non-EDNS and non-TSIG additional section records.
     *
     * @return immutable additional records
     */
    public List<DnsRecord> additionals() {
        return additionals;
    }

    /**
     * Validates and copies a DNS update wire message.
     *
     * @param message source wire message
     * @return copied wire message
     */
    private static byte[] copyMessage(final byte[] message) {
        if (message == null || message.length == 0) {
            throw new ValidateException("DNS update message must not be empty");
        }
        return Arrays.copyOf(message, message.length);
    }

    /**
     * Validates and copies prerequisite entries.
     *
     * @param prerequisites source prerequisite entries
     * @return immutable prerequisite entries
     */
    private static List<Prerequisite> immutablePrerequisites(final List<Prerequisite> prerequisites) {
        if (prerequisites == null) {
            throw new ValidateException("DNS update prerequisites must not be null");
        }
        final ArrayList<Prerequisite> result = new ArrayList<>(prerequisites.size());
        for (final Prerequisite prerequisite : prerequisites) {
            if (prerequisite == null) {
                throw new ValidateException("DNS update prerequisites must not contain null");
            }
            result.add(prerequisite);
        }
        return List.copyOf(result);
    }

    /**
     * Validates and copies update entries.
     *
     * @param updates source update entries
     * @return immutable update entries
     */
    private static List<Update> immutableUpdates(final List<Update> updates) {
        if (updates == null) {
            throw new ValidateException("DNS updates must not be null");
        }
        final ArrayList<Update> result = new ArrayList<>(updates.size());
        for (final Update update : updates) {
            if (update == null) {
                throw new ValidateException("DNS updates must not contain null");
            }
            result.add(update);
        }
        return List.copyOf(result);
    }

    /**
     * Validates and copies resource records.
     *
     * @param records source records
     * @param name    diagnostic collection name
     * @return immutable records
     */
    private static List<DnsRecord> immutableRecords(final List<DnsRecord> records, final String name) {
        if (records == null) {
            throw new ValidateException(name + " must not be null");
        }
        final ArrayList<DnsRecord> result = new ArrayList<>(records.size());
        for (final DnsRecord record : records) {
            if (record == null) {
                throw new ValidateException(name + " must not contain null");
            }
            result.add(record);
        }
        return List.copyOf(result);
    }

    /**
     * Requires a record TTL to be zero.
     *
     * @param record record to validate
     * @param name   diagnostic record name
     */
    private static void requireZeroTtl(final DnsRecord record, final String name) {
        if (record.ttl() != 0L) {
            throw new ProtocolException(name + " TTL must be zero");
        }
    }

    /**
     * RFC 2136 prerequisite categories.
     *
     * @author Kimi Liu
     */
    public enum PrerequisiteKind {

        /**
         * RRSet exists prerequisite.
         */
        RRSET_EXISTS,

        /**
         * RRSet does not exist prerequisite.
         */
        RRSET_NOT_EXISTS,

        /**
         * Name is in use prerequisite.
         */
        NAME_IN_USE,

        /**
         * Name is not in use prerequisite.
         */
        NAME_NOT_IN_USE

    }

    /**
     * RFC 2136 update operation categories.
     *
     * @author Kimi Liu
     */
    public enum UpdateKind {

        /**
         * Add one resource record.
         */
        ADD_RR,

        /**
         * Delete one RRSet at an owner name.
         */
        DELETE_RRSET,

        /**
         * Delete all RRSets at an owner name.
         */
        DELETE_ALL_RRSETS,

        /**
         * Delete one exact resource record.
         */
        DELETE_EXACT_RR

    }

    /**
     * Structured RFC 2136 prerequisite entry.
     *
     * @author Kimi Liu
     */
    public static class Prerequisite {

        /**
         * Prerequisite category.
         */
        private final PrerequisiteKind kind;

        /**
         * Source prerequisite record.
         */
        private final DnsRecord record;

        /**
         * Creates a prerequisite entry.
         *
         * @param kind   prerequisite category
         * @param record source prerequisite record
         */
        public Prerequisite(final PrerequisiteKind kind, final DnsRecord record) {
            if (kind == null) {
                throw new ValidateException("DNS update prerequisite kind must not be null");
            }
            if (record == null) {
                throw new ValidateException("DNS update prerequisite record must not be null");
            }
            this.kind = kind;
            this.record = record;
        }

        /**
         * Returns the prerequisite category.
         *
         * @return prerequisite category
         */
        public PrerequisiteKind kind() {
            return kind;
        }

        /**
         * Returns the prerequisite owner name.
         *
         * @return canonical owner name
         */
        public String name() {
            return record.name();
        }

        /**
         * Returns the prerequisite type code.
         *
         * @return unsigned 16-bit type code
         */
        public int typeCode() {
            return record.typeCode();
        }

        /**
         * Returns the prerequisite class code.
         *
         * @return unsigned 16-bit class code
         */
        public int recordClass() {
            return record.recordClass();
        }

        /**
         * Returns the prerequisite RDATA bytes.
         *
         * @return defensive copy of prerequisite RDATA
         */
        public byte[] wireData() {
            return record.wireData();
        }

        /**
         * Returns the source prerequisite record.
         *
         * @return immutable prerequisite record
         */
        public DnsRecord record() {
            return record;
        }

    }

    /**
     * Structured RFC 2136 update section entry.
     *
     * @author Kimi Liu
     */
    public static class Update {

        /**
         * Update operation category.
         */
        private final UpdateKind kind;

        /**
         * Source update record.
         */
        private final DnsRecord record;

        /**
         * Creates an update entry.
         *
         * @param kind   update operation category
         * @param record source update record
         */
        public Update(final UpdateKind kind, final DnsRecord record) {
            if (kind == null) {
                throw new ValidateException("DNS update kind must not be null");
            }
            if (record == null) {
                throw new ValidateException("DNS update record must not be null");
            }
            this.kind = kind;
            this.record = record;
        }

        /**
         * Returns the update operation category.
         *
         * @return update operation category
         */
        public UpdateKind kind() {
            return kind;
        }

        /**
         * Returns the update owner name.
         *
         * @return canonical owner name
         */
        public String name() {
            return record.name();
        }

        /**
         * Returns the update type code.
         *
         * @return unsigned 16-bit type code
         */
        public int typeCode() {
            return record.typeCode();
        }

        /**
         * Returns the update class code.
         *
         * @return unsigned 16-bit class code
         */
        public int recordClass() {
            return record.recordClass();
        }

        /**
         * Returns the update TTL.
         *
         * @return unsigned 32-bit TTL
         */
        public long ttl() {
            return record.ttl();
        }

        /**
         * Returns the update RDATA bytes.
         *
         * @return defensive copy of update RDATA
         */
        public byte[] wireData() {
            return record.wireData();
        }

        /**
         * Returns the source update record.
         *
         * @return immutable update record
         */
        public DnsRecord record() {
            return record;
        }

    }

}
