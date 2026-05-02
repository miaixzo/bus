/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.           ~
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
package org.miaixz.bus.health.linux.hardware;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.hardware.common.AbstractFirmware;
import org.miaixz.bus.health.linux.driver.Dmidecode;
import org.miaixz.bus.health.linux.driver.Sysfs;

/**
 * Firmware data obtained by sysfs.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
final class LinuxFirmware extends AbstractFirmware {

    // Jan 13 2013 16:24:29
    /**
     * The VCGEN_FORMATTER constant.
     */
    private static final DateTimeFormatter VCGEN_FORMATTER = DateTimeFormatter
            .ofPattern("MMM d uuuu HH:mm:ss", Locale.ENGLISH);
    /**
     * The vcGenCmd value.
     */
    private final Supplier<VcGenCmdStrings> vcGenCmd = Memoizer.memoize(LinuxFirmware::queryVcGenCmd);
    /**
     * The manufacturer value.
     */
    private final Supplier<String> manufacturer = Memoizer.memoize(this::queryManufacturer);
    /**
     * The description value.
     */
    private final Supplier<String> description = Memoizer.memoize(this::queryDescription);
    /**
     * The releaseDate value.
     */
    private final Supplier<String> releaseDate = Memoizer.memoize(this::queryReleaseDate);
    /**
     * The biosNameRev value.
     */
    private final Supplier<Pair<String, String>> biosNameRev = Memoizer.memoize(Dmidecode::queryBiosNameRev);
    /**
     * The version value.
     */
    private final Supplier<String> version = Memoizer.memoize(this::queryVersion);
    /**
     * The name value.
     */
    private final Supplier<String> name = Memoizer.memoize(this::queryName);

    /**
     * Queries the vc gen cmd.
     *
     * @return the query vc gen cmd result
     */
    private static VcGenCmdStrings queryVcGenCmd() {
        return queryVcGenCmd(Executor.runNative("vcgencmd version"));
    }

    /**
     * Parse vcgencmd version output for Raspberry Pi firmware info.
     *
     * @param vcgencmd output of {@code vcgencmd version}
     * @return parsed firmware strings
     */
    static VcGenCmdStrings queryVcGenCmd(List<String> vcgencmd) {
        String vcReleaseDate;
        String vcManufacturer;
        String vcVersion;

        if (vcgencmd.size() >= 3) {
            // First line is date
            try {
                vcReleaseDate = DateTimeFormatter.ISO_LOCAL_DATE.format(VCGEN_FORMATTER.parse(vcgencmd.get(0)));
            } catch (DateTimeParseException e) {
                vcReleaseDate = Normal.UNKNOWN;
            }
            // Second line is copyright
            String[] copyright = Pattern.SPACES_PATTERN.split(vcgencmd.get(1));
            vcManufacturer = copyright.length > 0 && !copyright[copyright.length - 1].isEmpty()
                    ? copyright[copyright.length - 1]
                    : Normal.UNKNOWN;
            // Third line is version
            vcVersion = vcgencmd.get(2).replace("version ", Normal.EMPTY);
            return new VcGenCmdStrings(vcReleaseDate, vcManufacturer, vcVersion, "RPi", "Bootloader");
        }
        return new VcGenCmdStrings(null, null, null, null, null);
    }

    /**
     * Returns the manufacturer.
     *
     * @return the get manufacturer result
     */
    @Override
    public String getManufacturer() {
        return manufacturer.get();
    }

    /**
     * Returns the description.
     *
     * @return the get description result
     */
    @Override
    public String getDescription() {
        return description.get();
    }

    /**
     * Returns the version.
     *
     * @return the get version result
     */
    @Override
    public String getVersion() {
        return version.get();
    }

    /**
     * Returns the release date.
     *
     * @return the get release date result
     */
    @Override
    public String getReleaseDate() {
        return releaseDate.get();
    }

    /**
     * Returns the name.
     *
     * @return the get name result
     */
    @Override
    public String getName() {
        return name.get();
    }

    /**
     * Queries the manufacturer.
     *
     * @return the query manufacturer result
     */
    private String queryManufacturer() {
        String result;
        if ((result = Sysfs.queryBiosVendor()) == null && (result = vcGenCmd.get().getManufacturer()) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    /**
     * Queries the description.
     *
     * @return the query description result
     */
    private String queryDescription() {
        String result;
        if ((result = Sysfs.queryBiosDescription()) == null && (result = vcGenCmd.get().getDescription()) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    /**
     * Queries the version.
     *
     * @return the query version result
     */
    private String queryVersion() {
        String result;
        if ((result = Sysfs.queryBiosVersion(this.biosNameRev.get().getRight())) == null
                && (result = vcGenCmd.get().getVersion()) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    /**
     * Queries the release date.
     *
     * @return the query release date result
     */
    private String queryReleaseDate() {
        String result;
        if ((result = Sysfs.queryBiosReleaseDate()) == null && (result = vcGenCmd.get().getReleaseDate()) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    /**
     * Queries the name.
     *
     * @return the query name result
     */
    private String queryName() {
        String result;
        if ((result = biosNameRev.get().getLeft()) == null && (result = vcGenCmd.get().getName()) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    /**
     * The VcGenCmdStrings class.
     */
    static final class VcGenCmdStrings {

        /**
         * The releaseDate value.
         */
        private final String releaseDate;
        /**
         * The manufacturer value.
         */
        private final String manufacturer;
        /**
         * The version value.
         */
        private final String version;
        /**
         * The name value.
         */
        private final String name;
        /**
         * The description value.
         */
        private final String description;

        /**
         * Creates a new VcGenCmdStrings instance.
         *
         * @param releaseDate  the release date
         * @param manufacturer the manufacturer
         * @param version      the version
         * @param name         the name
         * @param description  the description
         */
        VcGenCmdStrings(String releaseDate, String manufacturer, String version, String name, String description) {
            this.releaseDate = releaseDate;
            this.manufacturer = manufacturer;
            this.version = version;
            this.name = name;
            this.description = description;
        }

        /**
         * Returns the release date.
         *
         * @return the release date
         */
        String getReleaseDate() {
            return releaseDate;
        }

        /**
         * Returns the manufacturer.
         *
         * @return the manufacturer
         */
        String getManufacturer() {
            return manufacturer;
        }

        /**
         * Returns the version.
         *
         * @return the version
         */
        String getVersion() {
            return version;
        }

        /**
         * Returns the name.
         *
         * @return the name
         */
        String getName() {
            return name;
        }

        /**
         * Returns the description.
         *
         * @return the description
         */
        String getDescription() {
            return description;
        }
    }

}
