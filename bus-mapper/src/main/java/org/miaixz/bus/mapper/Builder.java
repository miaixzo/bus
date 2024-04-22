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
package org.miaixz.bus.mapper;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.decorators.SoftCache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.mapper.support.GenId;
import org.miaixz.bus.mapper.support.MetaObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

/**
 * 通用方法
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Builder {

    public static final Cache CLASS_CACHE = new SoftCache(new PerpetualCache("MAPPER_CLASS_CACHE"));
    public static final Map<Class<? extends GenId>, GenId> CACHE = new ConcurrentHashMap<>();
    public static final ReentrantLock LOCK = new ReentrantLock();
    private static final Pattern GET_PATTERN = Pattern.compile("^get[A-Z].*");
    private static final Pattern IS_PATTERN = Pattern.compile("^is[A-Z].*");

    /**
     * 生成 Id
     *
     * @param target   目标对象
     * @param property 属性
     * @param genClass class
     * @param table    表
     * @param column   列
     * @throws InternalException 异常
     */
    public static void genId(Object target, String property, Class<? extends GenId> genClass, String table, String column) throws InternalException {
        try {
            GenId genId;
            if (CACHE.containsKey(genClass)) {
                genId = CACHE.get(genClass);
            } else {
                LOCK.lock();
                try {
                    if (!CACHE.containsKey(genClass)) {
                        CACHE.put(genClass, genClass.newInstance());
                    }
                    genId = CACHE.get(genClass);
                } finally {
                    LOCK.unlock();
                }
            }
            org.apache.ibatis.reflection.MetaObject metaObject = MetaObject.forObject(target);
            if (metaObject.getValue(property) == null) {
                Object id = genId.genId(table, column);
                metaObject.setValue(property, id);
            }
        } catch (Exception e) {
            throw new InternalException("生成 ID 失败!", e);
        }
    }

}
