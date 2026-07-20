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
package org.miaixz.bus.fabric.network.kcp;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.network.Transport;
import org.miaixz.bus.fabric.network.udp.UdpNetwork;
import org.miaixz.bus.fabric.network.udp.UdpSession;

/**
 * Lightweight KCP packet endpoint over UDP sessions.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class KcpNetwork implements AutoCloseable {

    /**
     * Wrapped UDP network.
     */
    private final UdpNetwork udp;

    /**
     * Packet sequence.
     */
    private final AtomicLong sequence;

    /**
     * Logical message identifier.
     */
    private final AtomicLong messageId;

    /**
     * Clock.
     */
    private final Clock clock;

    /**
     * Send window size.
     */
    private final int sendWindowSize;

    /**
     * Receive window size.
     */
    private final int receiveWindowSize;

    /**
     * Retransmission delay.
     */
    private final Duration retransmitDelay;

    /**
     * Wire version emitted by this endpoint.
     */
    private final int wireVersion;

    /**
     * Unacknowledged outbound packets.
     */
    private final LinkedHashMap<Long, SentPacket> sendWindow;

    /**
     * Buffered inbound packets.
     */
    private final TreeMap<Long, KcpPacket> receiveWindow;

    /**
     * Complete logical messages waiting for send-window capacity.
     */
    private final ArrayDeque<OutboundMessage> outboundQueue;

    /**
     * Active V2 fragment reassemblies.
     */
    private final HashMap<ReassemblyKey, ReassemblyState> reassemblies;

    /**
     * Close flag.
     */
    private final AtomicBoolean closed;

    /**
     * Next inbound sequence to deliver.
     */
    private long expectedReceiveSequence;

    /**
     * Retransmission count.
     */
    private long retransmissions;

    /**
     * Duplicate inbound packet count.
     */
    private long duplicatePackets;

    /**
     * Dropped inbound packet count.
     */
    private long droppedPackets;

    /**
     * Accepted inbound packet count.
     */
    private long receivedPackets;

    /**
     * Delivered inbound packet count.
     */
    private long deliveredPackets;

    /**
     * Acknowledged outbound packet count.
     */
    private long acknowledgedPackets;

    /**
     * Last RTT sample in milliseconds.
     */
    private long lastRttMillis;

    /**
     * Smoothed RTT in milliseconds.
     */
    private long smoothedRttMillis;

    /**
     * Lightweight congestion window.
     */
    private int congestionWindow;

    /**
     * Logical bytes retained by the outbound queue.
     */
    private long outboundQueueBytes;

    /**
     * Fragment bytes retained by active reassemblies.
     */
    private long reassemblyBytes;

    /**
     * Creates a KCP network.
     *
     * @param udp UDP network
     */
    private KcpNetwork(final UdpNetwork udp) {
        this(udp, Clock.system(), Normal._1, Normal._32, Normal._32, Builder.KCP_NETWORK_DEFAULT_RETRANSMIT_DELAY);
    }

    /**
     * Creates a KCP network.
     *
     * @param udp               UDP network
     * @param clock             clock
     * @param wireVersion       wire version
     * @param sendWindowSize    send window size
     * @param receiveWindowSize receive window size
     * @param retransmitDelay   retransmission delay
     */
    private KcpNetwork(final UdpNetwork udp, final Clock clock, final int wireVersion, final int sendWindowSize,
            final int receiveWindowSize, final Duration retransmitDelay) {
        this.udp = Assert.notNull(udp, () -> new ValidateException("UDP network must not be null"));
        this.sequence = new AtomicLong();
        this.messageId = new AtomicLong();
        this.clock = Assert.notNull(clock, () -> new ValidateException("KCP clock must not be null"));
        if (wireVersion != Normal._1 && wireVersion != Normal._2) {
            throw new ValidateException("KCP wire version must be 1 or 2");
        }
        this.wireVersion = wireVersion;
        this.sendWindowSize = Assert.checkBetween(
                sendWindowSize,
                Normal._1,
                Normal._65535,
                () -> new ValidateException("KCP send window must be between 1 and 65535"));
        this.receiveWindowSize = Assert.checkBetween(
                receiveWindowSize,
                Normal._1,
                Normal._65535,
                () -> new ValidateException("KCP receive window must be between 1 and 65535"));
        this.retransmitDelay = Assert
                .notNull(retransmitDelay, () -> new ValidateException("KCP retransmit delay must not be null"));
        Assert.isTrue(
                !this.retransmitDelay.isNegative(),
                () -> new ValidateException("KCP retransmit delay must be non-negative"));
        this.sendWindow = new LinkedHashMap<>();
        this.receiveWindow = new TreeMap<>();
        this.outboundQueue = new ArrayDeque<>();
        this.reassemblies = new HashMap<>();
        this.closed = new AtomicBoolean();
        this.lastRttMillis = Normal.__1;
        this.smoothedRttMillis = Normal.__1;
        this.congestionWindow = this.sendWindowSize;
    }

    /**
     * Creates a KCP network.
     *
     * @param udp UDP network
     * @return KCP network
     */
    public static KcpNetwork create(final UdpNetwork udp) {
        return new KcpNetwork(udp);
    }

    /**
     * Creates a KCP network with explicit tuning for tests and advanced users.
     *
     * @param udp               UDP network
     * @param clock             clock
     * @param sendWindowSize    send window size
     * @param receiveWindowSize receive window size
     * @param retransmitDelay   retransmission delay
     * @return KCP network
     */
    public static KcpNetwork create(
            final UdpNetwork udp,
            final Clock clock,
            final int sendWindowSize,
            final int receiveWindowSize,
            final Duration retransmitDelay) {
        return new KcpNetwork(udp, clock, Normal._1, sendWindowSize, receiveWindowSize, retransmitDelay);
    }

    /**
     * Creates a KCP network with an explicit wire version.
     *
     * @param udp         UDP network
     * @param clock       clock
     * @param wireVersion wire version, either 1 or 2
     * @return KCP network
     */
    public static KcpNetwork create(final UdpNetwork udp, final Clock clock, final int wireVersion) {
        return new KcpNetwork(udp, clock, wireVersion, Normal._32, Normal._32,
                Builder.KCP_NETWORK_DEFAULT_RETRANSMIT_DELAY);
    }

    /**
     * Enqueues a complete logical payload and emits packets currently allowed by the send window.
     *
     * @param payload payload
     * @return packets ready for UDP send
     */
    public synchronized List<KcpPacket> encode(final Payload payload) {
        Assert.notNull(payload, () -> new ValidateException("KCP payload must not be null"));
        ensureOpen();
        expireReassemblies();
        final long limit = wireVersion == Normal._1 ? Builder.KCP_PACKET_V1_MAX_PAYLOAD
                : Builder.KCP_NETWORK_MAX_MESSAGE_BYTES;
        final ByteString bytes = ByteString.of(payload.bytes(limit));
        if (wireVersion == Normal._1 && bytes.size() > Builder.KCP_PACKET_V1_MAX_PAYLOAD) {
            throw new ValidateException("KCP V1 payload exceeds one packet");
        }
        if (outboundQueueBytes + bytes.size() > Builder.KCP_NETWORK_MAX_OUTBOUND_QUEUE_BYTES) {
            throw new StatefulException("KCP outbound queue byte limit exceeded");
        }
        final long id = wireVersion == Normal._2 ? nextMessageId() : Normal._0;
        outboundQueue.addLast(new OutboundMessage(bytes, id));
        outboundQueueBytes += bytes.size();
        return drainOutbound();
    }

    /**
     * Decodes a packet payload.
     *
     * @param packet packet
     * @return payload
     */
    public synchronized Payload decode(final KcpPacket packet) {
        Assert.notNull(packet, () -> new ValidateException("KCP packet must not be null"));
        ensureOpen();
        if (packet.type() == KcpPacket.Type.ACK) {
            acknowledge(packet);
            return Payload.empty();
        }
        final ByteString bytes = packet.payloadBytes();
        return bytes.size() == Normal._0 ? Payload.empty() : Payload.of(bytes);
    }

    /**
     * Processes one inbound packet and returns ACK plus ordered payloads ready for the application.
     *
     * @param packet packet
     * @return inbound processing result
     */
    public synchronized Inbound receive(final KcpPacket packet) {
        Assert.notNull(packet, () -> new ValidateException("KCP packet must not be null"));
        ensureOpen();
        expireReassemblies();
        if (packet.type() == KcpPacket.Type.ACK) {
            acknowledgePacket(packet);
            return new Inbound(List.of(), drainOutbound());
        }
        final KcpPacket ack = acknowledgementFor(packet);
        final ArrayList<Payload> delivered = new ArrayList<>();
        final ArrayList<KcpPacket> outbound = new ArrayList<>();
        outbound.add(ack);
        if (isBeforeExpected(packet.sequence())) {
            duplicatePackets++;
            return new Inbound(delivered, outbound);
        }
        if (sequenceDistance(expectedReceiveSequence, packet.sequence()) >= receiveWindowSize) {
            droppedPackets++;
            return new Inbound(delivered, List.of());
        }
        if (receiveWindow.putIfAbsent(packet.sequence(), packet) != null) {
            duplicatePackets++;
            return new Inbound(delivered, outbound);
        }
        receivedPackets++;
        while (true) {
            final KcpPacket ready = receiveWindow.remove(expectedReceiveSequence);
            if (ready == null) {
                break;
            }
            deliver(ready, delivered);
            expectedReceiveSequence = (expectedReceiveSequence + Normal._1) & Builder.UNSIGNED_INT_MASK;
        }
        return new Inbound(delivered, outbound);
    }

    /**
     * Removes acknowledged packets from the send window.
     *
     * @param packet ACK packet
     */
    public synchronized void acknowledge(final KcpPacket packet) {
        Assert.notNull(packet, () -> new ValidateException("KCP ACK packet must not be null"));
        ensureOpen();
        acknowledgePacket(packet);
    }

    /**
     * Removes one acknowledged packet without draining queued messages.
     *
     * @param packet ACK packet
     */
    private void acknowledgePacket(final KcpPacket packet) {
        if (packet.type() != KcpPacket.Type.ACK) {
            throw new ValidateException("KCP acknowledgement requires ACK packet");
        }
        final SentPacket sent = sendWindow.remove(packet.acknowledgement());
        if (sent != null) {
            acknowledgedPackets++;
            updateRtt(sent.sentAt);
            congestionWindow = Math.min(sendWindowSize, Math.max(Normal._1, congestionWindow + Normal._1));
        }
    }

    /**
     * Returns due retransmission packets and advances their retry timestamp.
     *
     * @return packets due for retransmission
     */
    public synchronized List<KcpPacket> retransmitDue() {
        ensureOpen();
        expireReassemblies();
        final long current = now();
        final ArrayList<KcpPacket> due = new ArrayList<>();
        for (final var entry : sendWindow.entrySet()) {
            final SentPacket sent = entry.getValue();
            if (current - sent.sentAt >= retransmitDelay.toMillis()) {
                if (sent.retries >= Builder.KCP_NETWORK_MAX_RETRANSMISSIONS) {
                    throw new StatefulException("KCP packet retry limit exhausted");
                }
                due.add(sent.packet);
                entry.setValue(new SentPacket(sent.packet, current, sent.retries + Normal._1));
            }
        }
        if (!due.isEmpty()) {
            retransmissions += due.size();
            congestionWindow = Math.max(Normal._1, congestionWindow / Normal._2);
        }
        return due;
    }

    /**
     * Returns unacknowledged packet count.
     *
     * @return pending count
     */
    public synchronized int pending() {
        return sendWindow.size();
    }

    /**
     * Returns buffered inbound packet count.
     *
     * @return buffered count
     */
    public synchronized int buffered() {
        int fragments = receiveWindow.size();
        for (final ReassemblyState state : reassemblies.values()) {
            fragments += state.fragments.size();
        }
        return fragments;
    }

    /**
     * Returns a KCP state snapshot for pressure tests and runtime telemetry.
     *
     * @return state snapshot
     */
    public synchronized Stats stats() {
        return new Stats(sendWindow.size(), buffered(), acknowledgedPackets, retransmissions, duplicatePackets,
                droppedPackets, receivedPackets, deliveredPackets, lastRttMillis, smoothedRttMillis, congestionWindow);
    }

    /**
     * Encodes a packet to datagram bytes.
     *
     * @param packet packet
     * @return datagram bytes
     */
    public byte[] pack(final KcpPacket packet) {
        Assert.notNull(packet, () -> new ValidateException("KCP packet must not be null"));
        return packet.datagram();
    }

    /**
     * Decodes a packet from datagram payload.
     *
     * @param payload payload
     * @return packet
     */
    public KcpPacket unpack(final Payload payload) {
        return unpack(payload, Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Decodes a packet from datagram payload.
     *
     * @param payload             payload
     * @param materializeMaxBytes materialization limit
     * @return packet
     */
    public KcpPacket unpack(final Payload payload, final long materializeMaxBytes) {
        Assert.notNull(payload, () -> new ValidateException("KCP payload must not be null"));
        final long maxBytes = Math.min(materializeMaxBytes, KcpPacket.maxDatagramBytes() + Normal._1);
        return KcpPacket.fromDatagram(payload.bytes(maxBytes));
    }

    /**
     * Opens the underlying UDP session.
     *
     * @param address remote address
     * @return UDP session
     */
    public UdpSession open(final Address address) {
        Assert.notNull(address, () -> new ValidateException("KCP address must not be null"));
        ensureOpen();
        return udp.connect(toUdp(address));
    }

    /**
     * Closes this KCP endpoint and releases retained packet windows.
     */
    @Override
    public synchronized void close() {
        if (closed.compareAndSet(false, true)) {
            sendWindow.clear();
            receiveWindow.clear();
            outboundQueue.clear();
            reassemblies.clear();
            outboundQueueBytes = Normal.LONG_ZERO;
            reassemblyBytes = Normal.LONG_ZERO;
            udp.close();
        }
    }

    /**
     * Returns whether this endpoint is closed.
     *
     * @return true when closed
     */
    public boolean closed() {
        return closed.get();
    }

    /**
     * Converts a KCP address to a UDP address for transport.
     *
     * @param address address
     * @return UDP address
     */
    private static Address toUdp(final Address address) {
        if (Transport.UDP.scheme().equals(address.scheme())) {
            return address;
        }
        return Address.parse(
                Transport.UDP.scheme() + Symbol.COLON + Symbol.SLASH + Symbol.SLASH + address.host() + Symbol.COLON
                        + address.port());
    }

    /**
     * Emits queued fragments until the current send and congestion windows are full.
     *
     * @return packets ready for UDP send
     */
    private List<KcpPacket> drainOutbound() {
        final ArrayList<KcpPacket> emitted = new ArrayList<>();
        while (sendWindow.size() < currentSendLimit() && !outboundQueue.isEmpty()) {
            final OutboundMessage message = outboundQueue.peekFirst();
            final int maxPayload = wireVersion == Normal._1 ? Builder.KCP_PACKET_V1_MAX_PAYLOAD
                    : Builder.KCP_PACKET_V2_MAX_PAYLOAD;
            final int start = message.nextFragment * maxPayload;
            final int end = Math.min(message.bytes.size(), start + maxPayload);
            final ByteString fragment = message.bytes.substring(start, end);
            final long currentSequence = nextSequence();
            final long currentTime = now();
            final KcpPacket packet = wireVersion == Normal._1
                    ? KcpPacket.data(currentSequence, fragment, remainingReceiveWindow(), currentTime)
                    : KcpPacket.dataV2(
                            currentSequence,
                            fragment,
                            remainingReceiveWindow(),
                            currentTime,
                            message.messageId,
                            message.nextFragment,
                            message.fragmentCount);
            sendWindow.put(currentSequence, new SentPacket(packet, currentTime, Normal._0));
            emitted.add(packet);
            message.nextFragment++;
            if (message.nextFragment == message.fragmentCount) {
                outboundQueue.removeFirst();
                outboundQueueBytes -= message.bytes.size();
            }
        }
        return List.copyOf(emitted);
    }

    /**
     * Delivers a V1 packet directly or adds a V2 fragment to bounded reassembly state.
     *
     * @param packet    ordered data packet
     * @param delivered completed logical payloads
     */
    private void deliver(final KcpPacket packet, final List<Payload> delivered) {
        if (packet.version() != Normal._2) {
            delivered.add(
                    packet.payloadBytes().size() == Normal._0 ? Payload.empty() : Payload.of(packet.payloadBytes()));
            deliveredPackets++;
            return;
        }
        final ReassemblyKey key = new ReassemblyKey(udp, packet.messageId());
        ReassemblyState state = reassemblies.get(key);
        final long added = packet.payloadBytes().size();
        if (state == null) {
            if (reassemblies.size() >= Builder.KCP_NETWORK_MAX_ACTIVE_REASSEMBLIES) {
                throw new StatefulException("KCP active reassembly limit exceeded");
            }
            if (added > Builder.KCP_NETWORK_MAX_MESSAGE_BYTES) {
                throw new StatefulException("KCP reassembled message byte limit exceeded");
            }
            if (reassemblyBytes + added > Builder.KCP_NETWORK_MAX_REASSEMBLY_BYTES) {
                throw new StatefulException("KCP total reassembly byte limit exceeded");
            }
            if (sourceReassemblyBytes(key.source) + added > Builder.KCP_NETWORK_MAX_SOURCE_REASSEMBLY_BYTES) {
                throw new StatefulException("KCP source reassembly byte limit exceeded");
            }
            state = new ReassemblyState(packet.fragmentCount(), now());
            reassemblies.put(key, state);
        } else if (state.fragmentCount != packet.fragmentCount()) {
            throw new StatefulException("KCP fragment count changed during reassembly");
        }
        if (state.fragments.containsKey(packet.fragmentIndex())) {
            return;
        }
        if (state.bytes + added > Builder.KCP_NETWORK_MAX_MESSAGE_BYTES) {
            throw new StatefulException("KCP reassembled message byte limit exceeded");
        }
        if (reassemblyBytes + added > Builder.KCP_NETWORK_MAX_REASSEMBLY_BYTES) {
            throw new StatefulException("KCP total reassembly byte limit exceeded");
        }
        if (sourceReassemblyBytes(key.source) + added > Builder.KCP_NETWORK_MAX_SOURCE_REASSEMBLY_BYTES) {
            throw new StatefulException("KCP source reassembly byte limit exceeded");
        }
        state.fragments.put(packet.fragmentIndex(), packet.payloadBytes());
        state.bytes += added;
        reassemblyBytes += added;
        if (state.fragments.size() != state.fragmentCount) {
            return;
        }
        final Buffer merged = new Buffer();
        for (int index = Normal._0; index < state.fragmentCount; index++) {
            final ByteString fragment = state.fragments.get(index);
            if (fragment == null) {
                return;
            }
            merged.write(fragment);
        }
        reassemblies.remove(key);
        reassemblyBytes -= state.bytes;
        delivered.add(merged.size() == Normal.LONG_ZERO ? Payload.empty() : Payload.of(merged.readByteString()));
        deliveredPackets++;
    }

    /**
     * Returns retained reassembly bytes for one source.
     *
     * @param source source identity
     * @return retained bytes
     */
    private long sourceReassemblyBytes(final Object source) {
        long bytes = Normal.LONG_ZERO;
        for (final Map.Entry<ReassemblyKey, ReassemblyState> entry : reassemblies.entrySet()) {
            if (entry.getKey().source == source) {
                bytes += entry.getValue().bytes;
            }
        }
        return bytes;
    }

    /**
     * Removes expired reassemblies and fails the owning session path.
     */
    private void expireReassemblies() {
        final long current = now();
        boolean expired = false;
        final var iterator = reassemblies.entrySet().iterator();
        while (iterator.hasNext()) {
            final ReassemblyState state = iterator.next().getValue();
            if (current - state.createdAt >= Builder.KCP_NETWORK_REASSEMBLY_TIMEOUT.toMillis()) {
                reassemblyBytes -= state.bytes;
                iterator.remove();
                expired = true;
            }
        }
        if (expired) {
            throw new StatefulException("KCP fragment reassembly expired");
        }
    }

    /**
     * Creates an ACK using the inbound packet version, with legacy data acknowledged as V1.
     *
     * @param packet inbound data packet
     * @return ACK packet
     */
    private KcpPacket acknowledgementFor(final KcpPacket packet) {
        if (packet.version() == Normal._2) {
            return new KcpPacket(Normal._2, KcpPacket.Type.ACK, Normal._0, packet.sequence(), remainingReceiveWindow(),
                    now(), Normal._0, Normal._0, Normal._0, ByteString.EMPTY);
        }
        return KcpPacket.ack(packet.sequence(), remainingReceiveWindow(), now());
    }

    /**
     * Returns and advances the unsigned packet sequence.
     *
     * @return current sequence
     */
    private long nextSequence() {
        return sequence.getAndUpdate(value -> (value + Normal._1) & Builder.UNSIGNED_INT_MASK);
    }

    /**
     * Returns and advances the unsigned logical message identifier.
     *
     * @return current message identifier
     */
    private long nextMessageId() {
        return messageId.getAndUpdate(value -> (value + Normal._1) & Builder.UNSIGNED_INT_MASK);
    }

    /**
     * Calculates how many sequence slots can still be accepted into the inbound reorder buffer.
     *
     * @return available receive window slots
     */
    private int remainingReceiveWindow() {
        return Math.max(Normal._0, receiveWindowSize - receiveWindow.size());
    }

    /**
     * Calculates the current outbound limit from the configured send window and congestion window.
     *
     * @return maximum unacknowledged packets allowed now
     */
    private int currentSendLimit() {
        return Math.min(sendWindowSize, Math.max(Normal._1, congestionWindow));
    }

    /**
     * Reads the clock used for retransmission and RTT accounting.
     *
     * @return current time in milliseconds
     */
    private long now() {
        return clock.millis();
    }

    /**
     * Returns whether a received sequence has already fallen behind the expected sequence space.
     *
     * @param current received sequence
     * @return {@code true} when the packet is older than the receive window can use
     */
    private boolean isBeforeExpected(final long current) {
        return sequenceDistance(expectedReceiveSequence, current) >= Builder.KCP_NETWORK_HALF_SEQUENCE_SPACE;
    }

    /**
     * Updates the last and smoothed RTT values after an acknowledgement reaches a sent packet.
     *
     * @param sentAt original send time in milliseconds
     */
    private void updateRtt(final long sentAt) {
        final long sample = Math.max(Normal._0, now() - sentAt);
        lastRttMillis = sample;
        smoothedRttMillis = smoothedRttMillis < Normal._0 ? sample
                : ((smoothedRttMillis * Normal._7) + sample) / Normal._8;
    }

    /**
     * Guards send and receive operations after the KCP endpoint has been closed.
     */
    private void ensureOpen() {
        if (closed.get()) {
            throw new StatefulException("KCP network is closed");
        }
    }

    /**
     * Computes unsigned 32-bit sequence distance for wrap-around comparisons.
     *
     * @param from base sequence
     * @param to   target sequence
     * @return distance in the KCP sequence space
     */
    private static long sequenceDistance(final long from, final long to) {
        return (to - from) & Builder.UNSIGNED_INT_MASK;
    }

    /**
     * Inbound KCP processing result.
     *
     * @param delivered complete logical payloads ready for application delivery
     * @param outbound  ACK and newly released data packets ready for UDP send
     */
    public record Inbound(List<Payload> delivered, List<KcpPacket> outbound) {

        /**
         * Creates an inbound result.
         */
        public Inbound {
            delivered = List.copyOf(delivered);
            outbound = List.copyOf(outbound);
        }

        /**
         * Returns payload snapshots as byte arrays for compatibility callers.
         *
         * @return payload snapshots
         */
        public List<byte[]> payloadArrays() {
            final ArrayList<byte[]> values = new ArrayList<>(delivered.size());
            for (final Payload payload : delivered) {
                values.add(payload.bytes(Builder.KCP_NETWORK_MAX_MESSAGE_BYTES));
            }
            return List.copyOf(values);
        }

    }

    /**
     * KCP telemetry snapshot.
     *
     * @param pending           unacknowledged outbound packet count
     * @param buffered          buffered inbound packet count
     * @param acknowledged      acknowledged outbound packet count
     * @param retransmissions   retransmission count
     * @param duplicates        duplicate inbound packet count
     * @param drops             dropped inbound packet count
     * @param received          accepted inbound packet count
     * @param delivered         delivered inbound packet count
     * @param lastRttMillis     last RTT sample in milliseconds, or -1
     * @param smoothedRttMillis smoothed RTT in milliseconds, or -1
     * @param congestionWindow  current congestion window
     */
    public record Stats(int pending, int buffered, long acknowledged, long retransmissions, long duplicates, long drops,
            long received, long delivered, long lastRttMillis, long smoothedRttMillis, int congestionWindow) {

    }

    /**
     * Outbound packet retained until an acknowledgement arrives or retransmission is needed.
     *
     * @param packet  packet snapshot
     * @param sentAt  send time in milliseconds
     * @param retries retransmission count
     */
    private record SentPacket(KcpPacket packet, long sentAt, int retries) {

    }

    /**
     * Complete logical message retained until every fragment enters the send window.
     */
    private final class OutboundMessage {

        /**
         * Complete message bytes.
         */
        private final ByteString bytes;

        /**
         * V2 logical message identifier.
         */
        private final long messageId;

        /**
         * Total fragment count.
         */
        private final int fragmentCount;

        /**
         * Next fragment index to emit.
         */
        private int nextFragment;

        /**
         * Creates a queued logical message.
         *
         * @param bytes     complete message bytes
         * @param messageId V2 message identifier
         */
        private OutboundMessage(final ByteString bytes, final long messageId) {
            this.bytes = bytes;
            this.messageId = messageId;
            final int maxPayload = wireVersion == Normal._1 ? Builder.KCP_PACKET_V1_MAX_PAYLOAD
                    : Builder.KCP_PACKET_V2_MAX_PAYLOAD;
            this.fragmentCount = Math.max(Normal._1, (bytes.size() + maxPayload - Normal._1) / maxPayload);
        }

    }

    /**
     * Reassembly identity scoped to one UDP source.
     *
     * @param source    source identity
     * @param messageId logical message identifier
     */
    private record ReassemblyKey(Object source, long messageId) {

    }

    /**
     * Mutable bounded state for one V2 logical message.
     */
    private static final class ReassemblyState {

        /**
         * Expected fragment count.
         */
        private final int fragmentCount;

        /**
         * Creation time in milliseconds.
         */
        private final long createdAt;

        /**
         * Fragments indexed by their wire position.
         */
        private final TreeMap<Integer, ByteString> fragments;

        /**
         * Retained payload bytes.
         */
        private long bytes;

        /**
         * Creates an empty reassembly.
         *
         * @param fragmentCount expected fragment count
         * @param createdAt     creation time
         */
        private ReassemblyState(final int fragmentCount, final long createdAt) {
            this.fragmentCount = fragmentCount;
            this.createdAt = createdAt;
            this.fragments = new TreeMap<>();
        }

    }

}
