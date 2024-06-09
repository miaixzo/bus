/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.cache.provider;

import org.miaixz.bus.cache.Hitting;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class MemoryHitting implements Hitting {

    private ConcurrentMap<String, AtomicLong> hitMap = new ConcurrentHashMap<>();

    private ConcurrentMap<String, AtomicLong> requireMap = new ConcurrentHashMap<>();

    @Override
    public void hitIncr(String pattern, int count) {
        hitMap.computeIfAbsent(
                pattern,
                (k) -> new AtomicLong()
        ).addAndGet(count);
    }

    @Override
    public void reqIncr(String pattern, int count) {
        requireMap.computeIfAbsent(
                pattern,
                (k) -> new AtomicLong()
        ).addAndGet(count);
    }

    @Override
    public Map<String, Hitting.HittingDO> getHitting() {
        Map<String, Hitting.HittingDO> result = new LinkedHashMap<>();

        AtomicLong statisticsHit = new AtomicLong(0);
        AtomicLong statisticsRequired = new AtomicLong(0);
        requireMap.forEach((pattern, count) -> {
            long hit = hitMap.computeIfAbsent(pattern, (key) -> new AtomicLong(0)).get();
            long require = count.get();

            statisticsHit.addAndGet(hit);
            statisticsRequired.addAndGet(require);

            result.put(pattern, Hitting.HittingDO.newInstance(hit, require));
        });

        result.put(summaryName(), Hitting.HittingDO.newInstance(statisticsHit.get(), statisticsRequired.get()));

        return result;
    }

    @Override
    public void reset(String pattern) {
        hitMap.remove(pattern);
        requireMap.remove(pattern);
    }

    @Override
    public void resetAll() {
        hitMap.clear();
        requireMap.clear();
    }

}
