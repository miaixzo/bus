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
package org.miaixz.bus.fabric.network.dns.recursive;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.dnssec.DnsDnssecValidator;
import org.miaixz.bus.fabric.network.dns.forward.DnsUpstream;
import org.miaixz.bus.fabric.network.dns.message.DnsCodec;
import org.miaixz.bus.fabric.network.dns.message.DnsDecodedResponse;
import org.miaixz.bus.fabric.network.dns.message.DnsName;
import org.miaixz.bus.fabric.network.dns.message.DnsQuery;
import org.miaixz.bus.fabric.network.dns.message.DnsQuestion;
import org.miaixz.bus.fabric.network.dns.message.DnsResponse;
import org.miaixz.bus.fabric.network.dns.message.DnsResponseCode;
import org.miaixz.bus.fabric.network.dns.record.DnsRecord;
import org.miaixz.bus.fabric.network.dns.record.DnsRecordType;
import org.miaixz.bus.fabric.network.dns.zone.DnsTrustAnchor;

/**
 * Recursive DNS resolver that follows referral responses with glue records.
 *
 * <p>
 * Instances are immutable and own no sockets or threads. Each call creates short-lived forwarding exchanges through
 * {@link DnsForwarder}. The resolver starts from caller-provided root hints or stub-zone upstreams, decodes each
 * response with the native DNS codec, returns final responses with {@code RA=true}, and follows NS referrals when
 * matching A or AAAA glue is present in the additional section.
 * </p>
 *
 * @author Kimi Liu
 */
public class DnsRecursiveResolver {

    /**
     * Maximum referral depth followed for one query.
     */
    private static final int MAX_REFERRAL_DEPTH = 16;

    /**
     * Standard DNS port used by referral glue.
     */
    private static final int DEFAULT_DNS_PORT = 53;

    /**
     * Standard query flag for internal NS-address lookups.
     */
    private static final int FLAG_RD = 0x0100;

    /**
     * Root hint or stub upstream list.
     */
    private final List<DnsUpstream> roots;

    /**
     * QNAME-minimization plan builder bound to the configured roots.
     */
    private final DnsRecursionPlanner recursionPlanner;

    /**
     * Timeout copied to referral glue upstreams.
     */
    private final Duration referralTimeout;

    /**
     * DNS port assigned to resolved referral name-server addresses.
     */
    private final int referralPort;

    /**
     * DNSSEC validator used for final decoded responses.
     */
    private final DnsDnssecValidator dnssecValidator;

    /**
     * Creates a recursive resolver.
     *
     * @param roots root hint or stub upstreams used as the first query candidates
     */
    public DnsRecursiveResolver(final List<DnsUpstream> roots) {
        this(roots, List.of(), DEFAULT_DNS_PORT);
    }

    /**
     * Creates a recursive resolver with DNSSEC trust anchors.
     *
     * @param roots        root hint or stub upstreams used as the first query candidates
     * @param trustAnchors DNSSEC trust anchors used by final-response validation
     */
    public DnsRecursiveResolver(final List<DnsUpstream> roots, final List<DnsTrustAnchor> trustAnchors) {
        this(roots, trustAnchors, DEFAULT_DNS_PORT);
    }

    /**
     * Creates a recursive resolver with an explicit referral port.
     *
     * <p>
     * The package-private constructor exists for black-box tests that run authoritative fixtures on high local ports.
     * Production callers use the public constructor and therefore keep standards-compliant referral port 53.
     * </p>
     *
     * @param roots        root hint or stub upstreams used as the first query candidates
     * @param referralPort DNS port assigned to referral name-server addresses
     */
    DnsRecursiveResolver(final List<DnsUpstream> roots, final int referralPort) {
        this(roots, List.of(), referralPort);
    }

    /**
     * Creates a recursive resolver with explicit trust anchors and referral port.
     *
     * <p>
     * The package-private constructor exists for black-box tests that run authoritative fixtures on high local ports.
     * Production callers use the public constructors and therefore keep standards-compliant referral port 53 unless a
     * test fixture overrides it.
     * </p>
     *
     * @param roots        root hint or stub upstreams used as the first query candidates
     * @param trustAnchors DNSSEC trust anchors used by final-response validation
     * @param referralPort DNS port assigned to referral name-server addresses
     */
    DnsRecursiveResolver(final List<DnsUpstream> roots, final List<DnsTrustAnchor> trustAnchors,
            final int referralPort) {
        if (roots == null || roots.isEmpty()) {
            throw new ValidateException("DNS recursive roots must not be empty");
        }
        for (final DnsUpstream root : roots) {
            if (root == null) {
                throw new ValidateException("DNS recursive roots must not contain null");
            }
        }
        this.roots = List.copyOf(roots);
        this.recursionPlanner = new DnsRecursionPlanner(this.roots);
        this.referralTimeout = roots.getFirst().timeout();
        this.referralPort = validatePort(referralPort);
        this.dnssecValidator = new DnsDnssecValidator(trustAnchors);
    }

