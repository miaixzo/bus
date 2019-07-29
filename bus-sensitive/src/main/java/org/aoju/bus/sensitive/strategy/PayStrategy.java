package org.aoju.bus.sensitive.strategy;

import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.sensitive.Context;
import org.aoju.bus.sensitive.provider.StrategyProvider;

/**
 * 签约协议号脱敏方式
 * 19031317273364059018
 * 签约协议号脱敏格式为前6位后6位保留明文，中间脱敏
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class PayStrategy implements StrategyProvider {

    @Override
    public Object build(Object object, Context context) {
        if (object == null) {
            return null;
        }
        // Mode mode = this.builder.getMode();

        String agreementNo = object.toString();
        return StringUtils.left(agreementNo, 6).concat(StringUtils.removeStart(StringUtils.leftPad(StringUtils.right(agreementNo, 6), StringUtils.length(agreementNo), "*"), "***"));
    }

}
