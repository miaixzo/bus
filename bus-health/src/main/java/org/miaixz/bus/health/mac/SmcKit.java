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
package org.miaixz.bus.health.mac;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.jna.NativeLong;
import com.sun.jna.platform.mac.IOKit.IOConnect;
import com.sun.jna.platform.mac.IOKit.IOService;
import com.sun.jna.platform.mac.IOKitUtil;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.jna.ByRef.CloseableNativeLongByReference;
import org.miaixz.bus.health.builtin.jna.ByRef.CloseablePointerByReference;
import org.miaixz.bus.health.mac.jna.IOKit;
import org.miaixz.bus.health.mac.jna.IOKit.SMCKeyData;
import org.miaixz.bus.health.mac.jna.IOKit.SMCKeyDataKeyInfo;
import org.miaixz.bus.health.mac.jna.IOKit.SMCVal;
import org.miaixz.bus.health.mac.jna.SystemB;
import org.miaixz.bus.logger.Logger;

/**
 * Provides access to SMC calls on macOS
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class SmcKit {

    /**
     * Keeps macOS SMC queries on the static API.
     */
    public SmcKit() {
        // No initialization required.
    }

    /**
     * Instance of IOKit.
     */
    private static final IOKit IO = IOKit.INSTANCE;

    /**
     * Thread-safe map for caching info retrieved by a key necessary for subsequent calls.
     */
    private static Map<Integer, SMCKeyDataKeyInfo> keyInfoCache = new ConcurrentHashMap<>();

    /**
     * Byte array used for matching return type
     */
    private static final byte[] DATATYPE_SP78 = Parsing.asciiStringToByteArray("sp78", 5);

    /**
     * Byte array used for matching FPE2 return type.
     */
    private static final byte[] DATATYPE_FPE2 = Parsing.asciiStringToByteArray("fpe2", 5);

    /**
     * Byte array used for matching FLT return type.
     */
    private static final byte[] DATATYPE_FLT = Parsing.asciiStringToByteArray("flt ", 5);

    /**
     * SMC key for the number of fans.
     */
    public static final String SMC_KEY_FAN_NUM = "FNum";

    /**
     * SMC key for fan speed, where %d is the fan index.
     */
    public static final String SMC_KEY_FAN_SPEED = "F%dAc";

    /**
     * SMC key for CPU temperature.
     */
    public static final String SMC_KEY_CPU_TEMP = "TC0P";

    /**
     * SMC key for CPU voltage.
     */
    public static final String SMC_KEY_CPU_VOLTAGE = "VC0C";

    /**
     * Apple Silicon CPU-die aggregate temperature keys, tried in order until one returns a plausible value.
     */
    public static final List<String> SMC_KEYS_CPU_TEMP_AGGREGATE_AS = Collections
            .unmodifiableList(Arrays.asList("TCMb", "TCMz"));

    /**
     * Apple Silicon CPU temperature keys, tried in order until one returns a plausible value.
     */
    public static final List<String> SMC_KEYS_CPU_TEMP_AS = Collections
            .unmodifiableList(Arrays.asList("Tp09", "Tp0T", "Tp01", "Tp05", "Tp0D"));

    /**
     * Fallback Apple Silicon GPU temperature keys, used when runtime discovery cannot complete.
     */
    public static final List<String> SMC_KEYS_GPU_TEMP_AS = Collections.unmodifiableList(
            Arrays.asList(
                    "Tg05",
                    "Tg0D",
                    "Tg0f",
                    "Tg0j",
                    "Tg0C",
                    "Tg04",
                    "Tg0K",
                    "Tg0L",
                    "Tg0d",
                    "Tg0e",
                    "Tg1k",
                    "Tg0X",
                    "Tg0S",
                    "Tg0y",
                    "Tg0z"));

    /**
     * SMC key whose value is the number of keys in the key index.
     */
    public static final String SMC_KEY_COUNT = "#KEY";

    /**
     * Prefix shared by Apple Silicon GPU cluster temperature keys.
     */
    private static final String GPU_KEY_PREFIX = "Tg";

    /**
     * Prefix shared by fan keys.
     */
    private static final String FAN_KEY_PREFIX = "F";

    /**
     * GPU temperature keys, discovered on first use.
     */
    private static final SmcKeyCache GPU_KEYS = new SmcKeyCache(Builder._MAC_SENSORS_GPUTEMPERATURE_KEYS,
            "GPU temperature", SMC_KEYS_GPU_TEMP_AS);

    /**
     * Fan speed keys, discovered on first use.
     */
    private static final SmcKeyCache FAN_KEYS = new SmcKeyCache(Builder._MAC_SENSORS_FANSPEED_KEYS, "fan speed",
            Collections.emptyList());

    /**
     * SMC key for CPU voltage.
     */
    public static final String SMC_KEY_CPU_VOLTAGE_AS = "VP0C";

    /**
     * CPU voltage keys, tried in order until one returns a plausible value.
     */
    public static final List<String> SMC_KEYS_CPU_VOLTAGE = Collections
            .unmodifiableList(Arrays.asList(SMC_KEY_CPU_VOLTAGE_AS, SMC_KEY_CPU_VOLTAGE));

    /**
     * SMC command to read bytes.
     */
    public static final byte SMC_CMD_READ_BYTES = 5;

    /**
     * SMC command to read a key name at an index in the key index.
     */
    public static final byte SMC_CMD_READ_INDEX = 8;

    /**
     * SMC command to read key information.
     */
    public static final byte SMC_CMD_READ_KEYINFO = 9;

    /**
     * Kernel index for SMC.
     */
    public static final int KERNEL_INDEX_SMC = 2;

    /**
     * Open a connection to SMC.
     *
     * @return The connection if successful, null if failure
     */
    public static IOConnect smcOpen() {
        IOService smcService = IOKitUtil.getMatchingService("AppleSMC");
        if (smcService != null) {
            try (CloseablePointerByReference connPtr = new CloseablePointerByReference()) {
                int result = IO.IOServiceOpen(smcService, SystemB.INSTANCE.mach_task_self(), 0, connPtr);
                if (result == 0) {
                    return new IOConnect(connPtr.getValue());
                } else if (Logger.isErrorEnabled()) {
                    Logger.error(
                            false,
                            "Health",
                            String.format(
                                    Locale.ROOT,
                                    "Unable to open connection to AppleSMC service. Error: 0x%08x",
                                    result));
                }
            } finally {
                smcService.release();
            }
        } else {
            Logger.error(false, "Health", "Unable to locate AppleSMC service");
        }
        return null;
    }

    /**
     * Close connection to SMC.
     *
     * @param conn The connection
     * @return 0 if successful, nonzero if failure
     */
    public static int smcClose(IOConnect conn) {
        return IO.IOServiceClose(conn);
    }

    /**
     * Get a value from SMC which is in a floating point datatype (SP78, FPE2, FLT)
     *
     * @param conn The connection
     * @param key  The key to retrieve
     * @return Double representing the value
     */
    public static double smcGetFloat(IOConnect conn, String key) {
        try (SMCVal val = new SMCVal()) {
            int result = smcReadKey(conn, key, val);
            if (result == 0 && val.dataSize > 0) {
                if (Arrays.equals(val.dataType, DATATYPE_SP78) && val.dataSize == 2) {
                    // First bit is sign, next 7 bits are integer portion, last 8 bits are the unsigned fractional
                    // portion.
                    return val.bytes[0] + (val.bytes[1] & 0xFF) / 256d;
                } else if (Arrays.equals(val.dataType, DATATYPE_FPE2) && val.dataSize == 2) {
                    // First E (14) bits are integer portion last 2 bits are fractional portion
                    return Parsing.byteArrayToFloat(val.bytes, val.dataSize, 2);
                } else if (Arrays.equals(val.dataType, DATATYPE_FLT) && val.dataSize == 4) {
                    // Standard 32-bit floating point
                    return ByteBuffer.wrap(val.bytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                }
            }
        }
        // Read failed
        return 0d;
    }

    /**
     * Get the first positive value from a list of SMC keys.
     *
     * @param conn The connection
     * @param keys The keys to try in order
     * @return The first value greater than 0, or 0 if all keys fail
     */
    public static double smcGetFirstFloat(IOConnect conn, List<String> keys) {
        for (String key : keys) {
            double val = smcGetFloat(conn, key);
            if (val > 0d) {
                return val;
            }
        }
        return 0d;
    }

    /**
     * Lowest reading accepted as a genuine temperature, in degrees Celsius.
     */
    public static final double MIN_PLAUSIBLE_TEMPERATURE = 15d;

    /**
     * Tests whether a reading is plausible as a temperature.
     *
     * @param celsius The reading to test, in degrees Celsius.
     * @return {@code true} if the reading is plausible.
     */
    public static boolean isPlausibleTemperature(double celsius) {
        return celsius >= MIN_PLAUSIBLE_TEMPERATURE;
    }

    /**
     * Gets the first plausible temperature from a list of SMC keys.
     *
     * @param conn The connection.
     * @param keys The keys to try in order.
     * @return The first plausible temperature, or {@code 0} if no key returned one.
     */
    public static double smcGetFirstTemperature(IOConnect conn, List<String> keys) {
        return SmcKeyIndex
                .firstPlausible(keys, key -> smcGetFloat(conn, key), SmcKit::isPlausibleTemperature, "temperature");
    }

    /**
     * Gets the highest plausible temperature among a list of SMC keys.
     *
     * @param conn The connection.
     * @param keys The keys to read.
     * @return The highest plausible temperature, or {@code 0} if none were plausible.
     */
    public static double smcGetMaxTemperature(IOConnect conn, List<String> keys) {
        return SmcKeyIndex.maxPlausible(keys, key -> smcGetFloat(conn, key), SmcKit::isPlausibleTemperature);
    }

    /**
     * Gets the name of the key at an index in the SMC key index.
     *
     * @param conn  The connection.
     * @param index The index to read.
     * @return The four-character key name, or {@code null} if it could not be read.
     */
    public static String smcReadKeyAtIndex(IOConnect conn, int index) {
        try (SMCKeyData input = new SMCKeyData(); SMCKeyData output = new SMCKeyData()) {
            input.data8 = SMC_CMD_READ_INDEX;
            input.data32 = index;
            if (smcCall(conn, KERNEL_INDEX_SMC, input, output) != 0) {
                return null;
            }
            byte[] keyBytes = Parsing.longToByteArray(output.key, 4, 4);
            StringBuilder sb = new StringBuilder(4);
            for (byte b : keyBytes) {
                sb.append((char) (b & 0xFF));
            }
            return sb.toString();
        }
    }

    /**
     * Gets the SMC data type of a key, for example {@code flt} or {@code sp78}.
     *
     * @param conn The connection.
     * @param key  The key to query.
     * @return The data type, or an empty string if it could not be read.
     */
    public static String smcGetDataType(IOConnect conn, String key) {
        try (SMCVal val = new SMCVal()) {
            if (smcReadKey(conn, key, val) != 0 || val.dataType == null) {
                return "";
            }
            StringBuilder sb = new StringBuilder(4);
            for (byte b : val.dataType) {
                if (b == 0) {
                    break;
                }
                sb.append((char) b);
            }
            return sb.toString().trim();
        }
    }

    /**
     * Gets Apple Silicon GPU temperature keys for this machine.
     *
     * @return The keys to read for GPU temperature, never {@code null}.
     */
    public static List<String> getGpuTemperatureKeys() {
        return GPU_KEYS.get(SmcKit::discoverGpuTemperatureKeys);
    }

    /**
     * Gets the keys to read for fan speeds.
     *
     * @return The keys to read for fan speeds, never {@code null}.
     */
    public static List<String> getFanSpeedKeys() {
        return FAN_KEYS.get(SmcKit::discoverFanSpeedKeys);
    }

    /**
     * Discovers fan speed keys from the SMC key index.
     *
     * @return The keys to read, or {@code null} if neither the key index nor {@code FNum} could be read.
     */
    private static List<String> discoverFanSpeedKeys() {
        IOConnect conn = smcOpen();
        if (conn == null) {
            return null;
        }
        try {
            int keyCount = (int) smcGetLong(conn, SMC_KEY_COUNT);
            List<String> discovered = SmcKeyIndex
                    .findKeys(keyCount, i -> smcReadKeyAtIndex(conn, i), FAN_KEY_PREFIX, SmcKeyIndex::isFanSpeedKey);
            return SmcKeyIndex.reconcileFanKeys(discovered, smcGetLong(conn, SMC_KEY_FAN_NUM));
        } finally {
            smcClose(conn);
        }
    }

    /**
     * Gets the keys to read for CPU voltage, in the order they should be tried.
     *
     * @return The keys to read for CPU voltage, never {@code null}.
     */
    public static List<String> getCpuVoltageKeys() {
        List<String> configured = SmcKeyIndex
                .parseConfiguredKeys(Builder.get(Builder._MAC_SENSORS_CPUVOLTAGE_KEYS, ""));
        return configured.isEmpty() ? SMC_KEYS_CPU_VOLTAGE : configured;
    }

    /**
     * Lowest reading accepted as a plausible CPU voltage, in volts.
     */
    public static final double MIN_PLAUSIBLE_VOLTAGE = SmcSensorValues.MIN_PLAUSIBLE_VOLTAGE;

    /**
     * Tests whether a reading is plausible as a CPU voltage.
     *
     * @param volts The reading to test, in volts.
     * @return {@code true} if the reading is plausible.
     */
    public static boolean isPlausibleVoltage(double volts) {
        return SmcSensorValues.isPlausibleVoltage(volts);
    }

    /**
     * Gets the first plausible CPU voltage from a list of SMC keys.
     *
     * @param conn The connection.
     * @param keys The keys to try in order.
     * @return The first plausible voltage, or {@code 0} if no key returned one.
     */
    public static double smcGetFirstVoltage(IOConnect conn, List<String> keys) {
        return SmcKeyIndex.firstPlausible(keys, key -> {
            double raw = smcGetFloat(conn, key);
            return raw == 0d ? 0d : SmcSensorValues.scaleVoltage(raw, smcGetDataType(conn, key));
        }, SmcKit::isPlausibleVoltage, "voltage");
    }

    /**
     * Discovers Apple Silicon GPU temperature keys from the SMC key index.
     *
     * @return The discovered keys, or {@code null} if the SMC key index could not be read.
     */
    private static List<String> discoverGpuTemperatureKeys() {
        IOConnect conn = smcOpen();
        if (conn == null) {
            return null;
        }
        try {
            int keyCount = (int) smcGetLong(conn, SMC_KEY_COUNT);
            return SmcKeyIndex.findKeys(
                    keyCount,
                    i -> smcReadKeyAtIndex(conn, i),
                    GPU_KEY_PREFIX,
                    SmcKeyIndex::isGpuTemperatureKey);
        } finally {
            smcClose(conn);
        }
    }

    /**
     * Get a 64-bit integer value from SMC
     *
     * @param conn The connection
     * @param key  The key to retrieve
     * @return Long representing the value
     */
    public static long smcGetLong(IOConnect conn, String key) {
        try (SMCVal val = new SMCVal()) {
            int result = smcReadKey(conn, key, val);
            if (result == 0) {
                return Parsing.byteArrayToLong(val.bytes, val.dataSize);
            }
        }
        // Read failed
        return 0;
    }

    /**
     * Get cached keyInfo if it exists, or generate new keyInfo
     *
     * @param conn            The connection
     * @param inputStructure  Key data input
     * @param outputStructure Key data output
     * @return 0 if successful, nonzero if failure
     */
    public static int smcGetKeyInfo(IOConnect conn, SMCKeyData inputStructure, SMCKeyData outputStructure) {
        if (keyInfoCache.containsKey(inputStructure.key)) {
            SMCKeyDataKeyInfo keyInfo = keyInfoCache.get(inputStructure.key);
            outputStructure.keyInfo.dataSize = keyInfo.dataSize;
            outputStructure.keyInfo.dataType = keyInfo.dataType;
            outputStructure.keyInfo.dataAttributes = keyInfo.dataAttributes;
        } else {
            inputStructure.data8 = SMC_CMD_READ_KEYINFO;
            int result = smcCall(conn, KERNEL_INDEX_SMC, inputStructure, outputStructure);
            if (result != 0) {
                return result;
            }
            SMCKeyDataKeyInfo keyInfo = new SMCKeyDataKeyInfo();
            keyInfo.dataSize = outputStructure.keyInfo.dataSize;
            keyInfo.dataType = outputStructure.keyInfo.dataType;
            keyInfo.dataAttributes = outputStructure.keyInfo.dataAttributes;
            keyInfoCache.put(inputStructure.key, keyInfo);
        }
        return 0;
    }

    /**
     * Read a key from SMC
     *
     * @param conn The connection
     * @param key  Key to read
     * @param val  Structure to receive the result
     * @return 0 if successful, nonzero if failure
     */
    public static int smcReadKey(IOConnect conn, String key, SMCVal val) {
        try (SMCKeyData inputStructure = new SMCKeyData(); SMCKeyData outputStructure = new SMCKeyData()) {
            inputStructure.key = (int) Parsing.strToLong(key, 4);
            int result = smcGetKeyInfo(conn, inputStructure, outputStructure);
            if (result == 0) {
                val.dataSize = outputStructure.keyInfo.dataSize;
                val.dataType = Parsing.longToByteArray(outputStructure.keyInfo.dataType, 4, 5);

                inputStructure.keyInfo.dataSize = val.dataSize;
                inputStructure.data8 = SMC_CMD_READ_BYTES;

                result = smcCall(conn, KERNEL_INDEX_SMC, inputStructure, outputStructure);
                if (result == 0) {
                    System.arraycopy(outputStructure.bytes, 0, val.bytes, 0, val.bytes.length);
                    return 0;
                }
            }
            return result;
        }
    }

    /**
     * Call SMC
     *
     * @param conn            The connection
     * @param index           Kernel index
     * @param inputStructure  Key data input
     * @param outputStructure Key data output
     * @return 0 if successful, nonzero if failure
     */
    public static int smcCall(IOConnect conn, int index, SMCKeyData inputStructure, SMCKeyData outputStructure) {
        try (CloseableNativeLongByReference size = new CloseableNativeLongByReference(
                new NativeLong(outputStructure.size()))) {
            return IO.IOConnectCallStructMethod(
                    conn,
                    index,
                    inputStructure,
                    new NativeLong(inputStructure.size()),
                    outputStructure,
                    size);
        }
    }

}
