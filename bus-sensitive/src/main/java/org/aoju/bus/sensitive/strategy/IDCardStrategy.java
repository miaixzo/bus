package org.aoju.bus.sensitive.strategy;

import org.aoju.bus.core.consts.Normal;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.sensitive.Context;
import org.aoju.bus.sensitive.provider.StrategyProvider;

/**
 * 二代身份证号脱敏：
 * <p>
 * 脱敏规则：123002**********01
 * <p>
 * 只保留前6位和后2位，其他用*代替。
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class IDCardStrategy implements StrategyProvider {

    /**
     * 脱敏卡号
     *
     * @param cardId 卡号
     * @return 脱敏结果
     */
    public static String cardId(final String cardId) {
        final int prefixLength = 6;
        final String middle = "**********";
        return StringUtils.buildString(cardId, middle, prefixLength);
    }

    @Override
    public Object build(Object object, Context context) {
        return this.cardId(ObjectUtils.isNull(object) ? Normal.EMPTY : object.toString());
    }

}
