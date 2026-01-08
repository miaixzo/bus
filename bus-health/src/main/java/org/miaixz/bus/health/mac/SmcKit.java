/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.               ~
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
package org.miaixz.bus.health.mac;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.jna.ByRef.CloseableNativeLongByReference;
import org.miaixz.bus.health.builtin.jna.ByRef.CloseablePointerByReference;
import org.miaixz.bus.health.mac.jna.IOKit;
import org.miaixz.bus.health.mac.jna.IOKit.SMCKeyData;
import org.miaixz.bus.health.mac.jna.IOKit.SMCKeyDataKeyInfo;
import org.miaixz.bus.health.mac.jna.IOKit.SMCVal;
import org.miaixz.bus.health.mac.jna.SystemB;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.NativeLong;
import com.sun.jna.platform.mac.IOKit.IOConnect;
import com.sun.jna.platform.mac.IOKit.IOService;
import com.sun.jna.platform.mac.IOKitUtil;

/**
 * Provides access to SMC calls on macOS
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class SmcKit {

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
     * SMC command to read bytes.
     */
    public static final byte SMC_CMD_READ_BYTES = 5;
    /**
     * SMC command to read key information.
     */
    public static final byte SMC_CMD_READ_KEYINFO = 9;
    /**
     * Kernel index for SMC.
     */
    public static final int KERNEL_INDEX_SMC = 2;
    /**
     * Instance of IOKit.
     */
    private static final IOKit IO = IOKit.INSTANCE;
    /**
     * Byte array used for matching SP78 return type.
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
     * Thread-safe map for caching key information necessary for subsequent calls.
     */
    private static final Map<Integer, SMCKeyDataKeyInfo> keyInfoCache = new ConcurrentHashMap<>();

    /**
     * Private constructor to prevent instantiation.
     */
    private SmcKit() {
    }

    /**
     * Opens a connection to the SMC (System Management Controller).
     *
     * @return The {@link IOConnect} object if the connection is successful, or {@code null} if an error occurs.
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
                            String.format(
                                    Locale.ROOT,
                                    "Unable to open connection to AppleSMC service. Error: 0x%08x",
                                    result));
                }
            } finally {
                smcService.release();
            }
        } else {
            Logger.error("Unable to locate AppleSMC service");
        }
        return null;
    }

    /**
     * Closes an open connection to the SMC.
     *
     * @param conn The {@link IOConnect} object representing the open connection.
     * @return 0 if the connection is successfully closed, a nonzero value if an error occurs.
     */
    public static int smcClose(IOConnect conn) {
        return IO.IOServiceClose(conn);
    }

    /**
     * Retrieves a floating-point value from the SMC for a given key. This method handles various floating-point data
     * types including SP78, FPE2, and FLT.
     *
     * @param conn The {@link IOConnect} object representing the open connection to the SMC.
     * @param key  The SMC key (e.g., "TC0P" for CPU temperature) to retrieve the value for.
     * @return A double representing the retrieved value. Returns 0.0 if the read fails or the data type is unknown.
     */
    public static double smcGetFloat(IOConnect conn, String key) {
        try (SMCVal val = new SMCVal()) {
            int result = smcReadKey(conn, key, val);
            if (result == 0 && val.dataSize > 0) {
                if (Arrays.equals(val.dataType, DATATYPE_SP78) && val.dataSize == 2) {
                    // First bit is sign, next 7 bits are integer portion, last 8 bits are
                    // fractional portion
                    return val.bytes[0] + val.bytes[1] / 256d;
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
     * Retrieves a 64-bit integer value from the SMC for a given key.
     *
     * @param conn The {@link IOConnect} object representing the open connection to the SMC.
     * @param key  The SMC key to retrieve the value for.
     * @return A long representing the retrieved value. Returns 0 if the read fails.
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
     * Retrieves cached key information for an SMC key, or generates it if not present in the cache.
     *
     * @param conn            The {@link IOConnect} object representing the open connection to the SMC.
     * @param inputStructure  The {@link SMCKeyData} input structure containing the key to query.
     * @param outputStructure The {@link SMCKeyData} output structure to populate with key information.
     * @return 0 if successful, a nonzero value if an error occurs.
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
     * Reads the value associated with an SMC key.
     *
     * @param conn The {@link IOConnect} object representing the open connection to the SMC.
     * @param key  The SMC key to read.
     * @param val  The {@link SMCVal} structure to receive the result.
     * @return 0 if successful, a nonzero value if an error occurs.
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
     * Makes a call to the SMC.
     *
     * @param conn            The {@link IOConnect} object representing the open connection to the SMC.
     * @param index           The kernel index for the SMC call.
     * @param inputStructure  The {@link SMCKeyData} input structure for the call.
     * @param outputStructure The {@link SMCKeyData} output structure to receive the result.
     * @return 0 if successful, a nonzero value if an error occurs.
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
