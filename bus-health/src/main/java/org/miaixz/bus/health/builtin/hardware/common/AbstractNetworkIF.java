/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.           ~
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
package org.miaixz.bus.health.builtin.hardware.common;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Config;
import org.miaixz.bus.health.Formats;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.NetworkIF;
import org.miaixz.bus.logger.Logger;

/**
 * Network interfaces implementation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public abstract class AbstractNetworkIF implements NetworkIF {

    /**
     * The vmMacAddrProps value.
     */
    private final Supplier<Properties> vmMacAddrProps = Memoizer.memoize(AbstractNetworkIF::queryVmMacAddrProps);
    /**
     * The networkInterface value.
     */
    private final NetworkInterface networkInterface;
    /**
     * The name value.
     */
    private final String name;
    /**
     * The displayName value.
     */
    private final String displayName;
    /**
     * The index value.
     */
    private final int index;
    /**
     * The mtu value.
     */
    private final long mtu;
    /**
     * The mac value.
     */
    private final String mac;
    /**
     * The ipv4 value.
     */
    private final String[] ipv4;
    /**
     * The subnetMasks value.
     */
    private final Short[] subnetMasks;
    /**
     * The ipv6 value.
     */
    private final String[] ipv6;
    /**
     * The prefixLengths value.
     */
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
            Logger.error(
                    false,
                    "Health",
                    ex,
                    "Network interface retrieval failed: exception={}",
                    ex.getClass().getSimpleName());
        }
        return Collections.emptyList();
    }

    /**
     * Returns whether the local interface condition is true.
     *
     * @param networkInterface the network interface
     * @return the is local interface result
     */
    private static boolean isLocalInterface(NetworkInterface networkInterface) {
        try {
            // getHardwareAddress also checks for loopback
            return networkInterface.getHardwareAddress() == null;
        } catch (SocketException e) {
            Logger.error(
                    false,
                    "Health",
                    "Socket exception when retrieving interface information for {}: {}",
                    networkInterface,
                    e.getClass().getSimpleName());
        }
        return false;
    }

    /**
     * Queries the vm mac addr props.
     *
     * @return the query vm mac addr props result
     */
    private static Properties queryVmMacAddrProps() {
        return Config.readProperties(Config._VM_MAC_ADDR_PROPERTIES);
    }

    /**
     * Queries the network interface.
     *
     * @return the query network interface result
     */
    @Override
    public NetworkInterface queryNetworkInterface() {
        return this.networkInterface;
    }

    /**
     * Returns the name.
     *
     * @return the get name result
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Returns the index.
     *
     * @return the get index result
     */
    @Override
    public int getIndex() {
        return this.index;
    }

    /**
     * Returns the display name.
     *
     * @return the get display name result
     */
    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Returns the mtu.
     *
     * @return the get mtu result
     */
    @Override
    public long getMTU() {
        return this.mtu;
    }

    /**
     * Returns the macaddr.
     *
     * @return the get macaddr result
     */
    @Override
    public String getMacaddr() {
        return this.mac;
    }

    /**
     * Returns the i pv4addr.
     *
     * @return the get i pv4addr result
     */
    @Override
    public String[] getIPv4addr() {
        return Arrays.copyOf(this.ipv4, this.ipv4.length);
    }

    /**
     * Returns the subnet masks.
     *
     * @return the get subnet masks result
     */
    @Override
    public Short[] getSubnetMasks() {
        return Arrays.copyOf(this.subnetMasks, this.subnetMasks.length);
    }

    /**
     * Returns the i pv6addr.
     *
     * @return the get i pv6addr result
     */
    @Override
    public String[] getIPv6addr() {
        return Arrays.copyOf(this.ipv6, this.ipv6.length);
    }

    /**
     * Returns the prefix lengths.
     *
     * @return the get prefix lengths result
     */
    @Override
    public Short[] getPrefixLengths() {
        return Arrays.copyOf(this.prefixLengths, this.prefixLengths.length);
    }

    /**
     * Returns whether the known vm mac addr condition is true.
     *
     * @return the is known vm mac addr result
     */
    @Override
    public boolean isKnownVmMacAddr() {
        String oui = getMacaddr().length() > 7 ? getMacaddr().substring(0, 8) : getMacaddr();
        return this.vmMacAddrProps.get().containsKey(oui.toUpperCase(Locale.ROOT));
    }

    /**
     * Returns the to string result.
     *
     * @return the to string result
     */
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
