package org.aoju.bus.validate.strategy;

import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.validate.Context;
import org.aoju.bus.validate.annotation.NotNull;
import org.aoju.bus.validate.validators.Complex;

/**
 * NOT NUll 校验
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class NotNullStrategy implements Complex<Object, NotNull> {

    @Override
    public boolean on(Object object, NotNull annotation, Context context) {
        return ObjectUtils.isNotEmpty(object);
    }

}
