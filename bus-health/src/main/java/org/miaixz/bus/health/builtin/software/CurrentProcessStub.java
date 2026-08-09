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
package org.miaixz.bus.health.builtin.software;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.software.common.AbstractOSProcess;

/**
 * Minimal stand-in for the current process when the platform query fails to return it.
 *
 * @author Kimi Liu
 */
@ThreadSafe
final class CurrentProcessStub extends AbstractOSProcess {

    /**
     * Creates a new CurrentProcessStub instance.
     *
     * @param pid the process ID
     */
    CurrentProcessStub(int pid) {
        super(pid);
    }

    /**
     * Returns the process name.
     *
     * @return an empty process name
     */
    @Override
    public String getName() {
        return Normal.EMPTY;
    }

    /**
     * Returns the process path.
     *
     * @return an empty process path
     */
    @Override
    public String getPath() {
        return Normal.EMPTY;
    }

    /**
     * Returns the command line.
     *
     * @return an empty command line
     */
    @Override
    public String getCommandLine() {
        return Normal.EMPTY;
    }

    /**
     * Returns the command arguments.
     *
     * @return an empty argument list
     */
    @Override
    public List<String> getArguments() {
        return Collections.emptyList();
    }

    /**
     * Returns the environment variables.
     *
     * @return an empty environment map
     */
    @Override
    public Map<String, String> getEnvironmentVariables() {
        return Collections.emptyMap();
    }

    /**
     * Returns the current working directory.
     *
     * @return an empty current working directory
     */
    @Override
    public String getCurrentWorkingDirectory() {
        return Normal.EMPTY;
    }

    /**
     * Returns the user name.
     *
     * @return unknown user
     */
    @Override
    public String getUser() {
        return Normal.UNKNOWN;
    }

    /**
     * Returns the user ID.
     *
     * @return unknown user ID
     */
    @Override
    public String getUserID() {
        return Normal.UNKNOWN;
    }

    /**
     * Returns the group name.
     *
     * @return unknown group
     */
    @Override
    public String getGroup() {
        return Normal.UNKNOWN;
    }

    /**
     * Returns the group ID.
     *
     * @return unknown group ID
     */
    @Override
    public String getGroupID() {
        return Normal.UNKNOWN;
    }

    /**
     * Returns the process state.
     *
     * @return the running state
     */
    @Override
    public State getState() {
        return State.RUNNING;
    }

    /**
     * Returns the parent process ID.
     *
     * @return zero
     */
    @Override
    public int getParentProcessID() {
        return 0;
    }

    /**
     * Returns the thread count.
     *
     * @return zero
     */
    @Override
    public int getThreadCount() {
        return 0;
    }

    /**
     * Returns the priority.
     *
     * @return zero
     */
    @Override
    public int getPriority() {
        return 0;
    }

    /**
     * Returns the virtual size.
     *
     * @return zero
     */
    @Override
    public long getVirtualSize() {
        return 0L;
    }

    /**
     * Returns the resident memory.
     *
     * @return zero
     */
    @Override
    public long getResidentMemory() {
        return 0L;
    }

    /**
     * Returns the kernel time.
     *
     * @return zero
     */
    @Override
    public long getKernelTime() {
        return 0L;
    }

    /**
     * Returns the user time.
     *
     * @return zero
     */
    @Override
    public long getUserTime() {
        return 0L;
    }

    /**
     * Returns the up time.
     *
     * @return zero
     */
    @Override
    public long getUpTime() {
        return 0L;
    }

    /**
     * Returns the start time.
     *
     * @return zero
     */
    @Override
    public long getStartTime() {
        return 0L;
    }

    /**
     * Returns bytes read.
     *
     * @return zero
     */
    @Override
    public long getBytesRead() {
        return 0L;
    }

    /**
     * Returns bytes written.
     *
     * @return zero
     */
    @Override
    public long getBytesWritten() {
        return 0L;
    }

    /**
     * Returns open files.
     *
     * @return zero
     */
    @Override
    public long getOpenFiles() {
        return 0L;
    }

    /**
     * Returns the soft open file limit.
     *
     * @return {@code -1}
     */
    @Override
    public long getSoftOpenFileLimit() {
        return -1L;
    }

    /**
     * Returns the hard open file limit.
     *
     * @return {@code -1}
     */
    @Override
    public long getHardOpenFileLimit() {
        return -1L;
    }

    /**
     * Returns the bitness.
     *
     * @return zero
     */
    @Override
    public int getBitness() {
        return 0;
    }

    /**
     * Returns the affinity mask.
     *
     * @return zero
     */
    @Override
    public long getAffinityMask() {
        return 0L;
    }

    /**
     * Updates process attributes.
     *
     * @return {@code false}
     */
    @Override
    public boolean updateAttributes() {
        return false;
    }

    /**
     * Returns the thread details.
     *
     * @return an empty thread list
     */
    @Override
    public List<OSThread> getThreadDetails() {
        return Collections.emptyList();
    }

}
