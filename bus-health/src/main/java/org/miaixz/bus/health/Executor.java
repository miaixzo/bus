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
package org.miaixz.bus.health;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.Platform;

/**
 * A class for executing commands on the command line and returning the results.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class Executor {

    /**
     * Default environment settings to ensure command output uses standard language format.
     */
    private static final String[] DEFAULT_ENV = getDefaultEnv();

    /**
     * Retrieves the default environment settings.
     *
     * @return {@code {"LANGUAGE=C"}} for Windows systems, and {@code {"LC_ALL=C"}} for other systems.
     */
    private static String[] getDefaultEnv() {
        if (Platform.isWindows()) {
            return new String[] { "LANGUAGE=C" };
        } else {
            return new String[] { "LC_ALL=C" };
        }
    }

    /**
     * Executes a command on the local command line and returns the result. This is a convenience method for calling
     * {@link java.lang.Runtime#exec(String)} and capturing the resulting output as a list of strings. On Windows,
     * built-in commands that are not executables may require prefixing the command with {@code cmd.exe /c}.
     *
     * @param cmdToRun The command to run.
     * @return A list of strings representing the command's result, or an empty list if the command fails.
     */
    public static List<String> runNative(String cmdToRun) {
        String[] cmd = cmdToRun.split(Symbol.SPACE);
        return runNative(cmd);
    }

    /**
     * Executes a command on the local command line and returns the result line by line. This is a convenience method
     * for calling {@link java.lang.Runtime#exec(String[])} and capturing the resulting output as a list of strings. On
     * Windows, built-in commands that are not executables may require prefixing the array with the strings
     * {@code cmd.exe} and {@code /c}.
     *
     * @param cmdToRunWithArgs The command and its arguments to run, as an array.
     * @return A list of strings representing the command's result, or an empty list if the command fails.
     */
    public static List<String> runNative(String[] cmdToRunWithArgs) {
        return runNative(cmdToRunWithArgs, DEFAULT_ENV);
    }

    /**
     * Executes a command on the local command line and returns the result line by line. This is a convenience method
     * for calling {@link java.lang.Runtime#exec(String[])} and capturing the resulting output as a list of strings. On
     * Windows, built-in commands that are not executables may require prefixing the array with the strings
     * {@code cmd.exe} and {@code /c}.
     *
     * @param cmdToRunWithArgs The command and its arguments to run, as an array.
     * @param envp             An array of strings, each element of which has the format {@code name=value}, or
     *                         {@code null} if the subprocess should inherit the environment of the current process.
     * @return A list of strings representing the command's result, or an empty list if the command fails.
     */
    public static List<String> runNative(String[] cmdToRunWithArgs, String[] envp) {
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(cmdToRunWithArgs, envp);
            return getProcessOutput(p, cmdToRunWithArgs);
        } catch (SecurityException | IOException e) {
            Logger.trace("Couldn't run command {}: {}", Arrays.toString(cmdToRunWithArgs), e.getMessage());
        } finally {
            // Ensure all resources are freed
            if (p != null) {
                // Windows and Solaris don't close descriptors on destroy,
                // so must be handled separately
                if (Platform.isWindows() || Platform.isSolaris()) {
                    try {
                        p.getOutputStream().close();
                    } catch (IOException e) {
                        // Do nothing on failure
                    }
                    try {
                        p.getInputStream().close();
                    } catch (IOException e) {
                        // Do nothing on failure
                    }
                    try {
                        p.getErrorStream().close();
                    } catch (IOException e) {
                        // Do nothing on failure
                    }
                }
                p.destroy();
            }
        }
        return Collections.emptyList();
    }

    /**
     * Retrieves the output from a process and stores it as a list of strings.
     *
     * @param p   The process that was run.
     * @param cmd The command array that was executed.
     * @return A list of strings representing the process's output.
     */
    private static List<String> getProcessOutput(Process p, String[] cmd) {
        ArrayList<String> sa = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(p.getInputStream(), Charset.defaultCharset()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sa.add(line);
            }
            p.waitFor();
        } catch (IOException e) {
            Logger.trace("Problem reading output from {}: {}", Arrays.toString(cmd), e.getMessage());
        } catch (InterruptedException ie) {
            Logger.trace("Interrupted while reading output from {}: {}", Arrays.toString(cmd), ie.getMessage());
            Thread.currentThread().interrupt();
        }
        return sa;
    }

    /**
     * Returns the first line of response from the specified command.
     *
     * @param cmd2launch The command to launch.
     * @return The response string, or an empty string if the command fails.
     */
    public static String getFirstAnswer(String cmd2launch) {
        return getAnswerAt(cmd2launch, 0);
    }

    /**
     * Returns the response at the specified line index (0-based) after running the given command.
     *
     * @param cmd2launch The command to launch.
     * @param answerIdx  The line index in the command's response.
     * @return The full line from the response, or an empty string if the index is invalid or the command fails to run.
     */
    public static String getAnswerAt(String cmd2launch, int answerIdx) {
        List<String> sa = Executor.runNative(cmd2launch);

        if (answerIdx >= 0 && answerIdx < sa.size()) {
            return sa.get(answerIdx);
        }
        return Normal.EMPTY;
    }

}
