/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org OSHI and other contributors.               ~
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
package org.miaixz.bus.health.windows;

import static com.sun.jna.platform.win32.WinError.ERROR_SUCCESS;
import static com.sun.jna.platform.win32.WinNT.KEY_READ;

import java.util.Objects;

import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg.HKEY;

/**
 * Utility class for reading data from the Windows Registry.
 * <p>
 * This class provides methods to retrieve Long and String values from the registry, handling various data types
 * (REG_DWORD, REG_SZ, REG_BINARY) and converting them to appropriate Java types. It also includes logic to interpret
 * registry integers as timestamps if they fall within a reasonable range.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class RegistryKit {

    /**
     * Constant for 30 years in seconds. Used for validating timestamps.
     */
    private static final long THIRTY_YEARS_IN_SECS = 30L * 365 * 24 * 60 * 60;

    /**
     * Instance of the Advapi32 library for direct registry access.
     */
    private static final Advapi32 ADV = Advapi32.INSTANCE;

    private RegistryKit() {

    }

    /**
     * Retrieves a registry value as a {@code long}. (No extra access flags)
     * <p>
     * Currently supports converting Registry types REG_SZ (String) and REG_DWORD (Integer) to long.
     * </p>
     *
     * @param root The root HKEY (e.g., HKEY_LOCAL_MACHINE).
     * @param path The registry path.
     * @param key  The registry key name.
     * @return The value as a {@code long}, or 0L if the key does not exist or an error occurs.
     */
    public static long getLongValue(HKEY root, String path, String key) {
        try {
            Object val = Advapi32Util.registryGetValue(root, path, key);
            return registryValueToLong(val);
        } catch (Win32Exception e) {
            Logger.trace("Unable to access " + path + ": " + e.getMessage());
        }
        return 0L;
    }

    /**
     * Retrieves a registry value as a {@code long} with specific access flags.
     * <p>
     * Currently supports converting Registry types REG_SZ (String) and REG_DWORD (Integer) to long.
     * </p>
     *
     * @param root       The root HKEY (e.g., HKEY_LOCAL_MACHINE).
     * @param path       The registry path.
     * @param key        The registry key name.
     * @param accessFlag Extra access flags (e.g., {@link com.sun.jna.platform.win32.WinNT#KEY_WOW64_64KEY}).
     * @return The value as a {@code long}, or 0L if the key does not exist or an error occurs.
     */
    public static long getLongValue(HKEY root, String path, String key, int accessFlag) {
        Object val = getRegistryValueOrNull(root, path, key, accessFlag);
        return registryValueToLong(val);
    }

    /**
     * Retrieves a raw registry value as an Object, handling the opening and closing of the registry key.
     *
     * @param root       The root HKEY.
     * @param path       The registry path.
     * @param key        The registry key name.
     * @param accessFlag Access flags to use when opening the registry key (combined with KEY_READ).
     * @return The registry value as an Object, or {@code null} if the key is not found or an error occurs.
     */
    public static Object getRegistryValueOrNull(HKEY root, String path, String key, int accessFlag) {
        HKEY hKey = null;
        try {
            hKey = Advapi32Util.registryGetKey(root, path, KEY_READ | accessFlag).getValue();
            Object value = Advapi32Util.registryGetValue(root, path, key);
            return Objects.isNull(value) ? null : value;
        } catch (Win32Exception e) {
            Logger.trace("Unable to access " + path + " with flag " + accessFlag + ": " + e.getMessage());
        } finally {
            if (hKey != null) {
                int rc = ADV.RegCloseKey(hKey);
                if (rc != ERROR_SUCCESS) {
                    throw new Win32Exception(rc);
                }
            }
        }
        return null;
    }

    /**
     * Converts a raw registry value object to a {@code long}.
     * <p>
     * Supports {@code Integer} and {@code String} (date) inputs. If the value is an {@code Integer} and appears to be a
     * Unix timestamp (seconds) within the last 30 years, it is converted to milliseconds.
     * </p>
     *
     * @param val The registry value object.
     * @return The converted long value, or 0L if conversion fails or input is null.
     */
    private static long registryValueToLong(Object val) {
        if (val == null) {
            return 0L;
        }

        // Calculate reasonable timestamp bounds (current time to 30 years ago)
        long currentTimeSecs = System.currentTimeMillis() / 1000L;
        long minSaneTimestamp = currentTimeSecs - THIRTY_YEARS_IN_SECS;
        if (val instanceof Integer) {
            int value = (Integer) val;
            if (value > minSaneTimestamp && value < currentTimeSecs) {
                return value * 1000L;
            }
            return value;
        } else if (val instanceof String) {
            String dateStr = ((String) val).trim();
            // Try yyyyMMdd first
            long epoch = Parsing.parseDateToEpoch(dateStr, "yyyyMMdd");
            if (epoch == 0) {
                // If that fails, try MM/dd/yyyy
                epoch = Parsing.parseDateToEpoch(dateStr, "MM/dd/yyyy");
            }
            return epoch;
        }
        return 0L;
    }

    /**
     * Retrieves a registry value as a {@code String}. (No extra access flags)
     * <p>
     * Currently supports converting Registry types REG_SZ (String) and REG_BINARY (byte[]) to String.
     * </p>
     *
     * @param root The root HKEY.
     * @param path The registry path.
     * @param key  The registry key name.
     * @return The value as a {@code String}, or {@code null} if the key does not exist or an error occurs.
     */
    public static String getStringValue(HKEY root, String path, String key) {
        try {
            Object val = Advapi32Util.registryGetValue(root, path, key);
            return registryValueToString(val);
        } catch (Win32Exception e) {
            Logger.trace("Unable to access " + path + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves a registry value as a {@code String} with specific access flags.
     * <p>
     * Currently supports converting Registry types REG_SZ (String) and REG_BINARY (byte[]) to String.
     * </p>
     *
     * @param root       The root HKEY.
     * @param path       The registry path.
     * @param key        The registry key name.
     * @param accessFlag Extra access flags (e.g., {@link com.sun.jna.platform.win32.WinNT#KEY_WOW64_64KEY}).
     * @return The value as a {@code String}, or {@code null} if the key does not exist or an error occurs.
     */
    public static String getStringValue(HKEY root, String path, String key, int accessFlag) {
        Object val = getRegistryValueOrNull(root, path, key, accessFlag);
        return registryValueToString(val);
    }

    /**
     * Decodes a registry value object to a {@code String} using multiple fallback encodings.
     * <p>
     * Handles {@code String} (REG_SZ, REG_EXPAND_SZ) and {@code byte[]} (REG_BINARY) types.
     * </p>
     *
     * @param val The registry value object.
     * @return The decoded String, or {@code null} if input is null or unsupported.
     */
    private static String registryValueToString(Object val) {
        if (val == null) {
            return null;
        }

        // Already a string (REG_SZ or REG_EXPAND_SZ)
        if (val instanceof String) {
            return ((String) val).trim();
        }

        // Handle binary (REG_BINARY)
        if (val instanceof byte[]) {
            return Parsing.decodeBinaryToString((byte[]) val);
        }

        return null;
    }

}
