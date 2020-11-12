package org.aoju.bus.starter.goalie.filter;

import org.springframework.core.Ordered;

/**
 * @author Justubborn
 * @since 2020/11/7
 */
public interface FilterOrders {

    // 前置
    int FIRST = Ordered.HIGHEST_PRECEDENCE;
    int DECRYPT = FIRST + 1;
    int PARAMETER_CHECK = DECRYPT + 1;
    int PERMISSION = PARAMETER_CHECK + 1;
    int AUTH = PERMISSION + 1;

    // 后置
    int ENCRYPT = Ordered.LOWEST_PRECEDENCE - 1;

}
