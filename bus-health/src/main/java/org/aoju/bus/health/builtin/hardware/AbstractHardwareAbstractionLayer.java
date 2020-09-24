/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org OSHI and other contributors.                 *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.health.builtin.hardware;

import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.health.Memoize;

import java.util.function.Supplier;

/**
 * 硬件信息特定于平台的实现所使用的公共字段或方法
 *
 * @author Kimi Liu
 * @version 6.1.0
 * @since JDK 1.8+
 */
@ThreadSafe
public abstract class AbstractHardwareAbstractionLayer implements HardwareAbstractionLayer {

    private final Supplier<ComputerSystem> computerSystem = Memoize.memoize(this::createComputerSystem);

    private final Supplier<CentralProcessor> processor = Memoize.memoize(this::createProcessor);

    private final Supplier<GlobalMemory> memory = Memoize.memoize(this::createMemory);

    private final Supplier<Sensors> sensors = Memoize.memoize(this::createSensors);

    @Override
    public ComputerSystem getComputerSystem() {
        return computerSystem.get();
    }

    /**
     * 实例化特定于平台的{@link ComputerSystem}对象
     *
     * @return 特定于平台的 {@link ComputerSystem} 对象
     */
    protected abstract ComputerSystem createComputerSystem();

    @Override
    public CentralProcessor getProcessor() {
        return processor.get();
    }

    /**
     * 实例化特定于平台的{@link CentralProcessor}对象
     *
     * @return 特定于平台的 {@link CentralProcessor} 对象
     */
    protected abstract CentralProcessor createProcessor();

    @Override
    public GlobalMemory getMemory() {
        return memory.get();
    }

    /**
     * 实例化特定于平台的{@link GlobalMemory}对象
     *
     * @return 特定于平台的 {@link GlobalMemory} 对象
     */
    protected abstract GlobalMemory createMemory();

    @Override
    public Sensors getSensors() {
        return sensors.get();
    }

    /**
     * 实例化特定于平台的{@link Sensors}对象
     *
     * @return 特定于平台的 {@link Sensors} 对象
     */
    protected abstract Sensors createSensors();

}
