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
package org.miaixz.bus.fabric.network.dns.resolve;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.dnssec.DnsAuthoritativeSigner;
import org.miaixz.bus.fabric.network.dns.message.DnsName;
import org.miaixz.bus.fabric.network.dns.message.DnsQuestion;
import org.miaixz.bus.fabric.network.dns.message.DnsResponseCode;
import org.miaixz.bus.fabric.network.dns.record.DnsRecord;
import org.miaixz.bus.fabric.network.dns.record.DnsRecordType;
import org.miaixz.bus.fabric.network.dns.zone.DnsZone;
import org.miaixz.bus.fabric.network.dns.zone.DnsZoneMode;

/**
 * Resolver for authoritative and override zones in a compiled runtime index.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DnsAuthoritativeResolver {

    /**
     * Maximum CNAME or synthesized DNAME chain depth.
     */
    private static final int MAX_ALIAS_DEPTH = 16;

    /**
     * TTL used by the minimal HINFO response for unrestricted ANY queries.
     */
    private static final long MINIMAL_ANY_TTL = 60L;

    /**
     * DNSSEC authoritative response signer.
     */
    private static final DnsAuthoritativeSigner SIGNER = new DnsAuthoritativeSigner();

    /**
     * Runtime lookup index.
     */
    private final RuntimeIndex index;

    /**
     * Creates a resolver.
     *
     * @param index runtime index
     */
    public DnsAuthoritativeResolver(final RuntimeIndex index) {
        if (index == null) {
            throw new ValidateException("DNS runtime index must not be null");
        }
        this.index = index;
    }

    /**
     * Resolves one DNS question.
     *
     * @param question decoded DNS question
     * @return resolution result
     */
    public DnsResolution resolve(final DnsQuestion question) {
        return resolve(question, null);
    }

    /**
     * Resolves one DNS question for a client address.
     *
     * @param question      decoded DNS question
     * @param clientAddress client address, or {@code null} when unavailable
     * @return resolution result
     */
    public DnsResolution resolve(final DnsQuestion question, final InetAddress clientAddress) {
        return resolve(question, clientAddress, false);
    }

    /**
     * Resolves one DNS question for a client address.
     *
     * @param question      decoded DNS question
     * @param clientAddress client address, or {@code null} when unavailable
     * @param dnssecOk      true when EDNS DNSSEC OK was requested
     * @return resolution result
     */
    public DnsResolution resolve(final DnsQuestion question, final InetAddress clientAddress, final boolean dnssecOk) {
        if (question == null) {
            throw new ValidateException("DNS question must not be null");
        }
        if (!question.internetClass()) {
            return DnsResolution.empty(DnsResponseCode.REFUSED, false, List.of());
        }
        final DnsZone zone = index.findZone(question.name(), clientAddress);
        if (zone == null) {
            return DnsResolution.empty(DnsResponseCode.REFUSED, false, List.of());
        }
        if (zone.mode() == DnsZoneMode.BLOCK) {
            return DnsResolution.empty(DnsResponseCode.NXDOMAIN, true, zone.soaRecords());
        }
        if (zone.mode() != DnsZoneMode.AUTHORITATIVE && zone.mode() != DnsZoneMode.OVERRIDE) {
            return DnsResolution.empty(DnsResponseCode.NOTIMP, true, zone.soaRecords());
        }
        if (question.typeCode() == DnsRecordType.ANY.code()) {
            return minimalAny(question);
        }
        try {
            return resolveName(
                    zone,
                    question.name(),
                    question.typeCode(),
                    question.recordClass(),
                    new HashSet<>(),
                    0,
                    dnssecOk);
        } catch (final RuntimeException e) {
            return DnsResolution.empty(DnsResponseCode.SERVFAIL, true, zone.soaRecords());
        }
    }

    /**
     * Resolves one owner name inside a zone.
     *
     * @param zone        matched zone
     * @param name        owner name to resolve
     * @param typeCode    query type code
     * @param recordClass query class code
     * @param visited     canonical names already visited in the alias chain
     * @param depth       current alias-chain depth
     * @param dnssecOk    true when EDNS DNSSEC OK was requested
     * @return resolution result
     */
    private static DnsResolution resolveName(
            final DnsZone zone,
            final String name,
            final int typeCode,
            final int recordClass,
            final Set<String> visited,
            final int depth,
            final boolean dnssecOk) {
        final String normalized = DnsName.normalize(name);
        if (depth > MAX_ALIAS_DEPTH || !visited.add(normalized)) {
            return DnsResolution.empty(DnsResponseCode.SERVFAIL, true, zone.soaRecords());
        }
        final List<DnsRecord> direct = zone.records(normalized, typeCode, recordClass);
        if (!direct.isEmpty()) {
            return DnsResolution.answer(signedAnswers(zone, normalized, direct, recordClass, dnssecOk, false));
        }
        final List<DnsRecord> cname = cnameRecords(zone, normalized, recordClass);
        if (!cname.isEmpty() && typeCode != DnsRecordType.CNAME.code()) {
            return followAlias(zone, cname, typeCode, recordClass, visited, depth, dnssecOk);
        }
        final DnsRecord dname = zone.coveringDname(normalized, recordClass);
        if (dname != null) {
            return followDname(zone, dname, normalized, typeCode, recordClass, visited, depth, dnssecOk);
        }
        final List<DnsRecord> wildcard = zone.wildcardRecords(normalized, typeCode, recordClass);
        if (!wildcard.isEmpty()) {
            return DnsResolution.answer(signedAnswers(zone, normalized, wildcard, recordClass, dnssecOk, true));
        }
        if (zone.hasName(normalized)) {
            return DnsResolution.empty(
                    DnsResponseCode.NOERROR,
                    true,
                    signedAuthorities(zone, zone.soaRecords(), recordClass, dnssecOk));
        }
        return DnsResolution.empty(
                DnsResponseCode.NXDOMAIN,
                true,
                signedAuthorities(zone, zone.soaRecords(), recordClass, dnssecOk));
    }

    /**
     * Returns exact or wildcard CNAME records for an owner name.
     *
     * @param zone        matched zone
     * @param name        owner name
     * @param recordClass query class code
     * @return exact or wildcard CNAME records
     */
    private static List<DnsRecord> cnameRecords(final DnsZone zone, final String name, final int recordClass) {
        final List<DnsRecord> exact = zone.cnameRecords(name, recordClass);
        if (!exact.isEmpty()) {
            return exact;
        }
        return zone.wildcardRecords(name, DnsRecordType.CNAME.code(), recordClass);
    }

    /**
     * Follows one CNAME alias and appends the target result.
     *
     * @param zone        matched zone
     * @param cname       CNAME records
     * @param typeCode    original query type code
     * @param recordClass query class code
     * @param visited     canonical names already visited in the alias chain
     * @param depth       current alias-chain depth
     * @param dnssecOk    true when EDNS DNSSEC OK was requested
     * @return resolution result containing CNAME and target answers
     */
    private static DnsResolution followAlias(
            final DnsZone zone,
            final List<DnsRecord> cname,
            final int typeCode,
            final int recordClass,
            final Set<String> visited,
            final int depth,
            final boolean dnssecOk) {
        final DnsRecord alias = cname.getFirst();
        final boolean dynamic = !zone.hasName(alias.name());
        final ArrayList<DnsRecord> answers = new ArrayList<>(
                signedAnswers(zone, alias.name(), cname, recordClass, dnssecOk, dynamic));
        final DnsResolution target = resolveName(
                zone,
                alias.targetName(),
                typeCode,
                recordClass,
                visited,
                depth + 1,
                dnssecOk);
        answers.addAll(target.answers());
        return new DnsResolution(target.responseCode(), true, answers, target.authorities());
    }

    /**
     * Follows one DNAME alias by synthesizing a CNAME for the query owner name.
     *
     * @param zone        matched zone
     * @param dname       matched DNAME record
     * @param queryName   original query owner name
     * @param typeCode    original query type code
     * @param recordClass query class code
     * @param visited     canonical names already visited in the alias chain
     * @param depth       current alias-chain depth
     * @param dnssecOk    true when EDNS DNSSEC OK was requested
     * @return resolution result containing DNAME, synthesized CNAME, and target answers
     */
    private static DnsResolution followDname(
            final DnsZone zone,
            final DnsRecord dname,
            final String queryName,
            final int typeCode,
            final int recordClass,
            final Set<String> visited,
            final int depth,
            final boolean dnssecOk) {
        final String target = synthesizeDnameTarget(dname, queryName);
        final DnsRecord cname = DnsRecord.cname(queryName, target, dname.ttl());
        final ArrayList<DnsRecord> answers = new ArrayList<>();
        answers.addAll(signedAnswers(zone, dname.name(), List.of(dname), recordClass, dnssecOk, false));
        answers.addAll(signedAnswers(zone, queryName, List.of(cname), recordClass, dnssecOk, true));
        final DnsResolution resolved = resolveName(zone, target, typeCode, recordClass, visited, depth + 1, dnssecOk);
        answers.addAll(resolved.answers());
        return new DnsResolution(resolved.responseCode(), true, answers, resolved.authorities());
    }

    /**
     * Appends same-owner RRSIG records when DNSSEC data was requested.
     *
     * @param zone        matched zone
     * @param owner       owner name whose signatures are attached
     * @param answers     answer RRSet
     * @param recordClass query class code
     * @param dnssecOk    true when EDNS DNSSEC OK was requested
     * @param dynamic     true when the RRSet was synthesized by the resolver
     * @return answer RRSet with matching RRSIG records appended
     */
    private static List<DnsRecord> signedAnswers(
            final DnsZone zone,
            final String owner,
            final List<DnsRecord> answers,
            final int recordClass,
            final boolean dnssecOk,
            final boolean dynamic) {
        return SIGNER.sign(zone, owner, answers, recordClass, dnssecOk, dynamic);
    }

    /**
     * Appends same-owner RRSIG records to authority records when DNSSEC data was requested.
     *
     * @param zone        matched zone
     * @param authorities authority records
     * @param recordClass query class code
     * @param dnssecOk    true when EDNS DNSSEC OK was requested
     * @return authority records with matching RRSIG records appended
     */
    private static List<DnsRecord> signedAuthorities(
            final DnsZone zone,
            final List<DnsRecord> authorities,
            final int recordClass,
            final boolean dnssecOk) {
        if (authorities.isEmpty()) {
            return authorities;
        }
        return SIGNER.sign(zone, authorities.getFirst().name(), authorities, recordClass, dnssecOk, false);
    }

    /**
     * Synthesizes the CNAME target produced by a DNAME record.
     *
     * @param dname     matched DNAME record
     * @param queryName original query owner name
     * @return synthesized target name
     */
    private static String synthesizeDnameTarget(final DnsRecord dname, final String queryName) {
        final String owner = dname.name();
        final String prefix = queryName.substring(0, queryName.length() - owner.length());
        return DnsName.normalize(prefix + dname.targetName());
    }

    /**
     * Creates the minimal standards-compatible answer used for unrestricted ANY queries.
     *
     * @param question original question
     * @return minimal HINFO response
     */
    private static DnsResolution minimalAny(final DnsQuestion question) {
        return DnsResolution.answer(List.of(DnsRecord.hinfo(question.name(), "RFC8482", "", MINIMAL_ANY_TTL)));
    }

}
