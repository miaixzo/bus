package org.miaixz.bus.core.lang.function;

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.mutable.MutableEntry;
import org.miaixz.bus.core.lang.reflect.LookupFactory;
import org.miaixz.bus.core.map.WeakMap;
import org.miaixz.bus.core.toolkit.ReflectKit;

import java.io.Serializable;
import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 以类似反射的方式动态创建Lambda，在性能上有一定优势，同时避免每次调用Lambda时创建匿名内部类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LambdaFactory {

    private static final Map<MutableEntry<Class<?>, Method>, Object> CACHE = new WeakMap<>();


    private LambdaFactory() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    /**
     * 构建Lambda
     * <pre>{@code
     * class Something {
     *     private Long id;
     *     private String name;
     *     // ... 省略GetterSetter方法
     * }
     * Function<Something, Long> getIdFunction = LambdaFactory.buildLambda(Function.class, Something.class, "getId");
     * XConsumer<Something, String> setNameConsumer = LambdaFactory.buildLambda(BiConsumer.class, Something.class, "setName", String.class);
     * }
     * </pre>
     *
     * @param functionInterfaceType 接受Lambda的函数式接口类型
     * @param methodClass           声明方法的类的类型
     * @param methodName            方法名称
     * @param paramTypes            方法参数数组
     * @param <F>                   Function类型
     * @return 接受Lambda的函数式接口对象
     */
    public static <F> F build(final Class<F> functionInterfaceType, final Class<?> methodClass, final String methodName, final Class<?>... paramTypes) {
        return build(functionInterfaceType, ReflectKit.getMethod(methodClass, methodName, paramTypes));
    }

    /**
     * 构建Lambda
     *
     * @param functionInterfaceType 接受Lambda的函数式接口类型
     * @param method                方法对象
     * @param <F>                   Function类型
     * @return 接受Lambda的函数式接口对象
     */
    public static <F> F build(final Class<F> functionInterfaceType, final Method method) {
        Assert.notNull(functionInterfaceType);
        Assert.notNull(method);
        final MutableEntry<Class<?>, Method> cacheKey = new MutableEntry<>(functionInterfaceType, method);
        //noinspection unchecked
        return (F) CACHE.computeIfAbsent(cacheKey, key -> {
            final List<Method> abstractMethods = Arrays.stream(functionInterfaceType.getMethods())
                    .filter(m -> Modifier.isAbstract(m.getModifiers()))
                    .collect(Collectors.toList());
            Assert.equals(abstractMethods.size(), 1, "不支持非函数式接口");
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            final Method invokeMethod = abstractMethods.get(0);
            final MethodHandles.Lookup caller = LookupFactory.lookup(method.getDeclaringClass());
            final String invokeName = invokeMethod.getName();
            final MethodType invokedType = MethodType.methodType(functionInterfaceType);
            final MethodType samMethodType = MethodType.methodType(invokeMethod.getReturnType(), invokeMethod.getParameterTypes());
            final MethodHandle implMethod = Optional.ofTry(() -> caller.unreflect(method)).get();
            final MethodType insMethodType = MethodType.methodType(method.getReturnType(), method.getDeclaringClass(), method.getParameterTypes());
            final boolean isSerializable = Serializable.class.isAssignableFrom(functionInterfaceType);
            try {
                final CallSite callSite = isSerializable ?
                        LambdaMetafactory.altMetafactory(
                                caller,
                                invokeName,
                                invokedType,
                                samMethodType,
                                implMethod,
                                insMethodType,
                                LambdaMetafactory.FLAG_SERIALIZABLE
                        ) :
                        LambdaMetafactory.metafactory(
                                caller,
                                invokeName,
                                invokedType,
                                samMethodType,
                                implMethod,
                                insMethodType
                        );
                return (F) callSite.getTarget().invoke();
            } catch (final Throwable e) {
                throw new InternalException(e);
            }
        });
    }

}
