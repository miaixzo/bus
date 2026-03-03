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
package org.miaixz.bus.health.windows.driver.wmi;

import java.util.Objects;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.windows.WmiQueryHandler;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;

/**
 * Utility to query WMI class {@code Win32_Processor}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class Win32Processor {

    private static final String WIN32_PROCESSOR = "Win32_Processor";

    /**
     * Returns processor voltage.
     *
     * @return Current voltage of the processor. If the eighth bit is set, bits 0-6 contain the voltage multiplied by
     *         10. If the eighth bit is not set, then the bit setting in VoltageCaps represents the voltage value.
     */
    public static WmiResult<VoltProperty> queryVoltage() {
        WmiQuery<VoltProperty> voltQuery = new WmiQuery<>(WIN32_PROCESSOR, VoltProperty.class);
        return Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(voltQuery);
    }

    /**
     * Returns processor ID.
     *
     * @return Processor information that describes the processor features. For an x86 class CPU, the field format
     *         depends on the processor support of the CPUID instruction. If the instruction is supported, the property
     *         contains 2 (two) DWORD formatted values. The first is an offset of 08h-0Bh, which is the EAX value that a
     *         CPUID instruction returns with input EAX set to 1. The second is an offset of 0Ch-0Fh, which is the EDX
     *         value that the instruction returns. Only the first two bytes of the property are significant and contain
     *         the contents of the DX register at CPU reset—all others are set to 0 (zero), and the contents are in
     *         DWORD format.
     */
    public static WmiResult<ProcessorIdProperty> queryProcessorId() {
        WmiQuery<ProcessorIdProperty> idQuery = new WmiQuery<>(WIN32_PROCESSOR, ProcessorIdProperty.class);
        return Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(idQuery);
    }

    /**
     * Returns address width.
     *
     * @return On a 32-bit operating system, the value is 32 and on a 64-bit operating system it is 64.
     */
    public static WmiResult<BitnessProperty> queryBitness() {
        WmiQuery<BitnessProperty> bitnessQuery = new WmiQuery<>(WIN32_PROCESSOR, BitnessProperty.class);
        return Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(bitnessQuery);
    }

    /**
     * Processor voltage properties.
     */
    public enum VoltProperty {
        CURRENTVOLTAGE, VOLTAGECAPS
    }

    /**
     * Processor ID property
     */
    public enum ProcessorIdProperty {
        PROCESSORID
    }

    /**
     * Processor bitness property
     */
    public enum BitnessProperty {
        ADDRESSWIDTH
    }

}
