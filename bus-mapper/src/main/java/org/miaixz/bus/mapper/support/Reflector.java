/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mybatis.io and other contributors.         ~
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
package org.miaixz.bus.mapper.support;

import java.beans.Introspector;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.decorators.SoftCache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.mapping.MappedStatement;
import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.lang.exception.MapperException;

/**
 * 反射支持
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Reflector {

    public static final Cache CLASS_CACHE = new SoftCache(new PerpetualCache("MAPPER_CLASS_CACHE"));
    private static final Pattern GET_PATTERN = Pattern.compile("^get[A-Z].*");
    private static final Pattern IS_PATTERN = Pattern.compile("^is[A-Z].*");

    /**
     * 根据msId获取接口类
     *
     * @param msId 方法
     * @return the class
     */
    public static Class<?> getMapperClass(String msId) {
        if (msId.indexOf(".") == -1) {
            throw new MapperException("当前MappedStatement的id=" + msId + ",不符合MappedStatement的规则!");
        }
        String mapperClassStr = msId.substring(0, msId.lastIndexOf("."));
        // 由于一个接口中的每个方法都会进行下面的操作，因此缓存
        Class<?> mapperClass = (Class<?>) CLASS_CACHE.getObject(mapperClassStr);
        if (mapperClass != null) {
            return mapperClass;
        }
        ClassLoader[] classLoader = getClassLoaders();

        for (ClassLoader cl : classLoader) {
            if (null != cl) {
                try {
                    mapperClass = Class.forName(mapperClassStr, true, cl);
                    if (mapperClass != null) {
                        break;
                    }
                } catch (ClassNotFoundException e) {
                    // we'll ignore this until all class loaders fail to locate the class
                }
            }
        }
        if (mapperClass == null) {
            throw new MapperException("class loaders failed to locate the class " + mapperClassStr);
        }
        CLASS_CACHE.putObject(mapperClassStr, mapperClass);
        return mapperClass;
    }

    public static String fnToFieldName(FunctionX fn) {
        try {
            Method method = fn.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(Boolean.TRUE);
            SerializedLambda serializedLambda = (SerializedLambda) method.invoke(fn);
            String getter = serializedLambda.getImplMethodName();
            if (GET_PATTERN.matcher(getter).matches()) {
                getter = getter.substring(3);
            } else if (IS_PATTERN.matcher(getter).matches()) {
                getter = getter.substring(2);
            }
            return Introspector.decapitalize(getter);
        } catch (ReflectiveOperationException e) {
            throw new MapperException(e);
        }
    }

    public static String[] fnToFieldNames(FunctionX... fns) {
        List<String> list = new ArrayList<>();
        for (FunctionX fn : fns) {
            list.add(fnToFieldName(fn));
        }
        return list.toArray(new String[0]);
    }

    private static ClassLoader[] getClassLoaders() {
        return new ClassLoader[] { Thread.currentThread().getContextClassLoader(), Reflector.class.getClassLoader() };
    }

    /**
     * 获取执行的方法名
     *
     * @param ms MappedStatement
     * @return the string
     */
    public static String getMethodName(MappedStatement ms) {
        return getMethodName(ms.getId());
    }

    /**
     * 获取执行的方法名
     *
     * @param msId 方法
     * @return the string
     */
    public static String getMethodName(String msId) {
        return msId.substring(msId.lastIndexOf(".") + 1);
    }

}
