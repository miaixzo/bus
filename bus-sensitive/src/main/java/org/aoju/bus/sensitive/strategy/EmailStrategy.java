package org.aoju.bus.sensitive.strategy;

import org.aoju.bus.core.consts.Normal;
import org.aoju.bus.core.consts.Symbol;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.sensitive.Context;
import org.aoju.bus.sensitive.provider.StrategyProvider;

/**
 * 邮箱脱敏策略
 * 脱敏规则：
 * 保留前三位，中间隐藏4位。其他正常显示
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class EmailStrategy implements StrategyProvider {

    /**
     * 脱敏邮箱
     *
     * @param email 邮箱
     * @return 结果
     */
    public static String email(final String email) {
        if (StringUtils.isEmpty(email)) {
            return null;
        }

        final int prefixLength = 3;

        final int atIndex = email.indexOf(Symbol.AT);
        String middle = "****";

        if (atIndex > 0) {
            int middleLength = atIndex - prefixLength;
            middle = StringUtils.repeat(Symbol.STAR, middleLength);
        }
        return StringUtils.buildString(email, middle, prefixLength);
    }

    @Override
    public Object build(Object object, Context context) {
        return this.email(ObjectUtils.isNull(object) ? Normal.EMPTY : object.toString());
    }

}
