package org.aoju.bus.sensitive.strategy;

import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.sensitive.Context;
import org.aoju.bus.sensitive.provider.StrategyProvider;

/**
 * 收货地址脱敏处理类
 * 地址只显示到地区，不显示详细地址；我们要对个人信息增强保护
 * 例子：北京市海淀区****
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class AddressStrategy implements StrategyProvider {

    private static final int RIGHT = 10;
    private static final int LEFT = 6;

    @Override
    public String build(Object object, Context context) {
        if (object == null) {
            return null;
        }
        //Mode mode = this.builder.getMode();

        String address = object.toString();
        int length = StringUtils.length(address);
        if (length > RIGHT + LEFT) {
            return StringUtils.rightPad(StringUtils.left(address, length - RIGHT), length, "*");
        }
        if (length <= LEFT) {
            return address;
        } else {
            return address.substring(0, LEFT + 1).concat("*****");
        }
    }

}