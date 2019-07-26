package org.aoju.bus.core.io;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * A {@link Timeout} which forwards calls to another. Useful for subclassing.
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class ForwardingTimeout extends Timeout {

    private Timeout delegate;

    public ForwardingTimeout(Timeout delegate) {
        if (delegate == null) throw new IllegalArgumentException("delegate == null");
        this.delegate = delegate;
    }

    /**
     * {@link Timeout} instance to which this instance is currently delegating.
     */
    public final Timeout delegate() {
        return delegate;
    }

    public final ForwardingTimeout setDelegate(Timeout delegate) {
        if (delegate == null) throw new IllegalArgumentException("delegate == null");
        this.delegate = delegate;
        return this;
    }

    @Override
    public Timeout timeout(long timeout, TimeUnit unit) {
        return delegate.timeout(timeout, unit);
    }

    @Override
    public long timeoutNanos() {
        return delegate.timeoutNanos();
    }

    @Override
    public boolean hasDeadline() {
        return delegate.hasDeadline();
    }

    @Override
    public long deadlineNanoTime() {
        return delegate.deadlineNanoTime();
    }

    @Override
    public Timeout deadlineNanoTime(long deadlineNanoTime) {
        return delegate.deadlineNanoTime(deadlineNanoTime);
    }

    @Override
    public Timeout clearTimeout() {
        return delegate.clearTimeout();
    }

    @Override
    public Timeout clearDeadline() {
        return delegate.clearDeadline();
    }

    @Override
    public void throwIfReached() throws IOException {
        delegate.throwIfReached();
    }

}
