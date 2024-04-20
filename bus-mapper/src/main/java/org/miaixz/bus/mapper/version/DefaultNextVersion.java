/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org mybatis.io and other contributors.         *
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
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.mapper.version;

import org.miaixz.bus.core.exception.VersionException;

import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 默认版本实现
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DefaultNextVersion implements NextVersion {

    private static final Map<Class<? extends NextVersion>, NextVersion> CACHE = new ConcurrentHashMap<>();

    private static final ReentrantLock LOCK = new ReentrantLock();

    /**
     * 获取下一个版本
     *
     * @param nextVersionClass 下个版本
     * @param current          当前版本
     * @return the object
     * @throws VersionException 异常
     */
    public static Object nextVersion(Class<? extends NextVersion> nextVersionClass, Object current) throws VersionException {
        try {
            NextVersion nextVersion;
            if (CACHE.containsKey(nextVersionClass)) {
                nextVersion = CACHE.get(nextVersionClass);
            } else {
                LOCK.lock();
                try {
                    if (!CACHE.containsKey(nextVersionClass)) {
                        CACHE.put(nextVersionClass, nextVersionClass.getConstructor().newInstance());
                    }
                    nextVersion = CACHE.get(nextVersionClass);
                } finally {
                    LOCK.unlock();
                }
            }
            return nextVersion.nextVersion(current);
        } catch (Exception e) {
            throw new VersionException("获取下一个版本号失败!", e);
        }
    }

    @Override
    public Object nextVersion(Object current) throws VersionException {
        if (current == null) {
            throw new VersionException("当前版本号为空!");
        }
        if (current instanceof Integer) {
            return (Integer) current + 1;
        } else if (current instanceof Long) {
            return (Long) current + 1L;
        } else if (current instanceof Timestamp) {
            return new Timestamp(System.currentTimeMillis());
        } else {
            throw new VersionException("默认的 NextVersion 只支持 Integer, Long 和 java.sql.Timestamp 类型的版本号，如果有需要请自行扩展!");
        }
    }

}
