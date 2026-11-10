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
package org.miaixz.bus.health.linux.software;

import java.io.File;
import java.util.*;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.ApplicationInfo;

/**
 * The linux installed apps class.
 *
 * @author Kimi Liu
 */
public class LinuxInstalledApps {

    /**
     * The PACKAGE_MANAGER_COMMANDS constant.
     */
    private static final Map<String, String> PACKAGE_MANAGER_COMMANDS = initializePackageManagerCommands();

    /**
     * Alpine Linux installed-package database.
     */
    private static final String APK_INSTALLED = "/lib/apk/db/installed";

    /**
     * Arch Linux local package database directory.
     */
    private static final String PACMAN_LOCAL = "/var/lib/pacman/local";

    /**
     * Creates a new LinuxInstalledApps instance.
     */
    public LinuxInstalledApps() {
        // No initialization required.
    }

    /**
     * Returns the initialize package manager commands result.
     *
     * @return the initialize package manager commands result
     */
    private static Map<String, String> initializePackageManagerCommands() {
        Map<String, String> commands = new HashMap<>();

        if (isPackageManagerAvailable("dpkg")) {
            commands.put(
                    "dpkg",
                    "dpkg-query -W -f=${Package}|${Version}|${Architecture}|${Installed-Size}|${db-fsys:Last-Modified}|${Maintainer}|${Source}|${Homepage}\\n");
        } else if (isPackageManagerAvailable("rpm")) {
            commands.put(
                    "rpm",
                    "rpm -qa --queryformat %{NAME}|%{VERSION}-%{RELEASE}|%{ARCH}|%{SIZE}|%{INSTALLTIME}|%{PACKAGER}|%{SOURCERPM}|%{URL}\\n");
        }

        return commands;
    }

    /**
     * Retrieves the list of installed applications on a Linux system. This method determines the appropriate package
     * manager and parses the installed application details.
     *
     * @return A list of {@link ApplicationInfo} objects representing installed applications.
     */
    public static List<ApplicationInfo> queryInstalledApps() {
        if (!PACKAGE_MANAGER_COMMANDS.isEmpty()) {
            return parseLinuxAppInfo(fetchInstalledApps());
        }
        return queryPackageDatabases(APK_INSTALLED, PACMAN_LOCAL);
    }

    /**
     * Reads installed packages directly from Alpine or Arch package databases.
     *
     * @param apkInstalled the Alpine installed-package database path
     * @param pacmanLocal  the Arch local package database directory
     * @return the installed applications, or an empty list when neither database exists
     */
    static List<ApplicationInfo> queryPackageDatabases(String apkInstalled, String pacmanLocal) {
        if (new File(apkInstalled).isFile()) {
            return parseApkInstalled(Builder.readFile(apkInstalled, false));
        }
        File[] pacmanPackages = new File(pacmanLocal).listFiles(File::isDirectory);
        if (pacmanPackages != null) {
            List<ApplicationInfo> apps = new ArrayList<>();
            for (File pkg : pacmanPackages) {
                ApplicationInfo app = parsePacmanDesc(Builder.readFile(pkg.getPath() + "/desc", false));
                if (app != null) {
                    apps.add(app);
                }
            }
            return apps;
        }
        return new ArrayList<>();
    }

    /**
     * Fetches the list of installed applications by executing the appropriate package manager command. The package
     * manager is determined during class initialization and stored in {@code PACKAGE_MANAGER_COMMANDS}. If no supported
     * package manager is found, an empty list is returned.
     *
     * @return A list of strings, where each entry represents an installed application with its details. Returns an
     *         empty list if no supported package manager is available.
     */
    private static List<String> fetchInstalledApps() {
        if (PACKAGE_MANAGER_COMMANDS.isEmpty()) {
            return Collections.emptyList();
        }

        // Get the first available package manager's command
        String command = PACKAGE_MANAGER_COMMANDS.values().iterator().next();
        return Executor.runNative(command);
    }

    /**
     * Returns whether the package manager available condition is true.
     *
     * @param packageManager the package manager
     * @return the is package manager available result
     */
    private static boolean isPackageManagerAvailable(String packageManager) {
        List<String> result = Executor.runNative(packageManager + " --version");
        // If the command executes fine the result is non-empty else empty
        return !result.isEmpty();
    }

