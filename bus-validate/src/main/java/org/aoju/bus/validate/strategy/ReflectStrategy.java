package org.aoju.bus.validate.strategy;

import org.aoju.bus.core.lang.exception.NoSuchException;
import org.aoju.bus.core.lang.exception.UncheckedException;
import org.aoju.bus.core.utils.ClassUtils;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.core.utils.ReflectUtils;
import org.aoju.bus.validate.Context;
import org.aoju.bus.validate.Registry;
import org.aoju.bus.validate.annotation.Reflect;
import org.aoju.bus.validate.validators.Complex;

import java.lang.reflect.Method;


/**
 * 反射信息校验
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class ReflectStrategy implements Complex<Object, Reflect> {

    @Override
    public boolean on(Object object, Reflect annotation, Context context) {
        if (ObjectUtils.isEmpty(object)) {
            return true;
        }
        Class<?> clazz = annotation.target();
        String methodName = annotation.method();
        Object result;
        try {
            Method method = clazz.getDeclaredMethod(methodName, object.getClass());
            Object bean = ClassUtils.getClass(clazz);
            result = ReflectUtils.invokeMethod(method, bean);
        } catch (NoSuchMethodException e) {
            throw new UncheckedException(e.getMessage(), e);
        }

        for (String name : annotation.validator()) {
            if (!Registry.getInstance().contains(name)) {
                throw new NoSuchException("尝试使用一个不存在的校验器：" + name);
            }
            Complex complex = (Complex) Registry.getInstance().get(name);
            if (!complex.on(result, null, context)) {
                return false;
            }
        }
        return true;
    }

}
