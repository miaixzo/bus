/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.windows.driver.wmi;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.windows.WmiQueryHandler;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;

/**
 * Utility to query WMI class {@code Win32_Process}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class Win32Process {

    private static final String WIN32_PROCESS = "Win32_Process";

    /**
     * Returns process command lines
     *
     * @param pidsToQuery Process IDs to query for command lines. Pass {@code null} to query all processes.
     * @return A {@link WmiResult} containing process IDs and command lines used to start the provided processes.
     */
    public static WmiResult<CommandLineProperty> queryCommandLines(Set<Integer> pidsToQuery) {
        String sb = WIN32_PROCESS;
        if (pidsToQuery != null) {
            sb += " WHERE ProcessID="
                    + pidsToQuery.stream().map(String::valueOf).collect(Collectors.joining(" OR PROCESSID="));
        }
        WmiQuery<CommandLineProperty> commandLineQuery = new WmiQuery<>(sb, CommandLineProperty.class);
        return Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(commandLineQuery);
    }

    /**
     * Returns process info
     *
     * @param pids Process IDs to query.
     * @return Information on the provided processes.
     */
    public static WmiResult<ProcessXPProperty> queryProcesses(Collection<Integer> pids) {
        String sb = WIN32_PROCESS;
        if (pids != null) {
            sb += " WHERE ProcessID="
                    + pids.stream().map(String::valueOf).collect(Collectors.joining(" OR PROCESSID="));
        }
        WmiQuery<ProcessXPProperty> processQueryXP = new WmiQuery<>(sb, ProcessXPProperty.class);
        return Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(processQueryXP);
    }

    /**
     * Process command lines.
     */
    public enum CommandLineProperty {
        PROCESSID, COMMANDLINE
    }

    /**
     * Process properties accessible from WTSEnumerateProcesses in Vista+
     */
    public enum ProcessXPProperty {
        PROCESSID, NAME, KERNELMODETIME, USERMODETIME, THREADCOUNT, PAGEFILEUSAGE, HANDLECOUNT, EXECUTABLEPATH
    }

}
