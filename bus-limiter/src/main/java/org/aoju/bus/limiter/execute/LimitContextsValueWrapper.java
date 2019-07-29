package org.aoju.bus.limiter.execute;


/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class LimitContextsValueWrapper {

    private boolean value;

    private Object limiterFailResolveResult;

    public LimitContextsValueWrapper(boolean value, Object limiterFailResolveResult) {
        this.value = value;
        this.limiterFailResolveResult = limiterFailResolveResult;
    }

    public boolean value() {
        return value;
    }

    public Object getLimiterFailResolveResult() {
        return limiterFailResolveResult;
    }
}