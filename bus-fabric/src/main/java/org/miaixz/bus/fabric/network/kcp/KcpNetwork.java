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

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Payload;
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
    private static final int DEFAULT_WINDOW = 32;

    /**
     * Default retransmission delay.
     */
    private static final Duration DEFAULT_RETRANSMIT_DELAY = Duration.ofMillis(200);

    /**
     * Half of the unsigned 32-bit sequence space.
     */
    private static final long HALF_SEQUENCE_SPACE = 2_147_483_648L;

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
        if (udp == null) {
            throw new ValidateException("UDP network must not be null");
        }
        if (clock == null) {
            throw new ValidateException("KCP clock must not be null");
        }
        if (sendWindowSize <= 0 || sendWindowSize > 65_535) {
            throw new ValidateException("KCP send window must be between 1 and 65535");
        }
        if (receiveWindowSize <= 0 || receiveWindowSize > 65_535) {
            throw new ValidateException("KCP receive window must be between 1 and 65535");
        }
        if (retransmitDelay == null || retransmitDelay.isNegative()) {
            throw new ValidateException("KCP retransmit delay must be non-null and non-negative");
        }
        this.udp = udp;
        this.sequence = new AtomicLong();
        this.clock = clock;
        this.sendWindowSize = sendWindowSize;
        this.receiveWindowSize = receiveWindowSize;
        this.retransmitDelay = retransmitDelay;
        this.sendWindow = new LinkedHashMap<>();
        this.receiveWindow = new TreeMap<>();
        this.closed = new AtomicBoolean();
        this.lastRttMillis = -1L;
        this.smoothedRttMillis = -1L;
        this.congestionWindow = sendWindowSize;
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
        if (payload == null) {
            throw new ValidateException("KCP payload must not be null");
        }
        ensureOpen();
        if (sendWindow.size() >= currentSendLimit()) {
            throw new StatefulException("KCP send window is full");
        }
        final long current = sequence.getAndUpdate(value -> (value + 1L) & 0xffff_ffffL);
        final KcpPacket packet = KcpPacket
                .data(current, payload.bytes(KcpPacket.maxPayloadBytes()), remainingReceiveWindow(), now());
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
        if (packet == null) {
            throw new ValidateException("KCP packet must not be null");
        }
        ensureOpen();
        if (packet.type() == KcpPacket.Type.ACK) {
            acknowledge(packet);
            return Payload.empty();
        }
        final byte[] bytes = packet.payload();
        return bytes.length == 0 ? Payload.empty() : Payload.of(bytes);
    }

    /**
     * Processes one inbound packet and returns ACK plus ordered payloads ready for the application.
     *
     * @param packet packet
     * @return inbound processing result
     */
    public synchronized Inbound receive(final KcpPacket packet) {
        if (packet == null) {
            throw new ValidateException("KCP packet must not be null");
        }
        ensureOpen();
        if (packet.type() == KcpPacket.Type.ACK) {
            acknowledge(packet);
            return new Inbound(null, List.of());
        }
        final KcpPacket ack = KcpPacket.ack(packet.sequence(), remainingReceiveWindow(), now());
        final ArrayList<byte[]> delivered = new ArrayList<>();
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
            delivered.add(ready.payload());
            deliveredPackets++;
            expectedReceiveSequence = (expectedReceiveSequence + 1L) & 0xffff_ffffL;
        }
        return new Inbound(ack, delivered);
    }

    /**
     * Removes acknowledged packets from the send window.
     *
     * @param packet ACK packet
     */
    public synchronized void acknowledge(final KcpPacket packet) {
        if (packet == null) {
            throw new ValidateException("KCP ACK packet must not be null");
        }
        ensureOpen();
        if (packet.type() != KcpPacket.Type.ACK) {
            throw new ValidateException("KCP acknowledgement requires ACK packet");
        }
        final SentPacket sent = sendWindow.remove(packet.acknowledgement());
        if (sent != null) {
            acknowledgedPackets++;
            updateRtt(sent.sentAt);
            congestionWindow = Math.min(sendWindowSize, Math.max(1, congestionWindow + 1));
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
            congestionWindow = Math.max(1, congestionWindow / 2);
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
        if (packet == null) {
            throw new ValidateException("KCP packet must not be null");
        }
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
        if (payload == null) {
            throw new ValidateException("KCP payload must not be null");
        }
        return KcpPacket.fromDatagram(payload.bytes(materializeMaxBytes));
    }

    /**
     * Opens the underlying UDP session.
     *
     * @param address remote address
     * @return UDP session
     */
    public UdpSession open(final Address address) {
        if (address == null) {
            throw new ValidateException("KCP address must not be null");
        }
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
        if ("udp".equals(address.scheme())) {
            return address;
        }
        return Address.parse("udp://" + address.host() + Symbol.COLON + address.port());
    }

    /**
     * Calculates how many sequence slots can still be accepted into the inbound reorder buffer.
     *
     * @return available receive window slots
     */
    private int remainingReceiveWindow() {
        return Math.max(0, receiveWindowSize - receiveWindow.size());
    }

    /**
     * Calculates the current outbound limit from the configured send window and congestion window.
     *
     * @return maximum unacknowledged packets allowed now
     */
    private int currentSendLimit() {
        return Math.min(sendWindowSize, Math.max(1, congestionWindow));
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
        final long sample = Math.max(0L, now() - sentAt);
        lastRttMillis = sample;
        smoothedRttMillis = smoothedRttMillis < 0L ? sample : ((smoothedRttMillis * 7L) + sample) / 8L;
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
        return (to - from) & 0xffff_ffffL;
    }

    /**
     * Inbound KCP processing result.
     *
     * @param ack      ACK to send, or null
     * @param payloads ordered payloads ready for application delivery
     */
    public record Inbound(KcpPacket ack, List<byte[]> payloads) {

        /**
         * Creates an inbound result.
         */
        public Inbound {
            payloads = List.copyOf(payloads);
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
