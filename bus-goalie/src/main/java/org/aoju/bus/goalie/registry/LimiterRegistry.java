package org.aoju.bus.goalie.registry;

import org.aoju.bus.goalie.Assets;
import org.aoju.bus.goalie.Registry;
import org.aoju.bus.goalie.metric.Limiter;

/**
 * 限流注册器
 *
 * @author Justubborn
 * @since 2020/12/21
 */
public interface LimiterRegistry extends Registry<Limiter> {

    void addLimiter(Limiter limiter);

    void amendLimiter(Limiter limiter);

    Assets getLimiter(String ip, String nameVersion);
}
