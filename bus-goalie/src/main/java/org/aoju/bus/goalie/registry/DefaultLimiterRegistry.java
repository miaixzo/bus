package org.aoju.bus.goalie.registry;

import org.aoju.bus.goalie.Assets;
import org.aoju.bus.goalie.metric.Limiter;

/**
 * 限流注册
 *
 * @author Justubborn
 * @version 6.2.8
 * @since JDK 1.8+
 */
public class DefaultLimiterRegistry extends AbstractRegistry<Limiter> implements LimiterRegistry {


    @Override
    public void addLimiter(Limiter limiter) {
        String nameVersion = limiter.getMethod() + limiter.getVersion();
        String ip = limiter.getIp();

        add(ip + nameVersion, limiter);
    }

    @Override
    public void amendLimiter(Limiter limitCfg) {

    }

    @Override
    public Assets getLimiter(String method, String version) {
        return null;
    }

    @Override
    public void init() {

    }

}
