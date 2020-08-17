/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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
package org.aoju.bus.metric.register;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.toolkit.ClassKit;
import org.aoju.bus.logger.Logger;
import org.objectweb.asm.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Kimi Liu
 * @version 6.0.6
 * @since JDK 1.8++
 */
public class SingleParameterContext {

    private static SingleParameterWrapper singleFieldWrapper = new SingleParameterWrapper();

    private static Map<String, SingleParameterContextValue> context = new ConcurrentHashMap<>(16);

    public static void add(Object handler, Method method, Parameter parameter) {
        String key = method.toString();
        String paramName = getMethodParameterName(handler.getClass(), method, 0);
        Class<?> wrapClass = singleFieldWrapper.wrapParam(parameter, paramName);
        SingleParameterContextValue value = new SingleParameterContextValue();
        value.setHandler(handler);
        value.setMethod(method);
        value.setWrapClass(wrapClass);
        value.setParamName(paramName);
        value.setParameter(parameter);
        Logger.info("包装参数，方法：{}，参数名：{}", method, paramName);
        context.put(key, value);
    }

    public static SingleParameterContextValue get(Method method) {
        return context.get(method.toString());
    }

    public static void setSingleFieldWrapper(SingleParameterWrapper singleFieldWrapper) {
        SingleParameterContext.singleFieldWrapper = singleFieldWrapper;
    }

    /**
     * 获取指定类指定方法的参数名
     *
     * @param clazz  要获取参数名的方法所属的类
     * @param method 要获取参数名的方法
     * @param index  参数索引，从0开始
     * @return 返回指定类指定方法的参数名，没有返回空字符串
     */
    public static String getMethodParameterName(Class<?> clazz, final Method method, int index) {
        String[] names = getMethodParameterNamesByAsm(clazz, method);
        if (names.length == 0) {
            return Normal.EMPTY;
        }
        return names[index];
    }

    /**
     * 获取指定类指定方法的参数名
     *
     * @param clazz  要获取参数名的方法所属的类
     * @param method 要获取参数名的方法
     * @return 按参数顺序排列的参数名列表，如果没有参数，则返回空数组
     */
    public static String[] getMethodParameterNamesByAsm(Class<?> clazz, final Method method) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes == null || parameterTypes.length == 0) {
            return new String[0];
        }
        final Type[] types = new Type[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            types[i] = Type.getType(parameterTypes[i]);
        }
        final String[] parameterNames = new String[parameterTypes.length];
        // 解决clazz对象是cglib对象导致空指针异常
        // 获取真实的class，如果是cglib类，则返回父类class
        Class<?> realClass = ClassKit.getUserClass(clazz);
        String className = realClass.getName();
        int lastDotIndex = className.lastIndexOf('.');
        className = className.substring(lastDotIndex + 1) + ".class";
        InputStream is = realClass.getResourceAsStream(className);
        try {
            ClassReader classReader = new ClassReader(is);
            classReader.accept(new ClassVisitor(Opcodes.ASM6) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                    // 只处理指定的方法
                    Type[] argumentTypes = Type.getArgumentTypes(desc);
                    if (!method.getName().equals(name) || !Arrays.equals(argumentTypes, types)) {
                        return null;
                    }
                    return new MethodVisitor(Opcodes.ASM6) {
                        @Override
                        public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                            int i = index - 1;
                            // 如果是静态方法，则第一就是参数
                            // 如果不是静态方法，则第一个是"this"，然后才是方法的参数
                            if (Modifier.isStatic(method.getModifiers())) {
                                i = index;
                            }
                            if (i >= 0 && i < parameterNames.length) {
                                parameterNames[i] = name;
                            }
                            super.visitLocalVariable(name, desc, signature, start,
                                    end, index);
                        }

                    };

                }
            }, 0);
        } catch (IOException e) {
            org.aoju.bus.logger.Logger.error("生成asm失败，oriClass:{}, realClass:{} method:{}", clazz.getName(), realClass.getName(), method.toGenericString(), e);
        }
        return parameterNames;
    }

    public static class SingleParameterContextValue {
        private Object handler;
        private Method method;
        private String paramName;
        private Parameter parameter;
        private Class<?> wrapClass;

        public Object getHandler() {
            return handler;
        }

        public void setHandler(Object handler) {
            this.handler = handler;
        }

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        public String getParamName() {
            return paramName;
        }

        public void setParamName(String paramName) {
            this.paramName = paramName;
        }

        public Parameter getParameter() {
            return parameter;
        }

        public void setParameter(Parameter parameter) {
            this.parameter = parameter;
        }

        public Class<?> getWrapClass() {
            return wrapClass;
        }

        public void setWrapClass(Class<?> wrapClass) {
            this.wrapClass = wrapClass;
        }

    }

}