    /**
     * Builds the immutable QNAME-minimization plan for one decoded client query.
     *
     * @param query decoded caller query
     * @return immutable recursion plan
     */
    public DnsRecursionPlanner.DnsRecursionPlan plan(final DnsQuery query) {
        return recursionPlanner.plan(query);
    }

    /**
     * Resolves one query by forwarding to roots and following referrals that contain usable glue.
     *
     * @param query   decoded caller query
     * @param request original query wire bytes
     * @return DNS response model with recursion available
     */
    public DnsResponse resolve(final DnsQuery query, final byte[] request) {
        return resolve(query, request, new HashSet<>(), DnsRetryBudget.recursive());
    }

    /**
     * Resolves one query while retaining the current NS-address lookup stack.
     *
     * @param query              decoded caller query
     * @param request            original query wire bytes
     * @param addressLookupStack name-server address lookups currently in progress
     * @param budget             retry budget shared by the recursive flow
     * @return DNS response model with recursion available
     */
    private DnsResponse resolve(
            final DnsQuery query,
            final byte[] request,
            final Set<String> addressLookupStack,
            final DnsRetryBudget budget) {
        if (query == null) {
            throw new ValidateException("DNS recursive query must not be null");
        }
        if (request == null || request.length == 0) {
            throw new ValidateException("DNS recursive request must not be empty");
        }
        if (budget == null) {
            throw new ValidateException("DNS recursive retry budget must not be null");
        }
        List<DnsUpstream> candidates = roots;
        DnsRetryBudget cursor = budget;
        RuntimeException failure = null;
        for (int depth = 0; depth < MAX_REFERRAL_DEPTH; depth++) {
            if (cursor.exhausted()) {
                return DnsResponse.empty(query, DnsResponseCode.SERVFAIL, false);
            }
            final DnsDecodedResponse decoded;
            try {
                final DnsNameServerRacer.DnsNameServerRace race = new DnsNameServerRacer(candidates)
                        .race(query, request, cursor);
                cursor = race.budget();
                decoded = DnsCodec.decodeResponse(race.response());
            } catch (final RuntimeException e) {
                failure = appendFailure(failure, e);
                break;
            }
            if (finalResponse(decoded)) {
                return dnssecValidator.validate(query, decoded);
            }
            final List<DnsUpstream> referrals = referralUpstreams(query, decoded, addressLookupStack);
            if (referrals.isEmpty()) {
                return dnssecValidator.validate(query, decoded);
            }
            candidates = referrals;
        }
        if (failure != null) {
            throw new SocketException("DNS recursive resolution failed", failure);
        }
        return DnsResponse.empty(query, DnsResponseCode.SERVFAIL, false);
    }

    /**
     * Returns whether a decoded response is final for this resolver.
     *
     * @param response decoded response
     * @return true when the response should be returned to the client
     */
    private static boolean finalResponse(final DnsDecodedResponse response) {
        if (response.responseCode() != DnsResponseCode.NOERROR || !response.answers().isEmpty()) {
            return true;
        }
        if (containsType(response.authorities(), DnsRecordType.SOA)) {
            return true;
        }
        return !containsType(response.authorities(), DnsRecordType.NS);
    }

    /**
     * Extracts referral upstreams from NS authority records and matching A or AAAA glue.
     *
     * @param query              original query used to derive internal lookup identifiers
     * @param response           decoded referral response
     * @param addressLookupStack name-server address lookups currently in progress
     * @return referral upstreams, or an empty list when no usable glue is present
     */
    private List<DnsUpstream> referralUpstreams(
            final DnsQuery query,
            final DnsDecodedResponse response,
            final Set<String> addressLookupStack) {
        final Set<String> nameservers = nameserverNames(response.authorities());
        if (nameservers.isEmpty()) {
            return List.of();
        }
        final ArrayList<DnsUpstream> upstreams = new ArrayList<>();
        for (final DnsRecord record : response.additionals()) {
            if (nameservers.contains(record.name()) && addressRecord(record)) {
                upstreams.add(DnsUpstream.udp(addressLiteral(record), referralPort, referralTimeout));
            }
        }
        if (!upstreams.isEmpty()) {
            return List.copyOf(upstreams);
        }
        return resolveNameserverAddresses(query, nameservers, addressLookupStack);
    }

    /**
     * Resolves name-server host names when a referral did not carry glue.
     *
     * @param query              original query used to derive internal lookup identifiers
     * @param nameservers        referred name-server host names
     * @param addressLookupStack name-server address lookups currently in progress
     * @return referral upstreams resolved from A and AAAA answers
     */
    private List<DnsUpstream> resolveNameserverAddresses(
            final DnsQuery query,
            final Set<String> nameservers,
            final Set<String> addressLookupStack) {
        final ArrayList<DnsUpstream> upstreams = new ArrayList<>();
        for (final String nameserver : nameservers) {
            appendResolvedAddressUpstreams(upstreams, query, nameserver, DnsRecordType.A.code(), addressLookupStack);
            appendResolvedAddressUpstreams(upstreams, query, nameserver, DnsRecordType.AAAA.code(), addressLookupStack);
        }
        return List.copyOf(upstreams);
    }

