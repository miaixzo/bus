/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.core.utils;

import org.aoju.bus.core.consts.Symbol;
import org.aoju.bus.core.io.FastByteArray;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.text.StrBuilder;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * 一些通用的函数
 *
 * @author Kimi Liu
 * @version 5.2.2
 * @since JDK 1.8+
 */
public class ObjectUtils {

    /**
     * 比较两个对象是否相等
     *
     * @param obj1 对象1
     * @param obj2 对象2
     * @return 是否相等
     */
    public static boolean equal(Object obj1, Object obj2) {
        return (obj1 == obj2) || (obj1 != null && obj1.equals(obj2));
    }

    /**
     * 比较两个对象是否不相等
     *
     * @param obj1 对象1
     * @param obj2 对象2
     * @return 是否不等
     * @since 3.0.7
     */
    public static boolean notEqual(Object obj1, Object obj2) {
        return false == equal(obj1, obj2);
    }

    /**
     * 计算对象长度,如果是字符串调用其length函数,
     * 集合类调用其size函数, 数组调用其length属性,
     * 其他可遍历对象遍历计算长度
     *
     * @param obj 被计算长度的对象
     * @return 长度
     */
    public static int length(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof CharSequence) {
            return ((CharSequence) obj).length();
        }
        if (obj instanceof Collection) {
            return ((Collection<?>) obj).size();
        }
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).size();
        }

        int count;
        if (obj instanceof Iterator) {
            Iterator<?> iter = (Iterator<?>) obj;
            count = 0;
            while (iter.hasNext()) {
                count++;
                iter.next();
            }
            return count;
        }
        if (obj instanceof Enumeration) {
            Enumeration<?> enumeration = (Enumeration<?>) obj;
            count = 0;
            while (enumeration.hasMoreElements()) {
                count++;
                enumeration.nextElement();
            }
            return count;
        }
        if (obj.getClass().isArray() == true) {
            return Array.getLength(obj);
        }
        return -1;
    }

    /**
     * 对象中是否包含元素
     *
     * @param obj     对象
     * @param element 元素
     * @return 是否包含
     */
    public static boolean contains(Object obj, Object element) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof String) {
            if (element == null) {
                return false;
            }
            return ((String) obj).contains(element.toString());
        }
        if (obj instanceof Collection) {
            return ((Collection<?>) obj).contains(element);
        }
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).values().contains(element);
        }

        if (obj instanceof Iterator) {
            Iterator<?> iter = (Iterator<?>) obj;
            while (iter.hasNext()) {
                Object o = iter.next();
                if (equal(o, element)) {
                    return true;
                }
            }
            return false;
        }
        if (obj instanceof Enumeration) {
            Enumeration<?> enumeration = (Enumeration<?>) obj;
            while (enumeration.hasMoreElements()) {
                Object o = enumeration.nextElement();
                if (equal(o, element)) {
                    return true;
                }
            }
            return false;
        }
        if (obj.getClass().isArray() == true) {
            int len = Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                Object o = Array.get(obj, i);
                if (equal(o, element)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查对象是否为null
     *
     * @param obj 对象
     * @return 是否为null
     */
    public static boolean isNull(Object obj) {
        return null == obj || obj.equals(null);
    }

    /**
     * 检查对象是否不为null
     *
     * @param obj 对象
     * @return 是否为null
     */
    public static boolean isNotNull(Object obj) {
        return null != obj && false == obj.equals(null);
    }

    /**
     * 如果给定对象为{@code null}返回默认值
     *
     * @param <T>          对象类型
     * @param object       被检查对象,可能为{@code null}
     * @param defaultValue 被检查对象为{@code null}返回的默认值,可以为{@code null}
     * @return 被检查对象为{ null}返回默认值,否则返回原值
     * @since 3.0.7
     */
    public static <T> T defaultIfNull(final T object, final T defaultValue) {
        return (null != object) ? object : defaultValue;
    }

    /**
     * 判断对象为true
     *
     * @param object 对象
     * @return 对象是否为true
     */
    public static boolean isTrue(Boolean object) {
        return Boolean.TRUE.equals(object);
    }

    /**
     * 判断对象为false
     *
     * @param object 对象
     * @return 对象是否为false
     */
    public static boolean isFalse(Boolean object) {
        return object == null || Boolean.FALSE.equals(object);
    }

    /**
     * 确定给定的对象是一个数组:对象数组还是基元数组
     *
     * @param object 要检查的对象
     * @return the true/false
     */
    public static boolean isArray(Object object) {
        return (object != null && object.getClass().isArray());
    }

    /**
     * 检查是否为有效的数字
     * 检查Double和Float是否为无限大,或者Not a Number
     * 非数字类型和Null将返回true
     *
     * @param obj 被检查类型
     * @return 检查结果, 非数字类型和Null将返回true
     */
    public static boolean isValidIfNumber(Object obj) {
        if (obj != null && obj instanceof Number) {
            if (obj instanceof Double) {
                return !((Double) obj).isInfinite() && !((Double) obj).isNaN();
            } else if (obj instanceof Float) {
                return !((Float) obj).isInfinite() && !((Float) obj).isNaN();
            }
        }
        return true;
    }

    /**
     * 克隆对象
     * 如果对象实现Cloneable接口,调用其clone方法
     * 如果实现Serializable接口,执行深度克隆
     * 否则返回null
     *
     * @param <T> 对象类型
     * @param obj 被克隆对象
     * @return 克隆后的对象
     */
    public static <T> T clone(T obj) {
        T result = ArrayUtils.clone(obj);
        if (null == result) {
            if (obj instanceof Cloneable) {
                result = ReflectUtils.invoke(obj, "clone", new Object[]{});
            } else {
                result = cloneByStream(obj);
            }
        }
        return result;
    }

    /**
     * 序列化后拷贝流的方式克隆
     * 对象必须实现Serializable接口
     *
     * @param <T> 对象类型
     * @param obj 被克隆对象
     * @return 克隆后的对象
     * @throws InstrumentException IO异常和ClassNotFoundException封装
     */
    public static <T> T cloneByStream(T obj) {
        if (null == obj || false == (obj instanceof Serializable)) {
            return null;
        }
        final FastByteArray byteOut = new FastByteArray();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(byteOut);
            out.writeObject(obj);
            out.flush();
            final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(byteOut.toByteArray()));
            return (T) in.readObject();
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * {@code null}安全的对象比较,{@code null}对象排在末尾
     *
     * @param <T> 被比较对象类型
     * @param c1  对象1,可以为{@code null}
     * @param c2  对象2,可以为{@code null}
     * @return 比较结果, 如果c1 &lt; c2,返回数小于0,c1==c2返回0,c1 &gt; c2 大于0
     * @see Comparator#compare(Object, Object)
     * @since 3.0.7
     */
    public static <T extends Comparable<? super T>> int compare(T c1, T c2) {
        return compare(c1, c2, false);
    }

    /**
     * {@code null}安全的对象比较
     *
     * @param <T>         被比较对象类型
     * @param c1          对象1,可以为{@code null}
     * @param c2          对象2,可以为{@code null}
     * @param nullGreater 当被比较对象为null时是否排在前面
     * @return 比较结果, 如果c1 &lt; c2,返回数小于0,c1==c2返回0,c1 &gt; c2 大于0
     * @see Comparator#compare(Object, Object)
     * @since 3.0.7
     */
    public static <T extends Comparable<? super T>> int compare(T c1, T c2, boolean nullGreater) {
        if (c1 == c2) {
            return 0;
        } else if (c1 == null) {
            return nullGreater ? 1 : -1;
        } else if (c2 == null) {
            return nullGreater ? -1 : 1;
        }
        return c1.compareTo(c2);
    }

    /**
     * 判断对象是否Empty(null或元素为0)
     * 实用于对如下对象做判断:String Collection及其子类 Map及其子类
     *
     * @param object 待检查对象
     * @return boolean 返回的布尔值
     */
    public static final boolean isEmpty(Object... object) {
        for (Object pObj :
                object) {
            if (pObj == null || "".equals(pObj)) {
                return true;
            }
            if (pObj instanceof String) {
                if (((String) pObj).trim().length() == 0) {
                    return true;
                }
            } else if (pObj instanceof Collection<?>) {
                if (((Collection<?>) pObj).size() == 0) {
                    return true;
                }
            } else if (pObj instanceof Map<?, ?>) {
                if (((Map<?, ?>) pObj).size() == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断对象是否为NotEmpty(!null或元素大于0)
     * 实用于对如下对象做判断:String Collection及其子类 Map及其子类
     *
     * @param object 待检查对象
     * @return boolean 返回的布尔值
     */
    public static final boolean isNotEmpty(Object... object) {
        return !isEmpty(object);
    }

    /**
     * 获取包括父类所有的属性
     *
     * @param object 对象
     * @return the field
     */
    public static Field[] getAllFields(Object object) {
        List<Field> fieldList = new ArrayList<Field>();
        Class tempClass = object.getClass();
        while (tempClass != null && !tempClass.getName().toLowerCase().equals("java.lang.object")) {
            // 当父类为null的时候说明到达了最上层的父类(Object类).
            fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
            // 得到父类,然后赋给自己
            tempClass = tempClass.getSuperclass();
        }
        Field[] fields = new Field[fieldList.size()];
        fieldList.toArray(fields);
        return fields;
    }

    public static void transMap2Bean(Map<String, Object> map, Object obj) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor property : propertyDescriptors) {
                String key = property.getName();
                if (map.containsKey(key)) {
                    Object value = map.get(key);
                    // 得到property对应的setter方法
                    Method setter = property.getWriteMethod();
                    setter.invoke(obj, value);
                }
            }
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * @param <T>     对象
     * @param oldBean 原对象
     * @param newBean 新对象
     * @return 对象
     */
    public static <T> T getDiff(T oldBean, T newBean) {
        if (oldBean == null && newBean != null) {
            return newBean;
        } else if (newBean == null) {
            return null;
        } else {
            Class<?> cls1 = oldBean.getClass();
            try {
                T object = (T) cls1.newInstance();
                BeanInfo beanInfo = Introspector.getBeanInfo(cls1);
                PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                for (PropertyDescriptor property : propertyDescriptors) {
                    String key = property.getName();
                    // 过滤class属性
                    if (!key.equals("class")) {
                        // 得到property对应的getter方法
                        Method getter = property.getReadMethod();
                        // 得到property对应的setter方法
                        Method setter = property.getWriteMethod();
                        Object oldValue = getter.invoke(oldBean);
                        Object newValue = getter.invoke(newBean);
                        if (newValue != null) {
                            if (oldValue == null) {
                                setter.invoke(object, newValue);
                            } else if (oldValue != null && !newValue.equals(oldValue)) {
                                setter.invoke(object, newValue);
                            }
                        }
                    }
                }
                return object;
            } catch (Exception e) {
                throw new InstrumentException(e);
            }
        }
    }


    /***
     * 将对象序列化后进行base64处理
     * @param obj    对象
     * @return base64的序列化对象数据
     */
    public static String toBase64(Object obj) {
        return StringUtils.toBase64(toByte(obj));
    }

    /**
     * 将对象进行序列化
     *
     * @param obj 对象
     * @return 对象序列化后的数据
     */
    public static byte[] toByte(Object obj) {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
            objOut.writeObject(obj);
            return byteOut.toByteArray();
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 将base64的序列化数据转换为对象
     *
     * @param <T>    对象
     * @param base64 经过base64的序列化对象数据
     * @return 原对象
     */
    public static <T> T toObject(String base64) {
        return toObject(StringUtils.base64ToByte(base64));
    }

    /**
     * 将序列化数据转换为对象
     *
     * @param <T> 对象
     * @param bts 序列化后的对象数据
     * @return 原对象
     */
    public static <T> T toObject(byte[] bts) {
        try {
            ByteArrayInputStream byteIn = new ByteArrayInputStream(bts);
            ObjectInputStream objIn = new ObjectInputStream(byteIn);
            return (T) objIn.readObject();
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /***
     * 依据class的名称获取对应class
     * @param classAllName    类的全称(如: java.lang.String)
     * @return 返回依据类名映射的class对象
     */
    public static Class getClassByName(String classAllName) {
        try {
            return Class.forName(classAllName);
        } catch (ClassNotFoundException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 初始化对象
     *
     * @param <T>     对象
     * @param clazz   创建的对象的类型
     * @param attrMap 初始对象的属性值
     * @return 创建的对象
     */
    public static <T> T initObject(Class<T> clazz, Map<String, Object> attrMap) {
        try {
            T obj = clazz.newInstance();
            if (attrMap != null) {
                // 移除所有的常量赋值
                for (Class tempClass = clazz; !tempClass.equals(Object.class); tempClass = tempClass.getSuperclass()) {
                    Field[] fs = tempClass.getDeclaredFields();
                    for (Field f : fs) {
                        f.setAccessible(true);
                        if (Modifier.isFinal(f.getModifiers())) {
                            attrMap.remove(f.getName());
                        }
                        f.setAccessible(false);
                    }
                }
                // 开始赋值
                for (String attrName : attrMap.keySet()) {
                    setAttribute(obj, attrName, attrMap.get(attrName));
                }
            }
            return obj;
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 给对象的属性赋值
     *
     * @param obj      对象
     * @param attrName 对象的属性名
     * @param value    对象的属性值
     */
    public static void setAttribute(Object obj, String attrName, Object value) {
        try {
            Class clazz = obj.getClass();
            while (!clazz.equals(Object.class)) {
                try {
                    Field f = clazz.getDeclaredField(attrName);
                    f.setAccessible(true);
                    f.set(obj, value);
                    f.setAccessible(false);
                    return;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 从对象中取值
     *
     * @param obj      对象
     * @param attrName 要取值的属性名
     * @return 值
     */
    public static Object getAttributeValue(Object obj, String attrName) {
        try {
            Class clazz = obj.getClass();
            while (!clazz.equals(Object.class)) {
                try {
                    Field f = clazz.getDeclaredField(attrName);
                    f.setAccessible(true);
                    Object value = f.get(obj);
                    f.setAccessible(false);
                    return value;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }

            return null;

        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获取对象中的所有属性
     *
     * @param bean 对象
     * @return 属性和值(Map[属性名, 属性值])
     */
    public static Map<String, Object> getFields(Object bean) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            for (Class clazz = bean.getClass(); !clazz.equals(Object.class); clazz = clazz.getSuperclass()) {
                Field[] fs = clazz.getDeclaredFields();
                for (Field f : fs) {
                    // 子类最大,父类值不覆盖子类
                    if (map.containsKey(f.getName())) {
                        continue;
                    }
                    f.setAccessible(true);
                    Object value = f.get(bean);
                    f.setAccessible(false);
                    map.put(f.getName(), value);
                }
            }
            map.remove("serialVersionUID");
            return map;
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获取类的所有属性与属性的类型
     *
     * @param clazz 类
     * @return 该类的所有属性名与属性类型(包含父类属性)
     */
    public static Map<String, Class> getFieldNames(Class clazz) {
        try {
            Map<String, Class> attrMap = new HashMap<String, Class>();
            for (; !clazz.equals(Object.class); clazz = clazz.getSuperclass()) {
                Field[] fs = clazz.getDeclaredFields();
                for (Field f : fs) {
                    attrMap.put(f.getName(), f.getType());
                }
            }
            attrMap.remove("serialVersionUID");
            return attrMap;
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获取对象中的非空属性(属性如果是对象,则只会在同一个map中新增,不会出现map嵌套情况)
     *
     * @param bean         对象
     * @param hasInitValue 是否过滤掉初始值(true:过滤掉)
     * @return 非空属性和值(Map[属性名, 属性值])
     */
    public static Map<String, Object> getNotNullFields(Object bean, boolean hasInitValue) {
        try {
            if (hasInitValue) {
                cleanInitValue(bean);
            }
            Map<String, Object> map = new HashMap<String, Object>();
            for (Class clazz = bean.getClass(); !clazz.equals(Object.class); clazz = clazz.getSuperclass()) {
                Field[] fs = clazz.getDeclaredFields();
                for (Field f : fs) {
                    // 子类最大,父类值不覆盖子类
                    if (map.containsKey(f.getName())) {
                        continue;
                    }
                    f.setAccessible(true);
                    Object value = f.get(bean);
                    f.setAccessible(false);
                    if (value != null) {
                        map.put(f.getName(), value);
                    }
                }
            }
            map.remove("serialVersionUID");
            return map;
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获取对象中的非空属性(属性如果是对象,则只会在同一个map中新增,不会出现map嵌套情况)
     * (不会清空初始值)
     *
     * @param bean 对象
     * @return 非空属性和值(Map[属性名, 属性值])
     */
    public static Map<String, Object> getNotNullFields(Object bean) {
        return getNotNullFields(bean, true);
    }


    /**
     * 获取对象中的非空属性(属性如果是对象,则只会在同一个map中新增,不会出现map嵌套情况)
     * (不会清空初始值)
     * <p>
     * request param
     *
     * @param bean 对象
     * @return 非空属性和值(Map[属性名, 属性值])
     */
    public static Map<String, List<String>> getNotNullFieldsParam(Object bean) {
        return getNotNullFieldsParam(bean, true);
    }

    /**
     * 获取对象中的非空属性(属性如果是对象,则只会在同一个map中新增,不会出现map嵌套情况)
     *
     * @param bean         对象
     * @param hasInitValue 是否过滤掉初始值(true:过滤掉)
     * @return 非空属性和值(Map[属性名, 属性值])
     */
    public static Map<String, List<String>> getNotNullFieldsParam(Object bean, boolean hasInitValue) {
        try {
            if (hasInitValue) {
                cleanInitValue(bean);
            }
            Map<String, List<String>> map = new HashMap<>();
            for (Class clazz = bean.getClass(); !clazz.equals(Object.class); clazz = clazz.getSuperclass()) {
                Field[] fs = clazz.getDeclaredFields();
                for (Field f : fs) {
                    // 子类最大,父类值不覆盖子类
                    if (map.containsKey(f.getName())) {
                        continue;
                    }
                    f.setAccessible(true);
                    Object value = f.get(bean);
                    f.setAccessible(false);
                    if (value != null) {
                        List<String> list = new ArrayList<>();
                        list.add(String.valueOf(value));
                        map.put(f.getName(), list);
                    }
                }
            }
            map.remove("serialVersionUID");
            return map;
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }


    /**
     * 获取对象中的非空属性(属性如果是对象,则会嵌套map)
     *
     * @param bean 对象
     * @return 非空属性和值(Map[属性名, 属性值])
     */
    public static Map<String, Object> getNotNullFieldsForStructure(Object bean) {
        try {
            Map<String, Object> map = new HashMap<>();
            for (Class clazz = bean.getClass(); !clazz.equals(Object.class); clazz = clazz.getSuperclass()) {
                Field[] fs = clazz.getDeclaredFields();
                for (Field f : fs) {
                    // 子类最大,父类值不覆盖子类
                    if (map.containsKey(f.getName())) {
                        continue;
                    }
                    f.setAccessible(true);
                    Object value = f.get(bean);
                    f.setAccessible(false);
                    if (value != null) {
                        if (!isNotStructure(value)) {
                            map.put(f.getName(), getNotNullFieldsForStructure(value));
                        } else {
                            map.put(f.getName(), value);
                        }
                    }
                }
            }
            map.remove("serialVersionUID");
            return map;
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /***
     * 依据类,获取该类的泛型class
     * @param <T> 对象
     * @param clazz   类对象
     * @return 泛型类型
     */
    public static <T extends Object> Class<T> getGeneric(Class clazz) {
        try {
            Type genType = clazz.getGenericSuperclass();
            if (!(genType instanceof ParameterizedType)) {
                return (Class<T>) Object.class;
            }
            Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
            return (Class<T>) params[0];
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 将byte字节转换成对象
     *
     * @param <T> 对象
     * @param bts 字节数据
     * @return 对象
     */
    public static <T extends Object> T parseByteForObj(byte[] bts) {
        ByteArrayInputStream input = new ByteArrayInputStream(bts);
        ObjectInputStream objectInput = null;
        try {
            objectInput = new ObjectInputStream(input);
            return (T) objectInput.readObject();
        } catch (Exception e) {
            throw new InstrumentException(e);
        } finally {
            try {
                if (objectInput != null) {
                    objectInput.close();
                }
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                throw new InstrumentException(e);
            }
        }
    }

    /**
     * 将对象转换为byte数据
     *
     * @param obj 对象
     * @return byte数据
     */
    public static byte[] parseObjForByte(Object obj) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = null;
        try {
            objOut = new ObjectOutputStream(byteOut);
            objOut.writeObject(obj);
            return byteOut.toByteArray();
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            try {
                if (objOut != null) {
                    objOut.close();
                }
                if (byteOut != null) {
                    byteOut.close();
                }
            } catch (IOException e) {
                throw new InstrumentException(e);
            }
        }
    }

    /***
     * 转换类型
     * @param <T>     对象
     * @param value   字符串的值
     * @param type    要转换的类型
     * @return 转换后的值
     */
    public static <T> T parseToObject(Object value, Class<T> type) {
        Object result = null;
        if (value == null || type == String.class) {
            result = value == null ? null : value.toString();
        } else if (type == Character.class || type == char.class) {
            char[] chars = value.toString().toCharArray();
            result = chars.length > 0 ? chars.length > 1 ? chars : chars[0] : Character.MIN_VALUE;
        } else if (type == Boolean.class || type == boolean.class) {
            result = Boolean.parseBoolean(value.toString());
        }
        // 处理boolean值转换
        else if (type == Double.class || type == double.class) {
            result = value.toString().equalsIgnoreCase("true") ? true : value.toString().equalsIgnoreCase("false") ? false : value;
        } else if (type == Long.class || type == long.class) {
            result = Long.parseLong(value.toString());
        } else if (type == Integer.class || type == int.class) {
            result = Integer.parseInt(value.toString());
        } else if (type == Double.class || type == double.class) {
            result = Double.parseDouble(value.toString());
        } else if (type == Float.class || type == float.class) {
            result = Float.parseFloat(value.toString());
        } else if (type == Byte.class || type == byte.class) {
            result = Byte.parseByte(value.toString());
        } else if (type == Short.class || type == short.class) {
            result = Short.parseShort(value.toString());
        }
        return (T) result;
    }

    /***
     * 是否非结构体(不再解析)
     * @param value    要验证数据
     * @return 是否是结构体
     */
    private static boolean isNotStructure(Object value) {
        if (!isBaseClass(value)) {
            if (value instanceof Collection) {
                return true;
            } else if (value instanceof Map) {
                return true;
            } else if (value instanceof Date) {
                return true;
            } else return value.getClass().isArray();
        }
        return true;
    }

    /***
     * 校验是否是九种基础类型(即：非用户定义的类型)
     * @param value 字符串的值	要校验的值
     * @return 是否是基础类型(true : 已经是基础类型了)
     */
    public static boolean isBaseClass(Object value) {
        if (value == null) {
            return true;
        } else if (value instanceof Long) {
            return true;
        } else if (value instanceof Integer) {
            return true;
        } else if (value instanceof Double) {
            return true;
        } else if (value instanceof Float) {
            return true;
        } else if (value instanceof Byte) {
            return true;
        } else if (value instanceof Boolean) {
            return true;
        } else if (value instanceof Short) {
            return true;
        } else if (value instanceof Character) {
            return true;
        } else return value instanceof String;
    }

    /***
     * 克隆有序列化的对象
     * @param <T>	要返回的数据类型
     * @param clazz 反射类
     * @param bean  所有继承过BaseBean的对象
     * @return 克隆后的对象
     */
    public static <T> T CloneObject(Class<T> clazz, Object bean) {
        try {
            Map<String, Object> attrMap = getFields(bean);
            return initObject(clazz, attrMap);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /***
     * 克隆有序列化的对象
     * @param <T>	要返回的数据类型
     * @param bean    要克隆的对象
     * @return 克隆后的对象
     */
    public static <T> T CloneObject(T bean) {
        try {
            Map<String, Object> attrMap = getFields(bean);
            return (T) initObject(bean.getClass(), attrMap);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 将新数据的非空属性值插入到基本数据中
     *
     * @param baseData 基本数据
     * @param newData  新数据
     */
    public static void insertObj(Object baseData, Object newData) {
        try {
            if (baseData == null || newData == null) {
                return;
            }
            // 清空初始值
            Map<String, Object> attrList = getNotNullFields(newData);
            Set<String> keys = attrList.keySet();
            if (keys != null && keys.size() > 0) {
                for (String key : keys) {
                    if (!key.equals("serialVersionUID")) {
                        setAttribute(baseData, key, attrList.get(key));
                    }
                }
            }
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 清空对象中所有属性的初始值
     *
     * @param <T>  对象
     * @param bean 对象
     */
    public static <T> void cleanInitValue(T bean) {
        if (bean == null) {
            return;
        }
        try {
            Class<?> clazz = bean.getClass();
            Object obj = clazz.newInstance();
            for (; !clazz.equals(Object.class); clazz = clazz.getSuperclass()) {
                Field[] fs = clazz.getDeclaredFields();
                for (Field f : fs) {
                    if (Modifier.isFinal(f.getModifiers())) {
                        continue;
                    }
                    f.setAccessible(true);
                    Object initValue = f.get(obj);
                    Object oldValue = f.get(bean);
                    if (initValue != null && initValue.equals(oldValue)) {
                        f.set(bean, null);
                    }
                    f.setAccessible(false);
                }
            }
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * <p>Gets the toString that would be produced by {@code Object}
     * if a class did not override toString itself. {@code null}
     * will return {@code null}.</p>
     *
     * <pre>
     * ObjectUtils.identityToString(null)         = null
     * ObjectUtils.identityToString("")           = "java.lang.String@1e23"
     * ObjectUtils.identityToString(Boolean.TRUE) = "java.lang.Boolean@7fa"
     * </pre>
     *
     * @param object the object to create a toString for, may be
     *               {@code null}
     * @return the default toString text, or {@code null} if
     * {@code null} passed in
     */
    public static String identityToString(final Object object) {
        if (object == null) {
            return null;
        }
        final String name = object.getClass().getName();
        final String hexString = Integer.toHexString(System.identityHashCode(object));
        final StringBuilder builder = new StringBuilder(name.length() + 1 + hexString.length());
        // @formatter:off
        builder.append(name)
                .append(Symbol.C_AT)
                .append(hexString);
        // @formatter:off
        return builder.toString();
    }

    /**
     * <p>Appends the toString that would be produced by {@code Object}
     * if a class did not override toString itself. {@code null}
     * will throw a NullPointerException for either of the two parameters. </p>
     *
     * <pre>
     * ObjectUtils.identityToString(appendable, "")            = appendable.append("java.lang.String@1e23"
     * ObjectUtils.identityToString(appendable, Boolean.TRUE)  = appendable.append("java.lang.Boolean@7fa"
     * ObjectUtils.identityToString(appendable, Boolean.TRUE)  = appendable.append("java.lang.Boolean@7fa")
     * </pre>
     *
     * @param appendable the appendable to append to
     * @param object     the object to create a toString for
     * @throws IOException if an I/O error occurs
     * @since 3.2.0
     */
    public static void identityToString(final Appendable appendable, final Object object) throws IOException {
        Assert.notNull(object, "Cannot get the toString of a null object");
        appendable.append(object.getClass().getName())
                .append(Symbol.C_AT)
                .append(Integer.toHexString(System.identityHashCode(object)));
    }

    /**
     * <p>Appends the toString that would be produced by {@code Object}
     * if a class did not override toString itself. {@code null}
     * will throw a NullPointerException for either of the two parameters. </p>
     *
     * <pre>
     * ObjectUtils.identityToString(builder, "")            = builder.append("java.lang.String@1e23"
     * ObjectUtils.identityToString(builder, Boolean.TRUE)  = builder.append("java.lang.Boolean@7fa"
     * ObjectUtils.identityToString(builder, Boolean.TRUE)  = builder.append("java.lang.Boolean@7fa")
     * </pre>
     *
     * @param builder the builder to append to
     * @param object  the object to create a toString for
     * @since 3.2.0
     */
    public static void identityToString(final StrBuilder builder, final Object object) {
        Assert.notNull(object, "Cannot get the toString of a null object");
        final String name = object.getClass().getName();
        final String hexString = Integer.toHexString(System.identityHashCode(object));
        builder.ensureCapacity(builder.length() + name.length() + 1 + hexString.length());
        builder.append(name)
                .append(Symbol.C_AT)
                .append(hexString);
    }

    /**
     * <p>Appends the toString that would be produced by {@code Object}
     * if a class did not override toString itself. {@code null}
     * will throw a NullPointerException for either of the two parameters. </p>
     *
     * <pre>
     * ObjectUtils.identityToString(buf, "")            = buf.append("java.lang.String@1e23"
     * ObjectUtils.identityToString(buf, Boolean.TRUE)  = buf.append("java.lang.Boolean@7fa"
     * ObjectUtils.identityToString(buf, Boolean.TRUE)  = buf.append("java.lang.Boolean@7fa")
     * </pre>
     *
     * @param buffer the buffer to append to
     * @param object the object to create a toString for
     * @since 2.4.0
     */
    public static void identityToString(final StringBuffer buffer, final Object object) {
        Assert.notNull(object, "Cannot get the toString of a null object");
        final String name = object.getClass().getName();
        final String hexString = Integer.toHexString(System.identityHashCode(object));
        buffer.ensureCapacity(buffer.length() + name.length() + 1 + hexString.length());
        buffer.append(name)
                .append(Symbol.C_AT)
                .append(hexString);
    }

    /**
     * <p>Appends the toString that would be produced by {@code Object}
     * if a class did not override toString itself. {@code null}
     * will throw a NullPointerException for either of the two parameters. </p>
     *
     * <pre>
     * ObjectUtils.identityToString(builder, "")            = builder.append("java.lang.String@1e23"
     * ObjectUtils.identityToString(builder, Boolean.TRUE)  = builder.append("java.lang.Boolean@7fa"
     * ObjectUtils.identityToString(builder, Boolean.TRUE)  = builder.append("java.lang.Boolean@7fa")
     * </pre>
     *
     * @param builder the builder to append to
     * @param object  the object to create a toString for
     * @since 3.2.0
     */
    public static void identityToString(final StringBuilder builder, final Object object) {
        Assert.notNull(object, "Cannot get the toString of a null object");
        final String name = object.getClass().getName();
        final String hexString = Integer.toHexString(System.identityHashCode(object));
        builder.ensureCapacity(builder.length() + name.length() + 1 + hexString.length());
        builder.append(name)
                .append(Symbol.C_AT)
                .append(hexString);
    }

    /**
     * Determine if the given objects are equal, returning {@code true} if
     * both are {@code null} or {@code false} if only one is {@code null}.
     * <p>Compares arrays with {@code Arrays.equals}, performing an equality
     * check based on the array elements rather than the array reference.
     *
     * @param o1 first Object to compare
     * @param o2 second Object to compare
     * @return whether the given objects are equal
     * @see Object#equals(Object)
     * @see Arrays#equals
     */
    public static boolean nullSafeEquals(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        if (o1.equals(o2)) {
            return true;
        }
        if (o1.getClass().isArray() && o2.getClass().isArray()) {
            return arrayEquals(o1, o2);
        }
        return false;
    }

    /**
     * Compare the given arrays with {@code Arrays.equals}, performing an equality
     * check based on the array elements rather than the array reference.
     *
     * @param o1 first array to compare
     * @param o2 second array to compare
     * @return whether the given objects are equal
     * @see #nullSafeEquals(Object, Object)
     * @see Arrays#equals
     */
    private static boolean arrayEquals(Object o1, Object o2) {
        if (o1 instanceof Object[] && o2 instanceof Object[]) {
            return Arrays.equals((Object[]) o1, (Object[]) o2);
        }
        if (o1 instanceof boolean[] && o2 instanceof boolean[]) {
            return Arrays.equals((boolean[]) o1, (boolean[]) o2);
        }
        if (o1 instanceof byte[] && o2 instanceof byte[]) {
            return Arrays.equals((byte[]) o1, (byte[]) o2);
        }
        if (o1 instanceof char[] && o2 instanceof char[]) {
            return Arrays.equals((char[]) o1, (char[]) o2);
        }
        if (o1 instanceof double[] && o2 instanceof double[]) {
            return Arrays.equals((double[]) o1, (double[]) o2);
        }
        if (o1 instanceof float[] && o2 instanceof float[]) {
            return Arrays.equals((float[]) o1, (float[]) o2);
        }
        if (o1 instanceof int[] && o2 instanceof int[]) {
            return Arrays.equals((int[]) o1, (int[]) o2);
        }
        if (o1 instanceof long[] && o2 instanceof long[]) {
            return Arrays.equals((long[]) o1, (long[]) o2);
        }
        if (o1 instanceof short[] && o2 instanceof short[]) {
            return Arrays.equals((short[]) o1, (short[]) o2);
        }
        return false;
    }

    /**
     * Convert the given array (which may be a primitive array) to an
     * object array (if necessary of primitive wrapper objects).
     * <p>A {@code null} source value will be converted to an
     * empty Object array.
     *
     * @param source the (potentially primitive) array
     * @return the corresponding object array (never {@code null})
     * @throws IllegalArgumentException if the parameter is not an array
     */
    public static Object[] toObjectArray(Object source) {
        if (source instanceof Object[]) {
            return (Object[]) source;
        }
        if (source == null) {
            return new Object[0];
        }
        if (!source.getClass().isArray()) {
            throw new IllegalArgumentException("Source is not an array: " + source);
        }
        int length = Array.getLength(source);
        if (length == 0) {
            return new Object[0];
        }
        Class<?> wrapperType = Array.get(source, 0).getClass();
        Object[] newArray = (Object[]) Array.newInstance(wrapperType, length);
        for (int i = 0; i < length; i++) {
            newArray[i] = Array.get(source, i);
        }
        return newArray;
    }

}
