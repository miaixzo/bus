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
package org.miaixz.bus.health.builtin.software.common;

import java.util.function.Supplier;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.software.OSProcess;

/**
 * A process is an instance of a computer program that is being executed. It contains the program code and its current
 * activity. Depending on the operating system (OS), a process may be made up of multiple threads of execution that
 * execute instructions concurrently.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public abstract class AbstractOSProcess implements OSProcess {

    private final Supplier<Double> cumulativeCpuLoad = Memoizer
            .memoize(this::queryCumulativeCpuLoad, Memoizer.defaultExpiration());

    private final int processID;

    protected AbstractOSProcess(int pid) {
        this.processID = pid;
    }

    @Override
    public int getProcessID() {
        return this.processID;
    }

    @Override
    public double getProcessCpuLoadCumulative() {
        return cumulativeCpuLoad.get();
    }

    private double queryCumulativeCpuLoad() {
        return getUpTime() > 0d ? (getKernelTime() + getUserTime()) / (double) getUpTime() : 0d;
    }

    @Override
    public double getProcessCpuLoadBetweenTicks(OSProcess priorSnapshot) {
        if (priorSnapshot != null && this.processID == priorSnapshot.getProcessID()
                && getUpTime() > priorSnapshot.getUpTime()) {
            return (getUserTime() - priorSnapshot.getUserTime() + getKernelTime() - priorSnapshot.getKernelTime())
                    / (double) (getUpTime() - priorSnapshot.getUpTime());
        }
        return getProcessCpuLoadCumulative();
    }

    @Override
    public String toString() {
        String builder = "OSProcess@" + Integer.toHexString(hashCode()) + "[processID=" + this.processID + ", name="
                + getName() + ']';
        return builder;
    }

}
