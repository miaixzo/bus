/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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
package org.aoju.bus.cron.factory;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.ClassUtils;
import org.aoju.bus.core.utils.ReflectUtils;
import org.aoju.bus.core.utils.StringUtils;

import java.lang.reflect.Method;

/**
 * 反射执行任务
 * 通过传入类名#方法名,通过反射执行相应的方法
 * 如果是静态方法直接执行,如果是对象方法,需要类有默认的构造方法
 *
 * @author Kimi Liu
 * @version 5.6.1
 * @since JDK 1.8+
 */
public class InvokeTask implements Task {

    private Class<?> clazz;
    private Object obj;
    private Method method;

    /**
     * 构造
     *
     * @param classNameWithMethodName 类名与方法名的字符串表示,方法名和类名使用#隔开或者.隔开
     */
    public InvokeTask(String classNameWithMethodName) {
        int splitIndex = classNameWithMethodName.lastIndexOf(Symbol.C_SHAPE);
        if (splitIndex <= 0) {
            splitIndex = classNameWithMethodName.lastIndexOf(Symbol.C_DOT);
        }
        if (splitIndex <= 0) {
            throw new InstrumentException("Invalid classNameWithMethodName [{}]!", classNameWithMethodName);
        }

        //类
        final String className = classNameWithMethodName.substring(0, splitIndex);
        if (StringUtils.isBlank(className)) {
            throw new IllegalArgumentException("Class name is blank !");
        }
        this.clazz = ClassUtils.loadClass(className);
        if (null == this.clazz) {
            throw new IllegalArgumentException("Load class with name of [" + className + "] fail !");
        }
        this.obj = ReflectUtils.newInstanceIfPossible(this.clazz);

        //方法
        final String methodName = classNameWithMethodName.substring(splitIndex + 1);
        if (StringUtils.isBlank(methodName)) {
            throw new IllegalArgumentException("Method name is blank !");
        }
        this.method = ClassUtils.getPublicMethod(this.clazz, methodName);
        if (null == this.method) {
            throw new IllegalArgumentException("No method with name of [" + methodName + "] !");
        }
    }

    @Override
    public void execute() {
        try {
            ReflectUtils.invoke(this.obj, this.method, new Object[]{});
        } catch (InstrumentException e) {
            throw new InstrumentException(e.getCause());
        }
    }

}
