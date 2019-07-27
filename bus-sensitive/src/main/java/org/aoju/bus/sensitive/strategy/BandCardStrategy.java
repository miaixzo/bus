package org.aoju.bus.sensitive.strategy;

import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.sensitive.Context;
import org.aoju.bus.sensitive.provider.StrategyProvider;

/**
 * 银行卡号脱敏
 * 只留前四位和后四位
 * 6227 0383 3938 3938 393 脱敏结果: 6227 **** **** ***8 393
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class BandCardStrategy implements StrategyProvider {

    @Override
    public String build(Object object, Context context) {
        if (object == null) {
            return null;
        }
        //Mode mode = this.builder.getMode();

        String bankCard = object.toString();
        return StringUtils.left(bankCard, 4).concat(StringUtils.removeStart(StringUtils.leftPad(StringUtils.right(bankCard, 4), StringUtils.length(bankCard), "*"), "***"));
    }

}
