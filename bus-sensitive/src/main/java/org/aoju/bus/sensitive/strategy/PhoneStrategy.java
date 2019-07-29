package org.aoju.bus.sensitive.strategy;

import org.aoju.bus.core.consts.Normal;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.sensitive.Context;
import org.aoju.bus.sensitive.provider.StrategyProvider;

/**
 * 手机号脱敏
 * 脱敏规则：180****1120
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class PhoneStrategy implements StrategyProvider {

    /**
     * 脱敏电话号码
     *
     * @param phone 电话号码
     * @return 结果
     */
    public static String phone(final String phone) {
        final int prefixLength = 3;
        final String middle = "****";
        return StringUtils.buildString(phone, middle, prefixLength);
    }

    @Override
    public Object build(Object object, Context context) {
        return this.phone(ObjectUtils.isNull(object) ? Normal.EMPTY : object.toString());
    }

}
