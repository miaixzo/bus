package org.aoju.bus.validate.strategy;

import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.validate.Context;
import org.aoju.bus.validate.annotation.Equals;
import org.aoju.bus.validate.validators.Complex;

import java.util.Objects;

/**
 * EQUALS 校验
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class EqualsStrategy implements Complex<String, Equals> {

    @Override
    public boolean on(String object, Equals annotation, Context context) {
        return ObjectUtils.isEmpty(object) || Objects.equals(object, annotation.value());
    }

}
