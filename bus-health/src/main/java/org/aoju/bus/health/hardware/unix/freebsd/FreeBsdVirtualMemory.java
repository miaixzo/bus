/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.health.hardware.unix.freebsd;

import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Command;
import org.aoju.bus.health.Memoizer;
import org.aoju.bus.health.common.unix.freebsd.BsdSysctlUtils;
import org.aoju.bus.health.hardware.AbstractVirtualMemory;

import java.util.function.Supplier;

/**
 * Memory obtained by swapinfo
 *
 * @author Kimi Liu
 * @version 5.5.1
 * @since JDK 1.8+
 */
public class FreeBsdVirtualMemory extends AbstractVirtualMemory {

    private final Supplier<Long> used = Memoizer.memoize(this::querySwapUsed, Memoizer.defaultExpiration());

    private final Supplier<Long> total = Memoizer.memoize(this::querySwapTotal, Memoizer.defaultExpiration());

    private final Supplier<Long> pagesIn = Memoizer.memoize(this::queryPagesIn, Memoizer.defaultExpiration());

    private final Supplier<Long> pagesOut = Memoizer.memoize(this::queryPagesOut, Memoizer.defaultExpiration());

    @Override
    public long getSwapUsed() {
        return used.get();
    }

    @Override
    public long getSwapTotal() {
        return total.get();
    }

    @Override
    public long getSwapPagesIn() {
        return pagesIn.get();
    }

    @Override
    public long getSwapPagesOut() {
        return pagesOut.get();
    }

    private long querySwapUsed() {
        String swapInfo = Command.getAnswerAt("swapinfo -k", 1);
        String[] split = Builder.whitespaces.split(swapInfo);
        if (split.length < 5) {
            return 0L;
        }
        return Builder.parseLongOrDefault(split[2], 0L) << 10;
    }

    private long querySwapTotal() {
        return BsdSysctlUtils.sysctl("vm.swap_total", 0L);
    }

    private long queryPagesIn() {
        return BsdSysctlUtils.sysctl("vm.stats.vm.v_swappgsin", 0L);
    }

    private long queryPagesOut() {
        return BsdSysctlUtils.sysctl("vm.stats.vm.v_swappgsout", 0L);
    }
}
