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
package org.miaixz.bus.cron.crontab;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.CrontabException;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.core.xyz.ReflectKit;
import org.miaixz.bus.core.xyz.StringKit;

import java.lang.reflect.Method;

/**
 * 反射执行任务
 * 通过传入类名#方法名，通过反射执行相应的方法
 * 如果是静态方法直接执行，如果是对象方法，需要类有默认的构造方法。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class InvokeCrontab implements Crontab {

    private final Object obj;
    private final Method method;

    /**
     * 构造
     *
     * @param classNameWithMethodName 类名与方法名的字符串表示，方法名和类名使用#隔开或者.隔开
     */
    public InvokeCrontab(final String classNameWithMethodName) {
        int splitIndex = classNameWithMethodName.lastIndexOf(Symbol.C_SHAPE);
        if (splitIndex <= 0) {
            splitIndex = classNameWithMethodName.lastIndexOf('.');
        }
        if (splitIndex <= 0) {
            throw new InternalException("Invalid classNameWithMethodName [{}]!", classNameWithMethodName);
        }

        //类
        final String className = classNameWithMethodName.substring(0, splitIndex);
        if (StringKit.isBlank(className)) {
            throw new IllegalArgumentException("Class name is blank !");
        }
        final Class<?> clazz = ClassKit.loadClass(className);
        if (null == clazz) {
            throw new IllegalArgumentException("Load class with name of [" + className + "] fail !");
        }
        this.obj = ReflectKit.newInstanceIfPossible(clazz);

        //方法
        final String methodName = classNameWithMethodName.substring(splitIndex + 1);
        if (StringKit.isBlank(methodName)) {
            throw new IllegalArgumentException("Method name is blank !");
        }
        this.method = MethodKit.getPublicMethod(clazz, false, methodName);
        if (null == this.method) {
            throw new IllegalArgumentException("No method with name of [" + methodName + "] !");
        }
    }

    @Override
    public void execute() {
        try {
            MethodKit.invoke(this.obj, this.method);
        } catch (final InternalException e) {
            throw new CrontabException(e.getCause());
        }
    }

}
