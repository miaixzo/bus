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
package org.miaixz.bus.health.builtin.hardware.common;

import java.util.List;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.hardware.*;

/**
 * Common fields or methods used by platform-specific implementations of HardwareAbstractionLayer
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public abstract class AbstractHardwareAbstractionLayer implements HardwareAbstractionLayer {

    private final Supplier<ComputerSystem> computerSystem = Memoizer.memoize(this::createComputerSystem);

    private final Supplier<CentralProcessor> processor = Memoizer.memoize(this::createProcessor);

    private final Supplier<GlobalMemory> memory = Memoizer.memoize(this::createMemory);

    private final Supplier<Sensors> sensors = Memoizer.memoize(this::createSensors);

    @Override
    public ComputerSystem getComputerSystem() {
        return computerSystem.get();
    }

    /**
     * Instantiates the platform-specific {@link ComputerSystem} object
     *
     * @return platform-specific {@link ComputerSystem} object
     */
    protected abstract ComputerSystem createComputerSystem();

    @Override
    public CentralProcessor getProcessor() {
        return processor.get();
    }

    /**
     * Instantiates the platform-specific {@link CentralProcessor} object
     *
     * @return platform-specific {@link CentralProcessor} object
     */
    protected abstract CentralProcessor createProcessor();

    @Override
    public GlobalMemory getMemory() {
        return memory.get();
    }

    /**
     * Instantiates the platform-specific {@link GlobalMemory} object
     *
     * @return platform-specific {@link GlobalMemory} object
     */
    protected abstract GlobalMemory createMemory();

    @Override
    public Sensors getSensors() {
        return sensors.get();
    }

    /**
     * Instantiates the platform-specific {@link Sensors} object
     *
     * @return platform-specific {@link Sensors} object
     */
    protected abstract Sensors createSensors();

    @Override
    public List<NetworkIF> getNetworkIFs() {
        return getNetworkIFs(false);
    }

}
