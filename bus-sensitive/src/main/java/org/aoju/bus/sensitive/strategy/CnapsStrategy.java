package org.aoju.bus.sensitive.strategy;

import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.sensitive.Context;
import org.aoju.bus.sensitive.provider.StrategyProvider;

/**
 * 公司开户银行联号
 * 前四位明文，后面脱敏
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class CnapsStrategy implements StrategyProvider {

    @Override
    public Object build(Object object, Context context) {
        if (object == null) {
            return null;
        }
        String snapCard = object.toString();
        return StringUtils.rightPad(StringUtils.left(snapCard, 4), StringUtils.length(snapCard), "*");
    }

}
