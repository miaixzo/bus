package org.aoju.bus.sensitive.strategy;

import org.aoju.bus.core.consts.Normal;
import org.aoju.bus.core.consts.Symbol;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.sensitive.Context;
import org.aoju.bus.sensitive.provider.StrategyProvider;

/**
 * 中文名称脱敏策略：
 * <p>
 * 0. 少于等于1个字 直接返回
 * 1. 两个字 隐藏姓
 * 2. 三个及其以上 只保留第一个和最后一个 其他用星号代替
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class NameStrategy implements StrategyProvider {

    /**
     * 脱敏中文名称
     *
     * @param chineseName 中文名称
     * @return 脱敏后的结果
     */
    public static String name(final String chineseName) {
        if (StringUtils.isEmpty(chineseName)) {
            return chineseName;
        }

        final int nameLength = chineseName.length();
        if (1 == nameLength) {
            return chineseName;
        }

        if (2 == nameLength) {
            return Symbol.STAR + chineseName.charAt(1);
        }

        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append(chineseName.charAt(0));
        for (int i = 0; i < nameLength - 2; i++) {
            stringBuffer.append(Symbol.STAR);
        }
        stringBuffer.append(chineseName.charAt(nameLength - 1));
        return stringBuffer.toString();
    }

    @Override
    public Object build(Object object, Context context) {
        return this.name(ObjectUtils.isNull(object) ? Normal.EMPTY : object.toString());
    }

}
