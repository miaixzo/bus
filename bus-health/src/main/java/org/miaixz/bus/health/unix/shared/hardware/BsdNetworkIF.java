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
package org.miaixz.bus.health.unix.shared.hardware;

import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.NetworkIF;
import org.miaixz.bus.health.builtin.hardware.common.AbstractNetworkIF;

/**
 * BsdNetworkIF applicable to FreeBSD and OpenBSD.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class BsdNetworkIF extends AbstractNetworkIF {

    /** Pattern fragment matching an Ethernet or wireless media rate. */
    private static final String MEDIA_RATE = "(?i)(?<!\\d)(\\d++(?:\\.\\d++)?+)";

    /** Pattern fragment matching the unit used by a BSD media description. */
    private static final String MEDIA_UNIT = "([GM]?+)(?:base|bps)";

    /** Pattern matching the active media speed in an {@code ifconfig} media line. */
    private static final java.util.regex.Pattern MEDIA_SPEED = java.util.regex.Pattern.compile(MEDIA_RATE + MEDIA_UNIT);

    /**
     * The bytesRecv value.
     */
    private volatile long bytesRecv;

    /**
     * The bytesSent value.
     */
    private volatile long bytesSent;

    /**
     * The packetsRecv value.
     */
    private volatile long packetsRecv;

    /**
     * The packetsSent value.
     */
    private volatile long packetsSent;

    /**
     * The inErrors value.
     */
    private volatile long inErrors;

    /**
     * The outErrors value.
     */
    private volatile long outErrors;

    /**
     * The inDrops value.
     */
    private volatile long inDrops;

    /**
     * The collisions value.
     */
    private volatile long collisions;

    /**
     * The interface speed in bits per second.
     */
    private volatile long speed;

    /**
     * The timeStamp value.
     */
    private volatile long timeStamp;

    /**
     * Constructs a new {@code BsdNetworkIF} object.
     *
     * @param netint The {@link NetworkInterface} object.
     * @throws InstantiationException If the network interface cannot be properly initialized.
     */
    public BsdNetworkIF(NetworkInterface netint) throws InstantiationException {
        super(netint);
        updateAttributes();
    }

    /**
     * Gets all network interfaces on this machine.
     *
     * @param includeLocalInterfaces include local interfaces in the result
     * @return A list of {@link NetworkIF} objects representing the interfaces
     */
    public static List<NetworkIF> getNetworks(boolean includeLocalInterfaces) {
        return getNetworks(includeLocalInterfaces, BsdNetworkIF::new);
    }

    /**
     * Returns the bytes recv.
     *
     * @return the get bytes recv result
     */
    @Override
    public long getBytesRecv() {
        return this.bytesRecv;
    }

    /**
     * Returns the bytes sent.
     *
     * @return the get bytes sent result
     */
    @Override
    public long getBytesSent() {
        return this.bytesSent;
    }

    /**
     * Returns the packets recv.
     *
     * @return the get packets recv result
     */
    @Override
    public long getPacketsRecv() {
        return this.packetsRecv;
    }

    /**
     * Returns the packets sent.
     *
     * @return the get packets sent result
     */
    @Override
    public long getPacketsSent() {
        return this.packetsSent;
    }

    /**
     * Returns the in errors.
     *
     * @return the get in errors result
     */
    @Override
    public long getInErrors() {
        return this.inErrors;
    }

    /**
     * Returns the out errors.
     *
     * @return the get out errors result
     */
    @Override
    public long getOutErrors() {
        return this.outErrors;
    }

    /**
     * Returns the in drops.
     *
     * @return the get in drops result
     */
    @Override
    public long getInDrops() {
        return this.inDrops;
    }

    /**
     * Returns the collisions.
     *
     * @return the get collisions result
     */
    @Override
    public long getCollisions() {
        return this.collisions;
    }

    /**
     * Returns the speed.
     *
     * @return the get speed result
     */
    @Override
    public long getSpeed() {
        return this.speed;
    }

    /**
     * Returns the time stamp.
     *
     * @return the get time stamp result
     */
    @Override
    public long getTimeStamp() {
        return this.timeStamp;
    }

    /**
     * Updates the attributes.
     *
     * @return the update attributes result
     */
    @Override
    public synchronized boolean updateAttributes() {
        Map<String, Long> stats = parseNetstat(Executor.runNative("netstat -bnI " + getName()));
        this.timeStamp = System.currentTimeMillis();
        if (!stats.containsKey("Ibytes")) {
            return false;
        }
        if (!stats.containsKey("Ipkts")) {
            stats.putAll(parseNetstat(Executor.runNative("netstat -nI " + getName())));
        }
        this.bytesRecv = stats.getOrDefault("Ibytes", 0L);
        this.bytesSent = stats.getOrDefault("Obytes", 0L);
        this.packetsRecv = stats.getOrDefault("Ipkts", 0L);
        this.packetsSent = stats.getOrDefault("Opkts", 0L);
        this.inErrors = stats.getOrDefault("Ierrs", stats.getOrDefault("Ifail", 0L));
        this.outErrors = stats.getOrDefault("Oerrs", stats.getOrDefault("Ofail", 0L));
        this.collisions = stats.getOrDefault("Coll", stats.getOrDefault("Colls", 0L));
        this.inDrops = stats.getOrDefault("Idrop", 0L);
        this.speed = parseMediaSpeed(Executor.runNative("/sbin/ifconfig " + getName()));
        return true;
    }

    /**
     * Parses the active link speed from BSD {@code ifconfig} output.
     *
     * @param lines the lines emitted by {@code ifconfig <interface>}
     * @return the link speed in bits per second, or {@code 0} if unavailable
     */
    static long parseMediaSpeed(List<String> lines) {
        long parsedSpeed = 0L;
        for (String line : lines) {
            String value = line.trim();
            if (value.startsWith("status: no ")) {
                return 0L;
            }
            if (parsedSpeed == 0L && value.startsWith("media:")) {
                parsedSpeed = parseMediaRate(value);
            }
        }
        return parsedSpeed;
    }

    /**
     * Parses one BSD media description.
     *
     * @param mediaLine the media description
     * @return the media speed in bits per second, or {@code 0}
     */
    private static long parseMediaRate(String mediaLine) {
        String media = mediaLine;
        int open = media.indexOf('(');
        int close = media.lastIndexOf(')');
        if (open >= 0 && close > open) {
            media = media.substring(open + 1, close);
        }
        Matcher matcher = MEDIA_SPEED.matcher(media);
        if (matcher.find()) {
            double rate = Parsing.parseDoubleOrDefault(matcher.group(1), 0D);
            return (long) (rate * ("G".equalsIgnoreCase(matcher.group(2)) ? 1_000_000_000L : 1_000_000L));
        }
        return 0L;
    }

    /**
     * Parses a link-level row from BSD {@code netstat -I} output by matching values to column headings.
     *
     * @param lines the command output
     * @return the counter values keyed by heading
     */
    static Map<String, Long> parseNetstat(List<String> lines) {
        Map<String, Long> stats = new HashMap<>();
        if (lines.size() < 2) {
            return stats;
        }
        String[] header = Pattern.SPACES_PATTERN.split(lines.get(0).trim());
        String[] row = Pattern.SPACES_PATTERN.split(lines.get(1).trim());
        int firstCounter = Arrays.asList(header).indexOf("Address") + 1;
        int counters = header.length - firstCounter;
        if (firstCounter == 0 || row.length <= counters) {
            return stats;
        }
        for (int index = 0; index < counters; index++) {
            stats.put(
                    header[firstCounter + index],
                    Parsing.parseUnsignedLongOrDefault(row[row.length - counters + index], 0L));
        }
        return stats;
    }

}
