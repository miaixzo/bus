/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org OSHI and other contributors.                 *
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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.health.builtin.hardware;

import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Formats;
import org.aoju.bus.health.Memoize;
import org.aoju.bus.logger.Logger;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 网络接口信息
 *
 * @author Kimi Liu
 * @version 6.1.9
 * @since JDK 1.8+
 */
@ThreadSafe
public abstract class AbstractNetworkIF implements NetworkIF {

    private final Supplier<Properties> vmMacAddrProps = Memoize.memoize(AbstractNetworkIF::queryVmMacAddrProps);
    private NetworkInterface networkInterface;
    private int mtu;
    private String mac;
    private String[] ipv4;
    private Short[] subnetMasks;
    private String[] ipv6;
    private Short[] prefixLengths;

    /**
     * 构造一个由指定的{@link NetworkInterface}支持的{@link NetworkIF}对象
     *
     * @param netint 支持此对象的核心java {@link NetworkInterface}
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
            // 设置IP阵列
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
     * Returns network interfaces on this machine.
     *
     * @param includeLocalInterfaces include local interfaces in the result
     * @return A list of network interfaces
     */
    protected static List<NetworkInterface> getNetworkInterfaces(boolean includeLocalInterfaces) {
        List<NetworkInterface> interfaces = getAllNetworkInterfaces();
        return includeLocalInterfaces ? interfaces
                : getAllNetworkInterfaces().stream().filter(networkInterface1 -> !isLocalInterface(networkInterface1))
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
            return networkInterface.isLoopback() || networkInterface.getHardwareAddress() == null;
        } catch (SocketException e) {
            Logger.error("Socket exception when retrieving interface information for {}: {}", networkInterface,
                    e.getMessage());
        }
        return false;
    }

    private static Properties queryVmMacAddrProps() {
        return Builder.readProperties(Builder.BUS_HEALTH_ADDR_PROPERTIES);
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
        return 0;
    }

    @Override
    public int getNdisPhysicalMediumType() {
        return 0;
    }

    @Override
    public boolean isConnectorPresent() {
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(getName()).append(Symbol.SPACE).append(Symbol.PARENTHESE_LEFT).append(getDisplayName()).append(Symbol.PARENTHESE_RIGHT).append(Symbol.LF);
        sb.append("  MAC Address: ").append(getMacaddr()).append(Symbol.LF);
        sb.append("  MTU: ").append(Builder.unsignedIntToLong(getMTU())).append(", ").append("Speed: ")
                .append(getSpeed()).append("\n");
        String[] ipv4withmask = getIPv4addr();
        if (this.ipv4.length == this.subnetMasks.length) {
            for (int i = 0; i < this.subnetMasks.length; i++) {
                ipv4withmask[i] += Symbol.SLASH + this.subnetMasks[i];
            }
        }
        sb.append("  IPv4: ").append(Arrays.toString(ipv4withmask)).append(Symbol.LF);
        String[] ipv6withprefixlength = getIPv6addr();
        if (this.ipv6.length == this.prefixLengths.length) {
            for (int j = 0; j < this.prefixLengths.length; j++) {
                ipv6withprefixlength[j] += Symbol.SLASH + this.prefixLengths[j];
            }
        }
        sb.append("  IPv6: ").append(Arrays.toString(ipv6withprefixlength)).append(Symbol.LF);
        sb.append("  Traffic: received ").append(getPacketsRecv()).append(" packets/")
                .append(Formats.formatBytes(getBytesRecv())).append(" (" + getInErrors() + " err, ")
                .append(getInDrops() + " drop);");
        sb.append(" transmitted ").append(getPacketsSent()).append(" packets/")
                .append(Formats.formatBytes(getBytesSent())).append(" (" + getOutErrors() + " err, ")
                .append(getCollisions() + " coll);");
        return sb.toString();
    }

}
