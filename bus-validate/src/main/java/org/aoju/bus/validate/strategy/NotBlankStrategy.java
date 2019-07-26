package org.aoju.bus.validate.strategy;

import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.validate.Context;
import org.aoju.bus.validate.annotation.NotBlank;
import org.aoju.bus.validate.validators.Complex;
import org.aoju.bus.validate.validators.Validator;

/**
 * NOT blank 校验
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class NotBlankStrategy implements Validator<String>, Complex<String, NotBlank> {

    @Override
    public boolean on(String object, NotBlank annotation, Context context) {
        return on(object, context);
    }

    @Override
    public boolean on(String object, Context context) {
        return StringUtils.isNotBlank(object);
    }

}
