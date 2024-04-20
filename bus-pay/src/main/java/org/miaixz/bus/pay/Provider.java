package org.miaixz.bus.pay;

import org.miaixz.bus.core.lang.Normal;

/**
 * 公共接口,交易类型
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Provider {

    /**
     * 获取交易类型
     *
     * @return 交易类型
     */
    default String getChannel() {
        return Normal.EMPTY;
    }

}
