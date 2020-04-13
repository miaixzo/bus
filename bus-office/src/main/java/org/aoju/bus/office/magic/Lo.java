/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
package org.aoju.bus.office.magic;

import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import org.aoju.bus.core.lang.exception.InstrumentException;

import java.util.Optional;

/**
 * 实用程序函数，使Office更易于使用.
 *
 * @author Kimi Liu
 * @version 5.8.3
 * @since JDK 1.8+
 */
public final class Lo {

    /**
     * 为给定的Java类(必须表示一个UNO接口类型)查询给定的UNO对象.
     *
     * @param <T>    请求的UNO接口类型.
     * @param type   表示UNO接口类型的Java类.
     * @param object 对表示UNO对象(方面)的任何Java对象的引用，可能为<code>null</code>.
     * @return 对请求的UNO接口类型的引用(如果可用)，否则null.
     * @see UnoRuntime#queryInterface(Class, Object)
     */
    public static <T> T qi(final Class<T> type, final Object object) {
        return UnoRuntime.queryInterface(type, object);
    }

    /**
     * 为给定的Java类(必须表示一个UNO接口类型)查询给定的UNO对象.
     *
     * @param <T>    请求的UNO接口类型.
     * @param type   表示UNO接口类型的Java类.
     * @param object 对表示UNO对象(方面)的任何Java对象的引用,可能为<code>null</code>.
     * @return 对请求的UNO接口类型的引用(如果可用)，否则null.
     * @see UnoRuntime#queryInterface(Class, Object)
     */
    public static <T> Optional<T> qiOptional(final Class<T> type, final Object object) {
        return Optional.ofNullable(UnoRuntime.queryInterface(type, object));
    }

    /**
     * 获取给定组件的XMultiServiceFactory.
     *
     * @param component 组件.
     * @return 服务工厂.
     */
    public static XMultiServiceFactory getServiceFactory(final XComponent component) {
        return qi(XMultiServiceFactory.class, component);
    }

    /**
     * 从给定的命名服务中创建给定类的接口对象;
     * 使用给定的XComponent和“旧的”XMultiServiceFactory，因此文档必须已经加载/创建.
     *
     * @param <T>         请求的UNO接口类型.
     * @param component   组件t.
     * @param type        表示UNO接口类型的Java类.
     * @param serviceName 服务名称.
     * @return 对请求的UNO接口类型的引用(如果可用)，否则null.
     * @throws InstrumentException 如果发生UNO异常。UNO异常将导致{@link InstrumentException}.
     */
    public static <T> T createInstanceMSF(
            final XComponent component, final Class<T> type, final String serviceName) {
        return createInstanceMSF(getServiceFactory(component), type, serviceName);
    }

    /**
     * 从给定的命名服务中创建给定类的接口对象;使用给定的“旧”XMultiServiceFactory，因此文档必须已经加载/创建.
     *
     * @param <T>         请求的UNO接口类型.
     * @param factory     服务工厂
     * @param type        表示UNO接口类型的Java类.
     * @param serviceName 服务名称.
     * @return 对请求的UNO接口类型的引用(如果可用)，否则null.
     * @throws InstrumentException 如果发生UNO异常。UNO异常将导致{@link InstrumentException}
     */
    public static <T> T createInstanceMSF(
            final XMultiServiceFactory factory, final Class<T> type, final String serviceName) {
        try {
            return qi(type, factory.createInstance(serviceName));
        } catch (Exception ex) {
            throw new InstrumentException(ex.getMessage(), ex);
        }
    }

    /**
     * 从给定的命名服务中创建给定类的接口对象;
     * 使用给定的XComponentContext和“新的”XMultiComponentFactory，
     * 因此只需要一个到office的桥梁
     *
     * @param <T>         请求的UNO接口类型.
     * @param context     组件的上下文.
     * @param type        表示UNO接口类型的Java类.
     * @param serviceName 服务名称.
     * @return 对请求的UNO接口类型的引用(如果可用)，否则null.
     * @throws InstrumentException 如果发生UNO异常。UNO异常将导致{@link InstrumentException}.
     */
    public static <T> T createInstanceMCF(
            final XComponentContext context, final Class<T> type, final String serviceName)
            throws InstrumentException {
        try {
            return qi(type, context.getServiceManager().createInstanceWithContext(serviceName, context));
        } catch (Exception ex) {
            throw new InstrumentException(ex);
        }
    }

}
