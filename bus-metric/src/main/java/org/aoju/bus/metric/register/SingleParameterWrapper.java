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
package org.aoju.bus.metric.register;

import org.aoju.bus.core.utils.ClassUtils;
import org.aoju.bus.core.utils.FileUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Kimi Liu
 * @version 5.5.0
 * @since JDK 1.8++
 */
public class SingleParameterWrapper implements Opcodes {

    private static final Logger logger = LoggerFactory.getLogger(SingleParameterWrapper.class);

    private static final String FOLDER_END_CHAR = "/";

    private static final String CLASS_FILE_SUFFIX = ".class";

    private static final String WRAPPER_PARAM = "WrapperParam";

    private static final String PKG = "org/aoju/bus/metric/gen/";
    private static final String OBJECT = "java/lang/Object";

    private static final ParameterClassLoader CLASS_LOADER = new ParameterClassLoader();

    private static AtomicInteger i = new AtomicInteger();

    // 生成class文件的保存路径
    private String savePath;

    /**
     * 生成一个类，里面指放这个字段
     *
     * @param parameter 字段，只能是基本类型或字符串类型
     * @param paramName 参数名称
     * @return 如果不是基本类型或字符串类型，返回null
     */
    public Class<?> wrapParam(Parameter parameter, String paramName) {
        Class<?> paramType = parameter.getType();
        if (!ClassUtils.isNumberOrStringType(paramType)) {
            return null;
        }
        /********************************class***********************************************/
        // 创建一个ClassWriter, 以生成一个新的类
        ClassWriter classWriter = new ClassWriter(0);
        // 类名
        String className = WRAPPER_PARAM + i.incrementAndGet() + paramName;
        // 类路径名：com/aoju/bus/metric/gen/WrapperParam
        String classpathName = PKG + className;
        classWriter.visit(V1_8, ACC_PUBLIC, classpathName, null, OBJECT, null);

        /*********************************constructor**********************************************/
        MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null,
                null);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, OBJECT, "<init>", "()V", false);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(1, 1);
        methodVisitor.visitEnd();


        /*************************************parameter******************************************/
        // 生成String name字段, Ljava/lang/String;
        Type type = Type.getType(paramType);
        FieldVisitor fieldVisitor = classWriter.visitField(ACC_PUBLIC, paramName, type.getDescriptor(), null, null);
        // 生成验证注解
        Annotation[] annotations = AnnotationUtils.getAnnotations(parameter);
        for (Annotation annotation : annotations) {
            ValidationAnnotationDefinition validationAnnotationDefinition = ValidationAnnotationFactory.build(annotation);
            if (validationAnnotationDefinition == null) {
                continue;
            }
            Class<?> annoClass = validationAnnotationDefinition.getAnnotationClass();
            Type annoType = Type.getType(annoClass);
            AnnotationVisitor annotationVisitor = fieldVisitor.visitAnnotation(annoType.getDescriptor(), true);
            Map<String, Object> properties = validationAnnotationDefinition.getProperties();
            if (properties != null) {
                try {
                    Set<Map.Entry<String, Object>> entrySet = properties.entrySet();
                    for (Map.Entry<String, Object> entry : entrySet) {
                        Object val = entry.getValue();
                        if (val != null) {
                            // 设置枚举值
                            if (val.getClass().isEnum()) {
                                Type eType = Type.getType(val.getClass());
                                annotationVisitor.visitEnum(entry.getKey(), eType.getDescriptor(), val.toString());
                            } else if (val instanceof Class<?>) {
                                // val是Class类型
                                Type vType = Type.getType((Class<?>) val);
                                annotationVisitor.visit(entry.getKey(), vType);
                            } else {
                                annotationVisitor.visit(entry.getKey(), val);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("ASM生成注解出错", e);
                }
            }
            // 结束生成注解
            annotationVisitor.visitEnd();
            logger.info("ASM生成参数注解，参数：{}，注解：{}，注解属性：{}", paramName, annoClass.getName(), properties);
        }
        fieldVisitor.visitEnd();

        classWriter.visitEnd();

        byte[] code = classWriter.toByteArray();

        if (StringUtils.isNotBlank(savePath)) {
            if (!savePath.endsWith(FOLDER_END_CHAR)) {
                savePath = savePath + FOLDER_END_CHAR;
            }
            this.writeClassFile(code, savePath + className + CLASS_FILE_SUFFIX);
        }

        Class<?> clazz = CLASS_LOADER.defineClass(code);
        logger.info("生成参数包装类：{}，包装参数名：{}，参数类型：{}", clazz.getName(), paramName, paramType);
        return clazz;
    }

    protected void writeClassFile(byte[] code, String filepath) {
        // 将二进制流写到本地磁盘上
        FileUtils.writeBytes(code, new File(filepath));
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }


    // 自定义类加载器
    static class ParameterClassLoader extends ClassLoader {
        public ParameterClassLoader() {
            /*
                指定父加载器，不指定默认为AppClassLoader
                springboot启动会使用自带的org.springframework.boot.loader.LaunchedURLClassLoader
                如果不指定，会出现加载器不一致，导致ASM生成的class获取不到字段的注解。
                因此ASM生成的class必须使用当前ClassLoader进行生成。
             */
            super(Thread.currentThread().getContextClassLoader());
        }

        /**
         * 加载class
         *
         * @param clazz 字节码
         * @return the class
         */
        public Class<?> defineClass(byte[] clazz) {
            return this.defineClass(null, clazz, 0, clazz.length);
        }
    }

}
