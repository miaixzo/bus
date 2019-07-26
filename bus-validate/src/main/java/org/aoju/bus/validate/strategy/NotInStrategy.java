package org.aoju.bus.validate.strategy;

import org.aoju.bus.core.utils.ArrayUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.validate.Context;
import org.aoju.bus.validate.annotation.NotIn;
import org.aoju.bus.validate.validators.Complex;

/**
 * NOT IN 校验
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class NotInStrategy implements Complex<String, NotIn> {

    @Override
    public boolean on(String object, NotIn annotation, Context context) {
        if (StringUtils.isEmpty(object)) {
            return true;
        }
        return !ArrayUtils.contains(annotation.value(), object);
    }

}
