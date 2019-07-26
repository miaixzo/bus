package org.aoju.bus.validate.strategy;

import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.validate.Context;
import org.aoju.bus.validate.annotation.Null;
import org.aoju.bus.validate.validators.Complex;
import org.aoju.bus.validate.validators.Validator;

/**
 * NULL校验
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class NullStrategy implements Validator<Object>, Complex<Object, Null> {

    @Override
    public boolean on(Object object, Context context) {
        return ObjectUtils.isEmpty(object);
    }

    @Override
    public boolean on(Object object, Null annotation, Context context) {
        return on(object, context);
    }

}
