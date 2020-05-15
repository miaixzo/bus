/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org OSHI and other contributors.                 *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.health.builtin.hardware;

import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.health.Builder;
import org.aoju.bus.logger.Logger;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.function.Supplier;

import static org.aoju.bus.health.Memoize.memoize;

/**
 * Network interfaces implementation.
 *
 * @author Kimi Liu
 * @version 5.9.1
 * @since JDK 1.8+
 */
@ThreadSafe
public abstract class AbstractNetworkIF implements NetworkIF {

    private final Supplier<Properties> vmMacAddrProps = memoize(AbstractNetworkIF::queryVmMacAddrProps);
    private NetworkInterface networkInterface;
    private int mtu;
    private String mac;
    private String[] ipv4;
    private Short[] subnetMasks;
    private String[] ipv6;
    private Short[] prefixLengths;

    /**
     * Construct a {@link NetworkIF} object backed by the specified
     * {@link NetworkInterface}.
     *
     * @param netint The core java {@link NetworkInterface} backing this object.
     */
    protected AbstractNetworkIF(NetworkInterface netint) {
        this.networkInterface = netint;
        try {
            // Set MTU
            this.mtu = networkInterface.getMTU();
            // Set MAC
            byte[] hwmac = networkInterface.getHardwareAddress();
            if (hwmac != null) {
                List<String> octets = new ArrayList<>(6);
                for (byte b : hwmac) {
                    octets.add(String.format("%02x", b));
                }
                this.mac = String.join(Symbol.COLON, octets);
            } else {
                this.mac = Normal.UNKNOWN;
            }
            // Set IP arrays
            List<String> ipv4list = new ArrayList<>();
            List<Short> subnetMaskList = new ArrayList<>();
            List<String> ipv6list = new ArrayList<>();
            List<Short> prefixLengthList = new ArrayList<>();

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
            Logger.error("Socket exception: {}", e.getMessage());
        }
    }

    /**
     * Returns network interfaces that are not Loopback, and have a hardware
     * address.
     *
     * @return A list of network interfaces
     */
    protected static List<NetworkInterface> getNetworkInterfaces() {
        List<NetworkInterface> result = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces = null;

        try {
            interfaces = NetworkInterface.getNetworkInterfaces(); // can return null
        } catch (SocketException ex) {
            Logger.error("Socket exception when retrieving interfaces: {}", ex.getMessage());
        }
        if (interfaces != null) {
            while (interfaces.hasMoreElements()) {
                NetworkInterface netint = interfaces.nextElement();
                try {
                    if (!netint.isLoopback() && netint.getHardwareAddress() != null) {
                        result.add(netint);
                    }
                } catch (SocketException ex) {
                    Logger.error("Socket exception when retrieving interface \"{}\": {}", netint.getName(),
                            ex.getMessage());
                }
            }
        }
        return result;
    }

    private static Properties queryVmMacAddrProps() {
        return Builder.readPropertiesFromFilename(Builder.BUS_HEALTH_ADDR_PROPERTIES);
    }

    @Override
    public NetworkInterface queryNetworkInterface() {
        return this.networkInterface;
    }

    @Override
    public String getName() {
        return this.networkInterface.getName();
    }

    @Override
    public String getDisplayName() {
        return this.networkInterface.getDisplayName();
    }

    @Override
    public int getMTU() {
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
        return this.vmMacAddrProps.get().containsKey(oui.toUpperCase());
    }

    @Override
    public int getIfType() {
        // default
        return 0;
    }

    @Override
    public int getNdisPhysicalMediumType() {
        // default
        return 0;
    }

    @Override
    public boolean isConnectorPresent() {
        // default
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(getName()).append(Symbol.SPACE).append(Symbol.PARENTHESE_LEFT).append(getDisplayName()).append(Symbol.PARENTHESE_RIGHT).append("\n");
        sb.append("  MAC Address: ").append(getMacaddr()).append("\n");
        sb.append("  MTU: ").append(getMTU()).append(", ").append("Speed: ").append(getSpeed()).append("\n");
        sb.append("  IPv4: ").append(Arrays.toString(getIPv4addr())).append("\n");
        sb.append("  Netmask:  ").append(Arrays.toString(getSubnetMasks())).append("\n");
        sb.append("  IPv6: ").append(Arrays.toString(getIPv6addr())).append("\n");
        sb.append("  Prefix Lengths:  ").append(Arrays.toString(getPrefixLengths())).append("\n");
        sb.append("  Traffic: received ").append(getPacketsRecv()).append(" packets/")
                .append(Builder.formatBytes(getBytesRecv())).append(" (" + getInErrors() + " err, ")
                .append(getInDrops() + " drop);");
        sb.append(" transmitted ").append(getPacketsSent()).append(" packets/")
                .append(Builder.formatBytes(getBytesSent())).append(" (" + getOutErrors() + " err, ")
                .append(getCollisions() + " coll);");
        return sb.toString();
    }

}
