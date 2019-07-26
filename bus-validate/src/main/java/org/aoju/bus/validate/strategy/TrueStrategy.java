package org.aoju.bus.validate.strategy;

import org.aoju.bus.core.utils.BooleanUtils;
import org.aoju.bus.validate.Context;
import org.aoju.bus.validate.annotation.True;
import org.aoju.bus.validate.validators.Complex;

/**
 * Boolean true 校验
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class TrueStrategy implements Complex<Boolean, True> {

    @Override
    public boolean on(Boolean object, True annotation, Context context) {
        if (BooleanUtils.isFalse(object)) {
            return annotation.nullable();
        }
        return object;
    }

}
