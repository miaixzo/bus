package org.aoju.bus.sensitive.provider;

import org.aoju.bus.sensitive.Context;

/**
 * 执行上下文接口
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface ConditionProvider {

    /**
     * 是否执行脱敏
     *
     * @param context 执行上下文
     * @return 结果：是否执行
     */
    boolean valid(Context context);

}
