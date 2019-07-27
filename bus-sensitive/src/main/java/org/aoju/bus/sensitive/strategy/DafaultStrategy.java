package org.aoju.bus.sensitive.strategy;

import org.aoju.bus.sensitive.Context;
import org.aoju.bus.sensitive.provider.StrategyProvider;

/**
 * 默认脱敏处理类
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class DafaultStrategy implements StrategyProvider {

    private static final int SIZE = 6;
    private static final int TWO = 2;
    private static final String SYMBOL = "*";

    @Override
    public Object build(Object object, Context context) {
        if (null == object || "".equals(object)) {
            return null;
        }
        //  Mode mode = this.builder.getMode();

        String value = object.toString();

        int len = value.length();
        int pamaone = len / TWO;
        int pamatwo = pamaone - 1;
        int pamathree = len % TWO;
        StringBuilder stringBuilder = new StringBuilder();
        if (len <= TWO) {
            if (pamathree == 1) {
                return SYMBOL;
            }
            stringBuilder.append(SYMBOL);
            stringBuilder.append(value.charAt(len - 1));
        } else {
            if (pamatwo <= 0) {
                stringBuilder.append(value.substring(0, 1));
                stringBuilder.append(SYMBOL);
                stringBuilder.append(value.substring(len - 1, len));

            } else if (pamatwo >= SIZE / TWO && SIZE + 1 != len) {
                int pamafive = (len - SIZE) / 2;
                stringBuilder.append(value.substring(0, pamafive));
                for (int i = 0; i < SIZE; i++) {
                    stringBuilder.append(SYMBOL);
                }
                if (ispamaThree(pamathree)) {
                    stringBuilder.append(value.substring(len - pamafive, len));
                } else {
                    stringBuilder.append(value.substring(len - (pamafive + 1), len));
                }
            } else {
                int pamafour = len - 2;
                stringBuilder.append(value.substring(0, 1));
                for (int i = 0; i < pamafour; i++) {
                    stringBuilder.append(SYMBOL);
                }
                stringBuilder.append(value.substring(len - 1, len));
            }
        }
        return stringBuilder.toString();
    }

    private boolean ispamaThree(int pamathree) {
        return (pamathree == 0 && SIZE / 2 == 0) || (pamathree != 0 && SIZE % 2 != 0);
    }

}
