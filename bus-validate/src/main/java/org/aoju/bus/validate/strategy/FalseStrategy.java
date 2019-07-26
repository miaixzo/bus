package org.aoju.bus.validate.strategy;

import org.aoju.bus.core.utils.BooleanUtils;
import org.aoju.bus.validate.Context;
import org.aoju.bus.validate.annotation.False;
import org.aoju.bus.validate.validators.Complex;

/**
 * FALSE 校验
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class FalseStrategy implements Complex<Boolean, False> {

    @Override
    public boolean on(Boolean object, False annotation, Context context) {
        if (BooleanUtils.isFalse(object)) {
            return annotation.nullable();
        }
        return !object;
    }

}
