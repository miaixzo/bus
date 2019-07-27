package org.aoju.bus.sensitive.provider;

import org.aoju.bus.sensitive.Context;

/**
 * 脱敏策略
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface StrategyProvider {

    /**
     * 脱敏
     *
     * @param object  原始内容
     * @param context 执行上下文
     * @return 脱敏后的字符串
     */
    Object build(final Object object, final Context context);

}
