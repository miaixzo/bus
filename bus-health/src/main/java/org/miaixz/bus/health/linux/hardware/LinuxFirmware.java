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
package org.miaixz.bus.health.linux.hardware;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.hardware.common.AbstractFirmware;
import org.miaixz.bus.health.linux.driver.Dmidecode;
import org.miaixz.bus.health.linux.driver.Sysfs;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * Firmware data obtained by sysfs.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
final class LinuxFirmware extends AbstractFirmware {

    // Jan 13 2013 16:24:29
    private static final DateTimeFormatter VCGEN_FORMATTER = DateTimeFormatter.ofPattern("MMM d uuuu HH:mm:ss",
            Locale.ENGLISH);
    private final Supplier<VcGenCmdStrings> vcGenCmd = Memoizer.memoize(LinuxFirmware::queryVcGenCmd);
    private final Supplier<String> manufacturer = Memoizer.memoize(this::queryManufacturer);
    private final Supplier<String> description = Memoizer.memoize(this::queryDescription);
    private final Supplier<String> releaseDate = Memoizer.memoize(this::queryReleaseDate);
    private final Supplier<Pair<String, String>> biosNameRev = Memoizer.memoize(Dmidecode::queryBiosNameRev);
    private final Supplier<String> version = Memoizer.memoize(this::queryVersion);
    private final Supplier<String> name = Memoizer.memoize(this::queryName);

    private static VcGenCmdStrings queryVcGenCmd() {
        String vcReleaseDate;
        String vcManufacturer;
        String vcVersion;

        List<String> vcgencmd = Executor.runNative("vcgencmd version");
        if (vcgencmd.size() >= 3) {
            // First line is date
            try {
                vcReleaseDate = DateTimeFormatter.ISO_LOCAL_DATE.format(VCGEN_FORMATTER.parse(vcgencmd.get(0)));
            } catch (DateTimeParseException e) {
                vcReleaseDate = Normal.UNKNOWN;
            }
            // Second line is copyright
            String[] copyright = Pattern.SPACES_PATTERN.split(vcgencmd.get(1));
            vcManufacturer = copyright[copyright.length - 1];
            // Third line is version
            vcVersion = vcgencmd.get(2).replace("version ", Normal.EMPTY);
            return new VcGenCmdStrings(vcReleaseDate, vcManufacturer, vcVersion, "RPi", "Bootloader");
        }
        return new VcGenCmdStrings(null, null, null, null, null);
    }

    @Override
    public String getManufacturer() {
        return manufacturer.get();
    }

    @Override
    public String getDescription() {
        return description.get();
    }

    @Override
    public String getVersion() {
        return version.get();
    }

    @Override
    public String getReleaseDate() {
        return releaseDate.get();
    }

    @Override
    public String getName() {
        return name.get();
    }

    private String queryManufacturer() {
        String result;
        if ((result = Sysfs.queryBiosVendor()) == null && (result = vcGenCmd.get().manufacturer) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    private String queryDescription() {
        String result;
        if ((result = Sysfs.queryBiosDescription()) == null && (result = vcGenCmd.get().description) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    private String queryVersion() {
        String result;
        if ((result = Sysfs.queryBiosVersion(this.biosNameRev.get().getRight())) == null
                && (result = vcGenCmd.get().version) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    private String queryReleaseDate() {
        String result;
        if ((result = Sysfs.queryBiosReleaseDate()) == null && (result = vcGenCmd.get().releaseDate) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    private String queryName() {
        String result;
        if ((result = biosNameRev.get().getLeft()) == null && (result = vcGenCmd.get().name) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    private static final class VcGenCmdStrings {
        private final String releaseDate;
        private final String manufacturer;
        private final String version;
        private final String name;
        private final String description;

        private VcGenCmdStrings(String releaseDate, String manufacturer, String version, String name,
                String description) {
            this.releaseDate = releaseDate;
            this.manufacturer = manufacturer;
            this.version = version;
            this.name = name;
            this.description = description;
        }
    }

}
