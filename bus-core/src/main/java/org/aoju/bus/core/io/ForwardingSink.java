package org.aoju.bus.core.io;

import java.io.IOException;

/**
 * A {@link Sink} which forwards calls to another. Useful for subclassing. * *
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public abstract class ForwardingSink implements Sink {

    private final Sink delegate;

    public ForwardingSink(Sink delegate) {
        if (delegate == null) throw new IllegalArgumentException("delegate == null");
        this.delegate = delegate;
    }

    /**
     * {@link Sink} to which this instance is delegating.
     */
    public final Sink delegate() {
        return delegate;
    }

    @Override
    public void write(Buffer source, long byteCount) throws IOException {
        delegate.write(source, byteCount);
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
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
