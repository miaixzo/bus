/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mapper.io and other contributors.         ~
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
package org.miaixz.bus.mapper.builder;

import java.lang.reflect.*;
import java.util.*;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.cursor.Cursor;

/**
 * 泛型类型解析器，基于 MyBatis 3 的源码，添加了 resolveMapperTypes 方法以支持接口泛型解析 源码来自 https://github.com/mybatis/mybatis-3
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TypeResolver {

    /**
     * 私有构造函数，防止实例化
     */
    public TypeResolver() {

    }

    /**
     * 获取方法的实际返回值类型
     *
     * @param method  方法
     * @param srcType 方法所属类
     * @return 方法返回值实际类型
     */
    public static Class<?> getReturnType(Method method, Class<?> srcType) {
        Class<?> returnType = method.getReturnType();
        Type resolvedReturnType = resolveReturnType(method, srcType);
        if (resolvedReturnType instanceof Class) {
            returnType = (Class<?>) resolvedReturnType;
            if (returnType.isArray()) {
                returnType = returnType.getComponentType();
            }
            if (void.class.equals(returnType)) {
                ResultType rt = method.getAnnotation(ResultType.class);
                if (rt != null) {
                    returnType = rt.value();
                }
            }
        } else if (resolvedReturnType instanceof ParameterizedType parameterizedType) {
            Class<?> rawType = (Class<?>) parameterizedType.getRawType();
            if (Collection.class.isAssignableFrom(rawType) || Cursor.class.isAssignableFrom(rawType)) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length == 1) {
                    Type returnTypeParameter = actualTypeArguments[0];
                    if (returnTypeParameter instanceof Class<?>) {
                        returnType = (Class<?>) returnTypeParameter;
                    } else if (returnTypeParameter instanceof ParameterizedType) {
                        // (gcode issue #443) actual type can be a also a parameterized type
                        returnType = (Class<?>) ((ParameterizedType) returnTypeParameter).getRawType();
                    } else if (returnTypeParameter instanceof GenericArrayType) {
                        Class<?> componentType = (Class<?>) ((GenericArrayType) returnTypeParameter)
                                .getGenericComponentType();
                        // (gcode issue #525) support List<byte[]>
                        returnType = Array.newInstance(componentType, 0).getClass();
                    }
                }
            } else if (method.isAnnotationPresent(MapKey.class) && Map.class.isAssignableFrom(rawType)) {
                // (gcode issue 504) Do not look into Maps if there is not MapKey annotation
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length == 2) {
                    Type returnTypeParameter = actualTypeArguments[1];
                    if (returnTypeParameter instanceof Class<?>) {
                        returnType = (Class<?>) returnTypeParameter;
                    } else if (returnTypeParameter instanceof ParameterizedType) {
                        // (gcode issue 443) actual type can be a also a parameterized type
                        returnType = (Class<?>) ((ParameterizedType) returnTypeParameter).getRawType();
                    }
                }
            } else if (Optional.class.equals(rawType)) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                Type returnTypeParameter = actualTypeArguments[0];
                if (returnTypeParameter instanceof Class<?>) {
                    returnType = (Class<?>) returnTypeParameter;
                }
            }
        }

        return returnType;
    }

    /**
     * 解析接口的泛型类型
     *
     * @param srcType 接口类型
     * @return 接口上的泛型参数实际类型数组
     */
    public static Type[] resolveMapperTypes(Class<?> srcType) {
        Type[] types = srcType.getGenericInterfaces();
        List<Type> result = new ArrayList<>();
        for (Type type : types) {
            if (type instanceof Class) {
                result.addAll(Arrays.asList(resolveMapperTypes((Class<?>) type)));
            } else if (type instanceof ParameterizedType) {
                Collections.addAll(result, ((ParameterizedType) type).getActualTypeArguments());
            }
        }
        return result.toArray(new Type[] {});
    }

    /**
     * 解析方法所在接口的泛型类型
     *
     * @param method  方法
     * @param srcType 接口类型
     * @return 接口上的泛型参数实际类型数组
     */
    public static Type[] resolveMapperTypes(Method method, Type srcType) {
        Class<?> declaringClass = method.getDeclaringClass();
        TypeVariable<? extends Class<?>>[] typeParameters = declaringClass.getTypeParameters();
        Type[] result = new Type[typeParameters.length];
        for (int i = 0; i < typeParameters.length; i++) {
            result[i] = resolveType(typeParameters[i], srcType, declaringClass);
        }
        return result;
    }

    /**
     * 解析字段的泛型类型
     *
     * @param field   字段
     * @param srcType 源类型
     * @return 字段的实际类型
     */
    public static Type resolveFieldType(Field field, Type srcType) {
        Type fieldType = field.getGenericType();
        Class<?> declaringClass = field.getDeclaringClass();
        return resolveType(fieldType, srcType, declaringClass);
    }

    /**
     * 解析字段的实际类类型
     *
     * @param field   字段
     * @param srcType 源类型
     * @return 字段的实际类类型
     */
    public static Class<?> resolveFieldClass(Field field, Type srcType) {
        Type fieldType = field.getGenericType();
        Class<?> declaringClass = field.getDeclaringClass();
        Type type = resolveType(fieldType, srcType, declaringClass);
        return resolveTypeToClass(type);
    }

    /**
     * 将 Type 转换为 Class 类型
     *
     * @param type 类型
     * @return 对应的 Class 类型
     */
    public static Class<?> resolveTypeToClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else if (type instanceof TypeVariable<?>) {
            Type[] bounds = ((TypeVariable<?>) type).getBounds();
            return (Class<?>) bounds[0];
        } else if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            if (componentType instanceof Class) {
                return Array.newInstance((Class<?>) componentType, 0).getClass();
            } else {
                Class<?> componentClass = resolveTypeToClass(componentType);
                return Array.newInstance(componentClass, 0).getClass();
            }
        }
        return Object.class;
    }

    /**
     * 解析方法的返回值类型
     *
     * @param method  方法
     * @param srcType 源类型
     * @return 方法返回值的实际类型
     */
    public static Type resolveReturnType(Method method, Type srcType) {
        Type returnType = method.getGenericReturnType();
        Class<?> declaringClass = method.getDeclaringClass();
        return resolveType(returnType, srcType, declaringClass);
    }

    /**
     * 解析方法的参数类型
     *
     * @param method  方法
     * @param srcType 源类型
     * @return 方法参数的实际类型数组
     */
    public static Type[] resolveParamTypes(Method method, Type srcType) {
        Type[] paramTypes = method.getGenericParameterTypes();
        Class<?> declaringClass = method.getDeclaringClass();
        Type[] result = new Type[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            result[i] = resolveType(paramTypes[i], srcType, declaringClass);
        }
        return result;
    }

    /**
     * 解析泛型类型
     *
     * @param type           类型
     * @param srcType        源类型
     * @param declaringClass 声明类
     * @return 解析后的实际类型
     */
    public static Type resolveType(Type type, Type srcType, Class<?> declaringClass) {
        if (type instanceof TypeVariable) {
            return resolveTypeVar((TypeVariable<?>) type, srcType, declaringClass);
        } else if (type instanceof ParameterizedType) {
            return resolveParameterizedType((ParameterizedType) type, srcType, declaringClass);
        } else if (type instanceof GenericArrayType) {
            return resolveGenericArrayType((GenericArrayType) type, srcType, declaringClass);
        } else {
            return type;
        }
    }

    /**
     * 解析泛型数组类型
     *
     * @param genericArrayType 泛型数组类型
     * @param srcType          源类型
     * @param declaringClass   声明类
     * @return 解析后的实际类型
     */
    private static Type resolveGenericArrayType(GenericArrayType genericArrayType, Type srcType,
            Class<?> declaringClass) {
        Type componentType = genericArrayType.getGenericComponentType();
        Type resolvedComponentType = null;
        if (componentType instanceof TypeVariable) {
            resolvedComponentType = resolveTypeVar((TypeVariable<?>) componentType, srcType, declaringClass);
        } else if (componentType instanceof GenericArrayType) {
            resolvedComponentType = resolveGenericArrayType((GenericArrayType) componentType, srcType, declaringClass);
        } else if (componentType instanceof ParameterizedType) {
            resolvedComponentType = resolveParameterizedType((ParameterizedType) componentType, srcType,
                    declaringClass);
        }
        if (resolvedComponentType instanceof Class) {
            return Array.newInstance((Class<?>) resolvedComponentType, 0).getClass();
        } else {
            return new GenericArrayTypes(resolvedComponentType);
        }
    }

    /**
     * 解析参数化类型
     *
     * @param parameterizedType 参数化类型
     * @param srcType           源类型
     * @param declaringClass    声明类
     * @return 解析后的参数化类型
     */
    private static ParameterizedType resolveParameterizedType(ParameterizedType parameterizedType, Type srcType,
            Class<?> declaringClass) {
        Class<?> rawType = (Class<?>) parameterizedType.getRawType();
        Type[] typeArgs = parameterizedType.getActualTypeArguments();
        Type[] args = new Type[typeArgs.length];
        for (int i = 0; i < typeArgs.length; i++) {
            if (typeArgs[i] instanceof TypeVariable) {
                args[i] = resolveTypeVar((TypeVariable<?>) typeArgs[i], srcType, declaringClass);
            } else if (typeArgs[i] instanceof ParameterizedType) {
                args[i] = resolveParameterizedType((ParameterizedType) typeArgs[i], srcType, declaringClass);
            } else if (typeArgs[i] instanceof WildcardType) {
                args[i] = resolveWildcardType((WildcardType) typeArgs[i], srcType, declaringClass);
            } else {
                args[i] = typeArgs[i];
            }
        }
        return new ParameterizedTypes(rawType, null, args);
    }

    /**
     * 解析通配符类型
     *
     * @param wildcardType   通配符类型
     * @param srcType        源类型
     * @param declaringClass 声明类
     * @return 解析后的通配符类型
     */
    private static Type resolveWildcardType(WildcardType wildcardType, Type srcType, Class<?> declaringClass) {
        Type[] lowerBounds = resolveWildcardTypeBounds(wildcardType.getLowerBounds(), srcType, declaringClass);
        Type[] upperBounds = resolveWildcardTypeBounds(wildcardType.getUpperBounds(), srcType, declaringClass);
        return new WildcardTypes(lowerBounds, upperBounds);
    }

    /**
     * 解析通配符类型的边界
     *
     * @param bounds         边界类型数组
     * @param srcType        源类型
     * @param declaringClass 声明类
     * @return 解析后的边界类型数组
     */
    private static Type[] resolveWildcardTypeBounds(Type[] bounds, Type srcType, Class<?> declaringClass) {
        Type[] result = new Type[bounds.length];
        for (int i = 0; i < bounds.length; i++) {
            if (bounds[i] instanceof TypeVariable) {
                result[i] = resolveTypeVar((TypeVariable<?>) bounds[i], srcType, declaringClass);
            } else if (bounds[i] instanceof ParameterizedType) {
                result[i] = resolveParameterizedType((ParameterizedType) bounds[i], srcType, declaringClass);
            } else if (bounds[i] instanceof WildcardType) {
                result[i] = resolveWildcardType((WildcardType) bounds[i], srcType, declaringClass);
            } else {
                result[i] = bounds[i];
            }
        }
        return result;
    }

    /**
     * 解析泛型变量类型
     *
     * @param typeVar        泛型变量
     * @param srcType        源类型
     * @param declaringClass 声明类
     * @return 解析后的实际类型
     */
    private static Type resolveTypeVar(TypeVariable<?> typeVar, Type srcType, Class<?> declaringClass) {
        Type result;
        Class<?> clazz;
        if (srcType instanceof Class) {
            clazz = (Class<?>) srcType;
        } else if (srcType instanceof ParameterizedType) {
            clazz = (Class<?>) ((ParameterizedType) srcType).getRawType();
        } else {
            throw new IllegalArgumentException(
                    "The 2nd arg must be Class or ParameterizedType, but was: " + srcType.getClass());
        }

        if (clazz == declaringClass) {
            Type[] bounds = typeVar.getBounds();
            if (bounds.length > 0) {
                return bounds[0];
            }
            return Object.class;
        }

        Type superclass = clazz.getGenericSuperclass();
        result = scanSuperTypes(typeVar, srcType, declaringClass, clazz, superclass);
        if (result != null) {
            return result;
        }

        Type[] superInterfaces = clazz.getGenericInterfaces();
        for (Type superInterface : superInterfaces) {
            result = scanSuperTypes(typeVar, srcType, declaringClass, clazz, superInterface);
            if (result != null) {
                return result;
            }
        }
        return Object.class;
    }

    /**
     * 扫描超类或接口以解析泛型变量
     *
     * @param typeVar        泛型变量
     * @param srcType        源类型
     * @param declaringClass 声明类
     * @param clazz          当前类
     * @param superclass     超类或接口类型
     * @return 解析后的实际类型
     */
    private static Type scanSuperTypes(TypeVariable<?> typeVar, Type srcType, Class<?> declaringClass, Class<?> clazz,
            Type superclass) {
        if (superclass instanceof ParameterizedType parentAsType) {
            Class<?> parentAsClass = (Class<?>) parentAsType.getRawType();
            TypeVariable<?>[] parentTypeVars = parentAsClass.getTypeParameters();
            if (srcType instanceof ParameterizedType) {
                parentAsType = translateParentTypeVars((ParameterizedType) srcType, clazz, parentAsType);
            }
            if (declaringClass == parentAsClass) {
                for (int i = 0; i < parentTypeVars.length; i++) {
                    if (typeVar.equals(parentTypeVars[i])) {
                        return parentAsType.getActualTypeArguments()[i];
                    }
                }
            }
            if (declaringClass.isAssignableFrom(parentAsClass)) {
                return resolveTypeVar(typeVar, parentAsType, declaringClass);
            }
        } else if (superclass instanceof Class && declaringClass.isAssignableFrom((Class<?>) superclass)) {
            return resolveTypeVar(typeVar, superclass, declaringClass);
        }
        return null;
    }

    /**
     * 转换父类类型变量
     *
     * @param srcType    源参数化类型
     * @param srcClass   源类
     * @param parentType 父类参数化类型
     * @return 转换后的参数化类型
     */
    private static ParameterizedType translateParentTypeVars(ParameterizedType srcType, Class<?> srcClass,
            ParameterizedType parentType) {
        Type[] parentTypeArgs = parentType.getActualTypeArguments();
        Type[] srcTypeArgs = srcType.getActualTypeArguments();
        TypeVariable<?>[] srcTypeVars = srcClass.getTypeParameters();
        Type[] newParentArgs = new Type[parentTypeArgs.length];
        boolean noChange = true;
        for (int i = 0; i < parentTypeArgs.length; i++) {
            if (parentTypeArgs[i] instanceof TypeVariable) {
                for (int j = 0; j < srcTypeVars.length; j++) {
                    if (srcTypeVars[j].equals(parentTypeArgs[i])) {
                        noChange = false;
                        newParentArgs[i] = srcTypeArgs[j];
                    }
                }
            } else {
                newParentArgs[i] = parentTypeArgs[i];
            }
        }
        return noChange ? parentType : new ParameterizedTypes((Class<?>) parentType.getRawType(), null, newParentArgs);
    }

    /**
     * 参数化类型实现类
     */
    public static class ParameterizedTypes implements ParameterizedType {
        /**
         * 原始类型
         */
        private final Class<?> rawType;

        /**
         * 拥有者类型
         */
        private final Type ownerType;

        /**
         * 实际类型参数
         */
        private final Type[] actualTypeArguments;

        /**
         * 构造函数，初始化参数化类型
         *
         * @param rawType             原始类型
         * @param ownerType           拥有者类型
         * @param actualTypeArguments 实际类型参数
         */
        public ParameterizedTypes(Class<?> rawType, Type ownerType, Type[] actualTypeArguments) {
            super();
            this.rawType = rawType;
            this.ownerType = ownerType;
            this.actualTypeArguments = actualTypeArguments;
        }

        /**
         * 获取实际类型参数
         *
         * @return 实际类型参数数组
         */
        @Override
        public Type[] getActualTypeArguments() {
            return actualTypeArguments;
        }

        /**
         * 获取拥有者类型
         *
         * @return 拥有者类型
         */
        @Override
        public Type getOwnerType() {
            return ownerType;
        }

        /**
         * 获取原始类型
         *
         * @return 原始类型
         */
        @Override
        public Type getRawType() {
            return rawType;
        }

        /**
         * 返回字符串表示形式
         *
         * @return 参数化类型的字符串表示
         */
        @Override
        public String toString() {
            return "ParameterizedTypeImpl [rawType=" + rawType + ", ownerType=" + ownerType + ", actualTypeArguments="
                    + Arrays.toString(actualTypeArguments) + "]";
        }
    }

    /**
     * 通配符类型实现类
     */
    public static class WildcardTypes implements WildcardType {
        /**
         * 下界类型
         */
        private final Type[] lowerBounds;

        /**
         * 上界类型
         */
        private final Type[] upperBounds;

        /**
         * 构造函数，初始化通配符类型
         *
         * @param lowerBounds 下界类型数组
         * @param upperBounds 上界类型数组
         */
        WildcardTypes(Type[] lowerBounds, Type[] upperBounds) {
            super();
            this.lowerBounds = lowerBounds;
            this.upperBounds = upperBounds;
        }

        /**
         * 获取下界类型
         *
         * @return 下界类型数组
         */
        @Override
        public Type[] getLowerBounds() {
            return lowerBounds;
        }

        /**
         * 获取上界类型
         *
         * @return 上界类型数组
         */
        @Override
        public Type[] getUpperBounds() {
            return upperBounds;
        }
    }

    /**
     * 泛型数组类型实现类
     */
    public static class GenericArrayTypes implements GenericArrayType {
        /**
         * 泛型组件类型
         */
        private final Type genericComponentType;

        /**
         * 构造函数，初始化泛型数组类型
         *
         * @param genericComponentType 泛型组件类型
         */
        GenericArrayTypes(Type genericComponentType) {
            super();
            this.genericComponentType = genericComponentType;
        }

        /**
         * 获取泛型组件类型
         *
         * @return 泛型组件类型
         */
        @Override
        public Type getGenericComponentType() {
            return genericComponentType;
        }
    }

}