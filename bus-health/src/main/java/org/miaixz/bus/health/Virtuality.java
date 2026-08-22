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
package org.miaixz.bus.health;

import java.util.*;

import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.builtin.hardware.HardwareAbstractionLayer;
import org.miaixz.bus.health.builtin.hardware.NetworkIF;

/**
 * Identifies a virtualized or containerized environment from hardware signatures.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class Virtuality {

    /**
     * CPUID signature prefix.
     */
    private static final String CPUID_PREFIX = "cpuid" + Symbol.DOT;

    /**
     * System model signature prefix.
     */
    private static final String MODEL_PREFIX = "model" + Symbol.DOT;

    /**
     * Whether the current platform reports connector presence.
     */
    private static final boolean CONNECTOR_REPORTED = Platform.isLinux() || Platform.isWindows();

    /**
     * Orders signatures longest first.
     */
    private static final Comparator<Pair<String, String>> LONGEST_FIRST = (a, b) -> {
        int byLength = Integer.compare(b.getLeft().length(), a.getLeft().length());
        return byLength == 0 ? a.getLeft().compareTo(b.getLeft()) : byLength;
    };

    /**
     * Virtual machine signature properties.
     */
    private static final SupplierX<Properties> VM_PROPS = Memoizer.memoize(Virtuality::queryVmProps);

    /**
     * Virtual machine MAC OUI signature properties.
     */
    private static final SupplierX<Properties> MAC_PROPS = Memoizer.memoize(Virtuality::queryMacProps);

    /**
     * System model signature table.
     */
    private static final SupplierX<List<Pair<String, String>>> MODEL_TABLE = Memoizer
            .memoize(Virtuality::queryModelTable);

    /**
     * Creates a new Virtuality instance.
     */
    private Virtuality() {
        // No initialization required.
    }

    /**
     * Attempts to identify the virtualized or containerized environment.
     *
     * @param hw The hardware abstraction layer.
     * @return The detected platform, or an empty optional.
     */
    public static Optional<String> identify(HardwareAbstractionLayer hw) {
        Optional<String> cpuid = matchCpuid(hw.getProcessor().getProcessorIdentifier().getVendor(), VM_PROPS.get());
        if (cpuid.isPresent()) {
            return cpuid;
        }
        Optional<String> system = matchSystem(
                hw.getComputerSystem().getManufacturer(),
                hw.getComputerSystem().getModel(),
                MODEL_TABLE.get());
        if (system.isPresent()) {
            return system;
        }
        return matchMac(candidateMacAddresses(hw.getNetworkIFs()), MAC_PROPS.get());
    }

    /**
     * Collects MAC addresses worth testing.
     *
     * @param networkIFs The interfaces to filter.
     * @return The MAC addresses to test.
     */
    private static List<String> candidateMacAddresses(List<NetworkIF> networkIFs) {
        List<String> macs = new ArrayList<>();
        for (NetworkIF nif : networkIFs) {
            if (!CONNECTOR_REPORTED || nif.isConnectorPresent()) {
                macs.add(nif.getMacaddr());
            }
        }
        return macs;
    }

    /**
     * Matches a CPUID vendor string.
     *
     * @param vendor The processor vendor string.
     * @param props  The signature table.
     * @return The platform name, or an empty optional.
     */
    static Optional<String> matchCpuid(String vendor, Properties props) {
        return Optional.ofNullable(props.getProperty(CPUID_PREFIX + vendor.trim()));
    }

    /**
     * Matches a computer system manufacturer/model pair.
     *
     * @param manufacturer The manufacturer.
     * @param model        The model.
     * @param table        The signature table.
     * @return The platform name, or an empty optional.
     */
    static Optional<String> matchSystem(String manufacturer, String model, List<Pair<String, String>> table) {
        String haystack = manufacturer + Symbol.C_SPACE + model;
        for (Pair<String, String> signature : table) {
            if (haystack.contains(signature.getLeft())) {
                return Optional.of(signature.getRight());
            }
        }
        return Optional.empty();
    }

    /**
     * Matches MAC addresses against the OUI table.
     *
     * @param macAddresses The MAC addresses to test.
     * @param props        The OUI table.
     * @return The platform name, or an empty optional.
     */
    static Optional<String> matchMac(List<String> macAddresses, Properties props) {
        for (String mac : macAddresses) {
            String oui = extractOui(mac);
            if (!oui.isEmpty()) {
                String platform = props.getProperty(oui);
                if (platform != null) {
                    return Optional.of(platform);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Extracts an OUI from a MAC address.
     *
     * @param macaddr The MAC address.
     * @return The uppercase colon-delimited OUI, or an empty string.
     */
    static String extractOui(String macaddr) {
        StringBuilder hex = new StringBuilder(Normal._6);
        for (int i = Normal._0; i < macaddr.length() && hex.length() < Normal._6; i++) {
            char c = macaddr.charAt(i);
            if ((c >= Symbol.C_ZERO && c <= Symbol.C_NINE) || (c >= Symbol.C_UPPER_A && c <= Symbol.C_UPPER_F)
                    || (c >= Symbol.C_LOWER_A && c <= Symbol.C_LOWER_F)) {
                hex.append(c);
            }
        }
        if (hex.length() < Normal._6) {
            return Normal.EMPTY;
        }
        String digits = hex.toString().toUpperCase(Locale.ROOT);
        return digits.substring(Normal._0, Normal._2) + Symbol.C_COLON + digits.substring(Normal._2, Normal._4)
                + Symbol.C_COLON + digits.substring(Normal._4, Normal._6);
    }

    /**
     * Builds the model signature table.
     *
     * @param props The signature properties.
     * @return The ordered model table.
     */
    static List<Pair<String, String>> buildModelTable(Properties props) {
        List<Pair<String, String>> table = new ArrayList<>();
        for (String key : props.stringPropertyNames()) {
            if (key.startsWith(MODEL_PREFIX)) {
                String value = props.getProperty(key);
                if (value != null) {
                    table.add(Pair.of(key.substring(MODEL_PREFIX.length()), value));
                }
            }
        }
        table.sort(LONGEST_FIRST);
        return table;
    }

    /**
     * Queries model signatures.
     *
     * @return The model signature table.
     */
    private static List<Pair<String, String>> queryModelTable() {
        return buildModelTable(VM_PROPS.get());
    }

    /**
     * Reads VM signature properties.
     *
     * @return VM signature properties.
     */
    private static Properties queryVmProps() {
        return Builder.readProperties(Builder._VM_PROPERTIES);
    }

    /**
     * Reads VM MAC OUI signature properties.
     *
     * @return VM MAC OUI signature properties.
     */
    private static Properties queryMacProps() {
        return Builder.readProperties(Builder._VM_MAC_ADDR_PROPERTIES);
    }

}
