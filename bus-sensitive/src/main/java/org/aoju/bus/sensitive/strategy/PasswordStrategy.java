package org.aoju.bus.sensitive.strategy;

import org.aoju.bus.core.consts.Normal;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.sensitive.Context;
import org.aoju.bus.sensitive.provider.StrategyProvider;

/**
 * 密码的脱敏策略：
 * 直接返回 null
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class PasswordStrategy implements StrategyProvider {

    /**
     * 脱敏密码
     *
     * @param password 原始密码
     * @return 结果
     */
    public static String password(final String password) {
        return null;
    }

    @Override
    public Object build(Object object, Context context) {
        return this.password(ObjectUtils.isNull(object) ? Normal.EMPTY : object.toString());
    }

}