    /**
     * Parses the linux app info.
     *
     * @param output the output
     * @return the parse linux app info result
     */
    private static List<ApplicationInfo> parseLinuxAppInfo(List<String> output) {
        Set<ApplicationInfo> appInfoSet = new LinkedHashSet<>();

        for (String line : output) {
            // Not a precompiled Pattern: a two-character backslash escape takes String.split's
            // regex-free fast path, which measures faster than Pattern.split.
            String[] parts = line.split("\\|", -1); // -1 to keep empty fields

            // Check if we have all 8 fields
            if (parts.length >= 8) {
                appInfoSet.add(
                        toAppInfo(
                                parts[0],
                                parts[1],
                                parts[2],
                                parts[3],
                                Parsing.parseLongOrDefault(parts[4], 0L) * Normal._1000,
                                parts[5],
                                parts[6],
                                parts[7]));
            }
        }

        return new ArrayList<>(appInfoSet);
    }

    /**
     * Parses Alpine's installed-package database records.
     *
     * @param lines the database lines
     * @return the installed applications
     */
    static List<ApplicationInfo> parseApkInstalled(List<String> lines) {
        List<ApplicationInfo> apps = new ArrayList<>();
        Map<Character, String> record = new HashMap<>();
        for (String line : lines) {
            if (line.isEmpty()) {
                addApkRecord(apps, record);
            } else if (line.length() > Normal._2 && line.charAt(Normal._1) == Symbol.C_COLON) {
                record.put(line.charAt(Normal._0), line.substring(Normal._2));
            }
        }
        addApkRecord(apps, record);
        return apps;
    }

    /**
     * Adds one parsed Alpine package record to the application list.
     *
     * @param apps   the destination application list
     * @param record the package fields keyed by Alpine field code
     */
    private static void addApkRecord(List<ApplicationInfo> apps, Map<Character, String> record) {
        String name = record.get('P');
        if (name != null) {
            apps.add(
                    toAppInfo(
                            name,
                            record.get('V'),
                            record.get('A'),
                            record.get('I'),
                            0L,
                            record.get('m'),
                            record.get('o'),
                            record.get('U')));
        }
        record.clear();
    }

    /**
     * Parses one Arch Linux package description file.
     *
     * @param lines the description file lines
     * @return the installed application, or {@code null} when no package name is present
     */
    static ApplicationInfo parsePacmanDesc(List<String> lines) {
        Map<String, String> fields = new HashMap<>();
        String key = null;
        for (String line : lines) {
            if (line.length() > Normal._2 && line.startsWith(Symbol.PERCENT) && line.endsWith(Symbol.PERCENT)) {
                key = line.substring(Normal._1, line.length() - Normal._1);
            } else if (key != null && !line.isEmpty()) {
                fields.putIfAbsent(key, line);
            }
        }
        String name = fields.get("NAME");
        if (name == null) {
            return null;
        }
        return toAppInfo(
                name,
                fields.get("VERSION"),
                fields.get("ARCH"),
                fields.get("SIZE"),
                Parsing.parseLongOrDefault(fields.get("INSTALLDATE"), 0L) * Normal._1000,
                fields.get("PACKAGER"),
                fields.get("BASE"),
                fields.get("URL"));
    }

    /**
     * Creates an application information record from package-manager fields.
     *
     * @param name            the package name
     * @param version         the package version
     * @param architecture    the package architecture
     * @param installedSize   the installed size
     * @param timestampMillis the installation timestamp in milliseconds
     * @param vendor          the package vendor or maintainer
     * @param source          the source package
     * @param homepage        the package homepage
     * @return the application information record
     */
    private static ApplicationInfo toAppInfo(
            String name,
            String version,
            String architecture,
            String installedSize,
            long timestampMillis,
            String vendor,
            String source,
            String homepage) {
        Map<String, String> additionalInfo = new LinkedHashMap<>();
        additionalInfo.put("architecture", Parsing.getStringValueOrUnknown(architecture));
        additionalInfo.put("installedSize", String.valueOf(Parsing.parseLongOrDefault(installedSize, 0L)));
        additionalInfo.put("source", Parsing.getStringValueOrUnknown(source));
        additionalInfo.put("homepage", Parsing.getStringValueOrUnknown(homepage));
        return new ApplicationInfo(Parsing.getStringValueOrUnknown(name), Parsing.getStringValueOrUnknown(version),
                Parsing.getStringValueOrUnknown(vendor), timestampMillis, additionalInfo);
    }

}
