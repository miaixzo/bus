/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               ~
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
package org.miaixz.bus.health.linux.driver.proc;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Tuple;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.linux.ProcPath;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Utility to read CPU info from {@code /proc/cpuinfo}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class CpuInfo {

    /**
     * Gets the CPU manufacturer from {@code /proc/cpuinfo}
     *
     * @return The manufacturer if known, null otherwise
     */
    public static String queryCpuManufacturer() {
        List<String> cpuInfo = Builder.readFile(ProcPath.CPUINFO);
        for (String line : cpuInfo) {
            if (line.startsWith("CPU implementer")) {
                int part = Parsing.parseLastInt(line, 0);
                switch (part) {
                case 0x41:
                    return "ARM";
                case 0x42:
                    return "Broadcom";
                case 0x43:
                    return "Cavium";
                case 0x44:
                    return "DEC";
                case 0x4e:
                    return "Nvidia";
                case 0x50:
                    return "APM";
                case 0x51:
                    return "Qualcomm";
                case 0x53:
                    return "Samsung";
                case 0x56:
                    return "Marvell";
                case 0x66:
                    return "Faraday";
                case 0x69:
                    return "Intel";
                default:
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Gets the board manufacturer, model, version, and serial number from {@code /proc/cpuinfo}
     *
     * @return A quartet of strings for manufacturer, model, version, and serial number. Each one may be null if
     *         unknown.
     */
    public static Tuple queryBoardInfo() {
        String pcManufacturer = null;
        String pcModel = null;
        String pcVersion = null;
        String pcSerialNumber = null;

        List<String> cpuInfo = Builder.readFile(ProcPath.CPUINFO);
        for (String line : cpuInfo) {
            String[] splitLine = Pattern.SPACES_COLON_SPACE_PATTERN.split(line);
            if (splitLine.length < 2) {
                continue;
            }
            switch (splitLine[0]) {
            case "Hardware":
                pcModel = splitLine[1];
                break;
            case "Revision":
                pcVersion = splitLine[1];
                if (pcVersion.length() > 1) {
                    pcManufacturer = queryBoardManufacturer(pcVersion.charAt(1));
                }
                break;
            case "Serial":
                pcSerialNumber = splitLine[1];
                break;
            default:
                // Do nothing
            }
        }
        return new Tuple(pcManufacturer, pcModel, pcVersion, pcSerialNumber);
    }

    private static String queryBoardManufacturer(char digit) {
        switch (digit) {
        case '0':
            return "Sony UK";
        case '1':
            return "Egoman";
        case '2':
            return "Embest";
        case '3':
            return "Sony Japan";
        case '4':
            return "Embest";
        case '5':
            return "Stadium";
        default:
            return Normal.UNKNOWN;
        }
    }

    public static List<String> queryFeatureFlags() {
        return Builder.readFile(ProcPath.CPUINFO).stream().filter(f -> {
            String s = f.toLowerCase(Locale.ROOT);
            return s.startsWith("flags") || s.startsWith("features");
        }).distinct().collect(Collectors.toList());
    }

}
