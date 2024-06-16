/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.health.builtin.hardware.common;

import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.health.Config;
import org.miaixz.bus.health.Formats;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.NetworkIF;
import org.miaixz.bus.logger.Logger;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Network interfaces implementation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public abstract class AbstractNetworkIF implements NetworkIF {

    private final Supplier<Properties> vmMacAddrProps = Memoizer.memoize(AbstractNetworkIF::queryVmMacAddrProps);
    private final NetworkInterface networkInterface;
    private final String name;
    private final String displayName;
    private final int index;
    private final long mtu;
    private final String mac;
    private final String[] ipv4;
    private final Short[] subnetMasks;
    private final String[] ipv6;
    private final Short[] prefixLengths;

    /**
     * Construct a {@link NetworkIF} object backed by the specified {@link NetworkInterface}.
     *
     * @param netint The core java {@link NetworkInterface} backing this object.
     * @throws InstantiationException If a socket exception prevents access to the backing interface.
     */
    protected AbstractNetworkIF(NetworkInterface netint) throws InstantiationException {
        this(netint, netint.getDisplayName());
    }

    /**
     * Construct a {@link NetworkIF} object backed by the specified {@link NetworkInterface}.
     *
     * @param netint      The core java {@link NetworkInterface} backing this object.
     * @param displayName A string to use for the display name in preference to the {@link NetworkInterface} value.
     * @throws InstantiationException If a socket exception prevents access to the backing interface.
     */
    protected AbstractNetworkIF(NetworkInterface netint, String displayName) throws InstantiationException {
        this.networkInterface = netint;
        try {
            this.name = networkInterface.getName();
            this.displayName = displayName;
            this.index = networkInterface.getIndex();
            // Set MTU
            this.mtu = Parsing.unsignedIntToLong(networkInterface.getMTU());
            // Set MAC
            byte[] hwmac = networkInterface.getHardwareAddress();
            if (hwmac != null) {
                List<String> octets = new ArrayList<>(6);
                for (byte b : hwmac) {
                    octets.add(String.format(Locale.ROOT, "%02x", b));
                }
                this.mac = String.join(Symbol.COLON, octets);
            } else {
                this.mac = Normal.UNKNOWN;
            }
            // Set IP arrays
            ArrayList<String> ipv4list = new ArrayList<>();
            ArrayList<Short> subnetMaskList = new ArrayList<>();
            ArrayList<String> ipv6list = new ArrayList<>();
            ArrayList<Short> prefixLengthList = new ArrayList<>();

            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                InetAddress address = interfaceAddress.getAddress();
                if (address.getHostAddress().length() > 0) {
                    if (address.getHostAddress().contains(Symbol.COLON)) {
                        ipv6list.add(address.getHostAddress().split(Symbol.PERCENT)[0]);
                        prefixLengthList.add(interfaceAddress.getNetworkPrefixLength());
                    } else {
                        ipv4list.add(address.getHostAddress());
                        subnetMaskList.add(interfaceAddress.getNetworkPrefixLength());
                    }
                }
            }

            this.ipv4 = ipv4list.toArray(new String[0]);
            this.subnetMasks = subnetMaskList.toArray(new Short[0]);
            this.ipv6 = ipv6list.toArray(new String[0]);
            this.prefixLengths = prefixLengthList.toArray(new Short[0]);
        } catch (SocketException e) {
            throw new InstantiationException(e.getMessage());
        }
    }

    /**
     * Returns network interfaces on this machine.
     *
     * @param includeLocalInterfaces include local interfaces in the result
     * @return A list of network interfaces
     */
    protected static List<NetworkInterface> getNetworkInterfaces(boolean includeLocalInterfaces) {
        List<NetworkInterface> interfaces = getAllNetworkInterfaces();

        return includeLocalInterfaces ? interfaces
                : getAllNetworkInterfaces().stream().parallel().filter(ni -> !isLocalInterface(ni))
                .collect(Collectors.toList());
    }

    /**
     * Returns all network interfaces.
     *
     * @return A list of network interfaces
     */
    private static List<NetworkInterface> getAllNetworkInterfaces() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            return interfaces == null ? Collections.emptyList() : Collections.list(interfaces);
        } catch (SocketException ex) {
            Logger.error("Socket exception when retrieving interfaces: {}", ex.getMessage());
        }
        return Collections.emptyList();
    }

    private static boolean isLocalInterface(NetworkInterface networkInterface) {
        try {
            // getHardwareAddress also checks for loopback
            return networkInterface.getHardwareAddress() == null;
        } catch (SocketException e) {
            Logger.error("Socket exception when retrieving interface information for {}: {}", networkInterface,
                    e.getMessage());
        }
        return false;
    }

    private static Properties queryVmMacAddrProps() {
        return Config.readProperties(Config._VM_MAC_ADDR_PROPERTIES);
    }

    @Override
    public NetworkInterface queryNetworkInterface() {
        return this.networkInterface;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public long getMTU() {
        return this.mtu;
    }

    @Override
    public String getMacaddr() {
        return this.mac;
    }

    @Override
    public String[] getIPv4addr() {
        return Arrays.copyOf(this.ipv4, this.ipv4.length);
    }

    @Override
    public Short[] getSubnetMasks() {
        return Arrays.copyOf(this.subnetMasks, this.subnetMasks.length);
    }

    @Override
    public String[] getIPv6addr() {
        return Arrays.copyOf(this.ipv6, this.ipv6.length);
    }

    @Override
    public Short[] getPrefixLengths() {
        return Arrays.copyOf(this.prefixLengths, this.prefixLengths.length);
    }

    @Override
    public boolean isKnownVmMacAddr() {
        String oui = getMacaddr().length() > 7 ? getMacaddr().substring(0, 8) : getMacaddr();
        return this.vmMacAddrProps.get().containsKey(oui.toUpperCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(getName());
        if (!getName().equals(getDisplayName())) {
            sb.append(" (").append(getDisplayName()).append(Symbol.PARENTHESE_RIGHT);
        }
        if (!getIfAlias().isEmpty()) {
            sb.append(" [IfAlias=").append(getIfAlias()).append("]");
        }
        sb.append("\n");
        sb.append("  MAC Address: ").append(getMacaddr()).append("\n");
        sb.append("  MTU: ").append(getMTU()).append(", ").append("Speed: ").append(getSpeed()).append("\n");
        String[] ipv4withmask = getIPv4addr();
        if (this.ipv4.length == this.subnetMasks.length) {
            for (int i = 0; i < this.subnetMasks.length; i++) {
                ipv4withmask[i] += "/" + this.subnetMasks[i];
            }
        }
        sb.append("  IPv4: ").append(Arrays.toString(ipv4withmask)).append("\n");
        String[] ipv6withprefixlength = getIPv6addr();
        if (this.ipv6.length == this.prefixLengths.length) {
            for (int j = 0; j < this.prefixLengths.length; j++) {
                ipv6withprefixlength[j] += "/" + this.prefixLengths[j];
            }
        }
        sb.append("  IPv6: ").append(Arrays.toString(ipv6withprefixlength)).append("\n");
        sb.append("  Traffic: received ").append(getPacketsRecv()).append(" packets/")
                .append(Formats.formatBytes(getBytesRecv())).append(" (" + getInErrors() + " err, ")
                .append(getInDrops() + " drop);");
        sb.append(" transmitted ").append(getPacketsSent()).append(" packets/")
                .append(Formats.formatBytes(getBytesSent())).append(" (" + getOutErrors() + " err, ")
                .append(getCollisions() + " coll);");
        return sb.toString();
    }
}
