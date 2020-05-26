/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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
package org.aoju.bus.health;

import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.convert.Convert;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.tuple.Pair;
import org.aoju.bus.core.utils.PinyinUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.health.builtin.hardware.*;
import org.aoju.bus.health.builtin.software.OperatingSystem;
import org.aoju.bus.logger.Logger;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String parsing utility.
 *
 * @author Kimi Liu
 * @version 5.9.3
 * @since JDK 1.8+
 */
@ThreadSafe
public final class Builder {

    public static final String BUS_HEALTH_ARCH_PROPERTIES = "bus-health-arch.properties";
    public static final String BUS_HEALTH_ADDR_PROPERTIES = "bus-health-addr.properties";
    public static final String BUS_HEALTH_PROPERTIES = "bus-health.properties";

    /**
     * The official/approved path for sysfs information. Note: /sys/class/dmi/id
     * symlinks here
     */
    public static final String SYSFS_SERIAL_PATH = "/sys/devices/virtual/dmi/id/";

    /**
     * The Unix Epoch, a default value when WMI DateTime queries return no value.
     */
    public static final OffsetDateTime UNIX_EPOCH = OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);

    /**
     * Constant <code>whitespacesColonWhitespace</code>
     */
    public static final Pattern whitespacesColonWhitespace = Pattern.compile("\\s+:\\s");
    /**
     * Constant <code>whitespaces</code>
     */
    public static final Pattern whitespaces = Pattern.compile("\\s+");
    /**
     * Constant <code>notDigits</code>
     */
    public static final Pattern notDigits = Pattern.compile("[^0-9]+");
    /**
     * Constant <code>startWithNotDigits</code>
     */
    public static final Pattern startWithNotDigits = Pattern.compile("^[^0-9]*");
    /**
     * Constant <code>HEX_ERROR="0x%08X"</code>
     */
    public static final String HEX_ERROR = "0x%08X";
    private static final String DEFAULT_Logger_MSG = "{} didn't parse. Returning default. {}";
    /*
     * Used for matching
     */
    private static final Pattern HERTZ_PATTERN = Pattern.compile("(\\d+(.\\d+)?) ?([kMGT]?Hz).*");
    /*
     * Used to check validity of a hexadecimal string
     */
    private static final Pattern VALID_HEX = Pattern.compile("[0-9a-fA-F]+");
    /*
     * Pattern for [dd-[hh:[mm:[ss[.sss]]]]]
     */
    private static final Pattern DHMS = Pattern.compile("(?:(\\d+)-)?(?:(\\d+):)??(?:(\\d+):)?(\\d+)(?:\\.(\\d+))?");
    /*
     * Pattern for a UUID
     */
    private static final Pattern UUID_PATTERN = Pattern
            .compile(".*([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}).*");
    /*
     * Pattern for Windows PnPDeviceID vendor and product ID
     */
    private static final Pattern VENDOR_PRODUCT_ID = Pattern
            .compile(".*(?:VID|VEN)_(\\p{XDigit}{4})&(?:PID|DEV)_(\\p{XDigit}{4}).*");
    /*
     * Pattern for Linux lspci machine readable
     */
    private static final Pattern LSPCI_MACHINE_READABLE = Pattern.compile("(.+)\\s\\[(.*?)\\]");
    /*
     * Pattern for Linux lspci memory
     */
    private static final Pattern LSPCI_MEMORY_SIZE = Pattern.compile(".+\\s\\[size=(\\d+)([kKMGT])\\]");
    /*
     * Hertz related variables.
     */
    private static final String HZ = "Hz";
    private static final String KHZ = "kHz";
    private static final String MHZ = "MHz";
    private static final String GHZ = "GHz";
    private static final String THZ = "THz";
    private static final String PHZ = "PHz";
    private static final Map<String, Long> multipliers;
    // PDH timestamps are 1601 epoch, local time
    // Constants to convert to UTC millis
    private static final long EPOCH_DIFF = 11644473600000L;
    private static final int TZ_OFFSET = TimeZone.getDefault().getOffset(System.currentTimeMillis());
    // Fast decimal exponentiation: pow(10,y) --> POWERS_OF_10[y]
    private static final long[] POWERS_OF_TEN = {1L, 10L, 100L, 1_000L, 10_000L, 100_000L, 1_000_000L, 10_000_000L,
            100_000_000L, 1_000_000_000L, 10_000_000_000L, 100_000_000_000L, 1_000_000_000_000L, 10_000_000_000_000L,
            100_000_000_000_000L, 1_000_000_000_000_000L, 10_000_000_000_000_000L, 100_000_000_000_000_000L,
            1_000_000_000_000_000_000L};
    // Fast hex character lookup
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    // Format returned by WMI for DateTime
    private static final DateTimeFormatter CIM_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss.SSSSSSZZZZZ",
            Locale.US);
    private static final String READING_LOG = "Reading file {}";
    private static final String READ_LOG = "Read {}";
    /**
     * Binary prefixes, used in IEC Standard for naming bytes.
     * (http://en.wikipedia.org/wiki/International_Electrotechnical_Commission)
     * <p>
     * Should be used for most representations of bytes
     */
    private static final long KIBI = 1L << 10;
    private static final long MEBI = 1L << 20;
    private static final long GIBI = 1L << 30;
    private static final long TEBI = 1L << 40;
    private static final long PEBI = 1L << 50;
    private static final long EXBI = 1L << 60;
    /**
     * Decimal prefixes, used for Hz and other metric units and for bytes by hard
     * drive manufacturers
     */
    private static final long KILO = 1_000L;
    private static final long MEGA = 1_000_000L;
    private static final long GIGA = 1_000_000_000L;
    private static final long TERA = 1_000_000_000_000L;
    private static final long PETA = 1_000_000_000_000_000L;
    private static final long EXA = 1_000_000_000_000_000_000L;
    /*
     * Two's complement reference: 2^64.
     */
    private static final BigInteger TWOS_COMPLEMENT_REF = BigInteger.ONE.shiftLeft(64);

    private static final Platform platform;
    /**
     * 硬件信息
     */
    private static final HardwareAbstractionLayer hardware;
    /**
     * 系统信息
     */
    private static final OperatingSystem os;

    static {
        multipliers = new HashMap<>();
        multipliers.put(HZ, 1L);
        multipliers.put(KHZ, 1_000L);
        multipliers.put(MHZ, 1_000_000L);
        multipliers.put(GHZ, 1_000_000_000L);
        multipliers.put(THZ, 1_000_000_000_000L);
        multipliers.put(PHZ, 1_000_000_000_000_000L);
        platform = new Platform();
        hardware = platform.getHardware();
        os = platform.getOperatingSystem();
    }

    private Builder() {
    }

    /**
     * 获取操作系统相关信息，包括系统版本、文件系统、进程等
     *
     * @return 操作系统相关信息
     */
    public static OperatingSystem getOs() {
        return os;
    }

    /**
     * 获取硬件相关信息，包括内存、硬盘、网络设备、显示器、USB、声卡等
     *
     * @return 硬件相关信息
     */
    public static HardwareAbstractionLayer getHardware() {
        return hardware;
    }

    /**
     * 获取BIOS中计算机相关信息，比如序列号、固件版本等
     *
     * @return 获取BIOS中计算机相关信息
     */
    public static ComputerSystem getSystem() {
        return hardware.getComputerSystem();
    }

    /**
     * 获取内存相关信息，比如总内存、可用内存等
     *
     * @return 内存相关信息
     */
    public static GlobalMemory getMemory() {
        return hardware.getMemory();
    }

    /**
     * 获取CPU(处理器)相关信息，比如CPU负载等
     *
     * @return CPU(处理器)相关信息
     */
    public static CentralProcessor getProcessor() {
        return hardware.getProcessor();
    }

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
     * @param pattern The String containing a wildcard pattern where ? represents a
     *                single character and * represents any number of characters. If the
     *                first character of the pattern is a carat (^) the test is
     *                performed against the remaining characters and the result of the
     *                test is the opposite.
     * @return True if the String matches or if the first character is ^ and the
     * remainder of the String does not match.
     */
    public static boolean wildcardMatch(String text, String pattern) {
        if (pattern.length() > 0 && pattern.charAt(0) == Symbol.C_CARET) {
            return !wildcardMatch(text, pattern.substring(1));
        }
        return text.matches(pattern.replace("?", ".?").replace(Symbol.STAR, ".*?"));
    }

    /**
     * Gets a map containing current working directory info
     *
     * @param pid a process ID, optional
     * @return a map of process IDs to their current working directory. If
     * {@code pid} is a negative number, all processes are returned;
     * otherwise the map may contain only a single element for {@code pid}
     */
    public static Map<Integer, String> getCwdMap(int pid) {
        List<String> lsof = Executor.runNative("lsof -Fn -d cwd" + (pid < 0 ? Normal.EMPTY : " -p " + pid));
        Map<Integer, String> cwdMap = new HashMap<>();
        Integer key = -1;
        for (String line : lsof) {
            if (line.isEmpty()) {
                continue;
            }
            switch (line.charAt(0)) {
                case 'p':
                    key = Builder.parseIntOrDefault(line.substring(1), -1);
                    break;
                case 'n':
                    cwdMap.put(key, line.substring(1));
                    break;
                case 'f':
                    // ignore the 'cwd' file descriptor
                default:
                    break;
            }
        }
        return cwdMap;
    }

    /**
     * Format bytes into a rounded string representation using IEC standard (matches
     * Mac/Linux). For hard drive capacities, use @link
     * {@link #formatBytesDecimal(long)}. For Windows displays for KB, MB and GB, in
     * JEDEC units, edit the returned string to remove the 'i' to display the
     * (incorrect) JEDEC units.
     *
     * @param bytes Bytes.
     * @return Rounded string representation of the byte size.
     */
    public static String formatBytes(long bytes) {
        if (bytes == 1L) { // bytes
            return String.format("%d byte", bytes);
        } else if (bytes < KIBI) { // bytes
            return String.format("%d bytes", bytes);
        } else if (bytes < MEBI) { // KiB
            return formatUnits(bytes, KIBI, "KiB");
        } else if (bytes < GIBI) { // MiB
            return formatUnits(bytes, MEBI, "MiB");
        } else if (bytes < TEBI) { // GiB
            return formatUnits(bytes, GIBI, "GiB");
        } else if (bytes < PEBI) { // TiB
            return formatUnits(bytes, TEBI, "TiB");
        } else if (bytes < EXBI) { // PiB
            return formatUnits(bytes, PEBI, "PiB");
        } else { // EiB
            return formatUnits(bytes, EXBI, "EiB");
        }
    }

    /**
     * Format units as exact integer or fractional decimal based on the prefix,
     * appending the appropriate units
     *
     * @param value  The value to format
     * @param prefix The divisor of the unit multiplier
     * @param unit   A string representing the units
     * @return A string with the value
     */
    private static String formatUnits(long value, long prefix, String unit) {
        if (value % prefix == 0) {
            return String.format("%d %s", value / prefix, unit);
        }
        return String.format("%.1f %s", (double) value / prefix, unit);
    }

    /**
     * Format bytes into a rounded string representation using decimal SI units.
     * These are used by hard drive manufacturers for capacity. Most other storage
     * should use {@link #formatBytes(long)}.
     *
     * @param bytes Bytes.
     * @return Rounded string representation of the byte size.
     */
    public static String formatBytesDecimal(long bytes) {
        if (bytes == 1L) { // bytes
            return String.format("%d byte", bytes);
        } else if (bytes < KILO) { // bytes
            return String.format("%d bytes", bytes);
        } else {
            return formatValue(bytes, "B");
        }
    }

    /**
     * Format hertz into a string to a rounded string representation.
     *
     * @param hertz Hertz.
     * @return Rounded string representation of the hertz size.
     */
    public static String formatHertz(long hertz) {
        return formatValue(hertz, "Hz");
    }

    /**
     * Format arbitrary units into a string to a rounded string representation.
     *
     * @param value The value
     * @param unit  Units to append metric prefix to
     * @return Rounded string representation of the value with metric prefix to
     * extension
     */
    public static String formatValue(long value, String unit) {
        if (value < KILO) {
            return String.format("%d %s", value, unit);
        } else if (value < MEGA) { // K
            return formatUnits(value, KILO, "K" + unit);
        } else if (value < GIGA) { // M
            return formatUnits(value, MEGA, "M" + unit);
        } else if (value < TERA) { // G
            return formatUnits(value, GIGA, "G" + unit);
        } else if (value < PETA) { // T
            return formatUnits(value, TERA, "T" + unit);
        } else if (value < EXA) { // P
            return formatUnits(value, PETA, "P" + unit);
        } else { // E
            return formatUnits(value, EXA, "E" + unit);
        }
    }

    /**
     * Formats an elapsed time in seconds as days, hh:mm:ss.
     *
     * @param secs Elapsed seconds
     * @return A string representation of elapsed time
     */
    public static String formatElapsedSecs(long secs) {
        long eTime = secs;
        final long days = TimeUnit.SECONDS.toDays(eTime);
        eTime -= TimeUnit.DAYS.toSeconds(days);
        final long hr = TimeUnit.SECONDS.toHours(eTime);
        eTime -= TimeUnit.HOURS.toSeconds(hr);
        final long min = TimeUnit.SECONDS.toMinutes(eTime);
        eTime -= TimeUnit.MINUTES.toSeconds(min);
        final long sec = eTime;
        return String.format("%d days, %02d:%02d:%02d", days, hr, min, sec);
    }

    /**
     * Round to certain number of decimals.
     *
     * @param d            Number to be rounded
     * @param decimalPlace Number of decimal places to round to
     * @return rounded result
     */
    public static float round(float d, int decimalPlace) {
        final BigDecimal bd = new BigDecimal(Float.toString(d)).setScale(decimalPlace, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    /**
     * Convert unsigned int to signed long.
     *
     * @param x Signed int representing an unsigned integer
     * @return long value of x unsigned
     */
    public static long getUnsignedInt(int x) {
        return x & 0x00000000ffffffffL;
    }

    /**
     * Represent a 32 bit value as if it were an unsigned integer.
     * <p>
     * This is a Java 7 implementation of Java 8's Integer.toUnsignedString.
     *
     * @param i a 32 bit value
     * @return the string representation of the unsigned integer
     */
    public static String toUnsignedString(int i) {
        if (i >= 0) {
            return Integer.toString(i);
        }
        return Long.toString(getUnsignedInt(i));
    }

    /**
     * Represent a 64 bit value as if it were an unsigned long.
     * <p>
     * This is a Java 7 implementation of Java 8's Long.toUnsignedString.
     *
     * @param l a 64 bit value
     * @return the string representation of the unsigned long
     */
    public static String toUnsignedString(long l) {
        if (l >= 0) {
            return Long.toString(l);
        }
        return BigInteger.valueOf(l).add(TWOS_COMPLEMENT_REF).toString();
    }

    /**
     * Translate an integer error code to its hex notation
     *
     * @param errorCode The error code
     * @return A string representing the error as 0x....
     */
    public static String formatError(int errorCode) {
        return String.format(HEX_ERROR, errorCode);
    }

    /**
     * Read an entire file at one time. Intended primarily for Linux /proc
     * filesystem to avoid recalculating file contents on iterative reads.
     *
     * @param filename The file to read
     * @return A list of Strings representing each line of the file, or an empty
     * list if file could not be read or is empty
     */
    public static List<String> readFile(String filename) {
        return readFile(filename, true);
    }

    /**
     * Read an entire file at one time. Intended primarily for Linux /proc
     * filesystem to avoid recalculating file contents on iterative reads.
     *
     * @param filename    The file to read
     * @param reportError Whether to log errors reading the file
     * @return A list of Strings representing each line of the file, or an empty
     * list if file could not be read or is empty
     */
    public static List<String> readFile(String filename, boolean reportError) {
        if (new File(filename).canRead()) {
            if (Logger.get().isDebug()) {
                Logger.debug(READING_LOG, filename);
            }
            try {
                return Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
            } catch (IOException e) {
                if (reportError) {
                    Logger.error("Error reading file {}. {}", filename, e.getMessage());
                }
            }
        } else if (reportError) {
            Logger.warn("File not found or not readable: {}", filename);
        }
        return new ArrayList<>();
    }

    /**
     * Read a file and return the long value contained therein. Intended primarily
     * for Linux /sys filesystem
     *
     * @param filename The file to read
     * @return The value contained in the file, if any; otherwise zero
     */
    public static long getLongFromFile(String filename) {
        if (Logger.get().isDebug()) {
            Logger.debug(READING_LOG, filename);
        }
        List<String> read = readFile(filename, false);
        if (!read.isEmpty()) {
            if (Logger.get().isTrace()) {
                Logger.trace(READ_LOG, read.get(0));
            }
            return Builder.parseLongOrDefault(read.get(0), 0L);
        }
        return 0L;
    }

    /**
     * Read a file and return the unsigned long value contained therein as a long.
     * Intended primarily for Linux /sys filesystem
     *
     * @param filename The file to read
     * @return The value contained in the file, if any; otherwise zero
     */
    public static long getUnsignedLongFromFile(String filename) {
        if (Logger.get().isDebug()) {
            Logger.debug(READING_LOG, filename);
        }
        List<String> read = readFile(filename, false);
        if (!read.isEmpty()) {
            if (Logger.get().isTrace()) {
                Logger.trace(READ_LOG, read.get(0));
            }
            return Builder.parseUnsignedLongOrDefault(read.get(0), 0L);
        }
        return 0L;
    }

    /**
     * Read a file and return the int value contained therein. Intended primarily
     * for Linux /sys filesystem
     *
     * @param filename The file to read
     * @return The value contained in the file, if any; otherwise zero
     */
    public static int getIntFromFile(String filename) {
        if (Logger.get().isDebug()) {
            Logger.debug(READING_LOG, filename);
        }
        try {
            List<String> read = readFile(filename, false);
            if (!read.isEmpty()) {
                if (Logger.get().isTrace()) {
                    Logger.trace(READ_LOG, read.get(0));
                }
                return Integer.parseInt(read.get(0));
            }
        } catch (NumberFormatException ex) {
            Logger.warn("Unable to read value from {}. {}", filename, ex.getMessage());
        }
        return 0;
    }

    /**
     * Read a file and return the String value contained therein. Intended primarily
     * for Linux /sys filesystem
     *
     * @param filename The file to read
     * @return The value contained in the file, if any; otherwise empty string
     */
    public static String getStringFromFile(String filename) {
        if (Logger.get().isDebug()) {
            Logger.debug(READING_LOG, filename);
        }
        List<String> read = readFile(filename, false);
        if (!read.isEmpty()) {
            if (Logger.get().isTrace()) {
                Logger.trace(READ_LOG, read.get(0));
            }
            return read.get(0);
        }
        return Normal.EMPTY;
    }

    /**
     * Read a file and return a map of string keys to string values contained
     * therein. Intended primarily for Linux /proc/[pid]/io
     *
     * @param filename  The file to read
     * @param separator Characters in each line of the file that separate the key and the
     *                  value
     * @return The map contained in the file, if any; otherwise empty map
     */
    public static Map<String, String> getKeyValueMapFromFile(String filename, String separator) {
        Map<String, String> map = new HashMap<>();
        if (Logger.get().isDebug()) {
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
     * Read a configuration file from the class path and return its properties
     *
     * @param fileName The filename
     * @return A {@link Properties} object containing the properties.
     */
    public static Properties readPropertiesFromFilename(String fileName) {
        Properties p = new Properties();
        try {
            String path = Symbol.SLASH + Normal.META_DATA_INF + "/healthy/" + fileName;
            InputStream is = PinyinUtils.class.getResourceAsStream(path);
            Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split(Symbol.EQUAL);
                p.setProperty(tokens[0], tokens[1]);
            }
            br.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return p;
    }

    /**
     * Gets the Manufacturer ID from (up to) 3 5-bit characters in bytes 8 and 9
     *
     * @param edid The EDID byte array
     * @return The manufacturer ID
     */
    public static String getManufacturerID(byte[] edid) {
        // Bytes 8-9 are manufacturer ID in 3 5-bit characters.
        String temp = String
                .format("%8s%8s", Integer.toBinaryString(edid[8] & 0xFF), Integer.toBinaryString(edid[9] & 0xFF))
                .replace(Symbol.C_SPACE, '0');
        Logger.debug("Manufacurer ID: {}", temp);
        return String.format("%s%s%s", (char) (64 + Integer.parseInt(temp.substring(1, 6), 2)),
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
     * @return If all 4 bytes represent alphanumeric characters, a 4-character
     * string, otherwise a hex string.
     */
    public static String getSerialNo(byte[] edid) {
        // Bytes 12-15 are Serial number (last 4 characters)
        if (Logger.get().isDebug()) {
            Logger.debug("Serial number: {}", Arrays.toString(Arrays.copyOfRange(edid, 12, 16)));
        }
        return String.format("%s%s%s%s", getAlphaNumericOrHex(edid[15]), getAlphaNumericOrHex(edid[14]),
                getAlphaNumericOrHex(edid[13]), getAlphaNumericOrHex(edid[12]));
    }

    private static String getAlphaNumericOrHex(byte b) {
        return Character.isLetterOrDigit((char) b) ? String.format("%s", (char) b) : String.format("%02X", b);
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
        return edid[18] + Symbol.DOT + edid[19];
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
        return String.format("Clock %dMHz, Active Pixels %dx%d ", clock, hActive, vActive);
    }

    /**
     * Parse descriptor range limits
     *
     * @param desc An 18-byte VESA descriptor
     * @return A string describing some of the range limits
     */
    public static String getDescriptorRangeLimits(byte[] desc) {
        return String.format("Field Rate %d-%d Hz vertical, %d-%d Hz horizontal, Max clock: %d MHz", desc[5], desc[6],
                desc[7], desc[8], desc[9] * 10);
    }

    /**
     * Parse descriptor text
     *
     * @param desc An 18-byte VESA descriptor
     * @return Plain text starting at the 4th byte
     */
    public static String getDescriptorText(byte[] desc) {
        return new String(Arrays.copyOfRange(desc, 4, 18), StandardCharsets.US_ASCII).trim();
    }

    /**
     * Parse an EDID byte array into user-readable information
     *
     * @param edid An EDID byte array
     * @return User-readable text represented by the EDID
     */
    public static String toString(byte[] edid) {
        StringBuilder sb = new StringBuilder();
        sb.append("  Manuf. ID=").append(getManufacturerID(edid));
        sb.append(", Product ID=").append(getProductID(edid));
        sb.append(", ").append(isDigital(edid) ? "Digital" : "Analog");
        sb.append(", Serial=").append(getSerialNo(edid));
        sb.append(", ManufDate=").append(getWeek(edid) * 12 / 52 + 1).append(Symbol.C_SLASH)
                .append(getYear(edid));
        sb.append(", EDID v").append(getVersion(edid));
        int hSize = getHcm(edid);
        int vSize = getVcm(edid);
        sb.append(String.format("%n  %d x %d cm (%.1f x %.1f in)", hSize, vSize, hSize / 2.54, vSize / 2.54));
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
                    sb.append("\n  White Point Data: ").append(Builder.byteArrayToHexString(b));
                    break;
                case 0xfa:
                    sb.append("\n  Standard Timing ID: ").append(Builder.byteArrayToHexString(b));
                    break;
                default:
                    if (getDescriptorType(b) <= 0x0f && getDescriptorType(b) >= 0x00) {
                        sb.append("\n  Manufacturer Data: ").append(Builder.byteArrayToHexString(b));
                    } else {
                        sb.append("\n  Preferred Timing: ").append(getTimingDescriptor(b));
                    }
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * Parse hertz from a string, eg. "2.00MHz" is 2000000L.
     *
     * @param hertz Hertz size.
     * @return {@link java.lang.Long} Hertz value or -1 if not parseable.
     */
    public static long parseHertz(String hertz) {
        Matcher matcher = HERTZ_PATTERN.matcher(hertz.trim());
        if (matcher.find() && matcher.groupCount() == 3) {
            // Regexp enforces #(.#) format so no test for NFE required
            double value = Double.valueOf(matcher.group(1)) * multipliers.getOrDefault(matcher.group(3), -1L);
            if (value >= 0d) {
                return (long) value;
            }
        }
        return -1L;
    }

    /**
     * Parse the last element of a space-delimited string to a value
     *
     * @param s The string to parse
     * @param i Default integer if not parsable
     * @return value or the given default if not parsable
     */
    public static int parseLastInt(String s, int i) {
        try {
            String ls = parseLastString(s);
            if (ls.toLowerCase().startsWith("0x")) {
                return Integer.decode(ls);
            } else {
                return Integer.parseInt(ls);
            }
        } catch (NumberFormatException e) {
            Logger.trace(DEFAULT_Logger_MSG, s, e);
            return i;
        }
    }

    /**
     * Parse the last element of a space-delimited string to a value
     *
     * @param s  The string to parse
     * @param li Default long integer if not parsable
     * @return value or the given default if not parsable
     */
    public static long parseLastLong(String s, long li) {
        try {
            String ls = parseLastString(s);
            if (ls.toLowerCase().startsWith("0x")) {
                return Long.decode(ls);
            } else {
                return Long.parseLong(ls);
            }
        } catch (NumberFormatException e) {
            Logger.trace(DEFAULT_Logger_MSG, s, e);
            return li;
        }
    }

    /**
     * Parse the last element of a space-delimited string to a value
     *
     * @param s The string to parse
     * @param d Default double if not parsable
     * @return value or the given default if not parsable
     */
    public static double parseLastDouble(String s, double d) {
        try {
            return Double.parseDouble(parseLastString(s));
        } catch (NumberFormatException e) {
            Logger.trace(DEFAULT_Logger_MSG, s, e);
            return d;
        }
    }

    /**
     * Parse the last element of a space-delimited string to a string
     *
     * @param s The string to parse
     * @return last space-delimited element
     */
    public static String parseLastString(String s) {
        String[] ss = whitespaces.split(s);
        if (ss.length < 1) {
            return s;
        } else {
            return ss[ss.length - 1];
        }
    }

    /**
     * Parse a byte aray into a string of hexadecimal digits including leading zeros
     *
     * @param bytes The byte array to represent
     * @return A string of hex characters corresponding to the bytes. The string is
     * upper case.
     */
    public static String byteArrayToHexString(byte[] bytes) {
        // Solution copied from https://stackoverflow.com/questions/9655181
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Parse a string of hexadecimal digits into a byte array
     *
     * @param digits The string to be parsed
     * @return a byte array with each pair of characters converted to a byte, or
     * empty array if the string is not valid hex
     */
    public static byte[] hexStringToByteArray(String digits) {
        int len = digits.length();
        // Check if string is valid hex
        if (!VALID_HEX.matcher(digits).matches() || (len & 0x1) != 0) {
            Logger.warn("Invalid hexadecimal string: {}", digits);
            return new byte[0];
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) (Character.digit(digits.charAt(i), 16) << 4
                    | Character.digit(digits.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Parse a human readable ASCII string into a byte array, truncating or padding
     * with zeros (if necessary) so the array has the specified length.
     *
     * @param text   The string to be parsed
     * @param length Length of the returned array.
     * @return A byte array of specified length, with each of the first length
     * characters converted to a byte. If length is longer than the provided
     * string length, will be filled with zeroes.
     */
    public static byte[] asciiStringToByteArray(String text, int length) {
        return Arrays.copyOf(text.getBytes(StandardCharsets.US_ASCII), length);
    }

    /**
     * Convert a long value to a byte array using Big Endian, truncating or padding
     * with zeros (if necessary) so the array has the specified length.
     *
     * @param value     The value to be converted
     * @param valueSize Number of bytes representing the value
     * @param length    Number of bytes to return
     * @return A byte array of specified length representing the long in the first
     * valueSize bytes
     */
    public static byte[] longToByteArray(long value, int valueSize, int length) {
        long val = value;
        // Convert the long to 8-byte BE representation
        byte[] b = new byte[8];
        for (int i = 7; i >= 0 && val != 0L; i--) {
            b[i] = (byte) val;
            val >>>= 8;
        }
        // Then copy the rightmost valueSize bytes
        // e.g., for an integer we want rightmost 4 bytes
        return Arrays.copyOfRange(b, 8 - valueSize, 8 + length - valueSize);
    }

    /**
     * Convert a string to an integer representation.
     *
     * @param str  A human readable ASCII string
     * @param size Number of characters to convert to the long. May not exceed 8.
     * @return An integer representing the string where each character is treated as
     * a byte
     */
    public static long strToLong(String str, int size) {
        return byteArrayToLong(str.getBytes(StandardCharsets.US_ASCII), size);
    }

    /**
     * Convert a byte array to its integer representation.
     *
     * @param bytes An array of bytes no smaller than the size to be converted
     * @param size  Number of bytes to convert to the long. May not exceed 8.
     * @return An integer representing the byte array as a 64-bit number
     */
    public static long byteArrayToLong(byte[] bytes, int size) {
        if (size > 8) {
            throw new IllegalArgumentException("Can't convert more than 8 bytes.");
        }
        if (size > bytes.length) {
            throw new IllegalArgumentException("Size can't be larger than array length.");
        }
        long total = 0L;
        for (int i = 0; i < size; i++) {
            total = total << 8 | bytes[i] & 0xff;
        }
        return total;
    }

    /**
     * Convert a byte array to its floating point representation.
     *
     * @param bytes  An array of bytes no smaller than the size to be converted
     * @param size   Number of bytes to convert to the float. May not exceed 8.
     * @param fpBits Number of bits representing the decimal
     * @return A float; the integer portion representing the byte array as an
     * integer shifted by the bits specified in fpBits; with the remaining
     * bits used as a decimal
     */
    public static float byteArrayToFloat(byte[] bytes, int size, int fpBits) {
        return byteArrayToLong(bytes, size) / (float) (1 << fpBits);
    }

    /**
     * Convert an unsigned integer to a long value. The method assumes that all bits
     * in the specified integer value are 'data' bits, including the
     * most-significant bit which Java normally considers a sign bit. The method
     * must be used only when it is certain that the integer value represents an
     * unsigned integer, for example when the integer is returned by JNA library in
     * a structure which holds unsigned integers.
     *
     * @param unsignedValue The unsigned integer value to convert.
     * @return The unsigned integer value widened to a long.
     */
    public static long unsignedIntToLong(int unsignedValue) {
        // use standard Java widening conversion to long which does
        // sign-extension,
        // then drop any copies of the sign bit, to prevent the value being
        // considered a negative one by Java if it is set
        long longValue = unsignedValue;
        return longValue & 0xffffffffL;
    }

    /**
     * Convert an unsigned long to a signed long value by stripping the sign bit.
     * This method "rolls over" long values greater than the max value but ensures
     * the result is never negative.
     *
     * @param unsignedValue The unsigned long value to convert.
     * @return The signed long value.
     */
    public static long unsignedLongToSignedLong(long unsignedValue) {
        return unsignedValue & 0x7fffffff_ffffffffL;
    }

    /**
     * Parses a string of hex digits to a string where each pair of hex digits
     * represents an ASCII character
     *
     * @param hexString A sequence of hex digits
     * @return The corresponding string if valid hex; otherwise the original
     * hexString
     */
    public static String hexStringToString(String hexString) {
        // Odd length strings won't parse, return
        if (hexString.length() % 2 > 0) {
            return hexString;
        }
        int charAsInt;
        StringBuilder sb = new StringBuilder();
        try {
            for (int pos = 0; pos < hexString.length(); pos += 2) {
                charAsInt = Integer.parseInt(hexString.substring(pos, pos + 2), 16);
                if (charAsInt < 32 || charAsInt > 127) {
                    return hexString;
                }
                sb.append((char) charAsInt);
            }
        } catch (NumberFormatException e) {
            Logger.trace(DEFAULT_Logger_MSG, hexString, e);
            // Hex failed to parse, just return the existing string
            return hexString;
        }
        return sb.toString();
    }

    /**
     * Attempts to parse a string to an int. If it fails, returns the default
     *
     * @param s          The string to parse
     * @param defaultInt The value to return if parsing fails
     * @return The parsed int, or the default if parsing failed
     */
    public static int parseIntOrDefault(String s, int defaultInt) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            Logger.trace(DEFAULT_Logger_MSG, s, e);
            return defaultInt;
        }
    }

    /**
     * Attempts to parse a string to a long. If it fails, returns the default
     *
     * @param s           The string to parse
     * @param defaultLong The value to return if parsing fails
     * @return The parsed long, or the default if parsing failed
     */
    public static long parseLongOrDefault(String s, long defaultLong) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            Logger.trace(DEFAULT_Logger_MSG, s, e);
            return defaultLong;
        }
    }

    /**
     * Attempts to parse a string to an "unsigned" long. If it fails, returns the
     * default
     *
     * @param s           The string to parse
     * @param defaultLong The value to return if parsing fails
     * @return The parsed long containing the same 64 bits that an unsigned long
     * would contain (which may produce a negative value)
     */
    public static long parseUnsignedLongOrDefault(String s, long defaultLong) {
        try {
            return new BigInteger(s).longValue();
        } catch (NumberFormatException e) {
            Logger.trace(DEFAULT_Logger_MSG, s, e);
            return defaultLong;
        }
    }

    /**
     * Attempts to parse a string to a double. If it fails, returns the default
     *
     * @param s             The string to parse
     * @param defaultDouble The value to return if parsing fails
     * @return The parsed double, or the default if parsing failed
     */
    public static double parseDoubleOrDefault(String s, double defaultDouble) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            Logger.trace(DEFAULT_Logger_MSG, s, e);
            return defaultDouble;
        }
    }

    /**
     * Attempts to parse a string of the form [DD-[hh:]]mm:ss[.ddd] to a number of
     * milliseconds. If it fails, returns the default.
     *
     * @param s           The string to parse
     * @param defaultLong The value to return if parsing fails
     * @return The parsed number of seconds, or the default if parsing fails
     */
    public static long parseDHMSOrDefault(String s, long defaultLong) {
        Matcher m = DHMS.matcher(s);
        if (m.matches()) {
            long milliseconds = 0L;
            if (m.group(1) != null) {
                milliseconds += parseLongOrDefault(m.group(1), 0L) * 86400000L;
            }
            if (m.group(2) != null) {
                milliseconds += parseLongOrDefault(m.group(2), 0L) * 3600000L;
            }
            if (m.group(3) != null) {
                milliseconds += parseLongOrDefault(m.group(3), 0L) * 60000L;
            }
            milliseconds += parseLongOrDefault(m.group(4), 0L) * 1000L;
            milliseconds += (long) (1000 * parseDoubleOrDefault("0." + m.group(5), 0d));
            return milliseconds;
        }
        return defaultLong;
    }

    /**
     * Attempts to parse a UUID. If it fails, returns the default.
     *
     * @param s          The string to parse
     * @param defaultStr The value to return if parsing fails
     * @return The parsed UUID, or the default if parsing fails
     */
    public static String parseUuidOrDefault(String s, String defaultStr) {
        Matcher m = UUID_PATTERN.matcher(s.toLowerCase());
        if (m.matches()) {
            return m.group(1);
        }
        return defaultStr;
    }

    /**
     * Parses a string key = 'value' (string)
     *
     * @param line The entire string
     * @return the value contained between single tick marks
     */
    public static String getSingleQuoteStringValue(String line) {
        return getStringBetween(line, '\'');
    }

    /**
     * Parse a string key = "value" (string)
     *
     * @param line the entire string
     * @return the value contained between double tick marks
     */
    public static String getDoubleQuoteStringValue(String line) {
        return getStringBetween(line, Symbol.C_DOUBLE_QUOTES);
    }

    /**
     * Gets a value between two characters having multiple same characters between
     * them. <b>Examples : </b>
     * <ul>
     * <li>"name = 'James Gosling's Java'" returns "James Gosling's Java"</li>
     * <li>"pci.name = 'Realtek AC'97 Audio Device'" returns "Realtek AC'97 Audio
     * Device"</li>
     * </ul>
     *
     * @param line The "key-value" pair line.
     * @param c    The Trailing And Leading characters of the string line
     * @return : The value having the characters between them.
     */
    public static String getStringBetween(String line, char c) {
        int firstOcc = line.indexOf(c);
        if (firstOcc < 0) {
            return Normal.EMPTY;
        }
        return line.substring(firstOcc + 1, line.lastIndexOf(c)).trim();
    }

    /**
     * Parses a string such as "10.12.2" or "key = 1 (0x1) (int)" to find the
     * integer value of the first set of one or more consecutive digits
     *
     * @param line The entire string
     * @return the value of first integer if any; 0 otherwise
     */
    public static int getFirstIntValue(String line) {
        return getNthIntValue(line, 1);
    }

    /**
     * Parses a string such as "10.12.2" or "key = 1 (0x1) (int)" to find the
     * integer value of the nth set of one or more consecutive digits
     *
     * @param line The entire string
     * @param n    Which set of integers to return
     * @return the value of nth integer if any; 0 otherwise
     */
    public static int getNthIntValue(String line, int n) {
        // Split the string by non-digits,
        String[] split = notDigits.split(startWithNotDigits.matcher(line).replaceFirst(Normal.EMPTY));
        if (split.length >= n) {
            return parseIntOrDefault(split[n - 1], 0);
        }
        return 0;
    }

    /**
     * Removes all matching sub strings from the string. More efficient than regexp.
     *
     * @param original source String to remove from
     * @param toRemove the sub string to be removed
     * @return The string with all matching substrings removed
     */
    public static String removeMatchingString(final String original, final String toRemove) {
        if (original == null || original.isEmpty() || toRemove == null || toRemove.isEmpty()) {
            return original;
        }

        int matchIndex = original.indexOf(toRemove, 0);
        if (matchIndex == -1) {
            return original;
        }

        StringBuilder buffer = new StringBuilder(original.length() - toRemove.length());
        int currIndex = 0;
        do {
            buffer.append(original.substring(currIndex, matchIndex));
            currIndex = matchIndex + toRemove.length();
            matchIndex = original.indexOf(toRemove, currIndex);
        } while (matchIndex != -1);

        buffer.append(original.substring(currIndex));
        return buffer.toString();
    }

    /**
     * Parses a delimited string to an array of longs. Optimized for processing
     * predictable-length arrays such as outputs of reliably formatted Linux proc or
     * sys filesystem, minimizing new object creation. Users should perform other
     * sanity checks of data.
     * <p>
     * As a special case, non-numeric fields (such as UUIDs in OpenVZ) at the end of
     * the list are ignored. Values greater than the max long value return the max
     * long value.
     * <p>
     * The indices parameters are referenced assuming the length as specified, and
     * leading characters are ignored. For example, if the string is "foo 12 34 5"
     * and the length is 3, then index 0 is 12, index 1 is 34, and index 2 is 5.
     *
     * @param s         The string to parse
     * @param indices   An array indicating which indexes should be populated in the final
     *                  array; other values will be skipped. This idex is zero-referenced
     *                  assuming the rightmost delimited fields of the string contain the
     *                  array.
     * @param length    The total number of elements in the string array. It is
     *                  permissible for the string to have more elements than this;
     *                  leading elements will be ignored. This should be calculated once
     *                  per text format by {@link #countStringToLongArray}.
     * @param delimiter The character to delimit by.
     * @return If successful, an array of parsed longs. If parsing errors occurred,
     * will be an array of zeros.
     */
    public static long[] parseStringToLongArray(String s, int[] indices, int length, char delimiter) {
        long[] parsed = new long[indices.length];
        // Iterate from right-to-left of String
        // Fill right to left of result array using index array
        int charIndex = s.length();
        int parsedIndex = indices.length - 1;
        int stringIndex = length - 1;

        int power = 0;
        int c;
        boolean delimCurrent = false;
        boolean numeric = true;
        boolean numberFound = false; // ignore nonnumeric at end
        boolean dashSeen = false; // to flag uuids as nonnumeric
        while (--charIndex > 0 && parsedIndex >= 0) {
            c = s.charAt(charIndex);
            if (c == delimiter) {
                // first parseable number?
                if (!numberFound && numeric) {
                    numberFound = true;
                }
                if (!delimCurrent) {
                    if (numberFound && indices[parsedIndex] == stringIndex--) {
                        parsedIndex--;
                    }
                    delimCurrent = true;
                    power = 0;
                    dashSeen = false;
                    numeric = true;
                }
            } else if (indices[parsedIndex] != stringIndex || c == Symbol.C_PLUS || !numeric) {
                // Doesn't impact parsing, ignore
                delimCurrent = false;
            } else if (c >= '0' && c <= '9' && !dashSeen) {
                if (power > 18 || power == 17 && c == '9' && parsed[parsedIndex] > 223372036854775807L) {
                    parsed[parsedIndex] = Long.MAX_VALUE;
                } else {
                    parsed[parsedIndex] += (c - '0') * Builder.POWERS_OF_TEN[power++];
                }
                delimCurrent = false;
            } else if (c == '-') {
                parsed[parsedIndex] *= -1L;
                delimCurrent = false;
                dashSeen = true;
            } else {
                // Flag as nonnumeric and continue unless we've seen a numeric
                // error on everything else
                if (numberFound) {
                    Logger.error("Illegal character parsing string '{}' to long array: {}", s, s.charAt(charIndex));
                    return new long[indices.length];
                }
                parsed[parsedIndex] = 0;
                numeric = false;
            }
        }
        if (parsedIndex > 0) {
            Logger.error("Not enough fields in string '{}' parsing to long array: {}", s, indices.length - parsedIndex);
            return new long[indices.length];
        }
        return parsed;
    }

    /**
     * Parses a delimited string to count elements of an array of longs. Intended to
     * be called once to calculate the {@code length} field for
     * {@link #parseStringToLongArray}.
     * <p>
     * As a special case, non-numeric fields (such as UUIDs in OpenVZ) at the end of
     * the list are ignored.
     *
     * @param s         The string to parse
     * @param delimiter The character to delimit by
     * @return The number of parsable long values which follow the last unparsable
     * value.
     */
    public static int countStringToLongArray(String s, char delimiter) {
        // Iterate from right-to-left of String
        // Fill right to left of result array using index array
        int charIndex = s.length();
        int numbers = 0;

        int c;
        boolean delimCurrent = false;
        boolean numeric = true;
        boolean dashSeen = false; // to flag uuids as nonnumeric
        while (--charIndex > 0) {
            c = s.charAt(charIndex);
            if (c == delimiter) {
                if (!delimCurrent) {
                    if (numeric) {
                        numbers++;
                    }
                    delimCurrent = true;
                    dashSeen = false;
                    numeric = true;
                }
            } else if (c == Symbol.C_PLUS || !numeric) {
                // Doesn't impact parsing, ignore
                delimCurrent = false;
            } else if (c >= '0' && c <= '9' && !dashSeen) {
                delimCurrent = false;
            } else if (c == '-') {
                delimCurrent = false;
                dashSeen = true;
            } else {
                // we found non-digit or delimiter. If not last field, exit
                if (numbers > 0) {
                    return numbers;
                }
                // Else flag as nonnumeric and continue
                numeric = false;
            }
        }
        // We got to beginning of string with only numbers, count start as a delimiter
        // and exit
        return numbers + 1;
    }

    /**
     * Get a String in a line of text between two marker strings
     *
     * @param text   Text to search for match
     * @param before Start matching after this text
     * @param after  End matching before this text
     * @return Text between the strings before and after, or empty string if either
     * marker does not exist
     */
    public static String getTextBetweenStrings(String text, String before, String after) {

        String result = Normal.EMPTY;

        if (text.indexOf(before) >= 0 && text.indexOf(after) >= 0) {
            result = text.substring(text.indexOf(before) + before.length(), text.length());
            result = result.substring(0, result.indexOf(after));
        }
        return result;
    }

    /**
     * Convert a long representing filetime (100-ns since 1601 epoch) to ms since
     * 1970 epoch
     *
     * @param filetime A 64-bit value equivalent to FILETIME
     * @param local    True if converting from a local filetime (PDH counter); false if
     *                 already UTC (WMI PerfRawData classes)
     * @return Equivalent milliseconds since the epoch
     */
    public static long filetimeToUtcMs(long filetime, boolean local) {
        return filetime / 10000L - EPOCH_DIFF - (local ? TZ_OFFSET : 0L);
    }

    /**
     * Parse a date in MM-DD-YYYY or MM/DD/YYYY to YYYY-MM-DD
     *
     * @param dateString The date in MM DD YYYY format
     * @return The date in ISO YYYY-MM-DD format if parseable, or the original
     * string
     */
    public static String parseMmDdYyyyToYyyyMmDD(String dateString) {
        try {
            // Date is MM-DD-YYYY, convert to YYYY-MM-DD
            return String.format("%s-%s-%s", dateString.substring(6, 10), dateString.substring(0, 2),
                    dateString.substring(3, 5));
        } catch (StringIndexOutOfBoundsException e) {
            return dateString;
        }
    }

    /**
     * Converts a string in CIM Date Format, as returned by WMI for DateTime types,
     * into a {@link java.time.OffsetDateTime}.
     *
     * @param cimDateTime A non-null DateTime String in CIM date format, e.g.,
     *                    <code>20160513072950.782000-420</code>
     * @return The parsed {@link java.time.OffsetDateTime} if the string is
     * parsable, otherwise {@link Builder#UNIX_EPOCH}.
     */
    public static OffsetDateTime parseCimDateTimeToOffset(String cimDateTime) {
        // Keep first 22 characters: digits, decimal, and + or - sign
        // But alter last 3 characters from a minute offset to hh:mm
        try {
            // From WMI as 20160513072950.782000-420,
            int tzInMinutes = Integer.parseInt(cimDateTime.substring(22));
            // modified to 20160513072950.782000-07:00 which can be parsed
            LocalTime offsetAsLocalTime = LocalTime.MIDNIGHT.plusMinutes(tzInMinutes);
            return OffsetDateTime.parse(
                    cimDateTime.substring(0, 22) + offsetAsLocalTime.format(DateTimeFormatter.ISO_LOCAL_TIME),
                    Builder.CIM_FORMAT);
        } catch (IndexOutOfBoundsException // if cimDate not 22+ chars
                | NumberFormatException // if TZ minutes doesn't parse
                | DateTimeParseException e) {
            Logger.trace("Unable to parse {} to CIM DateTime.", cimDateTime);
            return Builder.UNIX_EPOCH;
        }
    }

    /**
     * Checks if a file path equals or starts with an prefix in the given list
     *
     * @param prefixList A list of path prefixes
     * @param path       a string path to check
     * @return true if the path exactly equals, or starts with one of the strings in
     * prefixList
     */
    public static boolean filePathStartsWith(List<String> prefixList, String path) {
        for (String match : prefixList) {
            if (path.equals(match) || path.startsWith(match + Symbol.SLASH)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses a string such as "4096 MB" to its long. Used to parse macOS and *nix
     * memory chip sizes. Although the units given are decimal they must parse to
     * binary units.
     *
     * @param size A string of memory sizes like "4096 MB"
     * @return the size parsed to a long
     */
    public static long parseDecimalMemorySizeToBinary(String size) {
        String[] mem = Builder.whitespaces.split(size);
        long capacity = Builder.parseLongOrDefault(mem[0], 0L);
        if (mem.length == 2 && mem[1].length() > 1) {
            switch (mem[1].charAt(0)) {
                case 'T':
                    capacity <<= 40;
                    break;
                case 'G':
                    capacity <<= 30;
                    break;
                case 'M':
                    capacity <<= 20;
                    break;
                case 'K':
                case 'k':
                    capacity <<= 10;
                    break;
                default:
                    break;
            }
        }
        return capacity;
    }

    /**
     * Parse a Windows PnPDeviceID to get the vendor ID and product ID.
     *
     * @param pnpDeviceId The PnPDeviceID
     * @return A {@link Pair} where the first element is the vendor ID and second
     * element is the product ID, if parsing was successful, or {@code null}
     * otherwise
     */
    public static Pair<String, String> parsePnPDeviceIdToVendorProductId(String pnpDeviceId) {
        Matcher m = VENDOR_PRODUCT_ID.matcher(pnpDeviceId);
        if (m.matches()) {
            String vendorId = "0x" + m.group(1).toLowerCase();
            String productId = "0x" + m.group(2).toLowerCase();
            return Pair.of(vendorId, productId);
        }
        return null;
    }

    /**
     * Parse a Linux lshw resources string to calculate the memory size
     *
     * @param resources A string containing one or more elements of the form
     *                  {@code memory:b00000000-bffffffff}
     * @return The number of bytes consumed by the memory in the {@code resources}
     * string
     */
    public static long parseLshwResourceString(String resources) {
        long bytes = 0L;
        // First split by whitespace
        String[] resourceArray = whitespaces.split(resources);
        for (String r : resourceArray) {
            // Remove prefix
            if (r.startsWith("memory:")) {
                // Split to low and high
                String[] mem = r.substring(7).split(Symbol.HYPHEN);
                if (mem.length == 2) {
                    try {
                        // Parse the hex strings
                        bytes += Long.parseLong(mem[1], 16) - Long.parseLong(mem[0], 16) + 1;
                    } catch (NumberFormatException e) {
                        Logger.trace(DEFAULT_Logger_MSG, r, e);
                    }
                }
            }
        }
        return bytes;
    }

    /**
     * Parse a Linux lspci machine readble line to its name and id
     *
     * @param line A string in the form Foo [bar]
     * @return A pair separating the String before the square brackets and within
     * them if found, null otherwise
     */
    public static Pair<String, String> parseLspciMachineReadable(String line) {
        Matcher matcher = LSPCI_MACHINE_READABLE.matcher(line);
        if (matcher.matches()) {
            return Pair.of(matcher.group(1), matcher.group(2));
        }
        return null;
    }

    /**
     * Parse a Linux lspci line containing memory size
     *
     * @param line A string in the form Foo [size=256M]
     * @return A the memory size in bytes
     */
    public static long parseLspciMemorySize(String line) {
        Matcher matcher = LSPCI_MEMORY_SIZE.matcher(line);
        if (matcher.matches()) {
            return parseDecimalMemorySizeToBinary(matcher.group(1) + Symbol.SPACE + matcher.group(2) + "B");
        }
        return 0;
    }

    /**
     * Parse a space-delimited list of integers which include hyphenated ranges to a
     * list of just the integers. For example, 0 1 4-7 parses to a list containing
     * 0, 1, 4, 5, 6, and 7.
     *
     * @param str A string containing space-delimited integers or ranges of integers
     *            with a hyphen
     * @return A list of integers representing the provided range(s).
     */
    public static List<Integer> parseHyphenatedIntList(String str) {
        List<Integer> result = new ArrayList<>();
        for (String s : whitespaces.split(str)) {
            if (s.contains(Symbol.HYPHEN)) {
                int first = getFirstIntValue(s);
                int last = getNthIntValue(s, 2);
                for (int i = first; i <= last; i++) {
                    result.add(i);
                }
            } else {
                int only = Builder.parseIntOrDefault(s, -1);
                if (only >= 0) {
                    result.add(only);
                }
            }
        }
        return result;
    }

    /**
     * 输出到<code>StringBuilder</code>
     *
     * @param builder <code>StringBuilder</code>对象
     * @param caption 标题
     * @param value   值
     */
    public static void append(StringBuilder builder, String caption, Object value) {
        builder.append(caption).append(StringUtils.nullToDefault(Convert.toString(value), "[n/a]")).append("\n");
    }

    /**
     * 获取传感器相关信息，例如CPU温度、风扇转速等，传感器可能有多个
     *
     * @return 传感器相关信息
     */
    public Sensors getSensors() {
        return hardware.getSensors();
    }

    /**
     * 获取磁盘相关信息，可能有多个磁盘(包括可移动磁盘等)
     *
     * @return 磁盘相关信息
     */
    public List<HWDiskStore> getDiskStores() {
        return hardware.getDiskStores();
    }

    /**
     * 获取网络相关信息，可能多块网卡
     *
     * @return 网络相关信息
     */
    public static List<NetworkIF> getNetworkIFs() {
        return hardware.getNetworkIFs();
    }

    /**
     * 获取系统CPU 系统使用率、用户使用率、利用率等等 相关信息
     *
     * @return 系统 CPU 使用率 等信息
     */
    public static CpuInfo getCpuInfo() {
        return getCpuInfo(1000);
    }

    /**
     * 获取系统CPU 系统使用率、用户使用率、利用率等等 相关信息
     *
     * @param waitingTime 设置等待时间
     * @return 系统 CPU 使用率 等信息
     */
    public static CpuInfo getCpuInfo(long waitingTime) {
        return getCpuInfo(getProcessor(), waitingTime);
    }

    /**
     * 获取系统CPU 系统使用率、用户使用率、利用率等等 相关信息
     *
     * @param processor   {@link CentralProcessor}
     * @param waitingTime 设置等待时间
     * @return 系统 CPU 使用率 等信息
     */
    private static CpuInfo getCpuInfo(CentralProcessor processor, long waitingTime) {
        CpuInfo cpuInfo = new CpuInfo();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        sleep(waitingTime);
        long[] ticks = processor.getSystemCpuLoadTicks();
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softIrq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long ioWait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long totalCpu = Math.max(user + nice + cSys + idle + ioWait + irq + softIrq + steal, 0);
        final DecimalFormat format = new DecimalFormat("#.00");
        cpuInfo.setCpuNum(processor.getLogicalProcessorCount());
        cpuInfo.setToTal(totalCpu);
        cpuInfo.setSys(Double.parseDouble(format.format(cSys <= 0 ? 0 : (100d * cSys / totalCpu))));
        cpuInfo.setUsed(Double.parseDouble(format.format(user <= 0 ? 0 : (100d * user / totalCpu))));
        if (totalCpu == 0) {
            cpuInfo.setWait(0);
        } else {
            cpuInfo.setWait(Double.parseDouble(format.format(100d * ioWait / totalCpu)));
        }
        cpuInfo.setFree(Double.parseDouble(format.format(idle <= 0 ? 0 : (100d * idle / totalCpu))));
        cpuInfo.setCpuModel(processor.toString());
        return cpuInfo;
    }

    static class CpuInfo {

        /**
         * cpu核心数
         */
        private Integer cpuNum;

        /**
         * CPU总的使用率
         */
        private double toTal;

        /**
         * CPU系统使用率
         */
        private double sys;

        /**
         * CPU用户使用率
         */
        private double used;

        /**
         * CPU当前等待率
         */
        private double wait;

        /**
         * CPU当前空闲率
         */
        private double free;

        /**
         * CPU型号信息
         */
        private String cpuModel;

        public CpuInfo() {
        }

        public CpuInfo(Integer cpuNum,
                       double toTal,
                       double sys,
                       double used,
                       double wait,
                       double free,
                       String cpuModel) {
            this.cpuNum = cpuNum;
            this.toTal = toTal;
            this.sys = sys;
            this.used = used;
            this.wait = wait;
            this.free = free;
            this.cpuModel = cpuModel;
        }

        public Integer getCpuNum() {
            return cpuNum;
        }

        public void setCpuNum(Integer cpuNum) {
            this.cpuNum = cpuNum;
        }

        public double getToTal() {
            return toTal;
        }

        public void setToTal(double toTal) {
            this.toTal = toTal;
        }

        public double getSys() {
            return sys;
        }

        public void setSys(double sys) {
            this.sys = sys;
        }

        public double getUsed() {
            return used;
        }

        public void setUsed(double used) {
            this.used = used;
        }

        public double getWait() {
            return wait;
        }

        public void setWait(double wait) {
            this.wait = wait;
        }

        public double getFree() {
            return free;
        }

        public void setFree(double free) {
            this.free = free;
        }

        public String getCpuModel() {
            return cpuModel;
        }

        public void setCpuModel(String cpuModel) {
            this.cpuModel = cpuModel;
        }

        @Override
        public String toString() {
            return "CpuInfo{"
                    + " CPU型号信息=" + cpuModel
                    + ", CPU核心数=" + cpuNum
                    + ", CPU总的使用率=" + toTal
                    + ", CPU系统使用率=" + sys
                    + ", CPU用户使用率=" + used
                    + ", CPU当前等待率=" + wait
                    + ", CPU当前空闲率=" + free
                    + ", CPU利用率=" + Double.parseDouble(new DecimalFormat("#.00").format((100 - getFree()))) +
                    "}";
        }
    }

}
