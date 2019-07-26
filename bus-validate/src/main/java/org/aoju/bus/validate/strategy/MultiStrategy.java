package org.aoju.bus.validate.strategy;

import org.aoju.bus.core.lang.exception.NoSuchException;
import org.aoju.bus.validate.Context;
import org.aoju.bus.validate.Registry;
import org.aoju.bus.validate.annotation.Multi;
import org.aoju.bus.validate.validators.Complex;

import java.util.ArrayList;
import java.util.List;

/**
 * 多规则匹配校验
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class MultiStrategy implements Complex<Object, Multi> {

    @Override
    public boolean on(Object object, Multi multi, Context context) {
        List<Complex> validators = new ArrayList<>();
        for (String validatorName : multi.value()) {
            if (!Registry.getInstance().contains(validatorName)) {
                throw new NoSuchException("尝试使用一个不存在的校验器：" + validatorName);
            }
            validators.add((Complex) Registry.getInstance().get(validatorName));
        }
        for (Class<? extends Complex> clazz : multi.classes()) {
            if (!Registry.getInstance().contains(clazz.getSimpleName())) {
                throw new NoSuchException("尝试使用一个不存在的校验器：" + clazz.getName());
            }
            validators.add((Complex) Registry.getInstance().get(clazz.getSimpleName()));
        }
        for (Complex validator : validators) {
            if (!validator.on(object, null, context)) {
                return false;
            }
        }
        return true;
    }

}
