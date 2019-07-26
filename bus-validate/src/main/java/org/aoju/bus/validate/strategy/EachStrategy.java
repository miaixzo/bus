package org.aoju.bus.validate.strategy;

import org.aoju.bus.core.lang.exception.NoSuchException;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.validate.Context;
import org.aoju.bus.validate.Provider;
import org.aoju.bus.validate.Registry;
import org.aoju.bus.validate.annotation.Each;
import org.aoju.bus.validate.validators.Complex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 容器元素内部校验
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class EachStrategy implements Complex<Object, Each> {

    @Override
    public boolean on(Object object, Each annotation, Context context) {
        if (ObjectUtils.isEmpty(object)) {
            return true;
        }
        List<Complex> list = new ArrayList<>();
        for (String name : annotation.value()) {
            if (!Registry.getInstance().contains(name)) {
                throw new NoSuchException("尝试使用一个不存在的校验器：" + name);
            }
            list.add((Complex) Registry.getInstance().get(name));
        }
        for (Class<? extends Complex> clazz : annotation.classes()) {
            if (!Registry.getInstance().contains(clazz.getSimpleName())) {
                throw new NoSuchException("尝试使用一个不存在的校验器：" + clazz.getName());
            }
            list.add((Complex) Registry.getInstance().get(clazz.getSimpleName()));
        }

        if (Provider.isArray(object)) {
            for (Object item : (Object[]) object) {
                if (!fastValidate(list, item, context)) {
                    return false;
                }
            }

        } else if (Provider.isCollection(object)) {
            for (Object item : (Collection<?>) object) {
                if (!fastValidate(list, item, context)) {
                    return false;
                }
            }
        } else if (Provider.isMap(object)) {
            for (Object item : ((Map) object).values()) {
                if (!fastValidate(list, item, context)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 快速执行多个校验器，一旦有一个校验不通过，则返回false
     *
     * @param validators 校验器集合
     * @param object     校验对象
     * @param context    校验上下文
     * @return 校验结果
     */
    private boolean fastValidate(List<Complex> validators, Object object, Context context) {
        for (Complex validator : validators) {
            if (!validator.on(object, null, context)) {
                return false;
            }
        }
        return true;
    }

}
