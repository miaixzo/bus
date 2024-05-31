package org.miaixz.bus.starter.limiter;

import org.miaixz.bus.limiter.Context;
import org.miaixz.bus.limiter.Holder;
import org.springframework.beans.factory.InitializingBean;

/**
 *
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LimiterService implements InitializingBean {

    private final Context context;

    public LimiterService(Context context) {
        this.context = context;
    }

    @Override
    public void afterPropertiesSet() {
        Holder.set(context);
    }

}
