package org.aoju.bus.validate.strategy;

import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.validate.Context;
import org.aoju.bus.validate.annotation.Blank;
import org.aoju.bus.validate.validators.Complex;
import org.aoju.bus.validate.validators.Validator;

/**
 * BLANK 校验
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class BlankStrategy implements Validator<String>, Complex<String, Blank> {

    @Override
    public boolean on(String object, Context context) {
        return StringUtils.isBlank(object);
    }

    @Override
    public boolean on(String object, Blank annotation, Context context) {
        return on(object, context);
    }

}