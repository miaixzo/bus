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

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Options;
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
     * Default send and receive window.
     */
    private static final int DEFAULT_WINDOW = Normal._32;

    /**
     * Default retransmission delay.
     */
    private static final Duration DEFAULT_RETRANSMIT_DELAY = Duration.ofMillis(Normal._200);

    /**
     * Unsigned 32-bit sequence mask used by KCP wire fields.
     */
    private static final long SEQUENCE_MASK = 0xffff_ffffL;

    /**
     * Half of the unsigned 32-bit sequence space.
     */
    private static final long HALF_SEQUENCE_SPACE = (SEQUENCE_MASK + Normal._1) / Normal._2;

    /**
     * Wrapped UDP network.
     */
    private final UdpNetwork udp;

    /**
     * Packet sequence.
     */
    private final AtomicLong sequence;

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
     * Unacknowledged outbound packets.
     */
    private final LinkedHashMap<Long, SentPacket> sendWindow;

    /**
     * Buffered inbound packets.
     */
    private final TreeMap<Long, KcpPacket> receiveWindow;

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
     * Creates a KCP network.
     *
     * @param udp UDP network
     */
    private KcpNetwork(final UdpNetwork udp) {
        this(udp, Clock.systemUTC(), DEFAULT_WINDOW, DEFAULT_WINDOW, DEFAULT_RETRANSMIT_DELAY);
    }

    /**
     * Creates a KCP network.
     *
     * @param udp               UDP network
     * @param clock             clock
     * @param sendWindowSize    send window size
     * @param receiveWindowSize receive window size
     * @param retransmitDelay   retransmission delay
     */
    private KcpNetwork(final UdpNetwork udp, final Clock clock, final int sendWindowSize, final int receiveWindowSize,
            final Duration retransmitDelay) {
        this.udp = Assert.notNull(udp, () -> new ValidateException("UDP network must not be null"));
        this.sequence = new AtomicLong();
        this.clock = Assert.notNull(clock, () -> new ValidateException("KCP clock must not be null"));
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
        return new KcpNetwork(udp, clock, sendWindowSize, receiveWindowSize, retransmitDelay);
    }

    /**
     * Encodes a payload into a packet.
     *
     * @param payload payload
     * @return packet
     */
    public synchronized KcpPacket encode(final Payload payload) {
        Assert.notNull(payload, () -> new ValidateException("KCP payload must not be null"));
        ensureOpen();
        if (sendWindow.size() >= currentSendLimit()) {
            throw new StatefulException("KCP send window is full");
        }
        final long current = sequence.getAndUpdate(value -> (value + Normal._1) & SEQUENCE_MASK);
        final ByteString bytes = ByteString.of(payload.bytes(KcpPacket.maxPayloadBytes()));
        final KcpPacket packet = KcpPacket.data(current, bytes, remainingReceiveWindow(), now());
        sendWindow.put(current, new SentPacket(packet, packet.timestamp()));
        return packet;
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
        if (packet.type() == KcpPacket.Type.ACK) {
            acknowledge(packet);
            return new Inbound(null, List.of());
        }
        final KcpPacket ack = KcpPacket.ack(packet.sequence(), remainingReceiveWindow(), now());
        final ArrayList<ByteString> delivered = new ArrayList<>();
        if (isBeforeExpected(packet.sequence())) {
            duplicatePackets++;
            return new Inbound(ack, delivered);
        }
        if (sequenceDistance(expectedReceiveSequence, packet.sequence()) >= receiveWindowSize) {
            droppedPackets++;
            return new Inbound(null, delivered);
        }
        if (receiveWindow.putIfAbsent(packet.sequence(), packet) != null) {
            duplicatePackets++;
            return new Inbound(ack, delivered);
        }
        receivedPackets++;
        while (true) {
            final KcpPacket ready = receiveWindow.remove(expectedReceiveSequence);
            if (ready == null) {
                break;
            }
            delivered.add(ready.payloadBytes());
            deliveredPackets++;
            expectedReceiveSequence = (expectedReceiveSequence + Normal._1) & SEQUENCE_MASK;
        }
        return new Inbound(ack, delivered);
    }

    /**
     * Removes acknowledged packets from the send window.
     *
     * @param packet ACK packet
     */
    public synchronized void acknowledge(final KcpPacket packet) {
        Assert.notNull(packet, () -> new ValidateException("KCP ACK packet must not be null"));
        ensureOpen();
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
        final long current = now();
        final ArrayList<KcpPacket> due = new ArrayList<>();
        for (final var entry : sendWindow.entrySet()) {
            final SentPacket sent = entry.getValue();
            if (current - sent.sentAt >= retransmitDelay.toMillis()) {
                due.add(sent.packet);
                entry.setValue(new SentPacket(sent.packet, current));
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
        return receiveWindow.size();
    }

    /**
     * Returns a KCP state snapshot for pressure tests and runtime telemetry.
     *
     * @return state snapshot
     */
    public synchronized Stats stats() {
        return new Stats(sendWindow.size(), receiveWindow.size(), acknowledgedPackets, retransmissions,
                duplicatePackets, droppedPackets, receivedPackets, deliveredPackets, lastRttMillis, smoothedRttMillis,
                congestionWindow);
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
        return unpack(payload, Options.DEFAULT_MATERIALIZE_MAX_BYTES);
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
        return sequenceDistance(expectedReceiveSequence, current) >= HALF_SEQUENCE_SPACE;
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
        return (to - from) & SEQUENCE_MASK;
    }

    /**
     * Inbound KCP processing result.
     *
     * @param ack      ACK to send, or null
     * @param payloads ordered payloads ready for application delivery
     */
    public record Inbound(KcpPacket ack, List<ByteString> payloads) {

        /**
         * Creates an inbound result.
         */
        public Inbound {
            payloads = List.copyOf(payloads);
        }

        /**
         * Returns payload snapshots as byte arrays for compatibility callers.
         *
         * @return payload snapshots
         */
        public List<byte[]> payloadArrays() {
            final ArrayList<byte[]> values = new ArrayList<>(payloads.size());
            for (final ByteString payload : payloads) {
                values.add(payload.toByteArray());
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
     * @param packet packet snapshot
     * @param sentAt send time in milliseconds
     */
    private record SentPacket(KcpPacket packet, long sentAt) {

    }

}
