package org.aoju.bus.validate.strategy;

import org.aoju.bus.validate.Context;
import org.aoju.bus.validate.validators.Complex;

/**
 * 这个校验器的结果永远为true
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public final class AlwaysStrategy implements Complex {

    @Override
    public boolean on(Object object, Object annotation, Context context) {
        return true;
    }

}