    /**
     * Appends upstreams resolved from one internal address lookup.
     *
     * @param upstreams          mutable target upstream list
     * @param query              original query used to derive the internal identifier
     * @param nameserver         referred name-server host name
     * @param typeCode           address query type code
     * @param addressLookupStack name-server address lookups currently in progress
     */
    private void appendResolvedAddressUpstreams(
            final ArrayList<DnsUpstream> upstreams,
            final DnsQuery query,
            final String nameserver,
            final int typeCode,
            final Set<String> addressLookupStack) {
        final String lookupKey = nameserver + Symbol.OR + typeCode;
        if (!addressLookupStack.add(lookupKey)) {
            return;
        }
        final DnsQuery addressQuery = new DnsQuery(internalQueryId(query, nameserver, typeCode), DnsQuery.OPCODE_QUERY,
                true, new DnsQuestion(nameserver, typeCode, DnsRecord.CLASS_IN), 0, false);
        try {
            final DnsResponse response = resolve(
                    addressQuery,
                    encodeInternalQuery(addressQuery),
                    addressLookupStack,
                    DnsRetryBudget.recursive());
            if (response.responseCode() != DnsResponseCode.NOERROR) {
                return;
            }
            for (final DnsRecord answer : response.answers()) {
                if (answer.typeCode() == typeCode) {
                    upstreams.add(DnsUpstream.udp(addressLiteral(answer), referralPort, referralTimeout));
                }
            }
        } catch (final RuntimeException ignored) {
            return;
        } finally {
            addressLookupStack.remove(lookupKey);
        }
    }

    /**
     * Builds a deterministic internal query identifier.
     *
     * @param query      original query
     * @param nameserver referred name-server host name
     * @param typeCode   address query type code
     * @return unsigned 16-bit query identifier
     */
    private static int internalQueryId(final DnsQuery query, final String nameserver, final int typeCode) {
        return (query.id() ^ nameserver.hashCode() ^ typeCode) & Normal._65535;
    }

    /**
     * Encodes an internal standard DNS query without EDNS options.
     *
     * @param query internal address query
     * @return DNS query wire bytes
     */
    private static byte[] encodeInternalQuery(final DnsQuery query) {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            output.writeShort(query.id());
            output.writeShort(query.recursionDesired() ? FLAG_RD : 0);
            output.writeShort(1);
            output.writeShort(0);
            output.writeShort(0);
            output.writeShort(0);
            DnsName.write(output, query.question().name());
            output.writeShort(query.question().typeCode());
            output.writeShort(query.question().recordClass());
        } catch (final IOException e) {
            throw new ProtocolException("Unable to encode internal DNS recursive query", e);
        }
        return bytes.toByteArray();
    }

    /**
     * Extracts NS target names from authority records.
     *
     * @param records authority records
     * @return target names
     */
    private static Set<String> nameserverNames(final List<DnsRecord> records) {
        final HashSet<String> names = new HashSet<>();
        for (final DnsRecord record : records) {
            if (record.typeCode() == DnsRecordType.NS.code()) {
                names.add(record.targetName());
            }
        }
        return names;
    }

    /**
     * Returns whether a record can be used as glue address data.
     *
     * @param record candidate record
     * @return true when the record is A or AAAA
     */
    private static boolean addressRecord(final DnsRecord record) {
        return record.typeCode() == DnsRecordType.A.code() || record.typeCode() == DnsRecordType.AAAA.code();
    }

    /**
     * Converts an address record into an IP literal.
     *
     * @param record A or AAAA record
     * @return IP address literal
     */
    private static String addressLiteral(final DnsRecord record) {
        try {
            return InetAddress.getByAddress(record.wireData()).getHostAddress();
        } catch (final java.net.UnknownHostException e) {
            throw new ValidateException("DNS referral glue address length is invalid", e);
        }
    }

    /**
     * Returns whether a record list contains a type.
     *
     * @param records records to scan
     * @param type    record type
     * @return true when the type is present
     */
    private static boolean containsType(final List<DnsRecord> records, final DnsRecordType type) {
        for (final DnsRecord record : records) {
            if (record.typeCode() == type.code()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Appends a suppressed failure while retaining the first failure.
     *
     * @param current current first failure, or {@code null}
     * @param next    next failure
     * @return first failure
     */
    private static RuntimeException appendFailure(final RuntimeException current, final RuntimeException next) {
        if (current == null) {
            return next;
        }
        current.addSuppressed(next);
        return current;
    }

    /**
     * Validates a DNS port number.
     *
     * @param port port number
     * @return validated port
     */
    private static int validatePort(final int port) {
        if (port < Normal._1 || port > Normal._65535) {
            throw new ValidateException("DNS recursive referral port is out of range");
        }
        return port;
    }

}
