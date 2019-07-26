package org.aoju.bus.validate.strategy;

import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.validate.Context;
import org.aoju.bus.validate.annotation.Regex;
import org.aoju.bus.validate.validators.Complex;

import java.util.regex.Pattern;

/**
 * 正则匹配校验
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class RegexStrategy implements Complex<String, Regex> {

    @Override
    public boolean on(String object, Regex regexValidate, Context context) {
        if (StringUtils.isEmpty(object)) {
            return false;
        }
        if (regexValidate.zeroAble() && object.length() == 0) {
            return true;
        }
        Pattern pattern = Pattern.compile(regexValidate.pattern());
        return pattern.matcher(object).matches();
    }

}
