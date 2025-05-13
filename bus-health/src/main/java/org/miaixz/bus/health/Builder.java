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
package org.miaixz.bus.health;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.CharsetDecoder;
import java.nio.file.*;
import java.nio.file.FileSystem;
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
 * 通用工具方法
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class Builder {

    /** Unix 纪元时间，当 WMI DateTime 查询无返回值时使用的默认值 */
    public static final OffsetDateTime UNIX_EPOCH = OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);

    /** 通配符模式前缀：glob */
    private static final String GLOB_PREFIX = "glob:";

    /** 正则表达式模式前缀：regex */
    private static final String REGEX_PREFIX = "regex:";

    /** 日志消息：正在读取文件 */
    private static final String READING_LOG = "Reading file {}";

    /** 日志消息：已读取内容 */
    private static final String READ_LOG = "Read {}";

    /**
     * 测试字符串是否与通配符模式匹配。
     *
     * @param text    要测试的字符串
     * @param pattern 包含通配符的模式字符串，其中 ? 表示单个字符，* 表示任意数量的字符。 如果模式的第一个字符是 ^，则对剩余字符进行测试，并返回相反的结果。
     * @return 如果字符串匹配，或者模式以 ^ 开头且剩余部分不匹配，则返回 true
     */
    public static boolean wildcardMatch(String text, String pattern) {
        if (pattern.length() > 0 && pattern.charAt(0) == Symbol.C_CARET) {
            return !wildcardMatch(text, pattern.substring(1));
        }
        return text.matches(pattern.replace("?", ".?").replace(Symbol.STAR, ".*?"));
    }

    /**
     * 如果给定的指针是 Memory 类的实例，则调用其 close 方法以释放本地分配的内存。
     *
     * @param p 指针
     */
    public static void freeMemory(Pointer p) {
        if (p instanceof Memory) {
            ((Memory) p).close();
        }
    }

    /**
     * 测试用户在设备上的会话是否有效。
     *
     * @param user      登录的用户
     * @param device    用户使用的设备
     * @param loginTime 用户的登录时间
     * @return 如果会话有效返回 true；如果用户或设备为空，或登录时间小于 0 或大于当前时间，则返回 false
     */
    public static boolean isSessionValid(String user, String device, Long loginTime) {
        return !(user.isEmpty() || device.isEmpty() || loginTime < 0 || loginTime > System.currentTimeMillis());
    }

    /**
     * 根据配置判断文件存储（由 {@code path} 和 {@code volume} 标识）是否应被排除。
     * <p>
     * 包含优先于排除。如果未指定排除/包含模式，则文件存储不被排除。
     *
     * @param path           文件存储的挂载点
     * @param volume         文件存储卷
     * @param pathIncludes   路径包含模式列表
     * @param pathExcludes   路径排除模式列表
     * @param volumeIncludes 卷包含模式列表
     * @param volumeExcludes 卷排除模式列表
     * @return 如果文件存储应被排除返回 true，否则返回 false
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
     * 从配置加载并解析文件系统包含/排除行。
     *
     * @param configPropertyName 包含要解析行的配置属性名称
     * @return 用于匹配文件存储卷和路径的 PathMatcher 列表
     */
    public static List<PathMatcher> loadAndParseFileSystemConfig(String configPropertyName) {
        String config = Config.get(configPropertyName, Normal.EMPTY);
        return parseFileSystemConfig(config);
    }

    /**
     * 解析文件系统包含/排除行。
     *
     * @param config 要解析的配置行
     * @return 用于匹配文件存储卷和路径的 PathMatcher 列表
     */
    public static List<PathMatcher> parseFileSystemConfig(String config) {
        FileSystem fs = FileSystems.getDefault(); // 不可关闭
        List<PathMatcher> patterns = new ArrayList<>();
        for (String item : config.split(Symbol.COMMA)) {
            if (item.length() > 0) {
                // 默认使用 glob: 前缀，除非用户指定了 glob 或 regex
                if (!(item.startsWith(GLOB_PREFIX) || item.startsWith(REGEX_PREFIX))) {
                    item = GLOB_PREFIX + item;
                }
                patterns.add(fs.getPathMatcher(item));
            }
        }
        return patterns;
    }

    /**
     * 检查 {@code text} 是否与 {@param patterns} 中的任意模式匹配。
     *
     * @param text     要匹配的文本
     * @param patterns 模式列表
     * @return 如果给定文本至少匹配一个 glob 模式返回 true，否则返回 false
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
     * 从字节 8 和 9 中的（最多）3 个 5 位字符获取制造商 ID。
     *
     * @param edid EDID 字节数组
     * @return 制造商 ID
     */
    public static String getManufacturerID(byte[] edid) {
        // 字节 8-9 是制造商 ID，用 3 个 5 位字符表示
        String temp = String.format(Locale.ROOT, "%8s%8s", Integer.toBinaryString(edid[8] & 0xFF),
                Integer.toBinaryString(edid[9] & 0xFF)).replace(Symbol.C_SPACE, '0');
        Logger.debug("Manufacurer ID: {}", temp);
        return String.format(Locale.ROOT, "%s%s%s", (char) (64 + Integer.parseInt(temp.substring(1, 6), 2)),
                (char) (64 + Integer.parseInt(temp.substring(6, 11), 2)),
                (char) (64 + Integer.parseInt(temp.substring(11, 16), 2))).replace("@", "");
    }

    /**
     * 获取产品 ID，字节 10 和 11。
     *
     * @param edid EDID 字节数组
     * @return 产品 ID
     */
    public static String getProductID(byte[] edid) {
        // 字节 10-11 是产品 ID，以十六进制字符表示
        return Integer.toHexString(
                ByteBuffer.wrap(Arrays.copyOfRange(edid, 10, 12)).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xffff);
    }

    /**
     * 获取序列号，字节 12-15。
     *
     * @param edid EDID 字节数组
     * @return 如果所有 4 个字节表示字母数字字符，则返回 4 字符字符串；否则返回十六进制字符串
     */
    public static String getSerialNo(byte[] edid) {
        // 字节 12-15 是序列号（最后 4 个字符）
        if (Logger.isDebugEnabled()) {
            Logger.debug("Serial number: {}", Arrays.toString(Arrays.copyOfRange(edid, 12, 16)));
        }
        return String.format(Locale.ROOT, "%s%s%s%s", getAlphaNumericOrHex(edid[15]), getAlphaNumericOrHex(edid[14]),
                getAlphaNumericOrHex(edid[13]), getAlphaNumericOrHex(edid[12]));
    }

    /**
     * 将字节转换为字母数字字符或十六进制表示。
     *
     * @param b 要转换的字节
     * @return 如果字节是字母或数字，返回对应的字符；否则返回两位十六进制字符串
     */
    private static String getAlphaNumericOrHex(byte b) {
        return Character.isLetterOrDigit((char) b) ? String.format(Locale.ROOT, "%s", (char) b)
                : String.format(Locale.ROOT, "%02X", b);
    }

    /**
     * 返回制造年份的周数。
     *
     * @param edid EDID 字节数组
     * @return 制造周数
     */
    public static byte getWeek(byte[] edid) {
        // 字节 16 是制造周
        return edid[16];
    }

    /**
     * 返回制造年份。
     *
     * @param edid EDID 字节数组
     * @return 制造年份
     */
    public static int getYear(byte[] edid) {
        // 字节 17 是制造年份减去 1990
        byte temp = edid[17];
        Logger.debug("Year-1990: {}", temp);
        return temp + 1990;
    }

    /**
     * 返回 EDID 版本。
     *
     * @param edid EDID 字节数组
     * @return EDID 版本
     */
    public static String getVersion(byte[] edid) {
        // 字节 18-19 是 EDID 版本
        return edid[18] + "." + edid[19];
    }

    /**
     * 根据字节 20 测试此 EDID 是否为数字显示器。
     *
     * @param edid EDID 字节数组
     * @return 如果 EDID 表示数字显示器返回 true，否则返回 false
     */
    public static boolean isDigital(byte[] edid) {
        // 字节 20 是视频输入参数
        return 1 == (edid[20] & 0xff) >> 7;
    }

    /**
     * 获取显示器宽度（厘米）。
     *
     * @param edid EDID 字节数组
     * @return 显示器宽度（厘米）
     */
    public static int getHcm(byte[] edid) {
        // 字节 21 是水平尺寸（厘米）
        return edid[21];
    }

    /**
     * 获取显示器高度（厘米）。
     *
     * @param edid EDID 字节数组
     * @return 显示器高度（厘米）
     */
    public static int getVcm(byte[] edid) {
        // 字节 22 是垂直尺寸（厘米）
        return edid[22];
    }

    /**
     * 获取 VESA 描述符。
     *
     * @param edid EDID 字节数组
     * @return 一个包含四个 18 字节元素的二维数组，表示 VESA 描述符
     */
    public static byte[][] getDescriptors(byte[] edid) {
        byte[][] desc = new byte[4][18];
        for (int i = 0; i < desc.length; i++) {
            System.arraycopy(edid, 54 + 18 * i, desc[i], 0, 18);
        }
        return desc;
    }

    /**
     * 获取 VESA 描述符类型。
     *
     * @param desc 18 字节的 VESA 描述符
     * @return 表示 VESA 描述符前四个字节的整数
     */
    public static int getDescriptorType(byte[] desc) {
        return ByteBuffer.wrap(Arrays.copyOfRange(desc, 0, 4)).getInt();
    }

    /**
     * 解析详细时序描述符。
     *
     * @param desc 18 字节的 VESA 描述符
     * @return 描述详细时序描述符部分的字符串
     */
    public static String getTimingDescriptor(byte[] desc) {
        int clock = ByteBuffer.wrap(Arrays.copyOfRange(desc, 0, 2)).order(ByteOrder.LITTLE_ENDIAN).getShort() / 100;
        int hActive = (desc[2] & 0xff) + ((desc[4] & 0xf0) << 4);
        int vActive = (desc[5] & 0xff) + ((desc[7] & 0xf0) << 4);
        return String.format(Locale.ROOT, "Clock %dMHz, Active Pixels %dx%d ", clock, hActive, vActive);
    }

    /**
     * 解析描述符范围限制。
     *
     * @param desc 18 字节的 VESA 描述符
     * @return 描述部分范围限制的字符串
     */
    public static String getDescriptorRangeLimits(byte[] desc) {
        return String.format(Locale.ROOT, "Field Rate %d-%d Hz vertical, %d-%d Hz horizontal, Max clock: %d MHz",
                desc[5], desc[6], desc[7], desc[8], desc[9] * 10);
    }

    /**
     * 解析描述符文本。
     *
     * @param desc 18 字节的 VESA 描述符
     * @return 从第 4 个字节开始的纯文本
     */
    public static String getDescriptorText(byte[] desc) {
        return new String(Arrays.copyOfRange(desc, 4, 18), Charset.US_ASCII).trim();
    }

    /**
     * 将 EDID 字节数组解析为用户可读信息。
     *
     * @param edid EDID 字节数组
     * @return EDID 表示的用户可读文本
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
     * 一次性读取整个文件。主要用于 Linux /proc 文件系统，以避免在迭代读取时重新计算文件内容。
     *
     * @param filename 要读取的文件
     * @return 表示文件每行的字符串列表，如果文件无法读取或为空则返回空列表
     */
    public static List<String> readFile(String filename) {
        return readFile(filename, true);
    }

    /**
     * 一次性读取整个文件。主要用于 Linux /proc 文件系统，以避免在迭代读取时重新计算文件内容。
     *
     * @param filename    要读取的文件
     * @param reportError 是否记录读取文件的错误
     * @return 表示文件每行的字符串列表，如果文件无法读取或为空则返回空列表
     */
    public static List<String> readFile(String filename, boolean reportError) {
        if (new File(filename).canRead()) {
            if (Logger.isDebugEnabled()) {
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
     * 从文件中读取指定行数。主要用于 Linux /proc 文件系统，以避免在迭代读取时重新计算文件内容。
     *
     * @param filename 要读取的文件
     * @param count    要读取的行数
     * @return 表示文件前 count 行的字符串列表，如果文件无法读取或为空则返回空列表
     */
    public static List<String> readLines(String filename, int count) {
        return readLines(filename, count, true);
    }

    /**
     * 从文件中读取指定行数。主要用于 Linux /proc 文件系统，以避免在迭代读取时重新计算文件内容。
     *
     * @param filename    要读取的文件
     * @param count       要读取的行数
     * @param reportError 是否记录读取文件的错误
     * @return 表示文件前 count 行的字符串列表，如果文件无法读取或为空则返回空列表
     */
    public static List<String> readLines(String filename, int count, boolean reportError) {
        Path file = Paths.get(filename);
        if (Files.isReadable(file)) {
            if (Logger.isDebugEnabled()) {
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
     * 一次性读取整个文件。主要用于 Linux /proc 文件系统，以避免在迭代读取时重新计算文件内容。
     *
     * @param filename 要读取的文件
     * @return 表示文件的字节数组
     */
    public static byte[] readAllBytes(String filename) {
        return readAllBytes(filename, true);
    }

    /**
     * 一次性读取整个文件。主要用于 Linux /proc 文件系统，以避免在迭代读取时重新计算文件内容。
     *
     * @param filename    要读取的文件
     * @param reportError 是否记录读取文件的错误
     * @return 表示文件的字节数组
     */
    public static byte[] readAllBytes(String filename, boolean reportError) {
        if (new File(filename).canRead()) {
            if (Logger.isDebugEnabled()) {
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
     * 一次性读取整个文件。主要用于 Unix /proc 二进制文件，以避免在迭代读取时重新计算文件内容。
     *
     * @param filename 要读取的文件
     * @return 如果读取成功，返回表示文件的 ByteBuffer；否则返回 null
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
     * 从 ByteBuffer 读取字节值。
     *
     * @param buff 要读取的 ByteBuffer
     * @return 下一个字节值
     */
    public static byte readByteFromBuffer(ByteBuffer buff) {
        if (buff.position() < buff.limit()) {
            return buff.get();
        }
        return 0;
    }

    /**
     * 从 ByteBuffer 读取短整型值。
     *
     * @param buff 要读取的 ByteBuffer
     * @return 下一个短整型值
     */
    public static short readShortFromBuffer(ByteBuffer buff) {
        if (buff.position() <= buff.limit() - 2) {
            return buff.getShort();
        }
        return 0;
    }

    /**
     * 从 ByteBuffer 读取整型值。
     *
     * @param buff 要读取的 ByteBuffer
     * @return 下一个整型值
     */
    public static int readIntFromBuffer(ByteBuffer buff) {
        if (buff.position() <= buff.limit() - 4) {
            return buff.getInt();
        }
        return 0;
    }

    /**
     * 从 ByteBuffer 读取长整型值。
     *
     * @param buff 要读取的 ByteBuffer
     * @return 下一个长整型值
     */
    public static long readLongFromBuffer(ByteBuffer buff) {
        if (buff.position() <= buff.limit() - 8) {
            return buff.getLong();
        }
        return 0L;
    }

    /**
     * 从 ByteBuffer 读取 NativeLong 值。
     *
     * @param buff 要读取的 ByteBuffer
     * @return 下一个 NativeLong 值
     */
    public static NativeLong readNativeLongFromBuffer(ByteBuffer buff) {
        return new NativeLong(Native.LONG_SIZE == 4 ? readIntFromBuffer(buff) : readLongFromBuffer(buff));
    }

    /**
     * 从 ByteBuffer 读取 size_t 值。
     *
     * @param buff 要读取的 ByteBuffer
     * @return 下一个 size_t 值
     */
    public static LibCAPI.size_t readSizeTFromBuffer(ByteBuffer buff) {
        return new LibCAPI.size_t(Native.SIZE_T_SIZE == 4 ? readIntFromBuffer(buff) : readLongFromBuffer(buff));
    }

    /**
     * 从 ByteBuffer 读取字节数组值。
     *
     * @param buff  要读取的 ByteBuffer
     * @param array 要读取数据的目标数组
     */
    public static void readByteArrayFromBuffer(ByteBuffer buff, byte[] array) {
        if (buff.position() <= buff.limit() - array.length) {
            buff.get(array);
        }
    }

    /**
     * 从 ByteBuffer 读取 Pointer 值。
     *
     * @param buff 要读取的 ByteBuffer
     * @return 下一个 Pointer 值
     */
    public static Pointer readPointerFromBuffer(ByteBuffer buff) {
        if (buff.position() <= buff.limit() - Native.POINTER_SIZE) {
            return Native.POINTER_SIZE == 4 ? new Pointer(buff.getInt()) : new Pointer(buff.getLong());
        }
        return Pointer.NULL;
    }

    /**
     * 读取文件并返回其中包含的长整型值。主要用于 Linux /sys 文件系统。
     *
     * @param filename 要读取的文件
     * @return 文件中包含的值，如果无值则返回 0
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
     * 读取文件并返回其中包含的无符号长整型值（作为长整型）。主要用于 Linux /sys 文件系统。
     *
     * @param filename 要读取的文件
     * @return 文件中包含的值，如果无值则返回 0
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
     * 读取文件并返回其中包含的整型值。主要用于 Linux /sys 文件系统。
     *
     * @param filename 要读取的文件
     * @return 文件中包含的值，如果无值则返回 0
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
     * 读取文件并返回其中包含的字符串值。主要用于 Linux /sys 文件系统。
     *
     * @param filename 要读取的文件
     * @return 文件中包含的值，如果无值则返回空字符串
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
     * 读取文件并返回其中包含的字符串键值对映射。主要用于 Linux {@code /proc/[pid]} 文件， 以提供比 API 更详细或准确的信息。
     *
     * @param filename  要读取的文件
     * @param separator 文件每行中分隔键和值的字符
     * @return 文件中由分隔符分隔的键值对映射，值已去除空格。如果无法解析键值对，返回空映射
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
     * 读取符号链接的目标。
     *
     * @param file 要读取的文件
     * @return 符号链接名称，如果读取失败则返回 null
     */
    public static String readSymlinkTarget(File file) {
        try {
            return Files.readSymbolicLink(Paths.get(file.getAbsolutePath())).toString();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 将键值对追加到 {@code StringBuilder}
     *
     * @param builder {@code StringBuilder} 对象
     * @param caption 标题
     * @param value   值
     */
    public static void append(StringBuilder builder, String caption, Object value) {
        builder.append(caption).append(ObjectKit.defaultIfNull(Convert.toString(value), "[n/a]")).append(Symbol.LF);
    }

}