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
package org.miaixz.bus.health;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.logger.Logger;

/**
 * Utility class for privileged command execution and file reading.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class Privilege {

    /**
     * Memoized supplier for the command allowlist.
     */
    private static final AtomicReference<Supplier<Set<String>>> COMMAND_ALLOWLIST = new AtomicReference<>(
            Memoizer.memoize(Privilege::queryCommandAllowlist, Memoizer.defaultExpiration()));

    /**
     * Memoized supplier for the file allowlist.
     */
    private static final AtomicReference<Supplier<Set<String>>> FILE_ALLOWLIST = new AtomicReference<>(
            Memoizer.memoize(Privilege::queryFileAllowlist, Memoizer.defaultExpiration()));

    /**
     * Memoized supplier for the privilege command prefix.
     */
    private static final AtomicReference<Supplier<String>> PREFIX = new AtomicReference<>(
            Memoizer.memoize(Privilege::queryPrefix, Memoizer.defaultExpiration()));

    /**
     * Creates a new Privilege instance.
     */
    private Privilege() {
    }

    /**
     * Parses a comma-separated allowlist configuration string.
     *
     * @param allowlistConfig Comma-separated list of allowed entries.
     * @return A set of trimmed allowlist entries.
     */
    static Set<String> parseAllowlist(String allowlistConfig) {
        if (allowlistConfig == null || allowlistConfig.trim().isEmpty()) {
            return Collections.emptySet();
        }
        return Arrays.stream(allowlistConfig.split(",")).map(String::trim).filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * Checks if a command is allowed for privileged execution.
     *
     * @param command   The full command string.
     * @param allowlist Set of allowed command names or paths.
     * @return {@code true} if the command is in the allowlist.
     */
    public static boolean isCommandAllowed(String command, Set<String> allowlist) {
        if (command == null || command.trim().isEmpty() || allowlist == null || allowlist.isEmpty()) {
            return false;
        }

        String[] tokens = command.trim().split("\\s+");
        if (tokens.length == 0) {
            return false;
        }
        String cmdPath = tokens[0];
        String cmdName = getFileName(cmdPath);
        if (cmdName.isEmpty()) {
            return false;
        }

        for (String allowed : allowlist) {
            if (allowed.equals(cmdPath) || allowed.equals(cmdName)) {
                return true;
            }
            String allowedName = getFileName(allowed);
            if (!allowedName.isEmpty() && allowedName.equals(cmdName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a file path is allowed for privileged reading.
     *
     * @param filePath  The file path to check.
     * @param allowlist Set of allowed file paths or glob patterns.
     * @return {@code true} if the file path matches an allowlist entry.
     */
    public static boolean isFileAllowed(String filePath, Set<String> allowlist) {
        if (filePath == null || filePath.trim().isEmpty() || allowlist == null || allowlist.isEmpty()) {
            return false;
        }
        Path path = Paths.get(filePath);
        FileSystem fs = FileSystems.getDefault();
        for (String allowed : allowlist) {
            PathMatcher matcher = fs.getPathMatcher("glob:" + allowed);
            if (matcher.matches(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the command allowlist.
     *
     * @return The current command allowlist.
     */
    public static Set<String> getCommandAllowlist() {
        return COMMAND_ALLOWLIST.get().get();
    }

    /**
     * Gets the file allowlist.
     *
     * @return The current file allowlist.
     */
    public static Set<String> getFileAllowlist() {
        return FILE_ALLOWLIST.get().get();
    }

    /**
     * Gets the configured privileged command prefix for the current platform.
     *
     * @return The prefix string, or an empty string if unsupported or not configured.
     */
    public static String getPrefix() {
        return PREFIX.get().get();
    }

    /**
     * Reads a file with privileged fallback.
     *
     * @param filePath The file to read.
     * @return Lines read from the file.
     */
    public static List<String> readFile(String filePath) {
        if (!isFileAllowed(filePath, getFileAllowlist()) || IdGroup.isElevated()) {
            return Builder.readFile(filePath, false);
        }

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return Collections.emptyList();
        }
        if (Files.isReadable(path) || getPrefix().isEmpty()) {
            return Builder.readFile(filePath, false);
        }

        return Executor.runNative(buildCatCommand(filePath));
    }

    /**
     * Reads a file with privileged fallback.
     *
     * @param filePath The file to read.
     * @return Lines read from the file.
     */
    public static List<String> readFilePrivileged(String filePath) {
        return readFile(filePath);
    }

    /**
     * Reads a file and returns the first line with privileged fallback.
     *
     * @param filePath The file to read.
     * @return The first line, or an empty string if unreadable.
     */
    public static String getStringFromFile(String filePath) {
        List<String> lines = readFile(filePath);
        return lines.isEmpty() ? Normal.EMPTY : lines.get(0);
    }

    /**
     * Reads a file and returns the first line with privileged fallback.
     *
     * @param filePath The file to read.
     * @return The first line, or an empty string if unreadable.
     */
    public static String getStringFromFilePrivileged(String filePath) {
        return getStringFromFile(filePath);
    }

    /**
     * Reads a key-value file with privileged fallback.
     *
     * @param filePath  The file to read.
     * @param separator The separator between key and value.
     * @return Parsed key-value pairs.
     */
    public static Map<String, String> getKeyValueMapFromFile(String filePath, String separator) {
        return Parsing.parseStringListToMap(readFile(filePath), separator);
    }

    /**
     * Reads a key-value file with privileged fallback.
     *
     * @param filePath  The file to read.
     * @param separator The separator between key and value.
     * @return Parsed key-value pairs.
     */
    public static Map<String, String> getKeyValueMapFromFilePrivileged(String filePath, String separator) {
        return getKeyValueMapFromFile(filePath, separator);
    }

    /**
     * Reads all bytes from a file with privileged fallback.
     *
     * @param filePath    The file to read.
     * @param reportError Whether to log errors reading the file.
     * @return The file contents, or an empty byte array if unreadable.
     */
    public static byte[] readAllBytes(String filePath, boolean reportError) {
        if (!isFileAllowed(filePath, getFileAllowlist()) || IdGroup.isElevated()) {
            return Builder.readAllBytes(filePath, reportError);
        }

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return new byte[0];
        }
        if (Files.isReadable(path) || getPrefix().isEmpty()) {
            return Builder.readAllBytes(filePath, reportError);
        }

        String[] cmdArray = buildCatCommand(filePath);
        try {
            Process p = Runtime.getRuntime().exec(cmdArray);
            try {
                byte[] stdout;
                byte[] stderr;
                try (InputStream is = p.getInputStream(); InputStream es = p.getErrorStream()) {
                    stdout = is.readAllBytes();
                    stderr = es.readAllBytes();
                }
                int exitCode = p.waitFor();
                if (exitCode == 0) {
                    return stdout;
                }
                String error = new String(stderr, StandardCharsets.UTF_8).trim();
                if (reportError) {
                    Logger.error(false, "Health", "Privileged cat exited with code {}: {}", exitCode, error);
                } else {
                    Logger.debug(false, "Health", "Privileged cat exited with code {}: {}", exitCode, error);
                }
            } finally {
                Executor.destroyProcess(p);
            }
        } catch (SecurityException | IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            Logger.debug(false, "Health", "Failed to execute privileged cat command: {}", e.getClass().getSimpleName());
        }
        return new byte[0];
    }

    /**
     * Reads all bytes from a file with privileged fallback.
     *
     * @param filePath    The file to read.
     * @param reportError Whether to log errors reading the file.
     * @return The file contents, or an empty byte array if unreadable.
     */
    public static byte[] readAllBytesPrivileged(String filePath, boolean reportError) {
        return readAllBytes(filePath, reportError);
    }

    /**
     * Reinitializes memoized suppliers after configuration changes.
     */
    static void clearCaches() {
        COMMAND_ALLOWLIST.set(Memoizer.memoize(Privilege::queryCommandAllowlist, Memoizer.defaultExpiration()));
        FILE_ALLOWLIST.set(Memoizer.memoize(Privilege::queryFileAllowlist, Memoizer.defaultExpiration()));
        PREFIX.set(Memoizer.memoize(Privilege::queryPrefix, Memoizer.defaultExpiration()));
        Logger.debug(false, "Health", "Privileged access caches reset");
    }

    /**
     * Queries the prefix.
     *
     * @return the query prefix result
     */
    private static String queryPrefix() {
        String prefix = Platform.isLinux() ? Config.get(Config._LINUX_PRIVILEGED_PREFIX, Normal.EMPTY) : Normal.EMPTY;
        Logger.debug(
                false,
                "Health",
                "Privileged command prefix resolved: platformLinux={}, prefixConfigured={}",
                Platform.isLinux(),
                !prefix.isEmpty());
        return prefix;
    }

    /**
     * Queries the command allowlist.
     *
     * @return the query command allowlist result
     */
    private static Set<String> queryCommandAllowlist() {
        Set<String> allowlist = Platform.isLinux()
                ? parseAllowlist(Config.get(Config._LINUX_PRIVILEGED_ALLOWLIST, Normal.EMPTY))
                : Collections.emptySet();
        Logger.debug(
                false,
                "Health",
                "Privileged command allowlist resolved: platformLinux={}, allowedCommands={}",
                Platform.isLinux(),
                allowlist.size());
        return allowlist;
    }

    /**
     * Queries the file allowlist.
     *
     * @return the query file allowlist result
     */
    private static Set<String> queryFileAllowlist() {
        Set<String> allowlist = Platform.isLinux()
                ? parseAllowlist(Config.get(Config._LINUX_PRIVILEGED_FILE_ALLOWLIST, Normal.EMPTY))
                : Collections.emptySet();
        Logger.debug(
                false,
                "Health",
                "Privileged file allowlist resolved: platformLinux={}, allowedFiles={}",
                Platform.isLinux(),
                allowlist.size());
        return allowlist;
    }

    /**
     * Builds the cat command.
     *
     * @param filePath the file path
     * @return the build cat command result
     */
    private static String[] buildCatCommand(String filePath) {
        List<String> cmdList = new ArrayList<>();
        String prefix = getPrefix();
        if (!prefix.isEmpty()) {
            cmdList.addAll(Arrays.asList(prefix.split("\\s+")));
        }
        cmdList.add("cat");
        cmdList.add(filePath);
        String[] cmdArray = cmdList.toArray(new String[0]);
        Logger.debug(
                true,
                "Health",
                "Privileged file read command prepared: file={}, prefixConfigured={}",
                filePath,
                !getPrefix().isEmpty());
        return cmdArray;
    }

    /**
     * Returns the file name.
     *
     * @param path the path
     * @return the get file name result
     */
    private static String getFileName(String path) {
        return new File(path).getName();
    }

}
