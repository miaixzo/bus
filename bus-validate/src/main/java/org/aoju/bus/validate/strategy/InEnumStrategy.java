package org.aoju.bus.validate.strategy;

import org.aoju.bus.core.lang.exception.UncheckedException;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.core.utils.ReflectUtils;
import org.aoju.bus.validate.Context;
import org.aoju.bus.validate.annotation.InEnum;
import org.aoju.bus.validate.validators.Complex;

import java.lang.reflect.Method;
import java.util.Objects;


/**
 * int enum 校验
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class InEnumStrategy implements Complex<Object, InEnum> {

    @Override
    public boolean on(Object object, InEnum annotation, Context context) {
        if (ObjectUtils.isEmpty(object)) {
            return true;
        }
        Class<? extends Enum> enumClass = annotation.enumClass();
        try {
            Method method = enumClass.getMethod(annotation.method(), null);
            Enum[] enums = enumClass.getEnumConstants();
            for (Enum e : enums) {
                Object value = ReflectUtils.invokeMethod(method, e);
                if (Objects.equals(value, object)) {
                    return true;
                }
            }
            return false;
        } catch (NoSuchMethodException e) {
            throw new UncheckedException(e.getMessage(), e);
        }
    }

}
