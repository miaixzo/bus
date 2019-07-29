package org.aoju.bus.sensitive;

import org.aoju.bus.sensitive.provider.ConditionProvider;

/**
 * 返回真条件
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class Condition implements ConditionProvider {

    @Override
    public boolean valid(Context context) {
        return true;
    }

}
