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
package org.miaixz.bus.health;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.platform.unix.LibCAPI;

/**
 * General utility methods.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class Builder {

    /**
     * Unix epoch time, used as a default when WMI DateTime queries return no value.
     */
    public static final OffsetDateTime UNIX_EPOCH = OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);

    /**
     * Wildcard pattern prefix: glob
     */
    private static final String GLOB_PREFIX = "glob:";

    /**
     * Regex pattern prefix: regex
     */
    private static final String REGEX_PREFIX = "regex:";

    /**
     * Log message: Reading file
     */
    private static final String READING_LOG = "Reading file {}";

    /**
     * Log message: Content read
     */
    private static final String READ_LOG = "Read {}";

    /**
     * Tests if a string matches a wildcard pattern.
     *
     * @param text    The string to test.
     * @param pattern The pattern string, with {@code ?} matching a single character and {@code *} matching any number
     *                of characters. If the first character of the pattern is {@code ^}, the remaining characters are
     *                tested and the opposite result is returned.
     * @return {@code true} if the string matches, or if the pattern starts with {@code ^} and the remainder does not
     *         match.
     */
    public static boolean wildcardMatch(String text, String pattern) {
        if (!pattern.isEmpty() && pattern.charAt(0) == '^') {
            return !wildcardMatch(text, pattern.substring(1));
        }
        return text.matches(pattern.replace("?", ".?").replace(Symbol.STAR, ".*?"));
    }

    /**
     * If the given pointer is an instance of the Memory class, calls its close method to free the natively allocated
     * memory.
     *
     * @param p The pointer.
     */
    public static void freeMemory(Pointer p) {
        if (p instanceof Memory) {
            ((Memory) p).close();
        }
    }

    /**
     * Tests if a user's session on a device is valid.
     *
     * @param user      The logged-in user.
     * @param device    The device the user is using.
     * @param loginTime The user's login time.
     * @return {@code true} if the session is valid; {@code false} if the user or device is empty, or if the login time
     *         is less than 0 or greater than the current time.
     */
    public static boolean isSessionValid(String user, String device, Long loginTime) {
        return !(user.isEmpty() || device.isEmpty() || loginTime < 0 || loginTime > System.currentTimeMillis());
    }

    /**
     * Determines if a file store (identified by {@code path} and {@code volume}) should be excluded based on
     * configuration.
     * <p>
     * Inclusions take precedence over exclusions. If no exclude/include patterns are specified, the file store is not
     * excluded.
     *
     * @param path           The mount point of the file store.
     * @param volume         The file store volume.
     * @param pathIncludes   A list of path inclusion patterns.
     * @param pathExcludes   A list of path exclusion patterns.
     * @param volumeIncludes A list of volume inclusion patterns.
     * @param volumeExcludes A list of volume exclusion patterns.
     * @return {@code true} if the file store should be excluded, {@code false} otherwise.
     */
    public static boolean isFileStoreExcluded(
            String path,
            String volume,
            List<PathMatcher> pathIncludes,
            List<PathMatcher> pathExcludes,
            List<PathMatcher> volumeIncludes,
            List<PathMatcher> volumeExcludes) {
        Path p = Paths.get(path);
        Path v = Paths.get(volume);
        if (matches(p, pathIncludes) || matches(v, volumeIncludes)) {
            return false;
        }
        return matches(p, pathExcludes) || matches(v, volumeExcludes);
    }

    /**
     * Loads and parses file system include/exclude lines from configuration.
     *
     * @param configPropertyName The name of the configuration property containing the lines to parse.
     * @return A list of {@link PathMatcher} for matching file store volumes and paths.
     */
    public static List<PathMatcher> loadAndParseFileSystemConfig(String configPropertyName) {
        String config = Config.get(configPropertyName, Normal.EMPTY);
        return parseFileSystemConfig(config);
    }

    /**
     * Parses file system include/exclude lines.
     *
     * @param config The configuration line to parse.
     * @return A list of {@link PathMatcher} for matching file store volumes and paths.
     */
    public static List<PathMatcher> parseFileSystemConfig(String config) {
        FileSystem fs = FileSystems.getDefault(); // Not closable
        List<PathMatcher> patterns = new ArrayList<>();
        for (String item : config.split(Symbol.COMMA)) {
            if (item.length() > 0) {
                // Default to glob: prefix unless user specified glob or regex
                if (!(item.startsWith(GLOB_PREFIX) || item.startsWith(REGEX_PREFIX))) {
                    item = GLOB_PREFIX + item;
                }
                patterns.add(fs.getPathMatcher(item));
            }
        }
        return patterns;
    }

    /**
     * Checks if {@code text} matches any of the patterns in {@code patterns}.
     *
     * @param text     The text to match.
     * @param patterns The list of patterns.
     * @return {@code true} if the given text matches at least one glob pattern, {@code false} otherwise.
     * @see <a href="https://en.wikipedia.org/wiki/Glob_(programming)">Wikipedia - glob (programming)</a>
     */
    public static boolean matches(Path text, List<PathMatcher> patterns) {
        for (PathMatcher pattern : patterns) {
            if (pattern.matches(text)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the manufacturer ID from bytes 8 and 9, which represent (up to) 3 five-bit characters.
     *
     * @param edid The EDID byte array.
     * @return The manufacturer ID.
     */
    public static String getManufacturerID(byte[] edid) {
        // Bytes 8-9 are manufacturer ID, 3 5-bit characters
        String temp = String.format(
                Locale.ROOT,
                "%8s%8s",
                Integer.toBinaryString(edid[8] & 0xFF),
                Integer.toBinaryString(edid[9] & 0xFF)).replace(Symbol.C_SPACE, '0');
        Logger.debug("Manufacurer ID: {}", temp);
        return String.format(
                Locale.ROOT,
                "%s%s%s",
                (char) (64 + Integer.parseInt(temp.substring(1, 6), 2)),
                (char) (64 + Integer.parseInt(temp.substring(6, 11), 2)),
                (char) (64 + Integer.parseInt(temp.substring(11, 16), 2))).replace("@", "");
    }

    /**
     * Gets the product ID, bytes 10 and 11.
     *
     * @param edid The EDID byte array.
     * @return The product ID.
     */
    public static String getProductID(byte[] edid) {
        // Bytes 10-11 are product ID, in hex characters
        return Integer.toHexString(
                ByteBuffer.wrap(Arrays.copyOfRange(edid, 10, 12)).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xffff);
    }

    /**
     * Gets the serial number, bytes 12-15.
     *
     * @param edid The EDID byte array.
     * @return A 4-character string if all 4 bytes represent alphanumeric characters, otherwise a hex string.
     */
    public static String getSerialNo(byte[] edid) {
        // Bytes 12-15 are serial number (last 4 chars)
        if (Logger.isDebugEnabled()) {
            Logger.debug("Serial number: {}", Arrays.toString(Arrays.copyOfRange(edid, 12, 16)));
        }
        return String.format(
                Locale.ROOT,
                "%s%s%s%s",
                getAlphaNumericOrHex(edid[15]),
                getAlphaNumericOrHex(edid[14]),
                getAlphaNumericOrHex(edid[13]),
                getAlphaNumericOrHex(edid[12]));
    }

    /**
     * Converts a byte to an alphanumeric character or its hex representation.
     *
     * @param b The byte to convert.
     * @return The corresponding character if the byte is a letter or digit; otherwise, a two-digit hex string.
     */
    private static String getAlphaNumericOrHex(byte b) {
        return Character.isLetterOrDigit((char) b) ? String.format(Locale.ROOT, "%s", (char) b)
                : String.format(Locale.ROOT, "%02X", b);
    }

    /**
     * Returns the week of manufacture.
     *
     * @param edid The EDID byte array.
     * @return The week of manufacture.
     */
    public static byte getWeek(byte[] edid) {
        // Byte 16 is week of manufacture
        return edid[16];
    }

    /**
     * Returns the year of manufacture.
     *
     * @param edid The EDID byte array.
     * @return The year of manufacture.
     */
    public static int getYear(byte[] edid) {
        // Byte 17 is year of manufacture minus 1990
        byte temp = edid[17];
        Logger.debug("Year-1990: {}", temp);
        return temp + 1990;
    }

    /**
     * Returns the EDID version.
     *
     * @param edid The EDID byte array.
     * @return The EDID version.
     */
    public static String getVersion(byte[] edid) {
        // Bytes 18-19 are EDID version
        return edid[18] + "." + edid[19];
    }

    /**
     * Tests if this EDID is for a digital monitor, based on byte 20.
     *
     * @param edid The EDID byte array.
     * @return {@code true} if the EDID indicates a digital monitor, {@code false} otherwise.
     */
    public static boolean isDigital(byte[] edid) {
        // Byte 20 is video input parameter
        return 1 == (edid[20] & 0xff) >> 7;
    }

    /**
     * Gets the monitor width in centimeters.
     *
     * @param edid The EDID byte array.
     * @return The monitor width in centimeters.
     */
    public static int getHcm(byte[] edid) {
        // Byte 21 is horizontal size in cm
        return edid[21];
    }

    /**
     * Gets the monitor height in centimeters.
     *
     * @param edid The EDID byte array.
     * @return The monitor height in centimeters.
     */
    public static int getVcm(byte[] edid) {
        // Byte 22 is vertical size in cm
        return edid[22];
    }

    /**
     * Gets the VESA descriptors.
     *
     * @param edid The EDID byte array.
     * @return A two-dimensional array containing four 18-byte elements, representing the VESA descriptors.
     */
    public static byte[][] getDescriptors(byte[] edid) {
        byte[][] desc = new byte[4][18];
        for (int i = 0; i < desc.length; i++) {
            System.arraycopy(edid, 54 + 18 * i, desc[i], 0, 18);
        }
        return desc;
    }

    /**
     * Gets the VESA descriptor type.
     *
     * @param desc An 18-byte VESA descriptor.
     * @return An integer representing the first four bytes of the VESA descriptor.
     */
    public static int getDescriptorType(byte[] desc) {
        return ByteBuffer.wrap(Arrays.copyOfRange(desc, 0, 4)).getInt();
    }

    /**
     * Parses a detailed timing descriptor.
     *
     * @param desc An 18-byte VESA descriptor.
     * @return A string describing the detailed timing descriptor portion.
     */
    public static String getTimingDescriptor(byte[] desc) {
        int clock = ByteBuffer.wrap(Arrays.copyOfRange(desc, 0, 2)).order(ByteOrder.LITTLE_ENDIAN).getShort() / 100;
        int hActive = (desc[2] & 0xff) + ((desc[4] & 0xf0) << 4);
        int vActive = (desc[5] & 0xff) + ((desc[7] & 0xf0) << 4);
        return String.format(Locale.ROOT, "Clock %dMHz, Active Pixels %dx%d ", clock, hActive, vActive);
    }

    /**
     * Parses descriptor range limits.
     *
     * @param desc An 18-byte VESA descriptor.
     * @return A string describing the range limits portion.
     */
    public static String getDescriptorRangeLimits(byte[] desc) {
        return String.format(
                Locale.ROOT,
                "Field Rate %d-%d Hz vertical, %d-%d Hz horizontal, Max clock: %d MHz",
                desc[5],
                desc[6],
                desc[7],
                desc[8],
                desc[9] * 10);
    }

    /**
     * Parses descriptor text.
     *
     * @param desc An 18-byte VESA descriptor.
     * @return The plain text starting from the 4th byte.
     */
    public static String getDescriptorText(byte[] desc) {
        return new String(Arrays.copyOfRange(desc, 4, 18), Charset.US_ASCII).trim();
    }

    /**
     * Retrieves the preferred resolution of the monitor (e.g., 1920x1080).
     * <p>
     * This method parses the Detailed Timing Descriptor (DTD) section within the EDID (Extended Display Identification
     * Data) byte array to extract and calculate the preferred resolution of the monitor.
     *
     * @param edid The EDID byte array, containing hardware information about the monitor.
     * @return A string representation of the preferred resolution, in the format "horizontalResolution x
     *         verticalResolution".
     */
    public static String getPreferredResolution(byte[] edid) {
        int dtd = 54;
        int horizontalRes = (edid[dtd + 4] & 0xF0) << 4 | edid[dtd + 2] & 0xFF;
        int verticalRes = (edid[dtd + 7] & 0xF0) << 4 | edid[dtd + 5] & 0xFF;
        return horizontalRes + "x" + verticalRes;
    }

    /**
     * Retrieves the monitor model from the EDID.
     *
     * @param edid The EDID byte array.
     * @return The plain text monitor model.
     */
    public static String getModel(byte[] edid) {
        byte[][] desc = getDescriptors(edid);
        String model = null;
        for (byte[] b : desc) {
            if (getDescriptorType(b) == 0xfc) {
                model = getDescriptorText(b);
                break;
            }
        }
        assert model != null;
        String[] tokens = model.split("\\s+");
        if (tokens.length >= 1) {
            model = tokens[tokens.length - 1];
        }
        return model.trim();
    }

    /**
     * Parses an EDID byte array into human-readable information.
     *
     * @param edid The EDID byte array.
     * @return Human-readable text representing the EDID.
     */
    public static String getEdid(byte[] edid) {
        StringBuilder sb = new StringBuilder();
        sb.append("  Manuf. ID=").append(getManufacturerID(edid));
        sb.append(", Product ID=").append(getProductID(edid));
        sb.append(", ").append(isDigital(edid) ? "Digital" : "Analog");
        sb.append(", Serial=").append(getSerialNo(edid));
        sb.append(", ManufDate=").append(getWeek(edid) * 12 / 52 + 1).append('/').append(getYear(edid));
        sb.append(", EDID v").append(getVersion(edid));
        int hSize = getHcm(edid);
        int vSize = getVcm(edid);
        sb.append(
                String.format(
                        Locale.ROOT,
                        "%n  %d x %d cm (%.1f x %.1f in)",
                        hSize,
                        vSize,
                        hSize / 2.54,
                        vSize / 2.54));
        byte[][] desc = getDescriptors(edid);
        for (byte[] b : desc) {
            switch (getDescriptorType(b)) {
                case 0xff:
                    sb.append("\n  Serial Number: ").append(getDescriptorText(b));
                    break;

                case 0xfe:
                    sb.append("\n  Unspecified Text: ").append(getDescriptorText(b));
                    break;

                case 0xfd:
                    sb.append("\n  Range Limits: ").append(getDescriptorRangeLimits(b));
                    break;

                case 0xfc:
                    sb.append("\n  Monitor Name: ").append(getDescriptorText(b));
                    break;

                case 0xfb:
                    sb.append("\n  White Point Data: ").append(ByteKit.byteArrayToHexString(b));
                    break;

                case 0xfa:
                    sb.append("\n  Standard Timing ID: ").append(ByteKit.byteArrayToHexString(b));
                    break;

                default:
                    if (getDescriptorType(b) <= 0x0f && getDescriptorType(b) >= 0x00) {
                        sb.append("\n  Manufacturer Data: ").append(ByteKit.byteArrayToHexString(b));
                    } else {
                        sb.append("\n  Preferred Timing: ").append(getTimingDescriptor(b));
                    }
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * Reads an entire file at once. Primarily used for Linux /proc filesystem to avoid recalculating file contents when
     * iterating through reads.
     *
     * @param filename The file to read.
     * @return A list of strings representing each line of the file, or an empty list if the file cannot be read or is
     *         empty.
     */
    public static List<String> readFile(String filename) {
        return readFile(filename, true);
    }

    /**
     * Reads an entire file at once. Primarily used for Linux /proc filesystem to avoid recalculating file contents when
     * iterating through reads.
     *
     * @param filename    The file to read.
     * @param reportError Whether to log errors when reading the file.
     * @return A list of strings representing each line of the file, or an empty list if the file cannot be read or is
     *         empty.
     */
    public static List<String> readFile(String filename, boolean reportError) {
        Path path = Paths.get(filename);
        if (Files.isReadable(path)) {
            if (Logger.isDebugEnabled()) {
                Logger.debug(READING_LOG, filename);
            }
            try {
                return Files.readAllLines(path, Charset.UTF_8);
            } catch (IOException e) {
                if (reportError) {
                    Logger.error("Error reading file {}. {}", filename, e.getMessage());
                } else {
                    Logger.debug("Error reading file {}. {}", filename, e.getMessage());
                }
            }
        } else if (reportError) {
            Logger.warn("File not found or not readable: {}", filename);
        }
        return Collections.emptyList();
    }

    /**
     * Reads a specified number of lines from a file. Primarily used for Linux /proc filesystem to avoid recalculating
     * file contents when iterating through reads.
     *
     * @param filename The file to read.
     * @param count    The number of lines to read.
     * @return A list of strings representing the first {@code count} lines of the file, or an empty list if the file
     *         cannot be read or is empty.
     */
    public static List<String> readLines(String filename, int count) {
        return readLines(filename, count, true);
    }

    /**
     * Reads a specified number of lines from a file. Primarily used for Linux /proc filesystem to avoid recalculating
     * file contents when iterating through reads.
     *
     * @param filename    The file to read.
     * @param count       The number of lines to read.
     * @param reportError Whether to log errors when reading the file.
     * @return A list of strings representing the first {@code count} lines of the file, or an empty list if the file
     *         cannot be read or is empty.
     */
    public static List<String> readLines(String filename, int count, boolean reportError) {
        Path file = Paths.get(filename);
        if (Files.isReadable(file)) {
            if (Logger.isDebugEnabled()) {
                Logger.debug(READING_LOG, filename);
            }
            try (BufferedReader reader = Files.newBufferedReader(file, Charset.UTF_8)) {
                List<String> lines = new ArrayList<>(count);
                for (int i = 0; i < count; ++i) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    lines.add(line);
                }
                return lines;
            } catch (IOException e) {
                if (reportError) {
                    Logger.error("Error reading file {}. {}", filename, e.getMessage());
                } else {
                    Logger.debug("Error reading file {}. {}", filename, e.getMessage());
                }
            }
        } else if (reportError) {
            Logger.warn("File not found or not readable: {}", filename);
        }
        return Collections.emptyList();
    }

    /**
     * Reads an entire file at once. Primarily used for Linux /proc filesystem to avoid recalculating file contents when
     * iterating through reads.
     *
     * @param filename The file to read.
     * @return A byte array representing the file.
     */
    public static byte[] readAllBytes(String filename) {
        return readAllBytes(filename, true);
    }

    /**
     * Reads an entire file at once. Primarily used for Linux /proc filesystem to avoid recalculating file contents when
     * iterating through reads.
     *
     * @param filename    The file to read.
     * @param reportError Whether to log errors when reading the file.
     * @return A byte array representing the file.
     */
    public static byte[] readAllBytes(String filename, boolean reportError) {
        Path path = Paths.get(filename);
        if (Files.isReadable(path)) {
            if (Logger.isDebugEnabled()) {
                Logger.debug(READING_LOG, filename);
            }
            try {
                return Files.readAllBytes(path);
            } catch (IOException e) {
                if (reportError) {
                    Logger.error("Error reading file {}. {}", filename, e.getMessage());
                } else {
                    Logger.debug("Error reading file {}. {}", filename, e.getMessage());
                }
            }
        } else if (reportError) {
            Logger.warn("File not found or not readable: {}", filename);
        }
        return new byte[0];
    }

    /**
     * Reads an entire file at once. Primarily used for Unix /proc binary files to avoid recalculating file contents
     * when iterating through reads.
     *
     * @param filename The file to read.
     * @return A {@link ByteBuffer} representing the file if read successfully; otherwise, {@code null}.
     */
    public static ByteBuffer readAllBytesAsBuffer(String filename) {
        byte[] bytes = readAllBytes(filename, false);
        ByteBuffer buff = ByteBuffer.allocate(bytes.length);
        buff.order(ByteOrder.nativeOrder());
        for (byte b : bytes) {
            buff.put(b);
        }
        buff.flip();
        return buff;
    }

    /**
     * Reads a byte value from a {@link ByteBuffer}.
     *
     * @param buff The {@link ByteBuffer} to read from.
     * @return The next byte value.
     */
    public static byte readByteFromBuffer(ByteBuffer buff) {
        if (buff.position() < buff.limit()) {
            return buff.get();
        }
        return 0;
    }

    /**
     * Reads a short integer value from a {@link ByteBuffer}.
     *
     * @param buff The {@link ByteBuffer} to read from.
     * @return The next short integer value.
     */
    public static short readShortFromBuffer(ByteBuffer buff) {
        if (buff.position() <= buff.limit() - 2) {
            return buff.getShort();
        }
        return 0;
    }

    /**
     * Reads an integer value from a {@link ByteBuffer}.
     *
     * @param buff The {@link ByteBuffer} to read from.
     * @return The next integer value.
     */
    public static int readIntFromBuffer(ByteBuffer buff) {
        if (buff.position() <= buff.limit() - 4) {
            return buff.getInt();
        }
        return 0;
    }

    /**
     * Reads a long integer value from a {@link ByteBuffer}.
     *
     * @param buff The {@link ByteBuffer} to read from.
     * @return The next long integer value.
     */
    public static long readLongFromBuffer(ByteBuffer buff) {
        if (buff.position() <= buff.limit() - 8) {
            return buff.getLong();
        }
        return 0L;
    }

    /**
     * Reads a {@link NativeLong} value from a {@link ByteBuffer}.
     *
     * @param buff The {@link ByteBuffer} to read from.
     * @return The next {@link NativeLong} value.
     */
    public static NativeLong readNativeLongFromBuffer(ByteBuffer buff) {
        return new NativeLong(Native.LONG_SIZE == 4 ? readIntFromBuffer(buff) : readLongFromBuffer(buff));
    }

    /**
     * Reads a size_t value from a {@link ByteBuffer}.
     *
     * @param buff The {@link ByteBuffer} to read from.
     * @return The next size_t value.
     */
    public static LibCAPI.size_t readSizeTFromBuffer(ByteBuffer buff) {
        return new LibCAPI.size_t(Native.SIZE_T_SIZE == 4 ? readIntFromBuffer(buff) : readLongFromBuffer(buff));
    }

    /**
     * Reads a byte array value from a {@link ByteBuffer}.
     *
     * @param buff  The {@link ByteBuffer} to read from.
     * @param array The target array to read data into.
     */
    public static void readByteArrayFromBuffer(ByteBuffer buff, byte[] array) {
        if (buff.position() <= buff.limit() - array.length) {
            buff.get(array);
        }
    }

    /**
     * Reads a {@link Pointer} value from a {@link ByteBuffer}.
     *
     * @param buff The {@link ByteBuffer} to read from.
     * @return The next {@link Pointer} value.
     */
    public static Pointer readPointerFromBuffer(ByteBuffer buff) {
        if (buff.position() <= buff.limit() - Native.POINTER_SIZE) {
            return Native.POINTER_SIZE == 4 ? new Pointer(buff.getInt()) : new Pointer(buff.getLong());
        }
        return Pointer.NULL;
    }

    /**
     * Reads a file and returns the long integer value contained within it. Primarily used for Linux /sys filesystem.
     *
     * @param filename The file to read.
     * @return The value contained in the file, or 0 if no value is present.
     */
    public static long getLongFromFile(String filename) {
        if (Logger.isDebugEnabled()) {
            Logger.debug(READING_LOG, filename);
        }
        List<String> read = readLines(filename, 1, false);
        if (!read.isEmpty()) {
            if (Logger.isTraceEnabled()) {
                Logger.trace(READ_LOG, read.get(0));
            }
            return Parsing.parseLongOrDefault(read.get(0), 0L);
        }
        return 0L;
    }

    /**
     * Reads a file and returns the unsigned long integer value (as a long) contained within it. Primarily used for
     * Linux /sys filesystem.
     *
     * @param filename The file to read.
     * @return The value contained in the file, or 0 if no value is present.
     */
    public static long getUnsignedLongFromFile(String filename) {
        if (Logger.isDebugEnabled()) {
            Logger.debug(READING_LOG, filename);
        }
        List<String> read = readLines(filename, 1, false);
        if (!read.isEmpty()) {
            if (Logger.isTraceEnabled()) {
                Logger.trace(READ_LOG, read.get(0));
            }
            return Parsing.parseUnsignedLongOrDefault(read.get(0), 0L);
        }
        return 0L;
    }

    /**
     * Reads a file and returns the integer value contained within it. Primarily used for Linux /sys filesystem.
     *
     * @param filename The file to read.
     * @return The value contained in the file, or 0 if no value is present.
     */
    public static int getIntFromFile(String filename) {
        if (Logger.isDebugEnabled()) {
            Logger.debug(READING_LOG, filename);
        }
        try {
            List<String> read = readLines(filename, 1, false);
            if (!read.isEmpty()) {
                if (Logger.isTraceEnabled()) {
                    Logger.trace(READ_LOG, read.get(0));
                }
                return Parsing.parseIntOrDefault(read.get(0), 0);
            }
        } catch (NumberFormatException ex) {
            Logger.warn("Unable to read value from {}. {}", filename, ex.getMessage());
        }
        return 0;
    }

    /**
     * Reads a file and returns the string value contained within it. Primarily used for Linux /sys filesystem.
     *
     * @param filename The file to read.
     * @return The value contained in the file, or an empty string if no value is present.
     */
    public static String getStringFromFile(String filename) {
        if (Logger.isDebugEnabled()) {
            Logger.debug(READING_LOG, filename);
        }
        List<String> read = readLines(filename, 1, false);
        if (!read.isEmpty()) {
            if (Logger.isTraceEnabled()) {
                Logger.trace(READ_LOG, read.get(0));
            }
            return read.get(0);
        }
        return Normal.EMPTY;
    }

    /**
     * Reads a file and returns a map of string key-value pairs contained within it. Primarily used for Linux
     * {@code /proc/[pid]} files to provide more detailed or accurate information than the API.
     *
     * @param filename  The file to read.
     * @param separator The character that separates keys and values in each line of the file.
     * @return A map of key-value pairs from the file, with values trimmed of whitespace. Returns an empty map if no
     *         key-value pairs can be parsed.
     */
    public static Map<String, String> getKeyValueMapFromFile(String filename, String separator) {
        Map<String, String> map = new HashMap<>();
        if (Logger.isDebugEnabled()) {
            Logger.debug(READING_LOG, filename);
        }
        List<String> lines = readFile(filename, false);
        for (String line : lines) {
            String[] parts = line.split(separator);
            if (parts.length == 2) {
                map.put(parts[0], parts[1].trim());
            }
        }
        return map;
    }

    /**
     * Reads the target of a symbolic link.
     *
     * @param file The file to read.
     * @return The symbolic link name, or {@code null} if the read fails.
     */
    public static String readSymlinkTarget(File file) {
        try {
            return Files.readSymbolicLink(Paths.get(file.getAbsolutePath())).toString();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Appends a key-value pair to a {@code StringBuilder}.
     *
     * @param builder The {@code StringBuilder} object.
     * @param caption The caption (key).
     * @param value   The value.
     */
    public static void append(StringBuilder builder, String caption, Object value) {
        builder.append(caption).append(ObjectKit.defaultIfNull(Convert.toString(value), "[n/a]")).append(Symbol.LF);
    }

}
