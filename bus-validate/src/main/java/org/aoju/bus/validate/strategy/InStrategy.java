package org.aoju.bus.validate.strategy;

import org.aoju.bus.core.utils.ArrayUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.validate.Context;
import org.aoju.bus.validate.annotation.In;
import org.aoju.bus.validate.validators.Complex;

/**
 * IN 校验
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class InStrategy implements Complex<String, In> {

    @Override
    public boolean on(String object, In annotation, Context context) {
        if (StringUtils.isEmpty(object)) {
            return true;
        }
        return ArrayUtils.contains(annotation.value(), object);
    }

}
