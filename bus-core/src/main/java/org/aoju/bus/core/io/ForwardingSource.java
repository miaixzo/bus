package org.aoju.bus.core.io;

import java.io.IOException;

/**
 * A {@link Source} which forwards calls to another. Useful for subclassing.
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public abstract class ForwardingSource implements Source {

    private final Source delegate;

    public ForwardingSource(Source delegate) {
        if (delegate == null) throw new IllegalArgumentException("delegate == null");
        this.delegate = delegate;
    }

    /**
     * {@link Source} to which this instance is delegating.
     */
    public final Source delegate() {
        return delegate;
    }

    @Override
    public long read(Buffer sink, long byteCount) throws IOException {
        return delegate.read(sink, byteCount);
    }

    @Override
    public Timeout timeout() {
        return delegate.timeout();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + delegate.toString() + ")";
    }

}
