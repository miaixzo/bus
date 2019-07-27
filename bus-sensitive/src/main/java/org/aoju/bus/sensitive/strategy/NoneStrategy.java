package org.aoju.bus.sensitive.strategy;

import org.aoju.bus.sensitive.Context;
import org.aoju.bus.sensitive.provider.StrategyProvider;

/**
 * 不脱敏
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class NoneStrategy implements StrategyProvider {

    @Override
    public Object build(Object object, Context context) {
        if (object != null) {
            return object.toString();
        }
        return null;
    }

}
