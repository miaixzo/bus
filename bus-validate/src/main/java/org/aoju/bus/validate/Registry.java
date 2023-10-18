/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.validate;

import org.aoju.bus.core.exception.InternalException;
import org.aoju.bus.core.toolkit.ObjectKit;
import org.aoju.bus.validate.strategy.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 校验器注册中心
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Registry {

    /**
     * 验证组列表
     */
    private static Map<Object, Object> COMPLEX_CACHE = new ConcurrentHashMap<>();

    /**
     * 校验实例信息
     */
    private static Registry instance;

    static {
        register(Builder._ALWAYS, new AlwaysStrategy());
        register(Builder._BLANK, new BlankStrategy());
        register(Builder._CHINESE, new ChineseStrategy());
        register(Builder._CITIZENID, new CitizenIdStrategy());
        register(Builder._DATE, new DateStrategy());
        register(Builder._EACH, new EachStrategy());
        register(Builder._EMAIL, new EmailStrategy());
        register(Builder._ENGLISH, new EnglishStrategy());
        register(Builder._EQUALS, new EqualsStrategy());
        register(Builder._FALSE, new FalseStrategy());
        register(Builder._IN_ENUM, new InEnumStrategy());
        register(Builder._IN, new InStrategy());
        register(Builder._INT_RANGE, new IntRangeStrategy());
        register(Builder._IP_ADDRESS, new IPAddressStrategy());
        register(Builder._LENGTH, new LengthStrategy());
        register(Builder._MOBILE, new MobileStrategy());
        register(Builder._MULTI, new MultiStrategy());
        register(Builder._NOT_BLANK, new NotBlankStrategy());
        register(Builder._NOT_IN, new NotInStrategy());
        register(Builder._NOT_NULL, new NotNullStrategy());
        register(Builder._NULL, new NullStrategy());
        register(Builder._PHONE, new PhoneStrategy());
        register(Builder._REFLECT, new ReflectStrategy());
        register(Builder._REGEX, new RegexStrategy());
        register(Builder._TRUE, new TrueStrategy());
    }

    public Registry() {

    }

    /**
     * 单例模型初始化
     *
     * @return the object
     */
    public static Registry getInstance() {
        synchronized (Registry.class) {
            if (ObjectKit.isEmpty(instance)) {
                instance = new Registry();
            }
        }
        return instance;
    }

    /**
     * 注册组件
     *
     * @param name   组件名称
     * @param object 组件对象
     */
    public static void register(String name, Object object) {
        if (COMPLEX_CACHE.containsKey(name)) {
            throw new InternalException("重复注册同名称的校验器：" + name);
        }
        Class<?> clazz = object.getClass();
        if (COMPLEX_CACHE.containsKey(clazz.getSimpleName())) {
            throw new InternalException("重复注册同类型的校验器：" + clazz);
        }
        COMPLEX_CACHE.putIfAbsent(name, object);
        COMPLEX_CACHE.putIfAbsent(clazz.getSimpleName(), object);
    }

    /**
     * 是否包含指定名称的校验器
     *
     * @param name 校验器名称
     * @return true：包含, false：不包含
     */
    public boolean contains(String name) {
        return COMPLEX_CACHE.containsKey(name);
    }

    /**
     * 根据校验器名称获取校验器
     *
     * @param name 校验器名称
     * @return 校验器对象, 找不到时返回null
     */
    public Object require(String name) {
        return COMPLEX_CACHE.get(name);
    }

    /**
     * 优先根据校验器名称获取校验器,找不到时,根据类型获取校验器对象
     *
     * @param name  校验器名称
     * @param clazz 校验器类型
     * @return 校验器对象, 找不到时返回null
     */
    public Object require(String name, Class<?> clazz) {
        Object object = this.require(name);
        if (ObjectKit.isEmpty(object)) {
            object = this.require(clazz.getSimpleName());
        }
        return object;
    }

}
