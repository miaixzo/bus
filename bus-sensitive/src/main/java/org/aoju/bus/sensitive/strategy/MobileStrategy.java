package org.aoju.bus.sensitive.strategy;

import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.sensitive.Context;
import org.aoju.bus.sensitive.provider.StrategyProvider;

/**
 * 手机号脱敏处理类
 * 18233583070 脱敏后: 182****3030
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class MobileStrategy implements StrategyProvider {

    @Override
    public Object build(Object object, Context context) {
        if (object == null) {
            return null;
        }
        String value = object.toString();
        return StringUtils.left(value, 3).concat(StringUtils.removeStart(StringUtils.leftPad(StringUtils.right(value, 4), StringUtils.length(value), "*"), "***"));
    }

}
