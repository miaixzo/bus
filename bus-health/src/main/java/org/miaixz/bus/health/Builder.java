/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org OSHI Team and other contributors.          *
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
package org.miaixz.bus.health;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.platform.unix.LibCAPI;
import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.logger.Logger;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.CharsetDecoder;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * General utility methods
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class Builder {

    /**
     * The Unix Epoch, a default value when WMI DateTime queries return no value.
     */
    public static final OffsetDateTime UNIX_EPOCH = OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);

    private static final String GLOB_PREFIX = "glob:";
    private static final String REGEX_PREFIX = "regex:";

    private static final String READING_LOG = "Reading file {}";
    private static final String READ_LOG = "Read {}";

    /**
     * Sleeps for the specified number of milliseconds.
     *
     * @param ms How long to sleep
     */
    public static void sleep(long ms) {
        try {
            Logger.trace("Sleeping for {} ms", ms);
            Thread.sleep(ms);
        } catch (InterruptedException e) { // NOSONAR squid:S2142
            Logger.warn("Interrupted while sleeping for {} ms: {}", ms, e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Tests if a String matches another String with a wildcard pattern.
     *
     * @param text    The String to test
     * @param pattern The String containing a wildcard pattern where ? represents a single character and * represents
     *                any number of characters. If the first character of the pattern is a carat (^) the test is
     *                performed against the remaining characters and the result of the test is the opposite.
     * @return True if the String matches or if the first character is ^ and the remainder of the String does not match.
     */
    public static boolean wildcardMatch(String text, String pattern) {
        if (pattern.length() > 0 && pattern.charAt(0) == Symbol.C_CARET) {
            return !wildcardMatch(text, pattern.substring(1));
        }
        return text.matches(pattern.replace("?", ".?").replace(Symbol.STAR, ".*?"));
    }

    /**
     * If the given Pointer is of class Memory, executes the close method on it to free its native allocation
     *
     * @param p A pointer
     */
    public static void freeMemory(Pointer p) {
        if (p instanceof Memory) {
            ((Memory) p).close();
        }
    }

    /**
     * Tests if session of a user logged in a device is valid or not.
     *
     * @param user      The user logged in
     * @param device    The device used by user
     * @param loginTime The login time of the user
     * @return True if Session is valid or False if the user of device is empty or the login time is lesser than zero or
     * greater than current time.
     */
    public static boolean isSessionValid(String user, String device, Long loginTime) {
        return !(user.isEmpty() || device.isEmpty() || loginTime < 0 || loginTime > System.currentTimeMillis());
    }

    /**
     * Evaluates if file store (identified by {@code path} and {@code volume}) should be excluded or not based on
     * configuration {@code pathIncludes, pathExcludes, volumeIncludes, volumeExcludes}.
     * <p>
     * Inclusion has priority over exclusion. If no exclusion/inclusion pattern is specified, then filestore is not
     * excluded.
     *
     * @param path           Mountpoint of filestore.
     * @param volume         Filestore volume.
     * @param pathIncludes   List of patterns for path inclusions.
     * @param pathExcludes   List of patterns for path exclusions.
     * @param volumeIncludes List of patterns for volume inclusions.
     * @param volumeExcludes List of patterns for volume exclusions.
     * @return {@code true} if file store should be excluded or {@code false} otherwise.
     */
    public static boolean isFileStoreExcluded(String path, String volume, List<PathMatcher> pathIncludes,
                                              List<PathMatcher> pathExcludes, List<PathMatcher> volumeIncludes, List<PathMatcher> volumeExcludes) {
        Path p = Paths.get(path);
        Path v = Paths.get(volume);
        if (matches(p, pathIncludes) || matches(v, volumeIncludes)) {
            return false;
        }
        return matches(p, pathExcludes) || matches(v, volumeExcludes);
    }

    /**
     * Load from config and parse file system include/exclude line.
     *
     * @param configPropertyName The config property containing the line to be parsed.
     * @return List of PathMatchers to be used to match filestore volume and path.
     */
    public static List<PathMatcher> loadAndParseFileSystemConfig(String configPropertyName) {
        String config = Config.get(configPropertyName, Normal.EMPTY);
        return parseFileSystemConfig(config);
    }

    /**
     * Parse file system include/exclude line.
     *
     * @param config The config line to be parsed.
     * @return List of PathMatchers to be used to match filestore volume and path.
     */
    public static List<PathMatcher> parseFileSystemConfig(String config) {
        FileSystem fs = FileSystems.getDefault(); // can't be closed
        List<PathMatcher> patterns = new ArrayList<>();
        for (String item : config.split(",")) {
            if (item.length() > 0) {
                // Using glob: prefix as the defult unless user has specified glob or regex. See
                // https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystem.html#getPathMatcher-java.lang.String-
                if (!(item.startsWith(GLOB_PREFIX) || item.startsWith(REGEX_PREFIX))) {
                    item = GLOB_PREFIX + item;
                }
                patterns.add(fs.getPathMatcher(item));
            }
        }
        return patterns;
    }

    /**
     * Checks if {@code text} matches any of @param patterns}.
     *
     * @param text     The text to be matched.
     * @param patterns List of patterns.
     * @return {@code true} if given text matches at least one glob pattern or {@code false} otherwise.
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
     * Gets the Manufacturer ID from (up to) 3 5-bit characters in bytes 8 and 9
     *
     * @param edid The EDID byte array
     * @return The manufacturer ID
     */
    public static String getManufacturerID(byte[] edid) {
        // Bytes 8-9 are manufacturer ID in 3 5-bit characters.
        String temp = String.format(Locale.ROOT, "%8s%8s", Integer.toBinaryString(edid[8] & 0xFF),
                Integer.toBinaryString(edid[9] & 0xFF)).replace(Symbol.C_SPACE, '0');
        Logger.debug("Manufacurer ID: {}", temp);
        return String.format(Locale.ROOT, "%s%s%s", (char) (64 + Integer.parseInt(temp.substring(1, 6), 2)),
                (char) (64 + Integer.parseInt(temp.substring(7, 11), 2)),
                (char) (64 + Integer.parseInt(temp.substring(12, 16), 2))).replace(Symbol.AT, Normal.EMPTY);
    }

    /**
     * Gets the Product ID, bytes 10 and 11
     *
     * @param edid The EDID byte array
     * @return The product ID
     */
    public static String getProductID(byte[] edid) {
        // Bytes 10-11 are product ID expressed in hex characters
        return Integer.toHexString(
                ByteBuffer.wrap(Arrays.copyOfRange(edid, 10, 12)).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xffff);
    }

    /**
     * Gets the Serial number, bytes 12-15
     *
     * @param edid The EDID byte array
     * @return If all 4 bytes represent alphanumeric characters, a 4-character string, otherwise a hex string.
     */
    public static String getSerialNo(byte[] edid) {
        // Bytes 12-15 are Serial number (last 4 characters)
        if (Logger.isDebug()) {
            Logger.debug("Serial number: {}", Arrays.toString(Arrays.copyOfRange(edid, 12, 16)));
        }
        return String.format(Locale.ROOT, "%s%s%s%s", getAlphaNumericOrHex(edid[15]), getAlphaNumericOrHex(edid[14]),
                getAlphaNumericOrHex(edid[13]), getAlphaNumericOrHex(edid[12]));
    }

    private static String getAlphaNumericOrHex(byte b) {
        return Character.isLetterOrDigit((char) b) ? String.format(Locale.ROOT, "%s", (char) b)
                : String.format(Locale.ROOT, "%02X", b);
    }

    /**
     * Return the week of year of manufacture
     *
     * @param edid The EDID byte array
     * @return The week of year
     */
    public static byte getWeek(byte[] edid) {
        // Byte 16 is manufacture week
        return edid[16];
    }

    /**
     * Return the year of manufacture
     *
     * @param edid The EDID byte array
     * @return The year of manufacture
     */
    public static int getYear(byte[] edid) {
        // Byte 17 is manufacture year-1990
        byte temp = edid[17];
        Logger.debug("Year-1990: {}", temp);
        return temp + 1990;
    }

    /**
     * Return the EDID version
     *
     * @param edid The EDID byte array
     * @return The EDID version
     */
    public static String getVersion(byte[] edid) {
        // Bytes 18-19 are EDID version
        return edid[18] + "." + edid[19];
    }

    /**
     * Test if this EDID is a digital monitor based on byte 20
     *
     * @param edid The EDID byte array
     * @return True if the EDID represents a digital monitor, false otherwise
     */
    public static boolean isDigital(byte[] edid) {
        // Byte 20 is Video input params
        return 1 == (edid[20] & 0xff) >> 7;
    }

    /**
     * Get monitor width in cm
     *
     * @param edid The EDID byte array
     * @return Monitor width in cm
     */
    public static int getHcm(byte[] edid) {
        // Byte 21 is horizontal size in cm
        return edid[21];
    }

    /**
     * Get monitor height in cm
     *
     * @param edid The EDID byte array
     * @return Monitor height in cm
     */
    public static int getVcm(byte[] edid) {
        // Byte 22 is vertical size in cm
        return edid[22];
    }

    /**
     * Get the VESA descriptors
     *
     * @param edid The EDID byte array
     * @return A 2D array with four 18-byte elements representing VESA descriptors
     */
    public static byte[][] getDescriptors(byte[] edid) {
        byte[][] desc = new byte[4][18];
        for (int i = 0; i < desc.length; i++) {
            System.arraycopy(edid, 54 + 18 * i, desc[i], 0, 18);
        }
        return desc;
    }

    /**
     * Get the VESA descriptor type
     *
     * @param desc An 18-byte VESA descriptor
     * @return An integer representing the first four bytes of the VESA descriptor
     */
    public static int getDescriptorType(byte[] desc) {
        return ByteBuffer.wrap(Arrays.copyOfRange(desc, 0, 4)).getInt();
    }

    /**
     * Parse a detailed timing descriptor
     *
     * @param desc An 18-byte VESA descriptor
     * @return A string describing part of the detailed timing descriptor
     */
    public static String getTimingDescriptor(byte[] desc) {
        int clock = ByteBuffer.wrap(Arrays.copyOfRange(desc, 0, 2)).order(ByteOrder.LITTLE_ENDIAN).getShort() / 100;
        int hActive = (desc[2] & 0xff) + ((desc[4] & 0xf0) << 4);
        int vActive = (desc[5] & 0xff) + ((desc[7] & 0xf0) << 4);
        return String.format(Locale.ROOT, "Clock %dMHz, Active Pixels %dx%d ", clock, hActive, vActive);
    }

    /**
     * Parse descriptor range limits
     *
     * @param desc An 18-byte VESA descriptor
     * @return A string describing some of the range limits
     */
    public static String getDescriptorRangeLimits(byte[] desc) {
        return String.format(Locale.ROOT, "Field Rate %d-%d Hz vertical, %d-%d Hz horizontal, Max clock: %d MHz",
                desc[5], desc[6], desc[7], desc[8], desc[9] * 10);
    }

    /**
     * Parse descriptor text
     *
     * @param desc An 18-byte VESA descriptor
     * @return Plain text starting at the 4th byte
     */
    public static String getDescriptorText(byte[] desc) {
        return new String(Arrays.copyOfRange(desc, 4, 18), Charset.US_ASCII).trim();
    }

    /**
     * Parse an EDID byte array into user-readable information
     *
     * @param edid An EDID byte array
     * @return User-readable text represented by the EDID
     */
    public static String getEdid(byte[] edid) {
        StringBuilder sb = new StringBuilder();
        sb.append("  Manuf. ID=").append(getManufacturerID(edid));
        sb.append(", Product ID=").append(getProductID(edid));
        sb.append(", ").append(isDigital(edid) ? "Digital" : "Analog");
        sb.append(", Serial=").append(getSerialNo(edid));
        sb.append(", ManufDate=").append(getWeek(edid) * 12 / 52 + 1).append('/')
                .append(getYear(edid));
        sb.append(", EDID v").append(getVersion(edid));
        int hSize = getHcm(edid);
        int vSize = getVcm(edid);
        sb.append(String.format(Locale.ROOT, "%n  %d x %d cm (%.1f x %.1f in)", hSize, vSize, hSize / 2.54,
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
     * Read an entire file at one time. Intended primarily for Linux /proc filesystem to avoid recalculating file
     * contents on iterative reads.
     *
     * @param filename The file to read
     * @return A list of Strings representing each line of the file, or an empty list if file could not be read or is
     * empty
     */
    public static List<String> readFile(String filename) {
        return readFile(filename, true);
    }

    /**
     * Read an entire file at one time. Intended primarily for Linux /proc filesystem to avoid recalculating file
     * contents on iterative reads.
     *
     * @param filename    The file to read
     * @param reportError Whether to log errors reading the file
     * @return A list of Strings representing each line of the file, or an empty list if file could not be read or is
     * empty
     */
    public static List<String> readFile(String filename, boolean reportError) {
        if (new File(filename).canRead()) {
            if (Logger.isDebug()) {
                Logger.debug(READING_LOG, filename);
            }
            try {
                return Files.readAllLines(Paths.get(filename), Charset.UTF_8);
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
     * Read count lines from a file. Intended primarily for Linux /proc filesystem to avoid recalculating file contents
     * on iterative reads.
     *
     * @param filename The file to read
     * @param count    The number of lines to read
     * @return A list of Strings representing the first count lines of the file, or an empty list if file could not be
     * read or is empty
     */
    public static List<String> readLines(String filename, int count) {
        return readLines(filename, count, true);
    }

    /**
     * Read count lines from a file. Intended primarily for Linux /proc filesystem to avoid recalculating file contents
     * on iterative reads.
     *
     * @param filename    The file to read
     * @param count       The number of lines to read
     * @param reportError Whether to log errors reading the file
     * @return A list of Strings representing the first count lines of the file, or an empty list if file could not be
     * read or is empty
     */
    public static List<String> readLines(String filename, int count, boolean reportError) {
        Path file = Paths.get(filename);
        if (Files.isReadable(file)) {
            if (Logger.isDebug()) {
                Logger.debug(READING_LOG, filename);
            }
            CharsetDecoder decoder = Charset.UTF_8.newDecoder();
            try (Reader isr = new InputStreamReader(Files.newInputStream(file), decoder);
                 BufferedReader reader = new BufferedReader(isr, Normal._1024)) {
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
     * Read an entire file at one time. Intended primarily for Linux /proc filesystem to avoid recalculating file
     * contents on iterative reads.
     *
     * @param filename The file to read
     * @return A byte array representing the file
     */
    public static byte[] readAllBytes(String filename) {
        return readAllBytes(filename, true);
    }

    /**
     * Read an entire file at one time. Intended primarily for Linux /proc filesystem to avoid recalculating file
     * contents on iterative reads.
     *
     * @param filename    The file to read
     * @param reportError Whether to log errors reading the file
     * @return A byte array representing the file
     */
    public static byte[] readAllBytes(String filename, boolean reportError) {
        if (new File(filename).canRead()) {
            if (Logger.isDebug()) {
                Logger.debug(READING_LOG, filename);
            }
            try {
                return Files.readAllBytes(Paths.get(filename));
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
     * Read an entire file at one time. Intended for unix /proc binary files to avoid reading file contents on iterative
     * reads.
     *
     * @param filename The file to read
     * @return A bytebuffer representing the file if read was successful; null otherwise
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
     * Reads a byte value from a ByteBuffer
     *
     * @param buff The bytebuffer to read from
     * @return The next byte value
     */
    public static byte readByteFromBuffer(ByteBuffer buff) {
        if (buff.position() < buff.limit()) {
            return buff.get();
        }
        return 0;
    }

    /**
     * Reads a short value from a ByteBuffer
     *
     * @param buff The bytebuffer to read from
     * @return The next short value
     */
    public static short readShortFromBuffer(ByteBuffer buff) {
        if (buff.position() <= buff.limit() - 2) {
            return buff.getShort();
        }
        return 0;
    }

    /**
     * Reads an int value from a ByteBuffer
     *
     * @param buff The bytebuffer to read from
     * @return The next int value
     */
    public static int readIntFromBuffer(ByteBuffer buff) {
        if (buff.position() <= buff.limit() - 4) {
            return buff.getInt();
        }
        return 0;
    }

    /**
     * Reads a long value from a ByteBuffer
     *
     * @param buff The bytebuffer to read from
     * @return The next long value
     */
    public static long readLongFromBuffer(ByteBuffer buff) {
        if (buff.position() <= buff.limit() - 8) {
            return buff.getLong();
        }
        return 0L;
    }

    /**
     * Reads a NativeLong value from a ByteBuffer
     *
     * @param buff The bytebuffer to read from
     * @return The next value
     */
    public static NativeLong readNativeLongFromBuffer(ByteBuffer buff) {
        return new NativeLong(Native.LONG_SIZE == 4 ? readIntFromBuffer(buff) : readLongFromBuffer(buff));
    }

    /**
     * Reads a size_t value from a ByteBuffer
     *
     * @param buff The bytebuffer to read from
     * @return The next value
     */
    public static LibCAPI.size_t readSizeTFromBuffer(ByteBuffer buff) {
        return new LibCAPI.size_t(Native.SIZE_T_SIZE == 4 ? readIntFromBuffer(buff) : readLongFromBuffer(buff));
    }

    /**
     * Reads a byte array value from a ByteBuffer
     *
     * @param buff  The bytebuffer to read from
     * @param array The array into which to read the data
     */
    public static void readByteArrayFromBuffer(ByteBuffer buff, byte[] array) {
        if (buff.position() <= buff.limit() - array.length) {
            buff.get(array);
        }
    }

    /**
     * Reads a Pointer value from a ByteBuffer
     *
     * @param buff The bytebuffer to read from
     * @return The next value
     */
    public static Pointer readPointerFromBuffer(ByteBuffer buff) {
        if (buff.position() <= buff.limit() - Native.POINTER_SIZE) {
            return Native.POINTER_SIZE == 4 ? new Pointer(buff.getInt()) : new Pointer(buff.getLong());
        }
        return Pointer.NULL;
    }

    /**
     * Read a file and return the long value contained therein. Intended primarily for Linux /sys filesystem
     *
     * @param filename The file to read
     * @return The value contained in the file, if any; otherwise zero
     */
    public static long getLongFromFile(String filename) {
        if (Logger.isDebug()) {
            Logger.debug(READING_LOG, filename);
        }
        List<String> read = readLines(filename, 1, false);
        if (!read.isEmpty()) {
            if (Logger.isTrace()) {
                Logger.trace(READ_LOG, read.get(0));
            }
            return Parsing.parseLongOrDefault(read.get(0), 0L);
        }
        return 0L;
    }

    /**
     * Read a file and return the unsigned long value contained therein as a long. Intended primarily for Linux /sys
     * filesystem
     *
     * @param filename The file to read
     * @return The value contained in the file, if any; otherwise zero
     */
    public static long getUnsignedLongFromFile(String filename) {
        if (Logger.isDebug()) {
            Logger.debug(READING_LOG, filename);
        }
        List<String> read = readLines(filename, 1, false);
        if (!read.isEmpty()) {
            if (Logger.isTrace()) {
                Logger.trace(READ_LOG, read.get(0));
            }
            return Parsing.parseUnsignedLongOrDefault(read.get(0), 0L);
        }
        return 0L;
    }

    /**
     * Read a file and return the int value contained therein. Intended primarily for Linux /sys filesystem
     *
     * @param filename The file to read
     * @return The value contained in the file, if any; otherwise zero
     */
    public static int getIntFromFile(String filename) {
        if (Logger.isDebug()) {
            Logger.debug(READING_LOG, filename);
        }
        try {
            List<String> read = readLines(filename, 1, false);
            if (!read.isEmpty()) {
                if (Logger.isTrace()) {
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
     * Read a file and return the String value contained therein. Intended primarily for Linux /sys filesystem
     *
     * @param filename The file to read
     * @return The value contained in the file, if any; otherwise empty string
     */
    public static String getStringFromFile(String filename) {
        if (Logger.isDebug()) {
            Logger.debug(READING_LOG, filename);
        }
        List<String> read = readLines(filename, 1, false);
        if (!read.isEmpty()) {
            if (Logger.isTrace()) {
                Logger.trace(READ_LOG, read.get(0));
            }
            return read.get(0);
        }
        return Normal.EMPTY;
    }

    /**
     * Read a file and return a map of string keys to string values contained therein. Intended primarily for Linux
     * {@code /proc/[pid]} files to provide more detailed or accurate information not available in the API.
     *
     * @param filename  The file to read
     * @param separator Character(s) in each line of the file that separate the key and the value.
     * @return The map contained in the file, delimited by the separator, with the value whitespace trimmed. If keys and
     * values are not parsed, an empty map is returned.
     */
    public static Map<String, String> getKeyValueMapFromFile(String filename, String separator) {
        Map<String, String> map = new HashMap<>();
        if (Logger.isDebug()) {
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
     * Reads the target of a symbolic link
     *
     * @param file The file to read
     * @return The symlink name, or null if the read failed
     */
    public static String readSymlinkTarget(File file) {
        try {
            return Files.readSymbolicLink(Paths.get(file.getAbsolutePath())).toString();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 输出到<code>StringBuilder</code>
     *
     * @param builder <code>StringBuilder</code>对象
     * @param caption 标题
     * @param value   值
     */
    public static void append(StringBuilder builder, String caption, Object value) {
        builder.append(caption).append(ObjectKit.defaultIfNull(Convert.toString(value), "[n/a]")).append(Symbol.LF);
    }

}
